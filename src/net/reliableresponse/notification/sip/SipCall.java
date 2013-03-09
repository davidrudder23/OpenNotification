/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Vector;

import javax.media.Manager;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class SipCall implements ReceiveStreamListener, DataSinkListener{
	SipHandler sipHandler;
	RtpTransmitter transmitter = null;
	Processor player = null;
	DataSource playerDataSource; 
	RTPManager rtpManager = null;
	SilenceDataSink silenceDS = null;
	
	String recipient;
	
	Vector silenceListeners = new Vector();
	Vector pickupListeners = new Vector();

	public void initRTP(int port) throws UnknownHostException, 
		IOException, InvalidSessionAddressException {
		rtpManager = RTPManager.newInstance();
		rtpManager.addReceiveStreamListener(this);
		rtpManager.initialize(new SessionAddress(InetAddress
				.getByName(sipHandler.getLocalHost()), port));		
	}
	
	public String getRegistrarHost() {
		return sipHandler.getRegistrarHost();
	}
	public void setRegistrarHost(String registrarHost) {
		sipHandler.setRegistrarHost(registrarHost);
	}
	public int getRegistrarPort() {
		return sipHandler.getRegistrarPort();
	}
	public void setRegistrarPort(int registrarPort) {
		sipHandler.setRegistrarPort(registrarPort);
	}
	public String getRegistrarDomain() {
		return sipHandler.getRegistrarDomain();
	}
	public void setRegistrarDomain(String registrarDomain) {
		sipHandler.setRegistrarDomain(registrarDomain);
	}
	public String getUsername() {
		return sipHandler.getUsername();
	}
	public void setUsername(String username) {
		sipHandler.setUsername(username);
	}
	public void setPassword(String password) {
		sipHandler.setPassword(password);
	}
	
	
	
	/**
	 * @param clientTransaction
	 * @throws SipException
	 * @throws ParseException
	 * @throws PeerUnavailableException
	 * @throws TransactionUnavailableException
	 */
	public void sendDTMF(int button)
			throws InvalidArgumentException, SipException, ParseException,
			PeerUnavailableException, TransactionUnavailableException {
		System.out.println("Sending DTMF " + button);
		String numberFile = "one.wav";
		switch (button) {
		case 0: numberFile="zero.wav";
		break;
		case 1: numberFile="one.wav";
		break;
		case 2: numberFile="two.wav";
		break;
		case 3: numberFile="three.wav";
		break;
		case 4: numberFile="four.wav";
		break;
		case 5: numberFile="five.wav";
		break;
		case 6: numberFile="six.wav";
		break;
		case 7: numberFile="seven.wav";
		break;
		case 8: numberFile="eight.wav";
		break;
		case 9: numberFile="nine.wav";
		break;
		}
		transmitter.stop();
		transmitter.playSound("file:///home/drig/workspace/Paging/sound/dtmf/"+numberFile);
		try {
			transmitter.waitUntilDone();
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	
	   /**
     * DataSink Listener
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

	if (evt instanceof EndOfStreamEvent) {
	    System.err.println("All done!");
	    evt.getSourceDataSink().close();
	    System.exit(0);
	}
    }
    
	public void update(ReceiveStreamEvent evt) {
		ReceiveStream stream = evt.getReceiveStream();
		BrokerFactory.getLoggingBroker().logDebug("Got RSE: "+evt);

		try {
			playerDataSource = stream.getDataSource();
			if (player == null) {
				player = Manager.createProcessor(playerDataSource);
				player.configure();
				while (player.getState() == Processor.Configuring) {
					Thread.sleep(100);
				}
				
				TrackControl trackControl = null;
				
				TrackControl[] trackControls = player.getTrackControls();
				for (int i = 0; i < trackControls.length; i++) {
					if (trackControls[i].getFormat() instanceof AudioFormat) {
						trackControls[i].setFormat(new AudioFormat(AudioFormat.LINEAR, 8000, 8, 1, 
								AudioFormat.LITTLE_ENDIAN, AudioFormat.UNSIGNED));
					}
				}
				
				player.realize();
				while (player.getState() != Player.Realized) {
					Thread.sleep(10);
				}
				
				DataSource out = player.getDataOutput();

				BrokerFactory.getLoggingBroker().logDebug("content type="+out.getContentType());
				silenceDS = new SilenceDataSink();
				for (int i = 0; i < pickupListeners.size(); i++) {
					silenceDS.addPickupListener((PickupListener)pickupListeners.elementAt(i));
				}
				for (int i = 0; i < silenceListeners.size(); i++) {
					silenceDS.addSilenceListener((SilenceListener)silenceListeners.elementAt(i));
				}
				silenceDS.setSource(out);
				silenceDS.addDataSinkListener(this);
				silenceDS.start();

				player.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void hangup() {
		BrokerFactory.getLoggingBroker().logDebug("hanging up");
		sipHandler.hangup(recipient);
		if (transmitter != null)
			transmitter.stop();
		if (silenceDS != null)
			silenceDS.stop();
		if (player != null) {
			try {
				playerDataSource.stop();
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			playerDataSource.disconnect();
			player.stop();
			player.deallocate();
			BrokerFactory.getLoggingBroker().logDebug("player deactivated");
			player = null;
		}
	}
	
	public void addPickupListener(PickupListener listener) {
		pickupListeners.addElement(listener);
	}

	public void addSilenceListener(SilenceListener listener) {
		silenceListeners.addElement(listener);
	}
}
