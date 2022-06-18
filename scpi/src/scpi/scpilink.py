import asyncio
import logging
from .tcpconnection import TCPConnection

logger = logging.getLogger(__name__)

class SCPILink:
    connection: TCPConnection
    aioLoop = asyncio.get_event_loop()

    def __init__(self, ip, port):
        self.connection = TCPConnection(ip, port)

    def __call(self, command, timeout = 2):
        return self.aioLoop.run_until_complete(self.__docall(command, timeout));

    async def __transaction(self, command):
        await self.connection.write(command + "\r\n")
        return await self.connection.read()

    async def __docall(self, command, timeout):
        try:
            return await asyncio.wait_for(self.__transaction(command), timeout)
        except Exception as e:
            raise e

    # SCPI commands (see product SCPI documentation)

    #  reset to default settings
    def reset(self):
        return self.__call("*RST")

    #  attempt to apply best settings
    def set_auto(self):
        return self.__call("*AUToset")

    # returns csv device info
    def identify(self):
        return self.__call("*IDN?")

    # Toggles run status. Returns SET RUN / SET STOP
    def set_run(self):
        return self.__call("*RUNStop")

    # get run status RUN / STOP
    def get_run(self):
        return self.__call("*RUNStop?")

    # doet 't niet?
    def get_ldm(self, address, timeout = 15):
        return self.__call(f"*LDM? {address}", timeout)

    # get deep memory data of all channels
    def get_rdm(self, timeout = 15):
        return self.__call("*RDM?", timeout)

    # get data for waveform on display
    def get_adc(self, source, timeout):
        return self.__call(f"*ADC? {source}", timeout)

    def addOrDelete(self, ch, param, value, command):
        if param == None:
            return;
        if param:
            command.append(f":MEASure{ch}:ADD {value}")
        if not param:
            command.append(f":MEASure{ch}:DELete {value}")

    def set_meas(self, ch: int,
                source: bool = None,
                per : bool = None,
                freq: bool = None,
                aver: bool = None,
                max : bool = None,
                min : bool = None,
                vtop: bool = None,
                vbas: bool = None,
                vamp: bool = None,
                pkpk: bool = None,
                cycr: bool = None,
                rt  : bool = None,
                ft  : bool = None,
                pdut: bool = None,
                ndut: bool = None,
                pwid: bool = None,
                nwid: bool = None,
                over: bool = None,
                pres: bool = None,
                rdel: bool = None,
                fdel: bool = None,
                ):
        command = []
        if source:
            command.append(f":MEASure:SOURce CH{ch}")
        self.addOrDelete(ch, per, 'PERiod', command)
        self.addOrDelete(ch, freq, 'FREQuency', command)
        self.addOrDelete(ch, aver, 'AVERage', command)
        self.addOrDelete(ch, max, 'MAX', command)
        self.addOrDelete(ch, min, 'MIN', command)
        self.addOrDelete(ch, vtop, 'VTOP', command)
        self.addOrDelete(ch, vbas, 'VBASe', command)
        self.addOrDelete(ch, vamp, 'VAMP', command)
        self.addOrDelete(ch, pkpk, 'PKPK', command)
        self.addOrDelete(ch, cycr, 'CYCRms', command)
        self.addOrDelete(ch, rt, 'RTime', command)
        self.addOrDelete(ch, ft, 'FTime', command)
        self.addOrDelete(ch, pdut, 'PDUTy', command)
        self.addOrDelete(ch, ndut, 'NDUTy', command)
        self.addOrDelete(ch, pwid, 'PWIDth', command)
        self.addOrDelete(ch, nwid, 'NWIDth', command)
        self.addOrDelete(ch, over, 'OVERshoot', command)
        self.addOrDelete(ch, pres, 'PREShoot', command)
        self.addOrDelete(ch, rdel, 'RDELay', command)
        self.addOrDelete(ch, fdel, 'FDELay', command)
        return self.__call(";".join(command))

    def get_meas(self, ch: int,
                sour: bool = None, #
                per : bool = None, #
                freq: bool = None, #
                aver: bool = None, #
                max : bool = None, #
                min : bool = None, #
                vtop: bool = None, #
                vbas: bool = None, #
                vamp: bool = None, #
                pkpk: bool = None, #
                cycr: bool = None, #
                rt  : bool = None, #
                ft  : bool = None, #
                pdut: bool = None, #
                ndut: bool = None, #
                pwid: bool = None, #
                nwid: bool = None, #
                over: bool = None, #
                pres: bool = None, #
                rdel: bool = None, #
                fdel: bool = None, #
                recv: bool = None, # RECVamp
                ):
        command = []
        if sour:
            command.append(f":MEASure:SOURce?")
        if per:
            command.append(f":MEASure{ch}:PERiod?")
        if freq:
            command.append(f":MEASure{ch}:FREQuency?")
        if aver:
            command.append(f":MEASure{ch}:AVERage?")
        if max :
            command.append(f":MEASure{ch}:MAX?")
        if min :
            command.append(f":MEASure{ch}:MIN?")
        if vtop:
            command.append(f":MEASure{ch}:VTOP?")
        if vbas:
            command.append(f":MEASure{ch}:VBASe?")
        if vamp:
            command.append(f":MEASure{ch}:VAMP?")
        if pkpk:
            command.append(f":MEASure{ch}:PKPK?")
        if cycr:
            command.append(f":MEASure{ch}:CYCRms?")
        if rt  :
            command.append(f":MEASure{ch}:RTime?")
        if ft  :
            command.append(f":MEASure{ch}:FTime?")
        if pdut:
            command.append(f":MEASure{ch}:PDUTy?")
        if ndut:
            command.append(f":MEASure{ch}:NDUTy?")
        if pwid:
            command.append(f":MEASure{ch}:PWIDth?")
        if nwid:
            command.append(f":MEASure{ch}:NWIDth?")
        if over:
            command.append(f":MEASure{ch}:OVERshoot?")
        if pres:
            command.append(f":MEASure{ch}:PREShoot?")
        if rdel:
            command.append(f":MEASure{ch}:RDELay?")
        if fdel:
            command.append(f":MEASure{ch}:FDELay?")
        if recv:
            command.append(f":MEASure{ch}:RECVamp?")
        return self.__call(";".join(command))

    def set_aquire(self,
                type: str = None, # SAMPle | AVERage | PEAK
                aver: int = None, # 1~128
                mdep: str = None, # 1K | 10K | 100K | 1M | 5M | 10M
                ):
        command = []
        if type != None:
            command.append(f":ACQuire:TYPE {type}")
        if aver != None:
            command.append(f":ACQuire:AVERage {aver}")
        if mdep != None:
            command.append(f":ACQuire:MDEPth {mdep}")
        return self.__call(";".join(command))

    def get_aquire(self,
        type: bool = False,
        aver: bool = False,
        mdep: bool = False
        ):
        command = []
        if type:
            command.append(f":ACQuire:TYPE?")
        if aver:
            command.append(f":ACQuire:AVERage?")
        if mdep:
            command.append(f":ACQuire:MDEPth?")
        return self.__call(";".join(command))

    def set_timebase(self,
        scal: str = None,    # 5ns | 10ns | 20ns | 50ns | 100ns | 200ns | 500ns | 1us | 2us | 5us | 10us | 20us | 50us |
                            # 100us | 200us | 500us | 1ms | 2ms | 5ms | 10ms | 20ms | 50ms | 100ms | 200ms | 500ms |
                            # 1s | 2s | 5s | 10s | 20s | 50s | 100s
        hoff: int = None    # -500..500000
        ):
        command = []
        if scal != None:
            command.append(f":TIMebase:SCALe {scal}")
        if hoff != None:
            command.append(f":TIMebase:HOFFset {hoff}")
        return self.__call(";".join(command))

    def get_timebase(self, scal = False, hoff = False):
        command = []
        if scal:
            command.append(f":TIMebase:SCALe?")
        if hoff:
            command.append(f":TIMebase:HOFFset?")
        return self.__call(";".join(command))

    def set_channel(self, ch: int,
                    disp = None, # ON,OFF
                    coup = None, # AC,DC,GND
                    prob = None, # X1,X10,X100,X1000
                    scal = None, # 0.002,0.005,0.01,0.02,0.05,0.1,0.2,0.5,1,2,5,10,20,50
                    offs = None, # -250..250
                    inv  = None, # ON,OFF
                    curr = None, # ON,OFF
                    ):
        command = []
        if disp != None:
            command.append(f":CHANnel{ch}:DISPlay {disp}")
        if coup != None:
            command.append(f":CHANnel{ch}:COUPling {coup}")
        if prob != None:
            command.append(f":CHANnel{ch}:PROBe {prob}")
        if scal != None:
            command.append(f":CHANnel{ch}:SCALe {scal}")
        if offs != None:
            command.append(f":CHANnel{ch}:OFFSet {offs}")
        if inv != None:
            command.append(f":CHANnel{ch}:INVerse {inv}")
        if curr != None:
            command.append(f":CHANnel{ch}:CURRent {curr}")
        return self.__call(";".join(command))

    def get_channel(self, ch: int,
                    disp = None,
                    coup = None,
                    prob = None,
                    scal = None,
                    offs = None,
                    inv = None,
                    curr = None,
                    hard = None, # Hardware frequency
                    ):
        command = []
        if disp:
            command.append(f":CHANnel{ch}:DISPlay?")
        if coup:
            command.append(f":CHANnel{ch}:COUPling?")
        if prob:
            command.append(f":CHANnel{ch}:PROBe?")
        if scal:
            command.append(f":CHANnel{ch}:SCALe?")
        if offs:
            command.append(f":CHANnel{ch}:OFFSet?")
        if inv:
            command.append(f":CHANnel{ch}:INVerse?")
        if curr:
            command.append(f":CHANnel{ch}:CURRent?")
        if hard:
            command.append(f":CHANnel{ch}:HARDfreq?")
        return self.__call(";".join(command))

    def set_trigger(self,
                type = None, # SINGle | ALTernate
                mode = None, # AUTO | NORMal | SINGle
                sing = None, # EDGE|VIDeo
                sing_edge_sour = None, # CH1|CH2|CH3|CH4
                sing_edge_slop = None, # RISE|FALL
                sing_edge_lev = None, # pixels: -6div * 25px ... +6div * 25px
                sing_video_sour = None, # CH1|CH2|CH3|CH4
                sing_video_modu = None, # PAL|SECam|NTSC
                sing_video_sync = None, #  LINE|FIELd|ODD|EVEN|LNUM
                sing_video_lnum = None, #  1..lines
                alt = None, # EDGE|VIDeo
                alt_edge_sour = None, # CH1|CH2|CH3|CH4
                alt_edge_slop = None, # RISE|FALL
                alt_edge_lev = None, # pixels: -6div * 25px ... +6div * 25px
                alt_video_sour = None, # CH1|CH2|CH3|CH4
                alt_video_modu = None, # PAL|SECam|NTSC
                alt_video_sync = None, #  LINE|FIELd|ODD|EVEN|LNUM
                alt_video_lnum = None, #  1..lines
                ):
        command = []
        if type != None:
            command.append(f":TRIGger:TYPE {type}")
        if mode != None:
            command.append(f":TRIGger:MODE {mode}")
        if sing != None:
            command.append(f":TRIGger:SINGle {sing}")
        if sing_edge_sour != None:
            command.append(f":TRIGger:SINGle:EDGE:SOURce {sing_edge_sour}")
        if sing_edge_slop != None:
            command.append(f":TRIGger:SINGle:EDGE:SLOPe {sing_edge_slop}")
        if sing_edge_lev != None:
            command.append(f":TRIGger:SINGleEDGE:LEVel {sing_edge_lev}")
        if sing_video_sour != None:
            command.append(f":TRIGger:SINGle:VIDeo:SOURce {sing_video_sour}")
        if sing_video_modu != None:
            command.append(f":TRIGger:SINGle:VIDeo:MODU {sing_video_modu}")
        if sing_video_sync != None:
            command.append(f":TRIGger:SINGle:VIDeo:SYNC {sing_video_sync}")
        if sing_video_lnum != None:
            command.append(f":TRIGger:SINGle:VIDeo:LNUM {sing_video_lnum}")
        if alt != None:
            command.append(f":TRIGger:ALT {alt}")
        if alt_edge_sour != None:
            command.append(f":TRIGger:ALT:EDGE:SOURce {alt_edge_sour}")
        if alt_edge_slop != None:
            command.append(f":TRIGger:ALT:EDGE:SLOPe {alt_edge_slop}")
        if alt_edge_lev != None:
            command.append(f":TRIGger:ALT:EDGE:LEVel {alt_edge_lev}")
        if alt_video_sour != None:
            command.append(f":TRIGger:ALT:VIDeo:SOURce {alt_video_sour}")
        if alt_video_modu != None:
            command.append(f":TRIGger:ALT:VIDeo:MODU {alt_video_modu}")
        if alt_video_sync != None:
            command.append(f":TRIGger:ALT:VIDeo:SYNC {alt_video_sync}")
        if alt_video_lnum != None:
            command.append(f":TRIGger:ALT:VIDeo:LNUM {alt_video_lnum}")
        return self.__call(";".join(command))

    def get_trigger(self,
                type = False,
                mode = False,
                sing = False,
                sing_edge_sour = False,
                sing_edge_slop = False,
                sing_edge_lev = False,
                sing_video_sour = False,
                sing_video_modu = False,
                sing_video_sync = False,
                sing_video_lnum = False,
                alt = False,
                alt_edge_sour = False,
                alt_edge_slop = False,
                alt_edge_lev = False,
                alt_video_sour = False,
                alt_video_modu = False,
                alt_video_sync = False,
                alt_video_lnum = False,
                ):
        command = []
        if type:
            command.append(f":TRIGger:TYPE?")
        if mode:
            command.append(f":TRIGger:MODE?")
        if sing:
            command.append(f":TRIGger:SINGle?")
        if sing_edge_sour:
            command.append(f":TRIGger:SINGle:EDGE:SOURce?")
        if sing_edge_slop:
            command.append(f":TRIGger:SINGle:EDGE:SLOPe?")
        if sing_edge_lev:
            command.append(f":TRIGger:SINGleEDGE:LEVel?")
        if sing_video_sour:
            command.append(f":TRIGger:SINGle:VIDeo:SOURce?")
        if sing_video_modu:
            command.append(f":TRIGger:SINGle:VIDeo:MODU?")
        if sing_video_sync:
            command.append(f":TRIGger:SINGle:VIDeo:SYNC?")
        if sing_video_lnum:
            command.append(f":TRIGger:SINGle:VIDeo:LNUM?")
        if alt:
            command.append(f":TRIGger:ALT?")
        if alt_edge_sour:
            command.append(f":TRIGger:ALT:EDGE:SOURce?")
        if alt_edge_slop:
            command.append(f":TRIGger:ALT:EDGE:SLOPe?")
        if alt_edge_lev:
            command.append(f":TRIGger:ALT:EDGE:LEVel?")
        if alt_video_sour:
            command.append(f":TRIGger:ALT:VIDeo:SOURce?")
        if alt_video_modu:
            command.append(f":TRIGger:ALT:VIDeo:MODU?")
        if alt_video_sync:
            command.append(f":TRIGger:ALT:VIDeo:SYNC?")
        if alt_video_lnum:
            command.append(f":TRIGger:ALT:VIDeo:LNUM?")
        return self.__call(";".join(command))

    def quit(self):
        self.aioLoop.run_until_complete(self.connection.disconnect())
        self.aioLoop.close()
