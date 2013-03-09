/*
 * Created on Aug 26, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Vector;

import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.rtp.InvalidSessionAddressException;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.DocumentBuilderFactory;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.dialogic.DialogicAudioMessage;
import net.reliableresponse.notification.dialogic.DialogicOutgoingMessage;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.tts.FreeTTS;

import org.w3c.dom.Document;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SipOutgoing extends SipOutboundCall implements PickupListener {

	private static SipOutgoing instance;

	Vector messages;

	String host;

	int port;

	private SipOutgoing(String host, int port) {
		super(host, port);
		BrokerFactory.getLoggingBroker().logDebug(
				"Initializing Sip Outgoing at " + host + ":" + port);
		this.host = host;
		this.port = port;
		messages = new Vector();

		sipHandler.addDTMFListener(this);
		sipHandler.addRequestListener(this);
		sipHandler.addResponseListener(this);
		sipHandler.setUsername("reliableresponse");
		addPickupListener(this);
	}

	public static SipOutgoing getInstance() {
		if (instance == null) {
			String host = BrokerFactory.getConfigurationBroker()
					.getStringValue("sip.gateway.host");
			int port = BrokerFactory.getConfigurationBroker().getIntValue(
					"sip.gateway.port", 5061);
			instance = new SipOutgoing(host, port);
		}
		return instance;
	}

	public void addMessage(DialogicAudioMessage message) {
		messages.addElement(message);
		BrokerFactory.getLoggingBroker().logDebug("SIP Message queue has "+messages.size()+" messages");
		if (messages.size() == 1) {
			playNextMessage();
		}
	}

	private void playNextMessage() {
		if ((messages == null) || (messages.size() <= 0)) {
			BrokerFactory.getLoggingBroker().logWarn(
					"playNextMessage called without a message to play");
		}

		DialogicAudioMessage message = (DialogicAudioMessage) messages
				.elementAt(0);
		try {
			super.makeCall("sip:" + message.getPhoneNumber() + "@" + host + ":"
					+ port);
			message.setCallID(sipHandler.getCallID());
		} catch (PeerUnavailableException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (TransactionUnavailableException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ParseException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (InvalidArgumentException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SipException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	void startConversation(Message response, Transaction clientTransaction)
			throws SipException, SdpParseException, SdpException, IOException,
			NoDataSourceException, NoProcessorException, InterruptedException,
			NotConfiguredError, NotRealizedError, NoDataSinkException,
			InvalidSessionAddressException, UnknownHostException {
		super.startConversation(response, clientTransaction);
		DialogicAudioMessage message = (DialogicAudioMessage) messages.elementAt(0);
		transmitter.playSound("file:///" + message.getWaveFile());

	}

	public void handlePickup() {
		DialogicAudioMessage message =
			(DialogicAudioMessage)messages.elementAt(0);
		BrokerFactory.getLoggingBroker().logDebug ("transmitter="+transmitter);
		BrokerFactory.getLoggingBroker().logDebug ("message = "+message);
		transmitter.playSound("file:///"+message.getWaveFile());
	}

	public void handleDTMF(String digit) {
		DialogicOutgoingMessage message = (DialogicOutgoingMessage) messages
				.elementAt(0);
		Notification notification = message.getNotification();
		String[] responses = notification.getSender().getAvailableResponses(
				notification);
		int responseNum = 0;
		try {
			responseNum = Integer.parseInt(digit);
			BrokerFactory.getLoggingBroker().logDebug(
					"num responses = " + responses.length);
			BrokerFactory.getLoggingBroker().logDebug(
					"response num = " + responseNum);
			notification.getSender().handleResponse(notification, null,
					responses[responseNum - 1], null);

			// TODO: Make this work with the responses
			String responseMessage = "<jsml>Your response has been received.  Thank you</jsml>";
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setValidating(false);
			Document doc = factory.newDocumentBuilder().parse(
					new StringBufferInputStream(responseMessage));
			byte[] wav = new FreeTTS().getWav(doc);
			transmitter.playSound(new InputStreamDataSource(
					new ByteArrayInputStream(wav), "audio.x_wav"));
		} catch (NumberFormatException nfExc) {
			BrokerFactory.getLoggingBroker().logInfo(
					"Got a bad digit in the outgoing response: " + digit);
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
		}
	}

	public void handleRequest(RequestEvent requestEvent) {
		super.handleRequest(requestEvent);
		Request request = requestEvent.getRequest();
		BrokerFactory.getLoggingBroker().logDebug("Got request in SipOutgoing "+request);
		String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME))
				.getMethod();
		if (method.equals(Request.BYE)) {
			if (messages.size() > 0) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Got BYE, removing message");
				DialogicAudioMessage message = (DialogicAudioMessage) messages
						.elementAt(0);
				if (message != null) {
					CallIdHeader callIDHeader = (CallIdHeader) request
							.getHeader("Call-ID");
					BrokerFactory.getLoggingBroker().logDebug(
							"Message's call id = " + message.getCallID());
					BrokerFactory.getLoggingBroker().logDebug(
							"Response's call id = " + callIDHeader.getCallId());
					if (message.getCallID().equals(callIDHeader.getCallId())) {
						messages.remove(0);
					}
				}
			}
			if (messages.size() > 0) {
				playNextMessage();
			}
		}
	}

	public void handleResponse(ResponseEvent responseEvent) {
		super.handleResponse(responseEvent);
		Response response = responseEvent.getResponse();
		BrokerFactory.getLoggingBroker().logDebug("Got response in SipOutgoing "+response);
		String method = ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
				.getMethod();

		if (method.equals(Request.BYE)) {
			if (messages.size() > 0) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Got BYE, removing message");
				DialogicAudioMessage message = (DialogicAudioMessage) messages
						.elementAt(0);
				if (message != null) {
					CallIdHeader callIDHeader = (CallIdHeader) response
							.getHeader("Call-ID");
					BrokerFactory.getLoggingBroker().logDebug(
							"Message's call id = " + message.getCallID());
					BrokerFactory.getLoggingBroker().logDebug(
							"Response's call id = " + callIDHeader.getCallId());
					if (message.getCallID().equals(callIDHeader.getCallId())) {
						messages.remove(0);
					}
				}
			}
			if (messages.size() > 0) {
				playNextMessage();
			}
		} else if (response.getStatusCode() >= 400) {
			BrokerFactory.getLoggingBroker().logDebug("Got error - hanging up");
			hangup();
			if (messages.size() > 0) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Got BYE, removing message");
				DialogicAudioMessage message = (DialogicAudioMessage) messages
						.elementAt(0);
				if (message != null) {
					CallIdHeader callIDHeader = (CallIdHeader) response
							.getHeader("Call-ID");
					BrokerFactory.getLoggingBroker().logDebug(
							"Message's call id = " + message.getCallID());
					BrokerFactory.getLoggingBroker().logDebug(
							"Response's call id = " + callIDHeader.getCallId());
					NotificationProvider provider = message.getNotificationProvider();
					provider.setStatusOfSend(message.getNotification(), "Call failed with code "+response.getStatusCode());
					
					if (message.getCallID().equals(callIDHeader.getCallId())) {
						messages.remove(0);
					}
				}
			}
			if (messages.size() > 0) {
				playNextMessage();
			}
		}
	}
}
