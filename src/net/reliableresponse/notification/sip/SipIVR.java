/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.rtp.InvalidSessionAddressException;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sip.SipException;
import javax.sip.Transaction;
import javax.sip.message.Message;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.dialogic.DialogicMessage;
import net.reliableresponse.notification.dialogic.WelcomeDialogicMessage;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SipIVR extends SipInboundCall implements DTMFListener, ControllerListener {
	DialogicMessage diaMessage;
	DTMFThread dtmfThread;
	
	String digits;
	boolean inCall;
	boolean resetDelay = false;
	
	public SipIVR() {
		super();
		diaMessage = new WelcomeDialogicMessage();
		sipHandler.addDTMFListener(this);
		
		digits = new String();
		
		
	}
	
	void startConversation(Message response,
			Transaction clientTransaction) throws SipException,
			SdpParseException, SdpException, IOException,
			NoDataSourceException, NoProcessorException, InterruptedException,
			NotConfiguredError, NotRealizedError, NoDataSinkException,
			InvalidSessionAddressException, UnknownHostException {
		super.startConversation(response, clientTransaction);
		
		dtmfThread = new DTMFThread(this, transmitter);
		
		transmitter.addControllerListener(this);
		transmitter.playSound ("file://"+diaMessage.getWaveFilename());
	}
	
	public void handleDTMF(String digit) {
		BrokerFactory.getLoggingBroker().logDebug("Got DTMF digit "+digit+ " - " + System.currentTimeMillis());
		digits = digits + digit;

		dtmfThread = new DTMFThread(this, transmitter);
		BrokerFactory.getLoggingBroker().logDebug("Running dtmfthread");
		dtmfThread.setDigits(digits);
		dtmfThread.setDigit(digit);
		dtmfThread.setDiaMessage(diaMessage);
		dtmfThread.start();
		BrokerFactory.getLoggingBroker().logDebug("Finished DTMF digit "+digit+ " - " + System.currentTimeMillis());
	}
	

	public void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof EndOfMediaEvent) {
			if (diaMessage.getExpectedDigits() == 0) {
				handleDTMF("0");
			} else {
				resetDelay = true;
				while (resetDelay) {
					try {
						resetDelay = false;
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
				transmitter.stop();
				transmitter.playSound("file://"+diaMessage.getWaveFilename());
			}
		}
	}
	
	void resetDelay () {
		resetDelay = true;
	}
	
	public void clearDigits() {
		digits = "";
	}
	
	public void setDiaMessage(DialogicMessage message) {
		this.diaMessage = message;
	}
 
	public void hangup() {
		diaMessage = new WelcomeDialogicMessage();
		sipHandler.removeDTMFListener(this);
		super.hangup();
	}

	public static void main(String[] args) throws Exception {
		
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		SipIVR ivr = new SipIVR();
		ivr.init ("10.10.10.5", 5060);

	}

}

class DTMFThread extends Thread {
	String digits;
	String digit;
	DialogicMessage diaMessage;
	RtpTransmitter transmitter;
	SipIVR ivr;
	
	boolean started;
	
	public DTMFThread(SipIVR ivr, RtpTransmitter transmitter) {
		this.transmitter = transmitter;
		started = false;
		this.ivr = ivr;
	}
	
	
	public DialogicMessage getDiaMessage() {
		return diaMessage;
	}
	public void setDiaMessage(DialogicMessage diaMessage) {
		this.diaMessage = diaMessage;
	}
	public String getDigit() {
		return digit;
	}
	public void setDigit(String digit) {
		this.digit = digit;
	}
	public String getDigits() {
		return digits;
	}
	public void setDigits(String digits) {
		this.digits = digits;
	}
	
	public boolean isStarted() {
		BrokerFactory.getLoggingBroker().logDebug("is started="+started);
		return started;
	}
	public void run() {
		started = true;
		try {
			BrokerFactory.getLoggingBroker().logDebug("digits=" + digits);
			if (digit.equals("*")) {
				ivr.clearDigits();
				diaMessage = new WelcomeDialogicMessage();
				ivr.setDiaMessage(diaMessage);
				transmitter.stop();
				transmitter.playSound("file://" + diaMessage.getWaveFilename());
				started = false;
				return;
			}
			if (digits.length() >= diaMessage.getExpectedDigits()) {
				diaMessage = diaMessage.getNextMessage(digits);
				ivr.setDiaMessage(diaMessage);
				ivr.clearDigits();
				transmitter.stop();
				transmitter.playSound("file://" + diaMessage.getWaveFilename());
			} else {
				ivr.resetDelay();
			}
		} finally {
			started = false;
		}
	}
}
