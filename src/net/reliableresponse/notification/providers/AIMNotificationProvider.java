/*
 * Created on Aug 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.aim.Chatable;
import net.reliableresponse.notification.aim.JavaTOC2;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.device.AIMDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AIMNotificationProvider extends AbstractNotificationProvider implements Chatable {
	
	private static JavaTOC2 client;
	String accountName;
	String password;
	
	public AIMNotificationProvider() {
		init();
	}
	
	public void init(Hashtable params) {
		init();
	}
	public void init() {
		this.accountName = BrokerFactory.getConfigurationBroker().getStringValue("aim.account", accountName);
		this.password = BrokerFactory.getConfigurationBroker().getStringValue("aim.password", password);
		
		if (!ClusteredServiceManager.getInstance().willRun("AIM")) {
			return;
		}

		if (client == null) {
			BrokerFactory.getLoggingBroker().logDebug("Signing onto AIM as "+accountName);
			client = new JavaTOC2(this);
			try {
				client.login(this.accountName, this.password);
				new TOCThread(client).start();
				new TOCKeepAlive(client).start();
			} catch (IOException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
			
			try {
				// Sleep for 3 seconds to allow AOL's server to update themselves
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}

		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.providers.NotificationProvider#sendPage(net.reliableresponse.notification.usermgmt.User,
	 *      net.reliableresponse.notification.device.Device, java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Vector)
	 */
	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException {
		if (!ClusteredServiceManager.getInstance().willRun("AIM")) {
			ClusteredServiceManager.getInstance().sendNotificationToDevice
			(notification,
					"AIM",  
					notification.getDisplayText(), 
					device.getUuid());
			return new Hashtable();
		}
		String message = notification.getDisplayText();
		String subject = notification.getSubject();
		
		message = "Notification from Reliable Response Notification, ID="+notification.getID()+"\n"+
					subject+"\n"+
					message+"\n\n";
		
		if (notification.isPersistent()) {
			String[] responses = notification.getSender()
					.getAvailableResponses(notification);
			if (responses.length > 0) {
				message += "You may respond with:\n";
				for (int r = 0; r < responses.length; r++) {
					message += "\t \"" + responses[r] + " "
							+ notification.getID() + "\"\n";
				}
			}
		}

		// We need to add this message if it's an escalation thread
		// "To pass, please respond with \"Pass "+notification.getID()+"\"\n";
		
		Hashtable params = new Hashtable ();
		
		if (device instanceof AIMDevice) {
			String accountToSendTo =((AIMDevice)device).getAccount();
			BrokerFactory.getLoggingBroker().logDebug("Sending AIM message to "+accountToSendTo);
			
			List<String> parts = splitMessage(message, device.getMaxCharactersSize(), device.getMaxMessages());
			for (String part: parts) {
				client.send(accountToSendTo, part);
			}
			
			return params;
		} else {
			return params;
		}
	}
	
	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable ();
		return params;
	}
	
	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#cancelPage(net.reliableresponse.notification.Notification)
	 */
	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isConfirmed(Notification notification) {
		return false;
	}
	
	public boolean isPassed(Notification notification) {
		return false;
	}
	
	public String getName() {
		return "AOL IM";
	}	
	
	
	public void error(String arg0, String arg1) {
		BrokerFactory.getLoggingBroker().logWarn(arg0+":"+arg1);
	}
	
	public void im (String buddyName, String message) {
		User user = null;

		BrokerFactory.getLoggingBroker().logDebug("got AIM message: "+message);
		// Find out who sent the messahe
		User[] usersWithAIM = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType
			("net.reliableresponse.notification.device.AIMDevice");
		BrokerFactory.getLoggingBroker().logDebug("Got "+usersWithAIM.length+" users with AIM");
		for (int i = 0; i < usersWithAIM.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("user w/ aim ["+i+"]="+usersWithAIM[i]);
			Device[] devices = usersWithAIM[i].getDevices();
			for (int d = 0; d < devices.length; d++) {
				if (devices[d] instanceof AIMDevice) {
					if (((AIMDevice)devices[d]).getAccount().equals(buddyName)) {
						user = usersWithAIM[i];
					}
				}
			}
		}
		
		// We don't know who this is
		if (user == null) {
			BrokerFactory.getLoggingBroker().logInfo("Got AIM message from unknown source, "+buddyName+
					" - "+message);
			return;
		}

		List<Notification> pendingNotifications = BrokerFactory.getNotificationBroker().getNotificationsSince(8640000L);
		Vector responses = new Vector();
		for (Notification pendingNotification: pendingNotifications) {
			NotificationSender sender = pendingNotification.getSender();
			String[] respArray = sender.getAvailableResponses(pendingNotification);
			for (int r = 0; r < respArray.length; r++) {
				if (!responses.contains(respArray[r])) {
					responses.addElement(respArray[r]);
				}
			}
		}
		
		boolean notifFound = false;
		for (int i = 0; i < responses.size(); i++) {
			String response = (String)responses.elementAt(i);
			Pattern pattern = Pattern.compile("\\b(?i)"+response+"\\b"); 
			if (pattern.matcher(message).find()) {
				for (Notification pendingNotification: pendingNotifications) {
					if (message.indexOf(pendingNotification.getID()) >= 0) {
						notifFound = true;
						NotificationSender sender = pendingNotification.getSender();
						if (sender != null) {
							BrokerFactory.getLoggingBroker().logDebug("Responding to "+
									pendingNotification+" with \""+response+"\"via AIM message from "+buddyName);
							sender.handleResponse(pendingNotification, user, response, "Notification confirmed by AIM message: "+message);
							client.send(buddyName, "Responded to notification "+pendingNotification.getID()+" with "+response);
						}
					}
				}
			}
		}
		
		if (!notifFound) {
			client.send(buddyName, "Notification not found, please try again");
		}
		
	}

	public void unknown(String arg1) {
		BrokerFactory.getLoggingBroker().logInfo("AIM Unknown: "+arg1);
	}
	
	public static void clearSessions(){
		client = null;
	}
}

class TOCKeepAlive extends Thread {
	/*
	 * This thread sends a keep alive every 30 seconds
	 */
	JavaTOC2 toc;
	
	private boolean stopped;
	public TOCKeepAlive(JavaTOC2 toc) {
		this.toc = toc;
		stopped = true;
	}
	
	public void run() {
		try {
			while (!stopped) {
				toc.sendFlap(5, "keep alive");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public void stopKeepAlive() {
		stopped = true;
	}
}

class TOCThread extends Thread {
	JavaTOC2 toc;
	public TOCThread(JavaTOC2 toc) {
		this.toc = toc;
	}
	
	public void run() {
		try {
			toc.processTOCEvents();
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
}