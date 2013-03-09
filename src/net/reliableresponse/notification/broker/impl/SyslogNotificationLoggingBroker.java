/*
 * Created on Jan 26, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationLoggingBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SyslogNotificationLoggingBroker implements
		NotificationLoggingBroker {
	
	InetAddress address;
	int port;
	DatagramSocket socket;
	DateFormat formatter = new SimpleDateFormat("MMM d HH:mm:ss");
	
	private void sendLog (String message) {
		Date now = new Date();
		String date = formatter.format (new Date());
		String packetString = "<13>"+date+" "+message;
		DatagramPacket packet = new DatagramPacket(packetString.getBytes(), packetString.length(),
				address, port);
		try {
			BrokerFactory.getLoggingBroker().logDebug("Sending syslog packet: "+packetString);
			socket.send(packet);
		} catch (SocketException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public SyslogNotificationLoggingBroker() {
		String addressName = BrokerFactory.getConfigurationBroker().getStringValue("syslog.host");
		BrokerFactory.getLoggingBroker().logDebug("Will send notification logs to "+addressName);
		if ((addressName == null) || (addressName.length() == 0)) {
			BrokerFactory.getLoggingBroker().logWarn("Syslog hostname empty, defaulting to localhost");
		}
		try {
			address = InetAddress.getByName(addressName);
		} catch (UnknownHostException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		String portString = BrokerFactory.getConfigurationBroker().getStringValue("syslog.port");
		port = 514;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e2) {
			BrokerFactory.getLoggingBroker().logWarn("Syslog port, "+portString+" is invalid.  Using 514");
		}

		try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logNotification(net.reliableresponse.notification.Notification, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.device.Device, java.lang.String)
	 */
	public void logNotification(Notification notification, Member member,
			Device device, String status) {
		BrokerFactory.getLoggingBroker().logDebug("Logging notification");
		sendLog("New notification by "+notification.getSender()+" sent to "+member.toString()+"'s device "+device.toString()+" with status "+status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logConfirmation(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logConfirmation(Member confirmedBy, Notification notification) {
		sendLog("Notification #"+notification.getUuid()+" confirmed by "+confirmedBy.toString());
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logEscalation(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logEscalation(Member from, Member to, Notification notification) {
		sendLog("Notification #"+notification+" escalated from "+from.toString()+" to "+to.toString());
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logPassed(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logPassed(Member from, Member to, Notification notification) {
		sendLog("Notification #"+notification+" passed from "+from.toString()+" to "+to.toString());
	}

}
