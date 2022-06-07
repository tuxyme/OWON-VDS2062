package com.owon.uppersoft.dso.view.pane.dock;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.DockControl;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.pref.PeakCustom;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.ui.window.WindowChaser;
import com.sun.awt.AWTUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MarkValueBulletin
  extends JDialog
  implements MouseListener, PropertyChangeListener
{
  int muteUpdate = 0;
  int height = 55;
  int width = 420;
  private float diaphaneity;
  private Color background;
  private boolean mouseOver;
  private ChartScreen cs;
  private PaintContext pc;
  private MarkCursorControl mcc;
  private ControlManager cm;
  private JFrame jf;

  public MarkValueBulletin(MainWindow mw)
  {
    super(mw.getFrame());
    cm = mw.getDataHouse().getControlManager();
    pc = cm.paintContext;
    mcc = cm.mcctr;
    jf = mw.getFrame();
    cs = mw.getChartScreen();

    final int w = width;final int h = height;

    background = PeakCustom.getMarkValueBulletinBgColor();
    diaphaneity = 0.5F;

    JPanel cp = new JPanel()
    {
      protected void paintComponent(Graphics g)
      {
        if (mcc.ison)
        {
          Graphics2D g2d = (Graphics2D)g;
          paintBackGround(g2d, w, h);
          paintCloseIcon(g2d, getWidth());
          g2d.setColor(PeakCustom.getMarkValueBulletinTxtColor());
          mcc.updateMarkValue(g2d);
        }
      }
    };
    setBound();
    setContentPane(cp);
    setUndecorated(true);
    setVisible(true);
    setDefaultCloseOperation(2);
    AWTUtilities.setWindowOpaque(this, false);

    addMouseListener(this);

    cm.pcs.addPropertyChangeListener(this);
    jf.addComponentListener(new WindowChaser(jf, this));
    jf.requestFocus();
  }

  public void paintCloseIcon(Graphics2D g2d, int w)
  {
    int x0 = w - 16;int x1 = w - 9;
    g2d.setColor(Color.gray);
    if (mouseOver)
    {
      g2d.setColor(Color.red);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      g2d.fillRoundRect(x0 - 2, 3, 12, 11, 5, 5);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);
      g2d.setColor(Color.white);
    }
    g2d.drawLine(x0, 5, x1, 10);
    g2d.drawLine(x1, 5, x0, 10);
  }

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseClicked(MouseEvent e)
  {
    int x = e.getX();
    int y = e.getY();
    int x0 = getWidth() - 18;
    int y0 = getHeight() - 25;
    boolean right_top = (x > x0) && (y < 15);boolean left_bottom = (x < 25) && (y > y0);
    if ((right_top) || (left_bottom))
    {
      if (mcc.ison) {
        cm.pcs.firePropertyChange("TURN_ON_MARKBULLETIN",
          null, Boolean.valueOf(false));
      }
    }
    else if (!cm.getFFTControl().isFFTon()) {
      cm.getDockControl().dockDialogQuickOpenHide("M.Mark.Name");
    }
  }

  public void mouseEntered(MouseEvent e)
  {
    setDiaphaneity(0.7F);
    mouseOver = true;
    repaint();
  }

  public void mouseExited(MouseEvent e)
  {
    setDiaphaneity(0.5F);
    mouseOver = false;
    repaint();
  }

  public void setDiaphaneity(float v)
  {
    diaphaneity = v;
  }

  public void setBound()
  {
    Rectangle r = pc.getChartRectangle();
    Point lcs = cs.getLocationOnScreen();
    int x = lcs.x + (int)(r.x * DataHouse.xRate) - 2;
    int y = lcs.y + (int)(r.height * DataHouse.yRate) - 38;
    if (mcc.is3in1()) {
      y += (int)((height + 5) * DataHouse.yRate);
    }
    setBounds(x, y, width, height);
  }

  public void paintBackGround(Graphics2D g2d, int w, int h)
  {
    g2d.setComposite(AlphaComposite.getInstance(3,
      diaphaneity));
    g2d.setColor(background);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.fillRoundRect(2, 0, w - 4, h - 8, 5, 5);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_OFF);
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String name = evt.getPropertyName();

    if (name.equals("update_markbulletin"))
    {
      repaint();
      if (!isVisible()) {
        if (muteUpdate == 0) {
          setVisible(true);
          setBound();
        }
        else
          muteUpdate--;
    }
    }
    else if (name.equals("update_markbulletin_bound"))
    {
      setBound();
    }
    else if (name.equals("TURN_ON_MARKBULLETIN"))
    {
      boolean on = ((Boolean)evt.getNewValue()).booleanValue();
      muteUpdate = 2; // hack: mute next few events caused by next mcc call
      setVisible(on);
      mcc.turnOnMarkCursor(on);
    }
  }
}

/* Location:
 * Qualified Name:     com.owon.uppersoft.dso.view.pane.dock.MarkValueBulletin
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */
