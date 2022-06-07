package com.owon.uppersoft.vds.ui.prompt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class NoticeDialog
  extends JDialog
{
  public static final int arc = 6;
  public static final int msgNum = 2;
  public static final Font font = new Font("SansSerif", 1, 15);
  public static final Color CO_FADE_BG = new Color(60, 60, 60);
  private String msg = "";
  private boolean out;

  public NoticeDialog(Window owner)
  {
    super(owner);
    setLayout(new BorderLayout());
    if (owner != null) {
      owner.addWindowListener(new WindowAdapter()
      {
        public void windowClosed(WindowEvent e)
        {
          dispose();
        }
      });
    }
    setUndecorated(true);
    setAlwaysOnTop(true);

    JPanel jp = new JPanel()
    {
      protected void paintComponent(Graphics g)
      {
        Graphics2D g2d = (Graphics2D)g;
        int w = getWidth();int h = getHeight();
        int wc = w >> 1;
        int hc = h >> 1;
        g2d.setFont(NoticeDialog.font);

        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r2d = fm.getStringBounds(msg, getGraphics());
        int tw = (int)r2d.getWidth();
        int th = (int)r2d.getHeight();
        int x = wc - (tw >> 1);
        int y = hc - (th >> 1);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(NoticeDialog.CO_FADE_BG);
        g2d.fillRoundRect(2, 2, w - 5, h - 5, 6, 6);
        g2d.setColor(Color.WHITE);

        g2d.drawString(msg, x, y + fm.getAscent());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);
      }
    };
    add(jp, "Center");
    setFont(font);

    //AWTUtilities.setWindowOpaque(this, false);
    setLocationRelativeTo(null);

    //AWTUtilities.setWindowOpacity(this, 0.75F);
  }

  private void recomputeDialgSize()
  {
    FontMetrics fm = getFontMetrics(font);
    Rectangle2D r2d = fm.getStringBounds(msg, getGraphics());
    setSize((int)r2d.getWidth() + 30, (int)r2d.getHeight() + 30);

    setLocationRelativeTo(null);
  }

  public void setMessage(String m)
  {
    msg = m;
    recomputeDialgSize();
  }

  public void keepShow()
  {
    keepShow(2000);
  }

  public void keepShow(final int time)
  {
    new Thread()
    {
      public void run()
      {
        NoticeDialog.this.nofading(time);
      }
    }.start();
  }

  private void nofading(int time)
  {
    setVisible(true);
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    dispose();
  }

  private void fading()
  {
    float v = 0.0F;
    out = false;
    //AWTUtilities.setWindowOpacity(this, v += 0.1F);
    setVisible(true);
    try
    {
      while ((v < 0.85D) && (!out))
      {
        //AWTUtilities.setWindowOpacity(this, v);
        v += 0.1F;
        Thread.sleep(170L);
      }
      Thread.sleep(1000L);
      v -= 0.2F;
      for (;;)
      {
        //AWTUtilities.setWindowOpacity(this, v);
        v -= 0.06F;
        Thread.sleep(260L);
        if (v > 0.5D) {
          if (out) {
            break;
          }
        }
      }
      do
      {
        //AWTUtilities.setWindowOpacity(this, v);
        v -= 0.08F;
        Thread.sleep(180L);
        if (v <= 0.0F) {
          break;
        }
      } while (!out);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    dispose();
  }
}

/* Location:
 * Qualified Name:     com.owon.uppersoft.vds.ui.prompt.NoticeDialog
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */
