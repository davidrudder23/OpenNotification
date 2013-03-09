/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SipInboundCall extends SipCall implements RequestListener, ResponseListener {
	int localMediaPort;

	String remoteHost = null;
	SipFactory sipFactory;
	
	public SipInboundCall() {
		localMediaPort = -1;

		sipHandler = SipHandler.getInstance("127.0.0.1", 5060);
		sipFactory = SipFactory.getInstance();

		try {
			DatagramSocket socket = new DatagramSocket();
			localMediaPort = socket.getLocalPort();
			socket.close();
			initRTP(localMediaPort);
		} catch (Exception e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}


		sipHandler.addRequestListener(this);
		sipHandler.addResponseListener(this);

		BrokerFactory.getLoggingBroker().logDebug("Initialized SipInbound");
		
	}
	
	public void init(String hostName, int port) {
		sipHandler.setRemoteHost(hostName);
		sipHandler.setRemotePort(port);
		sipHandler.init();
	}

	public void handleRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME))
				.getMethod();
		if (method.equals(Request.INVITE)) {
			FromHeader fromHeader =  (FromHeader)request.getHeader(FromHeader.NAME);
			recipient = fromHeader.getAddress().toString();
			remoteHost = ((SipURI)fromHeader.getAddress().getURI()).getHost();
			sipHandler.setRemoteHost(remoteHost);
			sendInviteReply(requestEvent);
		} else if (method.equals (Request.BYE)) {
			hangup();
		}
	}

	public void handleResponse(ResponseEvent responseEvent) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * @param request
	 */
	private void sendInviteReply(RequestEvent requestEvent) {
		int port = 8000;

		try {
			DatagramSocket socket = new DatagramSocket();
			port = socket.getLocalPort();
			socket.close();
		} catch (SocketException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"New connection on port " + port);
		try {
			Request request = requestEvent.getRequest();
			ServerTransaction st = requestEvent.getServerTransaction();

			// Send the 180 Trying
			Response response = sipFactory.createMessageFactory()
					.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			response.addHeader(toHeader);

			Address address = sipFactory.createAddressFactory().createAddress(
					"Reliable Response <sip:" + sipHandler.getLocalHost() + ":"
							+ sipHandler.getLocalPort() + ">");

			ContactHeader contactHeader = sipFactory.createHeaderFactory()
					.createContactHeader(address);
			response.addHeader(contactHeader);

			if (st == null) {
				st = sipHandler.getSipProvider().getNewServerTransaction(
						request);
			}
			BrokerFactory.getLoggingBroker().logDebug(
					"Sending response = " + response);
			BrokerFactory.getLoggingBroker().logDebug(
					"Sending status = " + response.getStatusCode());
			st.sendResponse(response);

			// Send the 200 OK Invite
			response = sipFactory.createMessageFactory().createResponse(200,
					request);
			response.addHeader(toHeader);
			response.addHeader(contactHeader);

			ContentTypeHeader contentTypeHeader = null;
			//content type should be application/sdp (not applications)
			//reported by Oleg Shevchenko (Miratech)
			contentTypeHeader = sipFactory.createHeaderFactory()
					.createContentTypeHeader("application", "sdp");

			String sdpContent = "v=0\n" + "o=drig 0 0 IN IP4 "
					+ sipHandler.getLocalHost() + "\n" + "s=SIP Call\n"
					+ "c=IN IP4 " + sipHandler.getLocalHost() + "\n"
					+ "t=0 0\n" + "m=audio " + localMediaPort
					+ " RTP/AVP 4 3 0 5 6 8 15 18\n" + "a=sendrecv\n"
					+ "a=rtpmap:0 PCMU/8000\n";

			response.setContent(sdpContent, contentTypeHeader);

			BrokerFactory.getLoggingBroker().logDebug(
					"Sending response = " + response);
			BrokerFactory.getLoggingBroker().logDebug(
					"Sending status = " + response.getStatusCode());
			st.sendResponse(response);

			startConversation(request, st);
			synchronized (this) {
				this.notifyAll();
			}
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

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

			startReceiver(localMediaPort);
			transmitter = new RtpTransmitter(remoteHost, port, sdpFormat);
		}
	}

	private void startReceiver(int port) throws InvalidSessionAddressException,
			IOException, UnknownHostException {
		BrokerFactory.getLoggingBroker().logDebug(
				"Starting conversation on " + localMediaPort);

		SessionAddress remoteAddress = new SessionAddress(InetAddress
				.getByName(sipHandler.getRemoteHost()), port);
		rtpManager.addTarget(remoteAddress);
	}


	public static void main(String[] args) throws Exception {
		
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		SipInboundCall in = new SipInboundCall();
//		in.setRegistrarHost("sys1a.TelecomMatters.net");
//		in.setRegistrarPort(5060);
//		in.setRegistrarDomain("asterisk");
//		in.setUsername("daverudder");
//		in.setPassword("3035421990");
//		in.init("sys1a.TelecomMatters.net", 5060);
		in.init ("10.10.10.5", 5060);
	}
}