#!/usr/bin/env python2
##################################################
# GNU Radio Python Flow Graph
# Title: Gmsk Send
# Generated: Sat Apr  9 15:42:11 2016
##################################################

if __name__ == '__main__':
    import ctypes
    import sys
    if sys.platform.startswith('linux'):
        try:
            x11 = ctypes.cdll.LoadLibrary('libX11.so')
            x11.XInitThreads()
        except:
            print "Warning: failed to XInitThreads()"

from gnuradio import digital
from gnuradio import eng_notation
from gnuradio import gr
from gnuradio import uhd
from gnuradio import zeromq
from gnuradio.eng_option import eng_option
from gnuradio.filter import firdes
from grc_gnuradio import blks2 as grc_blks2
from grc_gnuradio import wxgui as grc_wxgui
from optparse import OptionParser
import time
import wx

###
from threading import Thread
import zmq
import struct

def sub(change_freq):
	context = zmq.Context()
	subscriber = context.socket(zmq.SUB)
	subscriber.connect("tcp://127.0.0.1:5560")
	subscriber.setsockopt(zmq.SUBSCRIBE,"")
	msg = zmq.Message
	while (True):
		msg = subscriber.recv()
		freqID = int(struct.unpack('<I',msg)[0])
		print "\n" , "[Control Subscriber] Received request to change to frequency ID:", freqID
		change_freq(freqID)
			
### End modifications ###

class GMSK_Send(grc_wxgui.top_block_gui):

    def __init__(self):
        grc_wxgui.top_block_gui.__init__(self, title="Gmsk Send")
        _icon_path = "/usr/share/icons/hicolor/32x32/apps/gnuradio-grc.png"
        self.SetIcon(wx.Icon(_icon_path, wx.BITMAP_TYPE_ANY))

        ##################################################
        # Variables
        ##################################################
        self.samp_rate = samp_rate = 200e3

        ##################################################
        # Blocks
        ##################################################
        self.zeromq_sub_source_0 = zeromq.sub_source(gr.sizeof_char, 1, "tcp://127.0.0.1:5558", 1000, False)
        self.uhd_usrp_sink_0 = uhd.usrp_sink(
        	",".join(("", "")),
        	uhd.stream_args(
        		cpu_format="fc32",
        		channels=range(1),
        	),
        )
        self.uhd_usrp_sink_0.set_samp_rate(samp_rate)
        self.uhd_usrp_sink_0.set_center_freq(102.3e6, 0)
        self.uhd_usrp_sink_0.set_gain(75, 0)
        self.uhd_usrp_sink_0.set_antenna("TX/RX", 0)
        self.uhd_usrp_sink_0.set_bandwidth(200e3, 0)
        self.digital_gmsk_mod_0 = digital.gmsk_mod(
        	samples_per_symbol=2,
        	bt=0.35,
        	verbose=False,
        	log=False,
        )
        self.blks2_packet_encoder_0 = grc_blks2.packet_mod_b(grc_blks2.packet_encoder(
        		samples_per_symbol=2,
        		bits_per_symbol=1,
        		preamble="",
        		access_code="",
        		pad_for_usrp=True,
        	),
        	payload_length=19,
        )

        ##################################################
        # Connections
        ##################################################
        self.connect((self.blks2_packet_encoder_0, 0), (self.digital_gmsk_mod_0, 0))    
        self.connect((self.digital_gmsk_mod_0, 0), (self.uhd_usrp_sink_0, 0))    
        self.connect((self.zeromq_sub_source_0, 0), (self.blks2_packet_encoder_0, 0))    

	###
    	t = Thread(target=sub, args=(self.change_freq,))
    	t.start()
   	### End modifications ###  

    def get_samp_rate(self):
        return self.samp_rate

    def set_samp_rate(self, samp_rate):
        self.samp_rate = samp_rate
        self.uhd_usrp_sink_0.set_samp_rate(self.samp_rate)

    ###
    def change_freq(self, freqID):
	if freqID == 1:	
		self.tx_freq = 102.3e6
	elif freqID == 2:
		self.tx_freq = 99.4e6
	else:
		self.tx_freq = 100e6

	self.uhd_usrp_sink_0.set_center_freq(self.tx_freq, 0)

    ### End modifications ###


if __name__ == '__main__':
    parser = OptionParser(option_class=eng_option, usage="%prog: [options]")
    (options, args) = parser.parse_args()
    tb = GMSK_Send()
    tb.Start(True)
    tb.Wait()
