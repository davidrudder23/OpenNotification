/*
 * Created on Aug 5, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import gov.nist.javax.sip.message.SIPRequest;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SipHandler implements SipListener, Router {
	RtpTransmitter transmitter;

	SipStack sipStack;

	SipFactory sipFactory;

	SipProvider sipProvider;

	private String localHost = "127.0.0.1";

	private String remoteHost;

	private int localPort = 5060;

	private int remotePort = 5060;

	private static SipHandler instance;
	
	private Vector requestListeners, responseListeners, dtmfListeners;
	
	int sequenceNum;
	
	String registrarHost;
	int registrarPort;
	String registrarDomain;
	String username;
	String password;
	
	String callID;
	
	boolean registered;
	
	static boolean initialized = false; 
	
	public boolean isRegistered() {
		return registered;
	}
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}
	public SipHandler(SipStack sipStack, String defaultRoute) {
	}

	private SipHandler(String remoteHost, int port) {
		BrokerFactory.getLoggingBroker().logDebug("New Sip Handler with remoteHost="+remoteHost);
		sipStack = null;
		sipFactory = null;
		this.remoteHost = remoteHost;
		this.remotePort = port;

		requestListeners = new Vector();
		responseListeners = new Vector();
		dtmfListeners = new Vector();
		
		sequenceNum = 1;

		// get the local address
		String foundLocalHost = "127.0.0.1";
		try {
			foundLocalHost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		localHost = BrokerFactory.getConfigurationBroker().getStringValue(
				"local.hostname", foundLocalHost);
		if (localHost.equals("127.0.0.1")) {
			BrokerFactory.getLoggingBroker().logWarn("SIP could not determine local host name.  Please set local.hostname in reliable.properties");
		}
	}
	
	public void init() {
		if (!initialized) {
			BrokerFactory.getLoggingBroker().logDebug("Initializing SIP Handler");
			initialized = true;
			try {
				Properties props = new Properties();
				props.setProperty("javax.sip.IP_ADDRESS", localHost);
				props.setProperty("javax.sip.ROUTER_PATH",
						"net.reliableresponse.notification.sip.SipHandler");
				props.setProperty("javax.sip.STACK_NAME", "SipHandler");
				props.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");

				props.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
						"1048576");
				
				// Register
				if (getRegistrarHost() != null) {
					BrokerFactory.getLoggingBroker().logDebug(
							"Initializing w/ Register " + getRegistrarHost()
									+ ":" + getRegistrarPort() + "/udp");
					props.setProperty("javax.sip.OUTBOUND_PROXY",
							getRegistrarHost() + ":" + getRegistrarPort()
									+ "/udp");
					props
							.setProperty(
									"net.java.sip.communicator.sip.DEFAULT_AUTHENTICATION_REALM",
									getRegistrarDomain());
					props.setProperty(
							"net.java.sip.communicator.sip.USER_NAME",
							getUsername());
					setRegistered(false);
				} else {
					setRegistered(true);
				}

				// Drop the client connection after we are done with the
				// transaction.
				props.setProperty(
						"gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
				// Set to 0 in your production code for max speed.
				// You need 16 for logging traces. 32 for debug + traces.
				// Your code will limp at 32 but it is best for debugging.
				props.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName("gov.nist");

				sipStack = sipFactory.createSipStack(props);

				ListeningPoint udpListeningPoint = sipStack
						.createListeningPoint(localPort, "udp");
				sipProvider = sipStack.createSipProvider(udpListeningPoint);
				sipProvider.addSipListener(this);

				// Setup callID
				callID = System.currentTimeMillis() + "@" + localHost;

				if (getRegistrarHost() != null) {
					register();
				}
				while (!isRegistered()) {
					BrokerFactory.getLoggingBroker().logDebug(
							"Waiting for register");
					Thread.sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendRequest (String recipient, String type, String content, ContentTypeHeader contentType) throws SipException, ParseException , InvalidArgumentException {
		URI uri = sipFactory.createAddressFactory().createURI(recipient);
		Request request = sipFactory.createMessageFactory().createRequest(
				uri,
				type,
				sipFactory.createHeaderFactory().createCallIdHeader(getCallID()),
				sipFactory.createHeaderFactory().createCSeqHeader(sequenceNum++, type),
				sipFactory.createHeaderFactory().createFromHeader(
						sipFactory.createAddressFactory().createAddress(
								"sip:"+username+"@"
										+ SipHandler.getInstance()
												.getLocalHost()
										+ ":"
										+ SipHandler.getInstance()
												.getLocalPort()),
						Integer.toString(hashCode())),
				sipFactory.createHeaderFactory().createToHeader(
						sipFactory.createAddressFactory().createAddress(
								recipient), null),
				new ArrayList(),
				sipFactory.createHeaderFactory().createMaxForwardsHeader(10));

		ContactHeader contactHeader = sipFactory.createHeaderFactory()
				.createContactHeader(
						sipFactory.createAddressFactory().createAddress(
								"sip:"+username+"@"
										+ SipHandler.getInstance()
												.getLocalHost()
										+ ":"
										+ SipHandler.getInstance()
												.getLocalPort()));
		request.addHeader(contactHeader);

		if (content != null) {
			request.setContent(content, contentType);
		}

		BrokerFactory.getLoggingBroker().logDebug("Sending SIP Request "+request);
		ClientTransaction ct = getSipProvider().getNewClientTransaction(request);
		ct.sendRequest();

	}

	public static SipHandler getInstance() {
		return instance;
	}

	public static SipHandler getInstance(String remoteHost, int port) {
		if (instance == null) {
			instance = new SipHandler(remoteHost, port);
		}

		return instance;
	}

	public SipProvider getSipProvider() {
		return sipProvider;
	}

	public int getLocalPort() {
		return localPort;
	}

	public String getLocalHost() {
		return localHost;
	}
	
	

	public String getRegistrarHost() {
		return registrarHost;
	}
	public void setRegistrarHost(String registrarHost) {
		this.registrarHost = registrarHost;
	}
	public int getRegistrarPort() {
		return registrarPort;
	}
	public void setRegistrarPort(int registrarPort) {
		this.registrarPort = registrarPort;
	}
	
	public String getRegistrarDomain() {
		return registrarDomain;
	}
	public void setRegistrarDomain(String registrarDomain) {
		this.registrarDomain = registrarDomain;
	}
	public String getUsername() {
		if (username == null) return "reliableresponse";
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	public String getCallID() {
		return callID;
	}
	public void setCallID(String callID) {
		this.callID = callID;
	}
	
	
	public ListIterator getNextHops(Request request) {
		LinkedList ll = new LinkedList();
		String remoteHost = getInstance().getRemoteHost();
		int port = getInstance().getRemotePort();
		ll.add(new HopImpl(remoteHost + ":" + port + "/udp"));
		return ll.listIterator();
	}

	public Hop getOutboundProxy() {
		return null;
	}
	
	public void hangup(String recipient) {
		try {
			sendRequest(recipient, Request.BYE, null, null);
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

	public void processRequest(RequestEvent requestEvent) {

		long begin = System.currentTimeMillis();
		Request request = requestEvent.getRequest();
		BrokerFactory.getLoggingBroker().logDebug("Process Request in Sip Handler "+request);
		for (int i = 0; i < requestListeners.size(); i++) {
			RequestListener listener = (RequestListener) requestListeners
					.elementAt(i);
			listener.handleRequest(requestEvent);
		}

		String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME))
				.getMethod();
		Header contentTypeHeader = request.getHeader("Content-Type");
		String contentType = "";
		if (contentTypeHeader != null)
			contentType = contentTypeHeader.toString();
		if ((contentType != null) && (contentType.indexOf(":") >= 0)) {
			contentType = contentType.substring(contentType.indexOf(":") + 2,
					contentType.length());
		}

		byte[] rawContent = request.getRawContent();
		String content = "";
		if (rawContent != null) {
			content = new String(rawContent);
		}

		if (method.equals(Request.INFO)) {
			if (contentType.toLowerCase().indexOf("application/dtmf-relay") >= 0) {
				int signalIndex = content.indexOf("Signal=");
				if (signalIndex >= 0) {
					signalIndex += 7;
					String dtmfString = content.substring(signalIndex,
							signalIndex + 1);

					BrokerFactory.getLoggingBroker().logDebug("Sending OK");
					Response okayResponse = ((SIPRequest) request)
							.createResponse(Response.OK);
					try {
						requestEvent.getServerTransaction().sendResponse(
								okayResponse);
					} catch (SipException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < dtmfListeners.size(); i++) {
						((DTMFListener) dtmfListeners.elementAt(i))
								.handleDTMF(dtmfString);
					}
				}
			}
		}
		if (method.equals(Request.REGISTER)) {
			sendOKReply(requestEvent);
		}
		BrokerFactory.getLoggingBroker().logDebug("processRequest took "+
				(System.currentTimeMillis()-begin)+" millis");
	}
	private void sendOKReply(RequestEvent requestEvent) {
		try {
			Request request = requestEvent.getRequest();
			ServerTransaction st = requestEvent.getServerTransaction();

			// Send the 180 Trying
			Response response = sipFactory.createMessageFactory()
					.createResponse(200, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			response.addHeader(toHeader);

			Address address = sipFactory.createAddressFactory().createAddress(
					"Reliable Response <sip:" + getLocalHost() + ":"
							+ getLocalPort() + ">");

			ContactHeader contactHeader = sipFactory.createHeaderFactory()
					.createContactHeader(address);
			response.addHeader(contactHeader);

			if (st == null) {
				st = getSipProvider().getNewServerTransaction(
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
			int seqNum = ((CSeqHeader) request.getHeader(CSeqHeader.NAME)).getSequenceNumber();
			String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME)).getMethod();
			response.addHeader (sipFactory.createHeaderFactory().createCSeqHeader(seqNum,method));

			st.sendResponse(response);
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public void register() throws PeerUnavailableException, ParseException{
		
		// Register with Registrar server
		if (registrarHost != null) {
			register(Request.REGISTER, "sip:"+registrarHost+":"+registrarPort+";transport=udp", 
					"sip:"+username+"@"+localHost+":"+localPort+";transport=udp", null, 
					sipFactory.createHeaderFactory().createCallIdHeader(callID));
		}

	}

	public void register(String type, String registrarURI, String to, AuthorizationHeader authnHeader, CallIdHeader callID) {
		try {
			Request registerRequest = sipFactory
					.createMessageFactory()
					.createRequest(
							sipFactory.createAddressFactory().createURI(registrarURI),
							type,callID,
							sipFactory.createHeaderFactory().createCSeqHeader(
									sequenceNum++, type),
							sipFactory
									.createHeaderFactory()
									.createFromHeader(
											sipFactory
													.createAddressFactory()
													.createAddress("sip:"+getUsername()+"@"+localHost+":"+localPort+";transport=udp"),
											Integer.toString(hashCode())),
							sipFactory
									.createHeaderFactory()
									.createToHeader(
											sipFactory
													.createAddressFactory()
													.createAddress(to),
											null),
							new ArrayList(),
							sipFactory.createHeaderFactory()
									.createMaxForwardsHeader(10));

			ContactHeader contactHeader = sipFactory.createHeaderFactory()
					.createContactHeader(
							sipFactory.createAddressFactory().createAddress(
									"sip:"+getUsername()+"@"
											+ localHost
											+ ":"
											+ localPort));
			registerRequest.addHeader(contactHeader);

			if (authnHeader != null) registerRequest.addHeader(authnHeader);
			ClientTransaction ct = getSipProvider().getNewClientTransaction(
					registerRequest);
			ct.sendRequest();
			
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public void processResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		BrokerFactory.getLoggingBroker().logDebug("Process Response in Sip Handler "+response);

		String method = ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod();
		
		if (response.getStatusCode() == Response.OK)  {
			if (method.equals(Request.REGISTER)) {
				setRegistered(true);
			}
		}
		
		if (response.getStatusCode() == 401) {
			handleAuthorization(responseEvent, response, method);
		} else if (response.getStatusCode() == 407) {
			handleProxyAuthorization(responseEvent, response, method);
		}  
		for (int i = 0; i < responseListeners.size(); i++) {
			ResponseListener listener = (ResponseListener) responseListeners
					.elementAt(i);
			listener.handleResponse(responseEvent);
		}
	}
	

	/**
	 * @param responseEvent
	 * @param response
	 * @param method
	 */
	private void handleAuthorization(ResponseEvent responseEvent, Response response, String method) {
		// Handle Unauthorized 
		Header authenticateHeader = response.getHeader("WWW-Authenticate");
		if (authenticateHeader instanceof WWWAuthenticateHeader) {
			try {
				Request registerRequest = responseEvent.getClientTransaction().getRequest();
				WWWAuthenticateHeader wwwAuthnHeader = (WWWAuthenticateHeader)authenticateHeader;

				BrokerFactory.getLoggingBroker().logDebug("method="+method);
				BrokerFactory.getLoggingBroker().logDebug("algo="+wwwAuthnHeader.getAlgorithm());
				BrokerFactory.getLoggingBroker().logDebug("content="+(registerRequest.getContent()==null?"":registerRequest.getContent().toString()));
				BrokerFactory.getLoggingBroker().logDebug("qop="+wwwAuthnHeader.getQop());

				String digest = MessageDigestAlgorithm.calculateResponse
					(wwwAuthnHeader.getAlgorithm(), 
					getUsername(), wwwAuthnHeader.getRealm(), password, 
					wwwAuthnHeader.getNonce(),
					null, null, method, 
					registerRequest.getRequestURI().toString(),
					registerRequest.getContent()==null?"":registerRequest.getContent().toString(),
					wwwAuthnHeader.getQop());
				
				AuthorizationHeader authorization = sipFactory.createHeaderFactory().createAuthorizationHeader(wwwAuthnHeader.getScheme());
				
				authorization.setUsername(getUsername());
		        authorization.setRealm(wwwAuthnHeader.getRealm());
		        authorization.setNonce(wwwAuthnHeader.getNonce());
		        authorization.setParameter("uri",registerRequest.getRequestURI().toString());
		        authorization.setResponse(digest);
		        if(wwwAuthnHeader.getAlgorithm() != null)
		        	authorization.setAlgorithm(wwwAuthnHeader.getAlgorithm());
		        if(wwwAuthnHeader.getOpaque() != null)
		        	authorization.setOpaque(wwwAuthnHeader.getOpaque());
		         
				register (registerRequest.getMethod(), "sip:"+username+"@"+registrarHost+":"+registrarPort+";transport=udp", 
						"sip:"+getUsername()+"@"+localHost+":"+localPort+";transport=udp", authorization, 
						(CallIdHeader)registerRequest.getHeader(CallIdHeader.NAME));
			} catch (PeerUnavailableException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ParseException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	/**
	 * @param responseEvent
	 * @param response
	 * @param method
	 */
	private void handleProxyAuthorization(ResponseEvent responseEvent, Response response, String method) {
		// Handle Unauthorized 
		Header authenticateHeader = response.getHeader("Proxy-Authenticate");
		if (authenticateHeader instanceof ProxyAuthenticateHeader) {
			try {
				Request registerRequest = responseEvent.getClientTransaction().getRequest();
				ProxyAuthenticateHeader wwwAuthnHeader = (ProxyAuthenticateHeader)authenticateHeader;

//				BrokerFactory.getLoggingBroker().logDebug("method="+method);
//				BrokerFactory.getLoggingBroker().logDebug("algo="+wwwAuthnHeader.getAlgorithm());
//				BrokerFactory.getLoggingBroker().logDebug("content="+(registerRequest.getContent()==null?"":registerRequest.getContent().toString()));
//				BrokerFactory.getLoggingBroker().logDebug("qop="+wwwAuthnHeader.getQop());

				String digest = MessageDigestAlgorithm.calculateResponse
					(wwwAuthnHeader.getAlgorithm(), 
					getUsername(), wwwAuthnHeader.getRealm(), password, 
					wwwAuthnHeader.getNonce(),
					null, null, method, 
					registerRequest.getRequestURI().toString(),
					registerRequest.getContent()==null?"":registerRequest.getContent().toString(),
					wwwAuthnHeader.getQop());
				
				AuthorizationHeader authorization = sipFactory.createHeaderFactory().createProxyAuthorizationHeader(wwwAuthnHeader.getScheme());
				
				authorization.setUsername(getUsername());
		        authorization.setRealm(wwwAuthnHeader.getRealm());
		        authorization.setNonce(wwwAuthnHeader.getNonce());
		        authorization.setParameter("uri",registerRequest.getRequestURI().toString());
		        authorization.setResponse(digest);
		        if(wwwAuthnHeader.getAlgorithm() != null)
		        	authorization.setAlgorithm(wwwAuthnHeader.getAlgorithm());
		        if(wwwAuthnHeader.getOpaque() != null)
		        	authorization.setOpaque(wwwAuthnHeader.getOpaque());
		         
				register (registerRequest.getMethod(), ((ToHeader)registerRequest.getHeader("to")).getAddress().toString(), 
						((ToHeader)registerRequest.getHeader("to")).getAddress().toString(), authorization, 
						(CallIdHeader)registerRequest.getHeader(CallIdHeader.NAME));
			} catch (PeerUnavailableException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ParseException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// TODO Auto-generated method stub
		System.out.println("timeoutEvent = " + timeoutEvent);

	}

	public void addRequestListener(RequestListener listener) {
		requestListeners.addElement(listener);
	}

	public void addResponseListener(ResponseListener listener) {
		responseListeners.addElement(listener);
	}

	public void addDTMFListener(DTMFListener listener) {
		dtmfListeners.addElement(listener);
	}
	
	public void removeDTMFListener(DTMFListener listener) {
		dtmfListeners.removeElement(listener);
	}

	public String getRemoteHost() {
		return remoteHost;
	}
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}
	
	
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	public static void main(String args[]) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		SipHandler handler = SipHandler.getInstance();

	}
}