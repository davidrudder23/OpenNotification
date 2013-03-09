/*
 * Created on Apr 20, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.BlackberryDevice;
import net.reliableresponse.notification.device.Device;

public class BlackberryNotificationProvider extends
		AbstractNotificationProvider {
	
	String mdsURL = null;
	public BlackberryNotificationProvider() {
		String mdsHost = BrokerFactory.getConfigurationBroker().getStringValue("blackberry.mdshost", "localhost");
		int mdsPort = BrokerFactory.getConfigurationBroker().getIntValue("blackberry.mdsport", 8080);
		mdsURL = "http://"+mdsHost+":"+mdsPort+"/push";
		BrokerFactory.getLoggingBroker().logDebug("mdsURL="+mdsURL);		
	}

	public void init(Hashtable params) throws NotificationException {
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		BrokerFactory.getLoggingBroker().logDebug("Sending Blackberry channel push");
		try {
			if (!(device instanceof BlackberryDevice)) {
				throw new NotificationException(NotificationException.FAILED, "Tried to send a Blackberry message with a non-Blackberry device");
			}
			
			String fullURL = mdsURL+"?DESTINATION="+
			((BlackberryDevice)device).getPin()+
			"&PORT=7874&REQUESTURI=/";
			
			StringBuffer body = new StringBuffer();
			body.append ("<html><head><title>Reliable Response Notification #"+notification.getUuid()+"</title></head>\n");
			body.append ("<h2>Notification #"+notification.getUuid()+"</h2><br>\n");
			body.append ("From: "+notification.getSender().toString()+"<br>\n");
			body.append ("Subject: "+notification.getSubject()+"\n");
			body.append ("<pre>\n");
			body.append (notification.getDisplayText());
			body.append ("</pre>\n");
			
			String[] responses = notification.getSender().getAvailableResponses(notification.getUltimateParent());
			for (int r = 0; r< responses.length; r++) {
				body.append ("<a href=\""+BrokerFactory.getConfigurationBroker().getStringValue("base.url"));
				body.append ("/actions/respond.jsp?id="+notification.getUuid()+"&response=" +responses[r]);
				body.append ("\">"+responses[r]+"</a><br>");
			}
			
			
			HttpURLConnection url = (HttpURLConnection)new URL(fullURL).openConnection();
			url.setDoOutput(true);
			url.setRequestMethod("POST");
			url.setRequestProperty("Content-Location", "http://www.reliableresponse.net/notification/browserpush");
			url.setRequestProperty("X-RIM-Push-Title", "RRN");
			url.setRequestProperty("X-RIM-Push-Type", "Browser-Channel");
			url.setRequestProperty("X-RIM-Push-Channel-ID", "http://www.reliableresponse.net");
			url.setRequestProperty("X-RIM-Push-Read-Icon-URL", BrokerFactory.getConfigurationBroker().getStringValue("base.url")+"/images/channel_read.gif");
			url.setRequestProperty("X-RIM-Push-UnRead-Icon-URL", BrokerFactory.getConfigurationBroker().getStringValue("base.url")+"/images/channel_unread.gif");
			url.setRequestProperty("X-RIM-Push-Priority", "High"); // always use high or else the icon doesn't show up
			url.setRequestProperty("Content-Type", "text/html");
			url.setRequestProperty("Content-Length", ""+body.length());
			
			OutputStream out = url.getOutputStream();
			out.write(body.toString().getBytes());
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
			String line;
			while ( (line = in.readLine()) != null) {
				BrokerFactory.getLoggingBroker().logDebug("bb line="+line);
			}
			out.close();
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new NotificationException(NotificationException.FAILED, e.getMessage());
		} 
		return getParameters(notification, device);
		
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable ();
		return params;
	}

	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Blackberry";
	}

}
