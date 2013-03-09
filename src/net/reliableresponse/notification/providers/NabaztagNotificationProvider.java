/*
 * Created on Aug 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.providers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.JabberDevice;
import net.reliableresponse.notification.device.NabaztagDevice;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;
/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class NabaztagNotificationProvider extends AbstractNotificationProvider
{
	String serialNumber;
	String token;
	String choreographySequence;
	
	public NabaztagNotificationProvider() {
		init (null);
	}
	
	public void init (Hashtable params) {
	}
	
	public static NabaztagNotificationProvider getInstance (Hashtable params) {
		NabaztagNotificationProvider provider = new NabaztagNotificationProvider();
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.providers.NotificationProvider#cancelPage(net.reliableresponse.notification.Notification)
	 */
	public boolean cancelPage(Notification page) {
		return false;
	}

	public String[] getResponses(Notification page) {
		return new String[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.providers.NotificationProvider#sendPage(net.reliableresponse.notification.usermgmt.User,
	 *      net.reliableresponse.notification.device.Device, java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Vector)
	 */
	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException {
		User user = (User)notification.getRecipient();
		NotificationSender sender = notification.getSender(); 
		String summary = notification.getSubject();
		Vector options = notification.getOptions();
		//String messageText = notification.getDisplayText(2048);
		String message = "You have a new notification from "+
		notification.getRecipient().toString()+".  The subject is "+
		notification.getSubject()+".  The message is "+
		notification.getDisplayText();
		
		/*if (notification.isPersistent()) {
			message += "\n\n";
			String[] responses = notification.getSender()
					.getAvailableResponses(notification);
			if (responses.length > 0) {
				message += "You may respond with:\n";
				for (int r = 0; r < responses.length; r++) {
					message += "\t \"" + responses[r] + " "
							+ notification.getID() + "\"\n";
				}
			}
		}*/


		Hashtable params = new Hashtable();
		
		if (device instanceof NabaztagDevice) {
			sendMessage(notification, device, message);
			return params;
		} else {
			throw new NotificationException(NotificationException.INTERNAL_ERROR, "Supplied device does not support Jabber");
		}
	}

	/**
	 * @param device
	 * @param message
	 * @throws SendMessageFailedException
	 */
	private void sendMessage(Notification notif, Device device, String message) throws NotificationException {
		NabaztagDevice nabaztagDevice = (NabaztagDevice) device;
		sendMessage (notif, nabaztagDevice.getSerialNumber(), 
				nabaztagDevice.getToken(), 
				nabaztagDevice.getChoreographySequence(),
				nabaztagDevice.getVoice(),
				message);
	}

	private void sendMessage(Notification notif, String serialNumber, 
						String token,
						String choreographySequence,
						String voice,
						String message) throws NotificationException {
		BrokerFactory.getLoggingBroker().logDebug("Sending Nabaztag message to "+serialNumber+"/"+token+": "+message);
		String url = "http://api.nabaztag.com/vl/FR/api.jsp?sn="+serialNumber+
		"&token="+token+
		"&tts="+URLEncoder.encode(message)+
		"&chor="+URLEncoder.encode(choreographySequence)+
		"&voice="+voice;
		BrokerFactory.getLoggingBroker().logDebug("Nabaztage URL: "+url);
		GetMethod get = new GetMethod(url);
		HttpClient http = new HttpClient();
		try {
			http.executeMethod(get);
			String response = get.getResponseBodyAsString();
			BrokerFactory.getLoggingBroker().logDebug(response);
			
			// Parse the response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setValidating(true);
			//factory.setNamespaceAware(true);
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(new StringReader(
						response)));

				NodeList nodeList = document
						.getElementsByTagName("rsp");
				Element element = (Element) nodeList.item(0);
				if (element != null) {
					NodeList messages = document.getElementsByTagName("message");
					NodeList comments = document.getElementsByTagName("comment");
					if ((messages != null) && (comments != null)) {
						for (int messageNum = 0; messageNum<messages.getLength(); messageNum++) {
							String notifMessage = messages.item(messageNum).getChildNodes().item(0).getNodeValue()+": "+
							comments.item(messageNum).getChildNodes().item(0).getNodeValue();
							notif.addMessage(notifMessage, null);
						}
					}
				}
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logError(anyExc);
			}
		} catch (HttpException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new NotificationException(NotificationException.FAILED, e.getMessage());
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new NotificationException(NotificationException.TEMPORARILY_FAILED, e.getMessage());
		}
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put ("recipient", "");
		return params;
	}
	
	public boolean isConfirmed(Notification page) {
		return false;
	}

	public boolean isPassed(Notification page) {
		return false;
	}
	
	public String getName() {
		return "Nabaztag";
	}
	

}