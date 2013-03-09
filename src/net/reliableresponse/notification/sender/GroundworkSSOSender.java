/*
 * Created on Apr 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.Base64;
import net.reliableresponse.notification.web.util.ModAuthTicket;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class GroundworkSSOSender extends AbstractNotificationSender {

	public final static int URL = 1;

	public final static int IS_SERVICE = 2;

	public final static int OBJECT_NAME = 3;

	public final static int HOST_NAME = 4;

	public final static int USERNAME = 5;

	public final static int PASSWORD = 6;
	
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
		case URL:
			url = value;
			break;
		case IS_SERVICE:
			service = (value.toLowerCase().startsWith("s"));
			break;
		case OBJECT_NAME:
			objectName = value;
			break;
		case HOST_NAME:
			hostName = value;
			break;
		case USERNAME:
			userName = value;
			break;
		case PASSWORD:
			password = value;
			break;
		case MY_IP:
			myIP = value;
			break;
		}

	}

	public String[] getVariables() {
		return new String[] { url, service ? "service" : "host", objectName,
				hostName, userName, password };
	}

	public void handleResponse(Notification notification, Member responder,
			String response, String text) {
		super.handleResponse(notification, responder, response, text);
		String responderString = "Unknown responder";
		if (responder != null)
			responderString = responder.toString();

		// Check to make sure we're not confirming an expired message
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory
					.getLoggingBroker()
					.logInfo(
							responderString
									+ " tried to confirm an expired notification with uuid "
									+ notification.getUuid());
			return;
		}

		try {
			// Find the time in Unix format. Java stores it in milliseconds,
			// where Unix uses seconds
			long time = System.currentTimeMillis() / 1000;
			String cgiData = "";

			// If we don't have any comment text, make it read "Acknowledge by"
			if (text == null) {
				text = "Responded with " + response + " by " + responderString;
			}

			// Now, form the Nagios CGI query string. This is kind of ugly and
			// uses
			// Nagios=internal numbers. I got this by looking at the Nagios web
			// pages.
			if ((response.equalsIgnoreCase("confirm"))
					|| (response.equalsIgnoreCase("ack"))) {
				cgiData = "?cmd_typ=34&cmd_mod=2&host="
						+ URLEncoder.encode(hostName, "UTF-8") + "&service="
						+ URLEncoder.encode(objectName, "UTF-8")
						+ "&com_author="
						+ URLEncoder.encode(responderString, "UTF-8")
						+ "&com_data=" + URLEncoder.encode(text, "UTF-8");
			} else {
				cgiData = "?cmd_typ=3&cmd_mod=2&host="
						+ URLEncoder.encode(hostName, "UTF-8") + "&service="
						+ URLEncoder.encode(objectName, "UTF-8")
						+ "&com_author="
						+ URLEncoder.encode(responderString, "UTF-8")
						+ "&com_data=" + text;
			}

			// Make the URL connection
			BrokerFactory.getLoggingBroker().logDebug(
					"Groundwork URL " + url + cgiData);

			String cookie = "nagios_auth_tkt=";
			cookie += Base64.byteArrayToBase64(ModAuthTicket.getTicket(userName, password, 
					InetAddress.getByName(myIP), null, null, System.currentTimeMillis()/1000).getBytes());
//				 Do the request
				String host = new URL(url).getHost();
				int port = new URL(url).getPort();
				if (port <=0) port = 80;
				Socket socket = new Socket(host, port);
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket
						.getOutputStream()));

				String path = new URL(url).getPath();
				
				out.println("GET "+ path+cgiData+" HTTP/1.1");
				out.println("Host: "+host);
				out.println("Connection: close");
				out.println ("Cookie: "+cookie);
				out.println();
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line;
				while ( (line = in.readLine()) != null) {
					BrokerFactory.getLoggingBroker().logDebug(line);
				}
				in.close();
				out.close();
				socket.close();
			
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public String toString() {
		return "Nagios Network Monitor";
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		String url = "http://localhost/nagios/cgi-bin/cmd.cgi";
		String cgiData = "?cmd_typ=3&cmd_mod=2&host="
			+ URLEncoder.encode("localhost", "UTF-8") + "&service="
			+ URLEncoder.encode("Root Partition", "UTF-8")
			+ "&com_author="
			+ URLEncoder.encode("Rudder, David", "UTF-8")
			+ "&com_data=" + "test confirm";

// Make the URL connection
		BrokerFactory.getLoggingBroker().logDebug(
				"Groundwork URL " + url + cgiData);

		String userName = "nagiosadmin";
		String password = "changethistosomethingunique";
		String myIP="127.0.0.1";
		String cookie = "nagios_auth_tkt=";
		cookie += Base64.byteArrayToBase64(ModAuthTicket.getTicket(userName,
				password, InetAddress.getByName(myIP), null, null,
				System.currentTimeMillis() / 1000).getBytes());
		// Do the request
		String host = new URL(url).getHost();
		int port = new URL(url).getPort();
		if (port <= 0)
			port = 80;
		Socket socket = new Socket(host, port);
		PrintWriter out = new PrintWriter(new OutputStreamWriter(socket
				.getOutputStream()));

		String path = new URL(url).getPath();
		
		out.println("GET " + path + cgiData + " HTTP/1.1");
		out.println("Host: " + host);
		out.println("Connection: close");
		out.println("Cookie: " + cookie);
		out.println();
		out.flush();

		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			BrokerFactory.getLoggingBroker().logDebug(line);
		}
		in.close();
		out.close();
		socket.close();
	}
}
