/*
 * Created on Apr 19, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.meterware.httpunit.Base64;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class OpenNMSSender extends AbstractNotificationSender {

	public static final int URL = 1;
	public static final int EVENT = 2;
	public static final int USERNAME = 3;
	public static final int PASSWORD = 4;

	String url;
	String event;
	String username;
	String password;
	
	public OpenNMSSender() {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.sender.NotificationSender#addVariable(int,
	 *      java.lang.String)
	 */
	public void addVariable(int index, String value) {
		switch (index) {
		case URL:
			url = value;
			break;
		case EVENT:
			event = value;
			break;
		case USERNAME:
			username = value;
			break;
		case PASSWORD:
			password=value;
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.sender.NotificationSender#getVariables()
	 */
	public String[] getVariables() {
		String[] variables = { url, event };

		return variables;
	}

	public void handleResponse(Notification notification, Member responder,
			String response, String text) {
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory.getLoggingBroker().logInfo(responder+" tried to confirm an expired notification with uuid "+notification.getUuid());
			return;
		}
		
		if (response.equalsIgnoreCase("acknowledge")) {
			try {
				URL url = new URL(this.url);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("action", "1");
				//String encoding = new sun.misc.BASE64Encoder().encode ((username+":"+password).getBytes());
				String encoding = Base64.encode((username+":"+password));
				conn.setRequestProperty ("Authorization", "Basic " +encoding); 
				PrintStream out = new PrintStream(conn.getOutputStream());
				out.print("action=1&event="+event);
				InputStream in = conn.getInputStream();
			} catch (MalformedURLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		String event = "164";
		if (args.length > 0) {
			event = args[0];
		}
		OpenNMSSender sender = new OpenNMSSender();
		sender.addVariable(OpenNMSSender.URL, "http://10.10.10.2:8080/opennms/event/acknowledge");
		sender.addVariable(OpenNMSSender.EVENT, event);
		sender.addVariable(OpenNMSSender.USERNAME, "admin");
		sender.addVariable(OpenNMSSender.PASSWORD, "admin");
		Notification notif = new Notification(null, BrokerFactory
				.getUserMgmtBroker().getUserByUuid("0000001"), sender,
				"test opennms sender", "testing opennms sender");

		sender.handleResponse(notif, BrokerFactory.getUserMgmtBroker()
				.getUserByUuid("0000001"), "acknowledge", "test");
	}
}