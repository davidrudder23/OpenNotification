/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Vector;

import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.SessionAddress;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.tts.FreeTTS;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SipOutboundCall extends SipCall implements RequestListener, ResponseListener, DTMFListener, SilenceListener {

	int remotePort;

	String remoteHost;

	int localMediaPort;

	SipStack sipStack;

	SipFactory sipFactory;

	
	FreeTTS tts;
	
	boolean initialized;
	
	SilenceDelayThread silenceThread;

	public SipOutboundCall(String host, int port) {
		remoteHost = host;
		remotePort = port;
		
		tts = new FreeTTS(); 

		sipHandler = SipHandler.getInstance(host, port);

		sipHandler.setRemoteHost(host);
		sipHandler.setRemotePort(port);
		localMediaPort = -1;
		try {
			DatagramSocket socket = new DatagramSocket();
			localMediaPort = socket.getLocalPort();
			socket.disconnect();
			socket.close();
			initRTP(localMediaPort);
		} catch (Exception e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		
		//sipHandler.addRequestListener(this);
		//sipHandler.addResponseListener(this);
		sipHandler.addDTMFListener(this);
		addSilenceListener(this);
		
		sipFactory = SipFactory.getInstance();
		initialized = false;
	}

	/**
	 * @throws ParseException
	 * @throws PeerUnavailableException
	 * @throws InvalidArgumentException
	 * @throws TransactionUnavailableException
	 * @throws SipException
	 */
	void makeCall(String recipient) throws ParseException,
			PeerUnavailableException, InvalidArgumentException,
			TransactionUnavailableException, SipException {
		if (!initialized) {
	
			sipHandler.init();
			initialized = true;
		}
		sipHandler.setCallID(System.currentTimeMillis()+ "@" + sipHandler.getLocalHost());
		this.recipient = recipient;
		sendInitialInvite(recipient);
	}

	/**
	 * @param localMediaPort
	 * @throws ParseException
	 * @throws PeerUnavailableException
	 * @throws InvalidArgumentException
	 * @throws TransactionUnavailableException
	 * @throws SipException
	 */
	private void sendInitialInvite(String recipient) throws ParseException,
			PeerUnavailableException, InvalidArgumentException,
			TransactionUnavailableException, SipException {
		BrokerFactory.getLoggingBroker().logDebug("Calling " + recipient+" at "+remoteHost);
		
		ContentTypeHeader contentTypeHeader = null;
		//content type should be application/sdp (not applications)
		//reported by Oleg Shevchenko (Miratech)
		contentTypeHeader = sipFactory.createHeaderFactory()
				.createContentTypeHeader("application", "sdp");

		String sdpContent = "v=0\n" + "o="+sipHandler.getUsername()+" 0 0 IN IP4 "
				+ SipHandler.getInstance().getLocalHost() + "\n" + "s=-\n"
				+ "c=IN IP4 " + SipHandler.getInstance().getLocalHost() + "\n"
				+ "t=0 0\n" + "m=audio " + localMediaPort
				+ " RTP/AVP 4 3 0 5 6 8 15 18\n" + "a=sendrecv\n"
				+ "a=rtpmap:101 telephone-event/8000 \n"
				+ "a=fmtp:101 64\n"
				+ "a=rtpmap:0 PCMU/8000\n";
		BrokerFactory.getLoggingBroker().logDebug("sdpContent ="+sdpContent);

		sipHandler.sendRequest(recipient, Request.INVITE, sdpContent, contentTypeHeader);

	}

	public void handleRequest(RequestEvent requestEvent) {
		Request request =requestEvent.getRequest();
		BrokerFactory.getLoggingBroker().logDebug("Got request in SipOutboundCall "+request);
		String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME)).getMethod();
		
		if (method.equals (Request.BYE)) {
			hangup();
		}
	}

	public void handleResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		BrokerFactory.getLoggingBroker().logDebug("Got response in SipOutboundCall "+response);
		String method = ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
				.getMethod();

		ClientTransaction clientTransaction = responseEvent
				.getClientTransaction();

		try {
			if (response.getStatusCode() == Response.OK) {
				if (method.equals(Request.INVITE)) {
					if (clientTransaction != null) {
						Request ack = (Request) clientTransaction.getDialog()
								.createRequest(Request.ACK);
						clientTransaction.getDialog().sendAck(ack);

						startConversation(response, clientTransaction);
					}
				} else if (method.equals(Request.ACK)) {
					System.out.println("Got ack");
				}
			} else if ((response.getStatusCode() == Response.BUSY_HERE) ||
					(response.getStatusCode() == Response.BUSY_EVERYWHERE)) {
				hangup();
			}
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/**
	 * @param response
	 * @param clientTransaction
	 * @throws SipException
	 * @throws SdpParseException
	 * @throws SdpException
	 * @throws IOException
	 * @throws NoDataSourceException
	 * @throws NoProcessorException
	 * @throws InterruptedException
	 * @throws NotConfiguredError
	 * @throws NotRealizedError
	 * @throws NoDataSinkException
	 * @throws InvalidSessionAddressException
	 * @throws UnknownHostException
	 */
	void startConversation(Message response,
			Transaction clientTransaction) throws SipException,
			SdpParseException, SdpException, IOException,
			NoDataSourceException, NoProcessorException, InterruptedException,
			NotConfiguredError, NotRealizedError, NoDataSinkException,
			InvalidSessionAddressException, UnknownHostException {
		System.out.println("Starting conversation");

		String sdpData = new String(response.getRawContent());
		SdpFactory sdpFactory = new SdpFactory();
		SessionDescription sessionDescription = sdpFactory
				.createSessionDescription(sdpData);
		Vector mediaDescriptions = sessionDescription
				.getMediaDescriptions(true);
		BrokerFactory.getLoggingBroker().logDebug("We have "+mediaDescriptions.size()+" media descriptions");
		for (int mdNum = 0; mdNum < mediaDescriptions.size(); mdNum++) {
			MediaDescription mediaDescription = (MediaDescription) mediaDescriptions
					.elementAt(mdNum);
			Media media = mediaDescription.getMedia();
			String proto = media.getProtocol();
			String type = media.getMediaType();
			int port = media.getMediaPort();

			Vector formats = media.getMediaFormats(true);

			if (formats.size() < 1) {
				BrokerFactory.getLoggingBroker().logWarn(
						"In SIP outbound call: No audio formats");
			}
			int sdpFormat = SdpConstants.PCMU;
			try {
				sdpFormat = Integer.parseInt((String) formats.elementAt(0));
			} catch (NumberFormatException nfExc) {
				nfExc.printStackTrace();
			}

                        transmitter = new RtpTransmitter(remoteHost, port, sdpFormat);

			startReceiver(remoteHost, port);
			BrokerFactory.getLoggingBroker().logDebug("Starting transmitter");
			transmitter = new RtpTransmitter(remoteHost, port, sdpFormat);
			BrokerFactory.getLoggingBroker().logDebug("transmitter="+transmitter);
		}
	}

	private void startReceiver(String remoteHost, int localPort)
			throws InvalidSessionAddressException, IOException,
			UnknownHostException {
		BrokerFactory.getLoggingBroker().logDebug(
				"Starting conversation on " + localMediaPort+" to "+remoteHost+":"+localPort);
		SessionAddress remoteAddress = new SessionAddress(InetAddress
				.getByName(remoteHost), localPort);
		rtpManager.addTarget(remoteAddress);
	}
	
	public void handleDTMF(String digit) {	
		transmitter.playFromInputStream(new ByteArrayInputStream(tts.getWav("You pressed "+digit)), "audio.x_wav");
	}
	
	public void handleSilenceStart() {
		// TODO Auto-generated method stub
		silenceThread = new SilenceDelayThread(this);
		silenceThread.start();
	}

	public void handleSilenceEnd() {
		// TODO Auto-generated method stub
		if (silenceThread != null) {
			silenceThread.stopDelay();
		}
	}

	public static void main(String[] args) throws Exception {
		
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		SipOutboundCall out;
		if (args.length >= 1) {
			out = new SipOutboundCall("sys1a.TelecomMatters.net", 5060);
			out.setRegistrarHost("sys1a.TelecomMatters.net");
			out.setRegistrarPort(5060);
			out.setRegistrarDomain("asterisk");
			out.setUsername("daverudder");
			out.setPassword("3035421990");
			BrokerFactory.getLoggingBroker().logDebug("Making call");
			out.makeCall("sip:93035689253@sys1a.TelecomMatters.net:5060");
		} else {
			out = new SipOutboundCall("10.10.10.5", 5060);
			out.setUsername("daverudder");
			//out.makeCall("sip:reliableresponse@10.10.10.5:5060");
//			Thread.sleep(4000);
//			char[] phoneNumber = "97025308877".toCharArray();
//			for (int p = 0; p < phoneNumber.length; p++) {
//				out.sendDTMF(Integer.parseInt(phoneNumber[p]+""));
//			}
			out.makeCall("sip:1990@10.10.10.5:5060");
			//out.makeCall("tel:+97205308877");
		}

//		new BufferedReader(new InputStreamReader(System.in)).readLine();
//		out.hangup();
	}
}

class SilenceDelayThread extends Thread {
	SipOutboundCall call;
	boolean stopped;
	public SilenceDelayThread (SipOutboundCall call) {
		this.call = call;
		stopped = false;
	}
	public void run() {
		int seconds = 0;
		
		BrokerFactory.getLoggingBroker().logDebug("Starting silence checker");
		while ((seconds < 15) && (!stopped)) {
			try {
				Thread.sleep(1000);
				seconds++;
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		if (!stopped) {
			call.hangup();
		}
	}
	
	public void stopDelay() {
		BrokerFactory.getLoggingBroker().logDebug("Stopping silence checker");
		stopped = true;
	}
}
