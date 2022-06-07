package com.owon.uppersoft.vds.socket.server;

import com.owon.uppersoft.vds.socket.command.CmdFactory;
import com.owon.uppersoft.vds.socket.command.CommandKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server
  extends Thread
{
  private boolean onlargeTransmit = false;
  private boolean onDmTransmit = false;
  protected boolean isRemoteTransmit = false;
  private boolean isExit = false;
  protected ByteBuffer clientBuffer = ByteBuffer.allocate(1024);
  public static final int DEFAULT_PORT = 5188;
  private int mPort;
  private CmdFactory factory;
  private ServerControl svCtr;
  private ScpiConsole dbgView;
  protected Selector selector;
  ServerSocketChannel server;

  public Server(ServerControl sc, int port)
  {
    mPort = port;
    svCtr = sc;
    factory = new CmdFactory();
  }

  public void terminateServer()
  {
    if (selector != null) {
      selector.wakeup();
    }
    isExit = true;
    try
    {
      server.socket().close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void run()
  {
    try
    {
      selector = getSelector(mPort);
      listen();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected Selector getSelector(int port)
    throws IOException
  {
    server = ServerSocketChannel.open();
    Selector sel = Selector.open();
    server.socket().bind(new InetSocketAddress(port));
    server.configureBlocking(false);
    server.register(sel, 16);
    return sel;
  }

  public void listen()
  {
    try
    {
      while (!isExit)
      {
        selector.select();
        Iterator<SelectionKey> iter = selector.selectedKeys()
          .iterator();
        while (iter.hasNext())
        {
          SelectionKey key = (SelectionKey)iter.next();
          iter.remove();
          process(key);
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected void process(SelectionKey key)
    throws IOException
  {
    if (key.isAcceptable())
    {
      ServerSocketChannel server = (ServerSocketChannel)key.channel();

      SocketChannel client = server.accept();

      client.configureBlocking(false);

      client.register(selector, 1);
    }
    else if (key.isReadable())
    {
      SocketChannel client = (SocketChannel)key.channel();
      int count = -1;
      try
      {
        count = client.read(clientBuffer);
      }
      catch (Exception localException)
      {
        client.close();
      }
      if (count > 0)
      {
        clientBuffer.flip();

        byte[] buf = clientBuffer.array();
        String cmd = new String(buf, 0, count).trim();
        if (dbgView != null) {
          dbgView.addReadText(cmd);
        }
        cmd = cmd + "\r";
        String trans = translation(cmd);
        System.out.println("TRANS: " + trans);
        if ((trans != null) && (!trans.equalsIgnoreCase("")))
        {
          SelectionKey sKey = client.register(selector,
            4);
          sKey.attach(trans);
        }
      }
      else
      {
        client.close();
      }
      clientBuffer.clear();
    }
    else if (key.isWritable())
    {
      SocketChannel client = (SocketChannel)key.channel();
      String cmd = (String)key.attachment();
      System.out.println("CMD: " + cmd);
      if (onlargeTransmit)
      {
        String[] splits = cmd.trim().split(";");
        for (int i = 0; i < splits.length; i++) {
          svCtr.writeLargeData(client, factory, splits[i]);
        }
        onlargeTransmit = false;
      }
      else if (onDmTransmit)
      {
        svCtr.writeDMData(client, cmd);
        onDmTransmit = false;
        isRemoteTransmit = false;
      }
      else
      {
        ByteBuffer block = ByteBuffer.wrap(cmd.getBytes());
        if (dbgView != null) {
          dbgView.addWriteText(cmd);
        }
        client.write(block);
      }
      client.register(selector, 1);
    }
  }

  protected String translation(String cmd)
  {
    String[] splits = cmd.split(";");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < splits.length; i++) {
      sb.append(analyse(splits[i]));
    }
    return sb.toString();
  }

  private String analyse(String line)
  {
    line = line.trim();
    String str = (String)analyseCMDs_1(line);
    return str;
  }

  public Object analyseCMDs_1(String line)
  {
    //line = line.toUpperCase().replaceAll("\\s*", "");
    line = line.replaceAll("\\s*", "");
    if (dealADC_CMDs(line)) {
      return line + ";";
    }
    CommandKey trunk = pickOutTrunk(line);

    CommandKey node = null;
    if (trunk != null) {
      node = trunk.hasKey(line, null);
    }
    if (node != null) {
      return node.handle();
    }
    return "CAN NOT ANALYSE THE ORDER";
  }

  private boolean dealADC_CMDs(String line)
  {
    boolean a = line.startsWith("*ADC?");
    boolean b = line.startsWith("*LDM?");
    boolean c = line.startsWith("*RDM?");
    if (a)
    {
      onlargeTransmit = true;
      return true;
    }
    if (b)
    {
      onDmTransmit = true;
      isRemoteTransmit = false;
      return true;
    }
    if (c)
    {
      onDmTransmit = true;
      isRemoteTransmit = true;
      return true;
    }
    return false;
  }

  private CommandKey pickOutTrunk(String line)
  {
    CommandKey cmd = null;
    String line_head = line;
    if (line.startsWith(":")) {
      line_head = line.split(":", 3)[1];
    }
    for (CommandKey ck : factory.getKeys()) {
      if ((line_head.startsWith(ck.getFitPre())) ||
        (line_head.startsWith(ck.getShortPre())))
      {
        cmd = ck;
        break;
      }
    }
    return cmd;
  }

  public void setupConsole()
  {
    dbgView = ScpiConsole.getInstance();
  }

  public int getPort()
  {
    return mPort;
  }
}

/* Location:
 * Qualified Name:     com.owon.uppersoft.vds.socket.server.Server
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */
