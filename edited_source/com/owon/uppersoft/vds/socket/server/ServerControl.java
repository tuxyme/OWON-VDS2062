package com.owon.uppersoft.vds.socket.server;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.OperateBlocker;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.socket.command.CmdFactory;
import com.owon.uppersoft.vds.socket.provider.MainProvider;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.Pref;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerControl
{
  private Server socketServer;
  private int mPort = 5188;
  private final int DATABLOCK = 2048;
  private final int net_dmbuflen = 4288;

  public ServerControl(Pref p)
  {
    load(p);
  }

  private void load(Pref p)
  {
    mPort = p.loadInt("ScpiPort");
  }

  public void persist(Pref p)
  {
    p.persistInt("ScpiPort", mPort);
  }

  public void startServer()
  {
    startServer(mPort);
  }

  public void startServer(int port)
  {
    if ((port > 0) && (port < 65535))
    {
      socketServer = new Server(this, port);
      socketServer.start();
    }
  }

  public void destroyServer()
  {
    if (socketServer != null) {
      socketServer.terminateServer();
    }
  }

  public void startSCPIConsole()
  {
    socketServer.setupConsole();
  }

  public boolean setPort(int port)
  {
    if (port == socketServer.getPort()) {
      return false;
    }
    boolean b = (port > 0) && (port < 65535);
    if (b)
    {
      destroyServer();
      socketServer = null;
      try
      {
        Thread.sleep(100L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      mPort = port;
      startServer(port);
    }
    return b;
  }

  public int getPort()
  {
    return socketServer.getPort();
  }

  public void writeLargeData(SocketChannel channel, CmdFactory factory, String cmd)
    throws IOException
  {
    byte[] buf = factory.getProvider().getData(cmd);
    channel.write(ByteBuffer.wrap("#".getBytes()));
    if (buf != null)
    {
      int dataLen = buf.length;
      int p = 0;int bl = 2048;int wn = 0;
      ByteBuffer head = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
      head.putInt(dataLen).flip();
      channel.write(head);
      channel.write(ByteBuffer.wrap("\n".getBytes()));
      while (p < dataLen)
      {
        if (bl > dataLen - p) {
          bl = dataLen - p;
        }
        wn = channel.write(ByteBuffer.wrap(buf, p, bl));
        p += wn;
      }
    }
    else
    {
      ByteBuffer head = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
      head.putInt(0).flip();
      channel.write(head);
    }
  }

  public void writeDMData(SocketChannel channel, String cmd)
    throws IOException
  {
    String rsp = "";
    ByteBuffer block = null;

    if (Platform.getControlManager().isRuntime())
    {
      Platform.getControlApps().interComm.statusStop(false);
      try
      {
        Thread.sleep(1000L);
      }
      catch (Exception localException1) {}
    }
    if ((Platform.getDataHouse().isRTWhenLoad()) &&
      (Platform.getControlManager().sourceManager.isConnected()))
    {
      ControlApps ca = Platform.getControlApps();
      ca.interComm.tryToPersistDMData();
      while (ca.getOperateBlocker().isBlock()) {
        try
        {
          Thread.sleep(200L);
        }
        catch (Exception localException2) {}
      }
      if (socketServer.isRemoteTransmit) {
        rsp = remote_dm_transmit(channel);
      } else {
        rsp = local_dm_transmit(channel, cmd);
      }
      block = ByteBuffer.wrap(rsp.getBytes());
    }
    else
    {
      rsp = "TRANSMITSSION FAILED AS OFFLINE\n";
      block = ByteBuffer.wrap(rsp.getBytes());
    }
    channel.write(block);
  }

  private String local_dm_transmit(SocketChannel channel, String cmd)
  {
    String path = cmd.replaceAll(";", "").substring("*LDM?".length()).trim();
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MMdd_HH.mm.ss");
    String fn = df.format(new Date());

    File dest = new File(path, fn + ".bin");
    FileUtil.checkPath(dest);

    File dmf = AbsInterCommunicator.dmf;
    boolean noUse = dmf.renameTo(dmf);
    if (dmf.exists())
    {
      boolean b = FileUtil.copyFile2(dmf, dest);
      String success = "SUCCESS: " + dest.getAbsolutePath() + "\n";
      String failure = "FAILED: " + dest.getAbsolutePath() + "\n";
      return b ? success : failure;
    }
    return "FAILED: DEEPMEMORY DATA NULL\n";
  }

  private String remote_dm_transmit(SocketChannel channel)
  {
    File dm = AbsInterCommunicator.dmf;
    boolean noUse = dm.renameTo(dm);

    File temp = AbsInterCommunicator.tmp_dmf;
    temp.delete();
    boolean b = FileUtil.copyFile2(dm, temp);
    if (!b) {
      return "TRANSMITSSION FAILED\n";
    }
    if (temp.exists())
    {
        int dataLen = (int)temp.length();
        ByteBuffer buf = ByteBuffer.allocate(dataLen).order(ByteOrder.BIG_ENDIAN);
        CByteArrayInputStream ba = new CByteArrayInputStream(temp);
        ba.get(buf.array(), 0, dataLen);
        ba.dispose();
        try
        {
            channel.write(ByteBuffer.wrap("#".getBytes()));
            ByteBuffer head = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            head.putInt(dataLen).flip();
            channel.write(head);
            channel.write(ByteBuffer.wrap("\n".getBytes()));
            while (buf.hasRemaining())
            {
                channel.write(buf);
            }
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "TRANSMITSSION FAILED\n";
        }
    }
    return "TRANSMITSSION FAILED\n";
  }

  private String test(SocketChannel channel)
  {
    String mytext = "0123456789012345678901234567890123456789";
    int count = 10000;int dataLen = mytext.length() * count;
    try
    {
      ByteBuffer head = ByteBuffer.allocate(4)
        .order(ByteOrder.BIG_ENDIAN);
      head.putInt(dataLen).flip();
      channel.write(head);

      ByteBuffer buf = ByteBuffer.allocate(dataLen).order(
        ByteOrder.BIG_ENDIAN);
      for (int i = 0; i < count; i++) {
        buf.put(mytext.getBytes());
      }
      buf.flip();
      channel.write(buf);

      return "TRANSMITSSION  DONE";
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return "TRANSMITSSION FAILED";
  }
}

/* Location:
 * Qualified Name:     com.owon.uppersoft.vds.socket.server.ServerControl
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */
