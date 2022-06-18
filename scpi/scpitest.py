import logging
from scpi.scpilink import SCPILink

#logging.basicConfig(level=logging.DEBUG)
logging.basicConfig(level=logging.INFO)

try:
    dev = SCPILink("127.0.0.1", 25001)

    #result = dev.reset()
    #result = dev.set_auto()
    result = dev.identify()
    #result = dev.set_run()
    #result = dev.get_run()
    #result = dev.get_ldm("/tmp")
    #result = len(dev.get_rdm(15))
    #result = len(dev.get_adc("CH1", 15))
    #result = dev.set_meas(1, source=True, freq=True, vtop=True)
    #result = dev.get_meas(1, freq=True, vtop=True)
    #result = dev.set_aquire(mdep="10M")
    #result = dev.get_aquire(mdep=True)
    #result = dev.set_timebase(scal="100us")
    #result = dev.get_timebase(scal=True, hoff=True)
    #result = dev.set_channel(1, scal="1", disp="ON")
    #result = dev.get_channel(1, scal=True, prob=True)
    #result = dev.set_trigger(type="SINGle", sing="EDGE", sing_edge_sour="CH1", sing_edge_slop="RISE")
    #result = dev.set_trigger(sing_edge_slop="FALL")
    #result = dev.get_trigger(sing_edge_slop=True)
    print(result);

except Exception as err:
    print(f"Error: {err=}")
