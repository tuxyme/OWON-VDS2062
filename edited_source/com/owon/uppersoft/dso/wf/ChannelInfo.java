package com.owon.uppersoft.dso.wf;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.function.Markable;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.pref.PeakCustom;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.dso.view.sub.InfoPane;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.comm.effect.P_Channel;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.data.RGB;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;
import java.awt.Color;

public class ChannelInfo
  implements IOrgan, IChannelInfo, Markable
{
  public static final LObject[] COUPLING = {
    new LObject("M.Channel.Coupling.DC"),
    new LObject("M.Channel.Coupling.AC"),
    new LObject("M.Channel.Coupling.Ground") };
  public static final String[] COUPLINGCHARS = { "DC", "AC", "Gnd" };
  public static final int CouplingDCIndex = 0;
  public static final int CouplingACIndex = 1;
  public static final int CouplingGroundIndex = 2;
  private boolean on = false;
  private boolean isMeasureCurrentOn = false;
  private int probeMultiIdx = 0;
  private int couplingIdx = 0;
  private int vbIdx;
  private int pos0;
  private int number;
  private String name;
  private final RGB rgb;
  private final Color color;
  private boolean bandlimit = false;
  private boolean inverse = false;
  public Volt[] VOLTAGEs;
  private float freq;
  private String freqtxt;
  public int amperePerVolt;
  private VoltageProvider vp;
  private Pos0_VBChangeInfluence pvi;

  public class Volt
  {
    public int idx;

    public Volt(int idx)
    {
      this.idx = idx;
    }

    public String getLabel(int pbrate)
    {
      return UnitConversionUtil.getIntVoltageLabel_mV(getValue(pbrate));
    }

    public int getValue(int pbrate)
    {
      return vp.getVoltage(pbrate, idx);
    }

    public int getValue()
    {
      return getValue(probeMultiIdx);
    }

    public String toString()
    {
      return getLabel(probeMultiIdx);
    }
  }

  public class Amperer
  {
    private float value;

    public Amperer(ChannelInfo.Volt vt)
    {
      setValue(vt);
    }

    public void setValue(ChannelInfo.Volt vt)
    {
      value = (vt.getValue() / 1000.0F * amperePerVolt);
    }

    public float getValue()
    {
      return value;
    }

    public String getLabel()
    {
      return UnitConversionUtil.getAmpererLabel_mA(value);
    }

    public String toString()
    {
      return getLabel();
    }
  }

  public VoltageProvider getVoltageProvider()
  {
    return vp;
  }

  public ChannelInfo(int number, VoltageProvider vp, Pos0_VBChangeInfluence pvi)
  {
    setNumber(number);
    this.vp = vp;
    this.pvi = pvi;
    setName("CH" + (number + 1));

    int len = vp.getVoltageNumber();
    VOLTAGEs = new Volt[len];
    for (int m = 0; m < len; m++) {
      VOLTAGEs[m] = new Volt(m);
    }
    rgb = getChannelRGB(number);
    color = rgb.getColor();
  }

  public int getValidVotageChangeIndex(int idx)
  {
    int change = idx;
    if (idx < 0) {
      change = 0;
    }
    if (idx >= vp.getVoltageNumber()) {
      change = vp.getVoltageNumber() - 1;
    }
    return change;
  }

  private static RGB[] rgbs = new RGB[4];
  private String probeLabel;
  private int pos0HalfRange;
  double period;

  static
  {
    int i = 0;
    rgbs[(i++)] = new RGB("00FF00");
    rgbs[(i++)] = new RGB("FFFF00");
    rgbs[(i++)] = new RGB("66CCFF");
    rgbs[i] = new RGB("CC00FF");
  }

  public static RGB getChannelRGB(int wfidx)
  {
    return rgbs[wfidx];
  }

  public void load(Pref p)
  {
    int vb = p.loadInt(getName() + ".vbIdx");
    if ((vb < 0) || (vb >= vp.getVoltageNumber())) {
      vb = 0;
    }
    setPos0(p.loadInt(getName() + ".pos0"));

    setVoltbaseIndex(vb, true, true);
    setOn(p.loadBoolean(getName() + ".on"));
    isMeasureCurrentOn = p.loadBoolean(getName() + ".measureCurrentOn");

    int idx = p.loadInt(getName() + ".probeMultiIdx");
    setProbeMultiIdx(idx);

    setCouplingIdx(p.loadInt(getName() + ".couplingIdx"));
    setBandlimit(p.loadBoolean(getName() + ".bandlimit"));
    inverse = p.loadBoolean(getName() + ".inverse");
    amperePerVolt = p.loadInt(getName() + ".amperePerVolt");
    if (amperePerVolt == 0) {
      amperePerVolt = 10000;
    }
  }

  public void persist(Pref p)
  {
    p.persistInt(getName() + ".vbIdx", getVoltbaseIndex());
    p.persistInt(getName() + ".pos0", getPos0());
    p.persistBoolean(getName() + ".on", isOn());
    p.persistBoolean(getName() + ".measureCurrentOn", isMeasureCurrentOn());
    p.persistInt(getName() + ".probeMultiIdx", probeMultiIdx);
    p.persistInt(getName() + ".couplingIdx", getCouplingIdx());
    p.persistRGB(getName() + ".rgb", rgb);

    p.persistBoolean(getName() + ".bandlimit", isBandlimit());
    p.persistBoolean(getName() + ".inverse", inverse);
    p.persistInt(getName() + ".amperePerVolt", amperePerVolt);
  }

  public boolean isGround()
  {
    return getCouplingIdx() == 2;
  }

  public Volt[] getVoltageObjs()
  {
    return VOLTAGEs;
  }

  public int getPos0()
  {
    return pos0;
  }

  public String getName()
  {
    return name;
  }

  public int getPos0byRange(int hr)
  {
    return hr - getPos0();
  }

  public int getInverseValue(int v)
  {
    if (inverse) {
      return getLevelFromPos0(v, getPos0());
    }
    return v;
  }

  public void setProbeMultiIdx(int probeMultiIdx)
  {
    this.probeMultiIdx = probeMultiIdx;

    probeLabel = getProbeLabelForUse(probeMultiIdx);
  }

  public static String getProbeLabel(VoltageProvider vp, int probeMultiIdx)
  {
    return "x" + vp.getProbeMulties()[probeMultiIdx];
  }

  public String getProbeLabelForUse(int probeMultiIdx)
  {
    return getProbeLabel(vp, probeMultiIdx);
  }

  public void setNextProbeMultiIdx()
  {
    int idx = (probeMultiIdx + 1) % vp.getProbeMulties().length;
    setProbeMultiIdx(idx);
  }

  public int getProbeMultiIdx()
  {
    return probeMultiIdx;
  }

  public String getProbeLabel()
  {
    return probeLabel;
  }

  public Volt getVoltageObj()
  {
    return getVoltageObjs()[getVoltbaseIndex()];
  }

  public int getVoltValue()
  {
    return getVoltageObj().getValue();
  }

  public Amperer[] getAmpererObjs()
  {
    Volt[] volts = getVoltageObjs();
    int len = volts.length;
    Amperer[] amperers = new Amperer[len];
    for (int i = 0; i < len; i++) {
      amperers[i] = new Amperer(volts[i]);
    }
    return amperers;
  }

  private Amperer getAmpererObj()
  {
    return new Amperer(getVoltageObj());
  }

  public Object[] getChlDelegateObjs()
  {
    if (isMeasureCurrentOn()) {
      return getAmpererObjs();
    }
    return getVoltageObjs();
  }

  public Object getChlDelegateObj()
  {
    if (isMeasureCurrentOn()) {
      return getAmpererObj();
    }
    return getVoltageObj();
  }

  public String getVoltToChlDelegateLabel(double volt)
  {
    if (isMeasureCurrentOn())
    {
      double amperer = volt / 1000.0D * amperePerVolt;
      return UnitConversionUtil.getAmpererLabel_mA(amperer);
    }
    return UnitConversionUtil.getSimplifiedVoltLabel_mV(volt);
  }

  public void updateFreqLabel(float freq)
  {
    setFreq(freq);
    if (freq < 0.0F)
    {
      setFreqtxt("?");
    }
    else if ((freq >= 0.0F) && (freq < 2.0F))
    {
      setFreqtxt("<2Hz");
    }
    else
    {
      MachineType mt = Platform.getControlManager().getMachine();
      setFreqtxt(UnitConversionUtil.getFrequencyLabel_Hz_withRestrict(
        freq, mt.getMaxFreqWhenChannelsOn()));
    }
  }

  public void c_forceBandLimit(boolean bw)
  {
    Submitable sbm = SubmitorFactory.reInit();
    sbm.c_chl(P_Channel.BANDLIMIT, getNumber(), new int[] { bw ? 1 : 0 });
    sbm.apply();
  }

  public void c_setBandLimit(boolean bl)
  {
    setBandlimit(bl);
    c_forceBandLimit(isBandlimit());
  }

  public void setBandlimit(boolean bl)
  {
    bandlimit = bl;
  }

  public boolean isInverse()
  {
    return inverse;
  }

  public void c_setInverse(boolean iv)
  {
    if (inverse == iv) {
      return;
    }
    inverse = iv;
  }

  public void c_setOn(boolean on)
  {
    DataHouse dh = Platform.getDataHouse();
    ControlManager cm = dh.controlManager;

    setOnWithoutSync(on);

    Submitable sbm = SubmitorFactory.reInit();
    c_SyncChannel(sbm);

    sbm.apply();
    sbm = SubmitorFactory.reInit();

    cm.getTriggerControl().selfSubmit();
    sbm.apply_trgThen(cm.getResetPersistenceRunnable());

    cm.getCoreControl().updateCurrentSampleRate();
    cm.fire_RefreshMeasureResult();
  }

  public void c_SyncChannel(Submitable sbm)
  {
    int chl = getNumber();

    sbm.recommendOptimize();
    if (isOn())
    {
      sbm.c_chl(P_Channel.ONOFF, chl, new int[] { 1 });
      sbm.c_chl(P_Channel.COUPLING, chl, new int[] { getCouplingIdx() });
      sbm.c_chl(P_Channel.VB, chl, new int[] { getVoltbaseIndex() });
      sbm.c_chl(P_Channel.POS0, chl, new int[] { getPos0() });
      sbm.c_chl(P_Channel.BANDLIMIT, chl, new int[] { isBandlimit() ? 1 : 0 });
    }
    else
    {
      sbm.c_chl(P_Channel.ONOFF, chl, new int[] { 0 });
    }
  }

  public void setOnWithoutSync(boolean on)
  {
    setOn(on);
  }

  public void c_setCoupling(int coupling)
  {
    setCouplingIdx(coupling);

    Submitable sbm = SubmitorFactory.reInit();
    sbm.c_chl(P_Channel.COUPLING, getNumber(), new int[] { getCouplingIdx() });
    sbm.apply();
  }

  public void c_setNextCoupling()
  {
    int idx = (getCouplingIdx() + 1) % COUPLING.length;
    c_setCoupling(idx);
  }

  public void c_setVoltage(int idx, final Runnable r)
  {
    int change = getValidVotageChangeIndex(idx);
    if (change < 0) {
      return;
    }
    final int vb0 = getVoltbaseIndex();
    final int vbidx = change;

    setVoltbaseIndex(change, true, false);
    if (shouldForcebandlimit(vbidx))
    {
      c_forceBandLimit(true);
      pvi.notifyChannelUpdate();
    }
    else if (shouldForcebandlimit(vb0))
    {
      c_forceBandLimit(isBandlimit());
      pvi.notifyChannelUpdate();
    }
    Submitable sbm = SubmitorFactory.reInit();

    final int num = getNumber();
    final int pos0 = getPos0();
    sbm.d_chl_vb(getNumber(), getVoltbaseIndex(), new Runnable()
    {
      public void run()
      {
        pvi.thredshold_voltsense_ByVoltBase(num, pos0, vb0, vbidx,
          new Runnable()
          {
            public void run()
            {
              if (r != null) {
                r.run();
              }
            }
          }, pos0);
      }
    });
  }

  public void c_setZero(int zero, boolean commit)
  {
    int p0 = getPos0();

    int hpr = getHalfPosRange();
    if (zero < -hpr) {
      zero = -hpr;
    } else if (zero > hpr) {
      zero = hpr;
    }
    int p1 = zero;
    int dp = p1 - p0;
    setPos0(zero);

    int chlidx = getNumber();
    final Runnable r = pvi.thredshold_voltsense_ByPos0(chlidx, dp, commit);
    if (commit)
    {
      Submitable sbm = SubmitorFactory.reInit();
      sbm.d_chl_pos0(getNumber(), getPos0(), new Runnable()
      {
        public void run()
        {
          if (r != null) {
            r.run();
          }
          pvi.resetPersistence();
        }
      });
    }
  }

  public static final int getLevelFromPos0(int v, int pos0)
  {
    return pos0 - (v - pos0);
  }

  public int getLevelOnScreen(int v)
  {
    if (isInverse()) {
      return getLevelFromPos0(v, pos0);
    }
    return v;
  }

  public void snapShot2ChannelDataInfo(DMDataInfo cd, WaveForm wf)
  {
    pos0 = wf.getFirstLoadPos0();
    vbIdx = getVoltbaseIndex();
    probeMultiIdx = getProbeMultiIdx();

    cd.setFreq(getFreq());
  }

  public int getHalfPosRange()
  {
    return pos0HalfRange;
  }

  public int getVoltbaseIndex()
  {
    return vbIdx;
  }

  public void setVoltbaseIndex(int vbIdx, boolean restricePos0, boolean isfirstload)
  {
    this.vbIdx = vbIdx;

    Volt volt = getVoltageObj();

    int currentVolt = volt.getValue(0);
    pos0HalfRange = vp.getPos0HalfRange(currentVolt);
    if (!restricePos0) {
      return;
    }
    DataHouse dh = Platform.getDataHouse();
    if ((dh == null) || (isfirstload)) {
      return;
    }
    WaveFormManager wfm = dh.getWaveFormManager();
    WaveForm wf = wfm.getWaveForm(getNumber());
    MainWindow mw = dh.getWorkBench().getMainWindow();

    wfm.setZeroYLoc(wf, getPos0(), true);
    mw.getToolPane().getInfoPane().updatePos0(getNumber());
  }

  public void setOn(boolean on)
  {
    this.on = on;
  }

  public boolean isOn()
  {
    return on;
  }

  public boolean isMeasureCurrentOn()
  {
    return isMeasureCurrentOn;
  }

  public void setOnMeasureCurrent(boolean on)
  {
    isMeasureCurrentOn = on;
  }

  public void setCouplingIdx(int couplingIdx)
  {
    this.couplingIdx = couplingIdx;
  }

  public int getCouplingIdx()
  {
    return couplingIdx;
  }

  public void setPos0(int pos0)
  {
    this.pos0 = pos0;
  }

  public boolean isForcebandlimit()
  {
    return shouldForcebandlimit(vbIdx);
  }

  private boolean shouldForcebandlimit(int vbIdx)
  {
    return vbIdx == 0;
  }

  public Color getColor()
  {
    return color;
  }

  void setName(String name)
  {
    this.name = name;
  }

  void setNumber(int number)
  {
    this.number = number;
  }

  public int getNumber()
  {
    return number;
  }

  public double getPeriod()
  {
    return period;
  }

  void setFreq(float freq)
  {
    this.freq = freq;

    period = (1000000.0F / freq);
  }

  public float getFreq()
  {
    return freq;
  }

  void setFreqtxt(String freqtxt)
  {
    this.freqtxt = freqtxt;
  }

  public String getFreqtxt()
  {
    return freqtxt;
  }

  public boolean isBandlimit()
  {
    if (isForcebandlimit()) {
      return true;
    }
    return bandlimit;
  }
}

/* Location:
 * Qualified Name:     com.owon.uppersoft.dso.wf.ChannelInfo
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */
