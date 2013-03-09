/*
 * Created on May 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.wctp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.apache.xerces.dom.DeferredElementImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.echomine.jabber.ErrorCode;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WctpLibrary {

	String parameter;

	String contentType;

	String hostURL;
	String username, password;

	public WctpLibrary(String hostURL, String parameter, String contentType, String username, String password) {
		this.hostURL = hostURL;
		this.parameter = parameter;
		this.contentType = contentType;
		this.username = username;
		this.password = password;

	}

	public String sendMessage(String to, String from, String message,
			Vector choices) throws DOMException, IOException {

		return sendMessage(formatWCTPMessage(to, from, message, choices));
	}

	public String sendMessage(String wctpMessage) throws IOException {
		BrokerFactory.getLoggingBroker().logInfo("Sending WCTP to "+hostURL);
		BrokerFactory.getLoggingBroker().logInfo("Sending WCTP Message:\n"+wctpMessage);
		URL url = new URL(hostURL);
		String host = url.getHost();
		int port = url.getPort();
		if (port <= 0)
			port = 80;
		String path = url.getPath();

		Socket socket = new Socket(host, port);
		OutputStream out = socket.getOutputStream();

		// This is a kludge. Skytel *requires* you to use Windows-style carriage
		// returns
		StringBuffer httpData = new StringBuffer();
		httpData.append("POST ");
		httpData.append(path);
		httpData.append(" HTTP/1.0\r\n");
		httpData.append("Content-Length: "+wctpMessage.length());
		httpData.append("\r\nContent-type: text/xml\r\n");
		httpData.append("Connection: close\r\n\r\n");
		httpData.append(wctpMessage);
		httpData.append("\r\n");
		out.write(httpData.toString().getBytes());
		out.flush();

		StringBuffer response = new StringBuffer();
		String line = null;

		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		char[] b = new char[1024];
		int size = 0;

		// Read in the header
		while ((!(line = in.readLine()).equals(""))) {
		}

		// Read in the response XML
		while ((size = in.read(b)) >= 0) {
			response.append(new String(b, 0, size));
		}
		
		String responseString = response.toString();
		BrokerFactory.getLoggingBroker().logInfo("Response to WCTP Message:\n"+responseString);
		return responseString;
	}

	public String formatWCTPClientQuery(String to, String from,
			String trackingNumber) throws DOMException, IOException {

		Document doc = new DocumentImpl();
		Element root = doc.createElement("wctp-Operation");
		// Create Root Element
		root.setAttribute("wctpVersion", "wctp-dtd-v1r1");
		Element submitClientQuery = doc.createElement("wctp-ClientQuery");

		if (username != null) {
			submitClientQuery.setAttribute("senderID", username);
		} else {
			submitClientQuery.setAttribute("senderID", from);
		}
		
		if (password != null) {
			submitClientQuery.setAttribute("securityCode", password);

		}
		submitClientQuery.setAttribute("recipientID", to);
		submitClientQuery.setAttribute("trackingNumber", trackingNumber);
		root.appendChild(submitClientQuery);
		// Attach another Element - grandaugther
		doc.appendChild(root); // Add Root to Document

		OutputFormat format = new OutputFormat(doc); //Serialize DOM
		format.setOmitXMLDeclaration(true);
		format.setDoctype(null, "http://dtd.wctp.org/wctp-dtd-v1r1.dtd");
		StringWriter stringOut = new StringWriter(); //Writer will be a String
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.asDOMSerializer(); // As a DOM Serializer

		serial.serialize(doc.getDocumentElement());
		// This is a kludge. Skytel rejects any XML definition with an encoding
		// Xerces always uses the encoding. So, I had to kludge around it.
		String header = "<?xml version=\"1.0\"?>\n";

		// If we need a parameter, use it now
		if ((parameter != null) && (parameter.length()>0)){
			header = parameter + "=" + header;
		}
		return header + stringOut.toString();

	}

	public ClientResponse readClientQueryResponse(String response)
			throws WctpException {
		ClientResponse clientResponse = new ClientResponse (200, null, null);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);
		//factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					response)));
			// Check for Errors
			NodeList nodeList = document
					.getElementsByTagName("wctp-Failure");
			if (nodeList.getLength() > 0) {
				Element errorElement = (Element) nodeList.item(0);
				String codeString = errorElement.getAttribute("errorCode");
				if ((codeString.equals("504")) || (codeString.startsWith("6"))) {
					clientResponse.setStatus("unknown");
					clientResponse.setCode (Integer.parseInt(codeString));
				} else {
					String errorMessage = errorElement.getAttribute("errorText");
					BrokerFactory.getLoggingBroker().logDebug(
						"Got an error - " + codeString + ":" + errorMessage);
					clientResponse.setCode (Integer.parseInt(codeString));
					clientResponse.setStatus("FAILED due to "+errorMessage);
					clientResponse.addMessage(errorMessage);
				}
			}

			nodeList = document.getElementsByTagName("wctp-ClientMessage");
			BrokerFactory.getLoggingBroker().logDebug("Got "+nodeList.getLength()+" nodes");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element clientMessageElement = (Element) nodeList.item(i);

				NodeList clientMessageNodes = clientMessageElement
						.getChildNodes();
				for (int cmNodeNum = 0; cmNodeNum < clientMessageNodes
						.getLength(); cmNodeNum++) {

					// Check for ClientMessageReply
					if (clientMessageNodes.item(cmNodeNum).getNodeName()
							.equals("wctp-ClientMessageReply")) {
						Element payload = (Element) ((Element) clientMessageNodes
								.item(cmNodeNum)).getElementsByTagName(
								"wctp-Payload").item(0);
						DeferredElementImpl text = (DeferredElementImpl) payload
								.getElementsByTagName("wctp-Alphanumeric")
								.item(0);
						clientResponse.addMessage(stripWhiteSpace(text
								.getTextContent()));
					}

					// Check for ClientMessageReply
					if (clientMessageNodes.item(cmNodeNum).getNodeName()
							.equals("wctp-ClientStatusInfo")) {
						Element status = (Element) ((Element) clientMessageNodes
								.item(cmNodeNum)).getElementsByTagName(
								"wctp-Notification").item(0);
						BrokerFactory.getLoggingBroker().logDebug("Setting status to "+status.getAttribute("type"));
						clientResponse.setStatus(status.getAttribute("type"));
					}

				}
			}

		} catch (ParserConfigurationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SAXException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		return clientResponse;
	}

	public String formatWCTPMessage(String to, String from, String message,
			Vector choices) throws DOMException, IOException {
		Document doc = new DocumentImpl();
		Element root = doc.createElement("wctp-Operation");
		// Create Root Element
		root.setAttribute("wctpVersion", "wctp-dtd-v1r1");
		Element submitClientMessage = doc
				.createElement("wctp-SubmitClientMessage");
		// Create element
		Element submitClientHeader = doc
					.createElement("wctp-SubmitClientHeader");
		Element sender = doc.createElement("wctp-ClientOriginator");
		if (username != null) {
			sender.setAttribute("senderID", username);
		} else {
			sender.setAttribute("senderID", from);
		}
		
		if (password != null) {
			sender.setAttribute("miscInfo", password);

		}

		submitClientHeader.appendChild(sender);

		Element control = doc.createElement("wctp-ClientMessageControl");
		control.setAttribute("notifyWhenQueued", "true");
		control.setAttribute("notifyWhenDelivered", "true");
		control.setAttribute("notifyWhenRead", "true");
		submitClientHeader.appendChild(control);

		Element recipient = doc.createElement("wctp-Recipient");
		recipient.setAttribute("recipientID", to);
		submitClientHeader.appendChild(recipient);

		submitClientMessage.appendChild(submitClientHeader);

		Element payload = doc.createElement("wctp-Payload");

		if ((choices == null) || (choices.size() <= 0)) {
			// If no choices, then send a normal alphanumeric
			Element alpha = doc.createElement("wctp-Alphanumeric");
			alpha.appendChild(doc.createTextNode(message));
			payload.appendChild(alpha);
		} else {
			// if we have choices, then use a multiple choice response message
			Element mcr = doc.createElement("wctp-MCR");
			Element text = doc.createElement("wctp-MessageText");
			text.appendChild(doc.createTextNode(message));
			mcr.appendChild(text);
			for (int i = 0; i < choices.size(); i++) {
				Element choice = doc.createElement("wctp-Choice");
				choice.appendChild(doc.createTextNode((String) choices
						.elementAt(i)));
				mcr.appendChild(choice);
			}
			payload.appendChild(mcr);
		}

		submitClientMessage.appendChild(payload);
		root.appendChild(submitClientMessage);
		// Attach another Element - grandaugther
		doc.appendChild(root); // Add Root to Document

		OutputFormat format = new OutputFormat(doc); //Serialize DOM
		format.setOmitXMLDeclaration(true);
		format.setDoctype(null, "http://dtd.wctp.org/wctp-dtd-v1r1.dtd");
		StringWriter stringOut = new StringWriter(); //Writer will be a String
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.asDOMSerializer(); // As a DOM Serializer

		serial.serialize(doc.getDocumentElement());
		// This is a kludge. Skytel rejects any XML definition with an encoding
		// Xerces always uses the encoding. So, I had to kludge around it.
		String header = "<?xml version=\"1.0\"?>";

		// If we need a parameter, use it now
		if ((parameter != null) && (parameter.length()>0)){
			header = parameter + "=" + header;
		}
		return header + stringOut.toString();
	}

	public ClientResponse readClientResponse(String response)
			throws WctpException {
		BrokerFactory.getLoggingBroker().logInfo("WCTP Client Response:\n"+response);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);
		//factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					response)));

			NodeList nodeList = document
					.getElementsByTagName("wctp-ClientSuccess");
			Element element = (Element) nodeList.item(0);
			if (element == null) {
				nodeList = document.getElementsByTagName("wctp-Failure");
				if (nodeList != null) {
					if (nodeList.getLength() > 0) {
						Element errorElement = (Element) nodeList.item(0);
						String codeString = errorElement.getAttribute("errorCode");
						String errorMessage = errorElement.getAttribute("errorText");
						BrokerFactory.getLoggingBroker().logDebug(
								"Got an error - " + codeString + ":" + errorMessage);
						ClientResponse clientResponse = new ClientResponse(400, "Failed", "000000");
						clientResponse.setCode (Integer.parseInt(codeString));
						clientResponse.setStatus("FAILED due to "+errorMessage);
						clientResponse.addMessage(errorMessage);
						return clientResponse;
					} else {
						return new ClientResponse(400, "Failed", "000000");
					}
				} else {
					return new ClientResponse(400, "Failed", "000000");
				}
			}

			int code = 0;
			String codeString = element.getAttribute("successCode");
			try {
				code = Integer.parseInt(codeString);
			} catch (NumberFormatException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

			String status = element.getAttribute("successText");

			String trackingNumber = element.getAttribute("trackingNumber");

			String message = element.getChildNodes().item(0).getNodeValue();
			ClientResponse clientResponse = new ClientResponse(code, status,
					trackingNumber);
			clientResponse.addMessage(message);

			if ((int) (clientResponse.getCode() / 100) != 2) {
				throw new WctpException(code, message);
			}

			BrokerFactory.getLoggingBroker().logDebug(
					"Client Response=" + clientResponse);
			return clientResponse;
		} catch (ParserConfigurationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SAXException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		return null;
	}
	
	public boolean subscriberExists(String from, String pagerNumber) throws IOException {
		String trackingNumber = BrokerFactory.getUUIDBroker().getUUID(from+pagerNumber+System.currentTimeMillis());
		Document doc = new DocumentImpl();
		Element root = doc.createElement("wctp-Operation");
		// Create Root Element
		root.setAttribute("wctpVersion", "wctp-dtd-v1r1");
		Element lookupSubscriber = doc
				.createElement("wctp-LookupSubscriber");
		// Create element
		Element sender = doc
				.createElement("wctp-Originator");
		if (username != null) {
			sender.setAttribute("senderID", username);
		} else {
			sender.setAttribute("senderID", from);
		}
		
		if (password != null) {
			sender.setAttribute("securityCode", password);

		}
		lookupSubscriber.appendChild(sender);

		Element lookupControl = doc.createElement("wctp-LookupMessageControl");
		lookupControl.setAttribute("messageID", trackingNumber);
		lookupSubscriber.appendChild(lookupControl);

		Element recipient = doc.createElement("wctp-Recipient");
		recipient.setAttribute("recipientID", pagerNumber);
		lookupSubscriber.appendChild(recipient);

		root.appendChild(lookupSubscriber);
		// Attach another Element - grandaugther
		doc.appendChild(root); // Add Root to Document

		OutputFormat format = new OutputFormat(doc); //Serialize DOM
		format.setOmitXMLDeclaration(true);
		format.setDoctype(null, "http://dtd.wctp.org/wctp-dtd-v1r1.dtd");
		StringWriter stringOut = new StringWriter(); //Writer will be a String
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.asDOMSerializer(); // As a DOM Serializer

		serial.serialize(doc.getDocumentElement());
		// This is a kludge. Skytel rejects any XML definition with an encoding
		// Xerces always uses the encoding. So, I had to kludge around it.
		String header = "<?xml version=\"1.0\"?>\n";

		// If we need a parameter, use it now
		if (parameter != null) {
			header = parameter + "=" + header;
		}
		BrokerFactory.getLoggingBroker().logDebug(stringOut.toString());
		String response = sendMessage(header + stringOut.toString());
		BrokerFactory.getLoggingBroker().logDebug(response);

		// Read the response
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					response)));

			NodeList nodeList = document
					.getElementsByTagName("wctp-Confirmation");
			Element element = (Element) nodeList.item(0);
			if (element == null)
				return false;

			NodeList success = element.getElementsByTagName("wctp-Success");
			if (success == null)
				return false;
			if (success.getLength() < 1) return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String stripWhiteSpace(String input) {
		char[] chars = input.toCharArray();

		int beginning = 0;
		int end = input.length() - 1;

		while ((chars[beginning] == '\n') || (chars[beginning] == '\r')
				|| (chars[beginning] == ' ') || (chars[beginning] == '\t'))
			beginning++;
		while ((chars[end] == '\n') || (chars[end] == '\r')
				|| (chars[end] == ' ') || (chars[end] == '\t'))
			end--;

		return input.substring(beginning, end + 1);
	}

	public static void main(String[] args) throws Exception {
//		String number = "8774650793";
//		String url = "http://wctp.skytel.com/wctp";
//		String parameter = null;
//		String contentType = "text/xml";
		
		String number = "3034023569";
		String url = "http://wctp.myairmail.com/wctp";
		//url = "http://localhost:8889/wctp";
		url = "http://wctp.arch.com/wctp";
		number = "1109700";
		String parameter = null;
		String contentType = "text/xml";
		
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		WctpLibrary l = new WctpLibrary(url, parameter, contentType, "reliableresponse", "r3liable");
		if (0==1) {
			if (args.length > 0) number = args[0];
			System.out.println (number+" exists? "+l.subscriberExists("drig@noses.org", number));
		} else {
		
		String response = null;
		String trackingNumber;
		String from = number;
		if (args.length < 1) {
			String message = l.formatWCTPMessage(number, from,
					"Tes WCTP", new Vector());
			System.out.println(message);
			response = l.sendMessage(message);
			System.out.println(response);
			ClientResponse cr = l.readClientResponse(response);
			//System.out.println (cr);
			System.out.println("Tracking number = " + cr.getTrackingNumber());
			trackingNumber = cr.getTrackingNumber();
		} else {
			trackingNumber = args[0];
		}

		while (true) {
			String clientQuery = l.formatWCTPClientQuery(number,
					from, trackingNumber);
			System.out.println(clientQuery);
			response = l.sendMessage(clientQuery);
			System.out.println("response = " + response);
			ClientResponse cr = l.readClientQueryResponse(response);
			String[] messages = cr.getMessages();
			System.out.println("\tresponse code=" + cr.getCode());
			System.out.println("\tresponse status=" + cr.getStatus());
					for (int i = 0; i < messages.length; i++) {
				
				System.out.println("\tresponse message [" + i + "] ="
						+ messages[i]);
			}
			Thread.sleep(10 * 1000);
		}
		}
	}
}
