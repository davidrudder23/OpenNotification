/*
 * Created on Apr 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.Base64;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NagiosSender extends AbstractNotificationSender {

	public final static int URL=1;
	public final static int IS_SERVICE=2;
	public final static int OBJECT_NAME=3;
	public final static int HOST_NAME=4;
	public final static int USERNAME=5;
	public final static int PASSWORD=6;

	String url;
	boolean service;
	String objectName;
	String hostName;
	String userName;
	String password;
	
	public void addVariable(int index, String value) {
		switch (index) {
		case URL: url = value;
		break;
		case IS_SERVICE: service = (value.toLowerCase().startsWith("s"));
		break;
		case OBJECT_NAME: objectName = value;
		break;
		case HOST_NAME: hostName = value;
		break;
		case USERNAME: userName = value;
		break;
		case PASSWORD: password = value;
		break;
		}

	}
	public String[] getVariables() {
		return new String[] {url, service?"service":"host", objectName, hostName, userName, password};
	}
	
	public void handleResponse(Notification notification, Member responder, String response, String text) {
		super.handleResponse(notification, responder, response, text);
		String responderString = "Unknown responder";
		if (responder != null) responderString = responder.toString();
		
		// Check to make sure we're not confirming an expired message
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory.getLoggingBroker().logInfo(responderString+" tried to confirm an expired notification with uuid "+notification.getUuid());
			return;
		}

		try {
			// Find the time in Unix format.  Java stores it in milliseconds, where Unix uses seconds
			long time = System.currentTimeMillis()/1000;
			String cgiData = "";

			// If we don't have any comment text, make it read "Acknowledge by" 
			if (text == null) {
				text = "Responded with " + response+ " by "+responderString;
			}
			
			// Now, form the Nagios CGI query string.  This is kind of ugly and uses
			// Nagios=internal numbers.  I got this by looking at the Nagios web pages.
			if ((response.equalsIgnoreCase("confirm")) ||(response.equalsIgnoreCase("ack"))) {
				int cmd_typ=34;
				if (!service) {
					cmd_typ=33;
				}
				cgiData = "?cmd_typ="+cmd_typ+"&cmd_mod=2&host="+URLEncoder.encode(hostName, "UTF-8")+"&persistent=true&service="+URLEncoder.encode(objectName, "UTF-8")+
					"&com_author="+URLEncoder.encode(responderString, "UTF-8")+"&com_data="+URLEncoder.encode(text, "UTF-8");
			} else {
				cgiData = "?cmd_typ=3&cmd_mod=2&host=" + URLEncoder.encode(hostName, "UTF-8") + "&service="
						+ URLEncoder.encode(objectName, "UTF-8")
						+ "&com_author="
						+ URLEncoder.encode(responderString, "UTF-8")
						+ "&com_data=" + URLEncoder.encode(text, "UTF-8");
			}
			
			// Make the URL connection
			BrokerFactory.getLoggingBroker().logDebug("Nagios URL "+url+cgiData);
			HttpURLConnection connection =(HttpURLConnection)(new URL(url+cgiData).openConnection());
			
			// Add the authentication
			if (userName != null || password != null) {
	            String up = userName + ":" + password;
	            String encoding;
	            //we do not use the sun impl of base64 for portability,
	            //and always use our own implementation for consistent
	            //testing
	            encoding = Base64.byteArrayToBase64(up.getBytes());
	            connection.setRequestProperty ("Authorization",
	                    "Basic " + encoding);
	        }
			// Do the request
			connection.getContent();
			BrokerFactory.getLoggingBroker().logDebug("Connection to Nagios succeeded");
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public String toString() {
		return "Nagios Network Monitor";
	}
}
