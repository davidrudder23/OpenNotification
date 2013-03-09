/*
 * Created on Apr 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.Base64;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.util.ModAuthTicket;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class GroundworkSiteMinderSender extends AbstractNotificationSender {

	public final static int URL=1;
	public final static int IS_SERVICE=2;
	public final static int OBJECT_NAME=3;
	public final static int HOST_NAME=4;
	public final static int USERNAME=5;
	public final static int PASSWORD=6;	
	public final static int MY_IP = 7;

	String url;
	boolean service;
	String objectName;
	String hostName;
	String userName;
	String password;
	String myIP = "127.0.0.1";
	
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
		case MY_IP: myIP = value;
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
			String cgiData = "";

			// If we don't have any comment text, make it read "Acknowledge by" 
			if (text == null) {
				text = "Responded with " + response+ " by "+responderString;
			}
			
			// Form the Nagios CGI query string.  This is kind of ugly and uses
			// Nagios internal numbers.  I got this by looking at the Nagios web pages.
			if ((response.equalsIgnoreCase("confirm")) ||(response.equalsIgnoreCase("ack")) || (response.equalsIgnoreCase("confirmall"))) {
				int cmd_typ=34;
				if (!service) {
					cmd_typ=33;
				}
				cgiData = "?cmd_typ="+cmd_typ+"&cmd_mod=2&host="+URLEncoder.encode(hostName, "UTF-8")+"&persistent=true"+
					"&com_author="+URLEncoder.encode(responderString, "UTF-8")+"&com_data="+URLEncoder.encode(text, "UTF-8");
				if (service) {
					cgiData += "&service="+URLEncoder.encode(objectName, "UTF-8");
				}
			} else {
				cgiData = "?cmd_typ=3&cmd_mod=2&host=" + URLEncoder.encode(hostName, "UTF-8") + "&service="
						+ URLEncoder.encode(objectName, "UTF-8")
						+ "&com_author="
						+ URLEncoder.encode(responderString, "UTF-8")
						+ "&com_data=" + URLEncoder.encode(text, "UTF-8");
			}

			// Do the siteminder login
			String siteMinderURL = BrokerFactory.getConfigurationBroker().getStringValue("siteminder.url");
			String siteMinderUsername = BrokerFactory.getConfigurationBroker().getStringValue("siteminder.username");
			String siteMinderPassword = BrokerFactory.getConfigurationBroker().getStringValue("siteminder.password");
			if (StringUtils.isEmpty(siteMinderURL)) {
				BrokerFactory.getLoggingBroker().logWarn("SiteMinder URL is empty!");
			}
			if (StringUtils.isEmpty(siteMinderUsername)) {
				BrokerFactory.getLoggingBroker().logWarn("SiteMinder username is empty!");
			}
			if (StringUtils.isEmpty(siteMinderPassword)) {
				BrokerFactory.getLoggingBroker().logWarn("SiteMinder password is empty!");
			}
			
			PostMethod postMethod = new PostMethod(siteMinderURL);
			postMethod.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			String requestBody = "SMENC=ISO-8859-1&SMLOCALE=US-EN&target="+
				URLEncoder.encode(url+cgiData, "UTF-8")+
				"&User=" +siteMinderUsername + "&PASSWORD=" + siteMinderPassword;
			postMethod.setRequestBody(requestBody);
			HttpClient client = new HttpClient();
			client.executeMethod(postMethod);
			HttpState state = client.getState();
			Cookie[] cookies = state.getCookies();
			BrokerFactory.getLoggingBroker().logDebug("SiteMinder response="+postMethod.getResponseBodyAsString());

	        String redirectLocation = null;
	        Header locationHeader = postMethod.getResponseHeader("location");
	        if (locationHeader != null) {
	            redirectLocation = locationHeader.getValue();
	            BrokerFactory.getLoggingBroker().logDebug("SiteMinder redirected us to "+redirectLocation);
	        } else {
	            // The response is invalid and did not provide the new location for
	            // the resource.  Report an error or possibly handle the response
	            // like a 404 Not Found error.
	        }
	        
        	HttpURLConnection nagiosConn = (HttpURLConnection )new URL(url+cgiData).openConnection(); 
        	String fullCookie = "";
	        if (redirectLocation != null) {
	        	HttpURLConnection urlConn = (HttpURLConnection )new URL(redirectLocation).openConnection(); 
				for (int c = 0; c < cookies.length; c++) {
        	 		if (fullCookie.length()>0) {
        	 			fullCookie += ";";
        	 		}
        	 		fullCookie+=cookies[c];
				}
				urlConn.setRequestProperty("Cookie", fullCookie);
	        	urlConn.getContent();
	        	String headerName=null;
	        	for (int i=1; (headerName = urlConn.getHeaderFieldKey(i))!=null; i++) {
	        	 	if (headerName.equals("Set-Cookie")) {
	        	 		String cookie = urlConn.getHeaderField(i);
	        	 		cookie = cookie.substring(0, cookie.indexOf(";"));
	        	 		if (fullCookie.length()>0) {
	        	 			fullCookie += ";";
	        	 		}
	        	 		fullCookie+=cookie;
	        	 	}
	        	}
	        }
//			// Make the web connection
			String cookie = "nagios_auth_tkt=";
			cookie += Base64.byteArrayToBase64(ModAuthTicket.getTicket(userName, password, 
					InetAddress.getByName(myIP), null, null, System.currentTimeMillis()/1000).getBytes());
	        //nagiosConn.addRequestProperty("Cookie", cookie);
			fullCookie += "; "+cookie;
	        nagiosConn.setRequestProperty("Cookie", fullCookie);
	        BrokerFactory.getLoggingBroker().logDebug("nagios cookie="+cookie);

		Object nagiosResponse = nagiosConn.getContent();
		if (nagiosResponse instanceof String) {
			BrokerFactory.getLoggingBroker().logDebug("Response="+nagiosResponse);
		}
	        	 	
			// Add the authentication
//			if (userName != null || password != null) {
//	            String up = userName + ":" + password;
//	            String encoding;
//	            //we do not use the sun impl of base64 for portability,
//	            //and always use our own implementation for consistent
//	            //testing
//	            encoding = Base64.byteArrayToBase64(up.getBytes());
//	            connection.setRequestProperty ("Authorization",
//	                    "Basic " + encoding);
//	        }
			// Do the request
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
