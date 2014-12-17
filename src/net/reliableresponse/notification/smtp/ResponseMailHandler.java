/*
 * Created on Nov 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.CellPhoneEmailDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResponseMailHandler implements MailHandler {
	Notification notification;
	
	String sender;

	String subject;

	StringBuffer message;
	Hashtable headers;
	
	boolean inHeader;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#CheckToUser(java.lang.String)
	 */
	public boolean CheckToUser(String strUser) {

		BrokerFactory.getLoggingBroker().logDebug("Response Handler to user = "+strUser);
		try {
		String uuid = strUser.substring(0, strUser.indexOf("@"));
		while (uuid.length()<7) {
			uuid = "0"+uuid;
		}
		notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
		BrokerFactory.getLoggingBroker().logDebug("Found SMTP notification = "+notification);
		if (notification.getStatus() == Notification.EXPIRED) {
			notification = null;
		}
		return (notification != null);
		} catch (Exception anyExc) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#CheckFromUser(java.lang.String)
	 */
	public boolean CheckFromUser(String strUser) {
		if ((strUser == null) || (strUser.length() == 0))
			return false;
		sender = strUser;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#Start()
	 */
	public boolean Start() {
		headers = new Hashtable();
		message = new StringBuffer();
		inHeader = true;
		subject = "";
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#Line(java.lang.String)
	 */
	public void Line(String strLine) {
		if (inHeader) {
			if (strLine.indexOf(":") >= 0) {
				String key = strLine.substring (0, strLine.indexOf(":")).toLowerCase();
				String value = strLine.substring (strLine.indexOf(":")+1, strLine.length());
				while (value.startsWith(" ")) value = value.substring(1, value.length());
				headers.put (key, value);
			} else if (strLine.indexOf("boundary=\"") >= 0) {
				Pattern pattern = Pattern.compile("boundary=\"(.*)\"");
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					String value = matcher.group(1);
					headers.put ("boundary", value);
				} else {
					BrokerFactory.getLoggingBroker().logDebug("Didn't find boundary");
				}
			}
			
			if (strLine.equals("")) {
				inHeader = false;
			} else if (strLine.toLowerCase().startsWith("subject:")) {
				subject = strLine.substring(8, strLine.length()).toLowerCase();
				subject.trim();
			}
		} else {
			message.append(strLine);
			message.append ("\n");
		}
	}
	
	private String contains (String response) {
		Pattern pattern = Pattern.compile("\\b(?i)"+response+"\\b"); 
		if (pattern.matcher(subject).find()) {
			return subject;
		}
		try {
			if (message != null) {
				String contentType = (String)headers.get("content-type");
				if (contentType == null) contentType = "text/plain";
				String firstLine = "";
				if (contentType.toLowerCase().startsWith("multipart")) {
					BufferedReader in = new BufferedReader(new StringReader(message.toString()));
					String boundary = (String)headers.get("boundary");
					if (boundary == null) {
						Pattern boundPattern = Pattern.compile("boundary=\"(.*)\"");
						Matcher matcher = boundPattern.matcher(contentType);
						if (matcher.find()) {
							boundary = matcher.group(1);
						}
					}
					
					if (boundary != null) {
						while ( firstLine.indexOf(boundary) < 0) firstLine = in.readLine();
						while ( !firstLine.equals("")) firstLine = in.readLine();
						firstLine = in.readLine();
					}
				} else {
					firstLine = new BufferedReader(new StringReader(message.toString())).readLine();
				}
				if (pattern.matcher(firstLine).find()) {
					return firstLine;
				}
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;
	}

	private Member findSenderFromEmail (String email) {
		BrokerFactory.getLoggingBroker().logDebug("Looking for sender with email "+email);
		User[] users= BrokerFactory.getUserMgmtBroker().getUsersWithEmailAddress(email);
		
		if ((users != null) && (users.length>0)) {
			BrokerFactory.getLoggingBroker().logDebug("We found member "+users[0]+" with email address "+email);
			return users[0];
		}

		BrokerFactory.getLoggingBroker().logDebug(email+" is not a known email address");
		if (CellPhoneEmailDevice.isCellPhoneAddress(email)) {
			BrokerFactory.getLoggingBroker().logDebug(email+" is a cell phone email address");
			users = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType("net.reliableresponse.notification.device.CellPhoneEmailDevice");
			if (users != null) {
				for (int i = 0; i < users.length; i++) {
					List<Device> devices = users[i].getDevices();
					for (Device device: devices) {
						if (device instanceof CellPhoneEmailDevice) {
							CellPhoneEmailDevice cellDevice = (CellPhoneEmailDevice)device;
							if (cellDevice.getEmailAddress().equalsIgnoreCase(email)) {
								return users[i];
							}
						}
					}
				}
			}
		}
		UnknownUser unknownUser = new UnknownUser();
		unknownUser.setEmailAddress(email);
		return unknownUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#End()
	 */
	public void End() {
		
		if (notification != null) {
			String[] responses = notification.getSender().getAvailableResponses(notification);
			for (int i = 0; i < responses.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug("Checking response "+responses[i]);
				String response = responses[i];
				String responseLine = contains(response);
				if (!StringUtils.isEmpty(responseLine)) {
					BrokerFactory.getLoggingBroker().logDebug("Responding to "+notification+" with response "+response+" via SMTP");
					notification.getSender().handleResponse(notification, 
							findSenderFromEmail(sender), responses[i], responseLine);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#Abort()
	 */
	public void Abort() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#ConnectionClosed(boolean)
	 */
	public void ConnectionClosed(boolean bCleanExit) {
		// TODO Auto-generated method stub

	}

}
