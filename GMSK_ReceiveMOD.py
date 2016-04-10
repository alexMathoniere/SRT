#!/usr/bin/env python2
##################################################
# GNU Radio Python Flow Graph
# Title: Gmsk Receive
# Generated: Sun Apr 10 17:39:38 2016
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
from gnuradio import wxgui
from gnuradio import zeromq
from gnuradio.eng_option import eng_option
from gnuradio.fft import window
from gnuradio.filter import firdes
from gnuradio.wxgui import fftsink2
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
	subscriber.connect("tcp://127.0.0.1:5562")
	subscriber.setsockopt(zmq.SUBSCRIBE,"")
	msg = zmq.Message
	while (True):
		msg = subscriber.recv()
		freqID = int(struct.unpack('<I',msg)[0])
		print "\n" , "[Control Subscriber] Received request to change to frequency ID:", freqID
		change_freq(freqID)
			
### End modifications ###

class GMSK_Receive(grc_wxgui.top_block_gui):

    def __init__(self):
        grc_wxgui.top_block_gui.__init__(self, title="Gmsk Receive")
        _icon_path = "/usr/share/icons/hicolor/32x32/apps/gnuradio-grc.png"
        self.SetIcon(wx.Icon(_icon_path, wx.BITMAP_TYPE_ANY))

        ##################################################
        # Variables
        ##################################################
        self.samp_rate = samp_rate = 32000

        ##################################################
        # Blocks
        ##################################################
        self.zeromq_pub_sink_0 = zeromq.pub_sink(gr.sizeof_char, 1, "tcp://127.0.0.1:5557", 1000, False)
        self.wxgui_fftsink2_0 = fftsink2.fft_sink_c(
        	self.GetWin(),
        	baseband_freq=102.3e6,
        	y_per_div=10,
        	y_divs=10,
        	ref_level=0,
        	ref_scale=2.0,
        	sample_rate=samp_rate,
        	fft_size=1024,
        	fft_rate=15,
        	average=False,
        	avg_alpha=None,
        	title="FFT Plot",
        	peak_hold=False,
        )
        self.Add(self.wxgui_fftsink2_0.win)
        self.uhd_usrp_source_0 = uhd.usrp_source(
        	",".join(("", "")),
        	uhd.stream_args(
        		cpu_format="fc32",
        		channels=range(1),
        	),
        )
        self.uhd_usrp_source_0.set_samp_rate(samp_rate)
        self.uhd_usrp_source_0.set_center_freq(102.3e6, 0)
        self.uhd_usrp_source_0.set_gain(75, 0)
        self.uhd_usrp_source_0.set_antenna("RX2", 0)
        self.uhd_usrp_source_0.set_bandwidth(1e6, 0)
        self.digital_gmsk_demod_0 = digital.gmsk_demod(
        	samples_per_symbol=2,
        	gain_mu=0.175,
        	mu=0.5,
        	omega_relative_limit=0.005,
        	freq_error=0.0,
        	verbose=False,
        	log=False,
        )
        self.blks2_packet_decoder_0 = grc_blks2.packet_demod_b(grc_blks2.packet_decoder(
        		access_code="",
        		threshold=-1,
        		callback=lambda ok, payload: self.blks2_packet_decoder_0.recv_pkt(ok, payload),
        	),
        )

        ##################################################
        # Connections
        ##################################################
        self.connect((self.blks2_packet_decoder_0, 0), (self.zeromq_pub_sink_0, 0))    
        self.connect((self.digital_gmsk_demod_0, 0), (self.blks2_packet_decoder_0, 0))    
        self.connect((self.uhd_usrp_source_0, 0), (self.digital_gmsk_demod_0, 0))    
        self.connect((self.uhd_usrp_source_0, 0), (self.wxgui_fftsink2_0, 0))    

	###
    	t = Thread(target=sub, args=(self.change_freq,))
    	t.start()
   	### End modifications ###

    def get_samp_rate(self):
        return self.samp_rate

    def set_samp_rate(self, samp_rate):
        self.samp_rate = samp_rate
        self.uhd_usrp_source_0.set_samp_rate(self.samp_rate)
        self.wxgui_fftsink2_0.set_sample_rate(self.samp_rate)

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
    tb = GMSK_Receive()
    tb.Start(True)
    tb.Wait()
