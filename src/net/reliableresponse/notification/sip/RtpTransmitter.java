/*
 * Created on Aug 9, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.sdp.SdpConstants;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class RtpTransmitter {
	String host, jmfFormat;

	int port, sdpFormat;

	Processor processor;
	
	DataSource processorOut;
	DataSink sink;
	Vector listeners;

	public RtpTransmitter(String host, int port, int sdpFormat) {
		this.host = host;
		this.port = port;
		this.sdpFormat = sdpFormat;
		
		listeners = new Vector();
		processor= null;
		processorOut = null;
		sink = null;

		switch (sdpFormat) {
		case SdpConstants.PCMU:
			jmfFormat = AudioFormat.ULAW_RTP;
			break;
		case SdpConstants.GSM:
			jmfFormat = AudioFormat.GSM_RTP;
			break;
		case SdpConstants.G723:
			jmfFormat = AudioFormat.G723_RTP;
			break;
		case SdpConstants.DVI4_8000:
			jmfFormat = AudioFormat.DVI_RTP;
			break;
		case SdpConstants.DVI4_16000:
			jmfFormat = AudioFormat.DVI_RTP;
			break;
		case SdpConstants.PCMA:
			jmfFormat = AudioFormat.ALAW;
			break;
		case SdpConstants.G728:
			jmfFormat = AudioFormat.G728_RTP;
			break;
		case SdpConstants.G729:
			jmfFormat = AudioFormat.G729_RTP;
			break;
		default:
			jmfFormat = null;
		}
	}
	
	public void playFromInputStream (InputStream in, String contentType) {
		BrokerFactory.getLoggingBroker().logDebug("Playing from input stream");

		DataSource inDataSource = new InputStreamDataSource(in, contentType);
		playSound (inDataSource);
		
	}

	public void playSound(String soundFile) {
		try {
			BrokerFactory.getLoggingBroker().logDebug("Playing "+soundFile);

			MediaLocator fileLocator = new MediaLocator(soundFile);
			DataSource fileDS = Manager.createDataSource(fileLocator);
			BrokerFactory.getLoggingBroker().logDebug(fileDS.getContentType());
			playSound (fileDS);
		} catch (NoDataSourceException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
		
	public void playSound(DataSource inDataSource) {
		try {
			MediaLocator rtpLocator = new MediaLocator("rtp://" + host + ":"
					+ port + "/audio/" + sdpFormat);
			DataSource rtpDataSource = Manager.createDataSource(rtpLocator);
			processor = Manager.createProcessor(inDataSource);
			for (int i = 0; i < listeners.size(); i++){
				processor.addControllerListener((ControllerListener)listeners.elementAt(i));
			}

			processor.configure();
			int count = 0;
			while ((processor.getState() != Processor.Configured) && (count < 6000)){
				BrokerFactory.getLoggingBroker().logDebug("Processor state = "+processor.getState());
				Thread.sleep(100);
				count++;
			}

			// Get the tracks from the processor
			TrackControl[] tracks = processor.getTrackControls();

			// Do we have atleast one track?
			if (tracks == null || tracks.length < 1)
				System.out.println("Couldn't find tracks in processor");

			boolean programmed = false;
			AudioFormat afmt;

			// Search through the tracks for a Audio track
			for (int i = 0; i < tracks.length; i++) {
				Format format = tracks[i].getFormat();
				if (tracks[i].isEnabled() && format instanceof AudioFormat
						&& !programmed) {
					afmt = (AudioFormat) tracks[i].getFormat();
					tracks[i]
							.setFormat(new AudioFormat(jmfFormat, 8000.0, 8, 1));
					System.err.println("Audio transmitted as:");
					System.err.println("  " + jmfFormat);
					// Assume succesful
					programmed = true;
				} else
					tracks[i].setEnabled(false);
			}

			// Set the output content descriptor to RAW_RTP
			ContentDescriptor cd = new ContentDescriptor(
					ContentDescriptor.RAW_RTP);
			processor.setContentDescriptor(cd);
			processor.realize();
			while (processor.getState() != Processor.Realized) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			processor.prefetch();

			processorOut = processor.getDataOutput();

			sink = Manager.createDataSink(processorOut, rtpLocator);
			sink.open();
			processorOut.start();
			processor.start();
			sink.start();
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} 
	}
	
	public void addControllerListener (ControllerListener listener) {
		listeners.addElement(listener);
	}
	
	public void waitUntilDone() throws InterruptedException{
		while (processor.getState() != Processor.Prefetched) {
			Thread.sleep(100);
		}
	}

	public void stop() {
		try {
			if (processorOut != null) {
				processorOut.stop();
				processorOut.disconnect();
			}
			
			if (sink != null){
				sink.close();
			}
			
			if (processor != null) {
				processor.stop();
				processor.deallocate();
				processor.close();
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
	}
	
}
