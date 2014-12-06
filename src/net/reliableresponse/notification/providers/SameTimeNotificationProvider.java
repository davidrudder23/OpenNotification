/*
 * Created on Sep 20, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.SameTimeDevice;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;

import com.echomine.common.SendMessageFailedException;
import com.lotus.sametime.awareness.AwarenessService;
import com.lotus.sametime.awareness.AwarenessServiceEvent;
import com.lotus.sametime.awareness.AwarenessServiceListener;
import com.lotus.sametime.community.CommunityService;
import com.lotus.sametime.community.LoginEvent;
import com.lotus.sametime.community.LoginListener;
import com.lotus.sametime.core.comparch.DuplicateObjectException;
import com.lotus.sametime.core.comparch.STSession;
import com.lotus.sametime.core.constants.EncLevel;
import com.lotus.sametime.core.constants.ImTypes;
import com.lotus.sametime.core.types.STObject;
import com.lotus.sametime.core.types.STUser;
import com.lotus.sametime.core.types.STUserStatus;
import com.lotus.sametime.directory.Directory;
import com.lotus.sametime.directory.DirectoryEvent;
import com.lotus.sametime.directory.DirectoryListener;
import com.lotus.sametime.directory.DirectoryService;
import com.lotus.sametime.directory.DirectoryServiceListener;
import com.lotus.sametime.im.Im;
import com.lotus.sametime.im.ImEvent;
import com.lotus.sametime.im.ImListener;
import com.lotus.sametime.im.ImServiceListener;
import com.lotus.sametime.im.InstantMessagingService;

/**
 * This is the SameTime IM interface.  SameTime is an instant messaging server
 * that is part of the Lotus Notes/Domino suite.  We use the IBM library for
 * accessing it.
 * 
 * @author David Rudder
 *
 * Copyright 2004 - David Rudder
 */

public class SameTimeNotificationProvider extends AbstractNotificationProvider 
	implements ImListener, ImServiceListener, DirectoryServiceListener, LoginListener, AwarenessServiceListener {
	private static STSession session;
	
	String serverName;
	
	static InstantMessagingService imService;
	DirectoryService dir;
	AwarenessService awarenessService;
	Im im;
	Vector imOpened; // We need to keep track of the open IM windows
					 // If we don't, we end up with a memory leak 
	static Directory[] directories;

	public SameTimeNotificationProvider(String serverName) {
		init (serverName);
	}
	
	public SameTimeNotificationProvider() {
	}

	public void init (Hashtable params) {
		String serverName = (String)params.get("serverName");
		init (serverName);		
	}
	
	public void init (String serverName) {
		this.serverName = serverName;
		
		imOpened = new Vector();
		
		if (!ClusteredServiceManager.getInstance().willRun("SameTime")) {
			return;
		}

		getSession ();
	}
	
	private STSession getSession () {

		if (session == null){
			String accountName = BrokerFactory.getConfigurationBroker().getStringValue("sametime.account", "");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("sametime.password", "");
			BrokerFactory.getLoggingBroker().logDebug(
				"Creating SameTime Context for " + accountName);
			
			try {
				session = new STSession ("Reliable Response " + this+" at "+System.currentTimeMillis());
				session.loadSemanticComponents();
				session.start();
				
				dir = (DirectoryService)session.getCompApi(DirectoryService.COMP_NAME);

				imService = (InstantMessagingService)session.getCompApi(InstantMessagingService.COMP_NAME);
				imService.registerImType(ImTypes.IM_TYPE_CHAT);
				imService.addImServiceListener(this);
				
				// Set up the awareness service so we can find out about network issues
				awarenessService = (AwarenessService)session.getCompApi(AwarenessService.COMP_NAME);
				awarenessService.addAwarenessServiceListener(this);
				
				BrokerFactory.getLoggingBroker().logDebug(
						"Logging into SameTime at "+serverName);
				CommunityService comm = (CommunityService)session.getCompApi(CommunityService.COMP_NAME);
				comm.addLoginListener(this);
				comm.loginByPassword(serverName, accountName, password);	
				comm.setKeepAliveRate(30000);
			} catch (DuplicateObjectException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		return session;
	}
		
	public static NotificationProvider getInstance (Hashtable params) {
		String serverName = (String)params.get("serverName");
		
		SameTimeNotificationProvider provider = new SameTimeNotificationProvider(serverName);
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
		if (!ClusteredServiceManager.getInstance().willRun("SameTime")) {
			ClusteredServiceManager.getInstance().sendNotificationToDevice
			(notification,
					"SameTime",  
					notification.getDisplayText(), 
					device.getUuid());
			return new Hashtable();
		}
		
		String summary = notification.getSubject();
		String messageText = notification.getDisplayText();
		
		String message = summary+"\n"+messageText;
		if (notification.isPersistent()) {
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
		}

		Hashtable params = new Hashtable();
		params.put ("serverName", serverName);
		
		if (device instanceof SameTimeDevice) {
			try {
				sendMessage(notification, device, message);
				if (notification != null)
					setStatusOfSend(notification, "pending");
				BrokerFactory.getLoggingBroker().logDebug("Sent SameTime message to  "+notification.getRecipient());
				return params;
			} catch (SendMessageFailedException e) {
				BrokerFactory.getLoggingBroker().logDebug(
						"SameTime message failed: " + e.getMessage());
				throw new NotificationException(NotificationException.FAILED, e.getMessage());
			}
		} else {
			BrokerFactory.getLoggingBroker().logDebug("Could not send to "+device+" because it is not a SameTime device");
			if (notification != null)
				setStatusOfSend(notification, "failed");
			throw new NotificationException(NotificationException.INTERNAL_ERROR, "Supplied device does not support SameTime");
		}
	}

	/**
	 * @param device
	 * @param message
	 * @throws SendMessageFailedException
	 */
	private void sendMessage(Notification notification, Device device, String message) throws SendMessageFailedException {
		SameTimeDevice sametimeDevice = (SameTimeDevice) device;
		sendMessage (notification, sametimeDevice.getAccount(), message);
	}

	private void sendMessage(Notification notification, String account, String message) throws SendMessageFailedException {
		// Check to make sure we're logged in
		BrokerFactory.getLoggingBroker().logDebug("Is ST available? "+awarenessService.isServiceAvailable());
		if (!awarenessService.isServiceAvailable()) {
			if (session != null) {
				session.unloadSession();
			}
			session = null;
			getSession();			
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Is ST logged in? "+((CommunityService)session.getCompApi(CommunityService.COMP_NAME)).isLoggedIn());
		if (!((CommunityService)session.getCompApi(CommunityService.COMP_NAME)).isLoggedIn()) {
			if (session != null) {
				session.unloadSession();
			}
			session = null;
			getSession();
		}
		
		if (SameTimeNotificationProvider.directories == null) {
			SameTimeNotificationProvider.directories = new Directory[0];
		}
		BrokerFactory.getLoggingBroker().logDebug("Querying "+directories.length+" directories");
		for (int i = 0; i < SameTimeNotificationProvider.directories.length; i++) {
			SameTimeNotificationProvider.directories[i].addDirectoryListener(new MessageSendingDirectoryListener
					(imService, this,  notification, message, account, SameTimeNotificationProvider.directories[i]));
			SameTimeNotificationProvider.directories[i].queryEntries(account);
		}
	}

	
	
	public void serviceAvailable(AwarenessServiceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("SameTime Service available: "+arg0);
	}

	public void serviceUnavailable(AwarenessServiceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("SameTime Service unavailable: "+arg0);
		
	}

	public void dataReceived(ImEvent arg0) {
	}
	public void imClosed(ImEvent arg0) {
	}
	public void imOpened(ImEvent arg0) {
	}
	
	public void openImFailed(ImEvent arg0) {
	}
	
	
	
	public void imReceived(ImEvent event) {
		Im im = event.getIm();
		boolean imExist = false;
		Im currentIm = null;

		for (int i = 0; i < imOpened.size(); i++) {
			currentIm = (Im) imOpened.elementAt(i);
			if (currentIm.equals(im)) {
				imExist = true;
				im = currentIm;
				break;
			}
		}

		if (!imExist) {
			imOpened.addElement(im);
			im.addImListener(this);
		}
	}
	
	public void textReceived(ImEvent event) {
		String text = event.getText();	
		String from = event.getIm().getPartner().getName();
		textReceived(text, from);
	}
	
	public void textReceived(String text, String from) {
		BrokerFactory.getLoggingBroker().logDebug("Got SameTime IM: "+text+" from "+from);
		if (text == null) {
			return;
		}
		User user = null;
		User[] usersWithSameTime = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType
			("net.reliableresponse.notification.device.SameTimeDevice");
		BrokerFactory.getLoggingBroker().logDebug("Got "+usersWithSameTime.length+" users with SameTime");
		for (int i = 0; i < usersWithSameTime.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("user w/ sametime ["+i+"]="+usersWithSameTime[i]);
			Device[] devices = usersWithSameTime[i].getDevices();
			for (int d = 0; d < devices.length; d++) {
				if (devices[d] instanceof SameTimeDevice) {
					if (((SameTimeDevice)devices[d]).getAccount().equals(from)) {
						user = usersWithSameTime[i];
					}
				}
			}
		}
			
		// We don't know who this is
		if (user == null) {
			BrokerFactory.getLoggingBroker().logInfo("Got SameTime message from unknown source, "+from+
					" - "+text);
			user = new UnknownUser();
		} else {
			String responseToAction = getResponseToAction(user, text);
			if (responseToAction != null) {
				try {
					sendMessage(null, from, responseToAction);
				} catch (SendMessageFailedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
				return;
			}			
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
			
		for (int i = 0; i < responses.size(); i++) {
			String response = (String)responses.elementAt(i);
			Pattern pattern = Pattern.compile("\\b(?i)"+response+"\\b"); 
			if (pattern.matcher(text).find()) {
				for (Notification pendingNotification: pendingNotifications) {
					if (text.indexOf(pendingNotification.getID()) >= 0) {
						NotificationSender sender = pendingNotification.getSender();
						if (sender != null) {
							BrokerFactory.getLoggingBroker().logDebug("Responding to "+
									pendingNotification+" with \""+response+"\"via SameTime message from "+from);
							sender.handleResponse(pendingNotification, user, response, "Notification confirmed by Sametime message: "+text);
							try {
								sendMessage(null, from, "Responded to notification "+pendingNotification.getID()+" with "+response);
							} catch (SendMessageFailedException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}
					}
				}
			}
		}
	}

	public void loggedIn(LoginEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("Logged into SameTime");
		dir.addDirectoryServiceListener(this);
		dir.queryAllDirectories();
	}
	public void loggedOut(LoginEvent evt) {
		// if we're logged out, force a new session
		BrokerFactory.getLoggingBroker().logInfo("SameTime logged out.");
		try {
			if (session != null) {
				session.stop();
				session.unloadSession();
			}
			BrokerFactory.getLoggingBroker().logInfo("SameTime logging in again in 120 seconds.");
			Thread.sleep(120000);
			session = null;
			getSession();
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	public void allDirectoriesQueried(DirectoryEvent arg0) {
		SameTimeNotificationProvider.directories = arg0.getDirectories();
		BrokerFactory.getLoggingBroker().logDebug("All directories queried, we have "+SameTimeNotificationProvider.directories+" directories");
		for (int i = 0; i < SameTimeNotificationProvider.directories.length; i++) {
			SameTimeNotificationProvider.directories[i].open();
		}
	}
	public void allDirectoriesQueryFailed(DirectoryEvent arg0) {
		BrokerFactory.getLoggingBroker().logError("Could not query SameTime directories");
		// TODO: This should be logged as a failure, but we don't have the notification object
	}
	
	public void serviceAvailable(DirectoryEvent arg0) {
	}

	public void serviceUnavailable(DirectoryEvent arg0) {
		// TODO: This should be logged as a failure, but we don't have the notification object
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put ("serverName", serverName);
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
		return "SameTime IM";
	}
}

class MessageSendingDirectoryListener implements DirectoryListener, ImListener {
	String text;
	String recipient;
	Directory directory;
	InstantMessagingService imService;
	SameTimeNotificationProvider parent;
	Notification notification;
	
	public MessageSendingDirectoryListener (InstantMessagingService imService, SameTimeNotificationProvider parent, Notification notification, String text, String recipient, Directory directory) {
		this.text = text;
		this.notification = notification;
		this.recipient = recipient;
		this.directory = directory;
		this.imService = imService;
		this.parent = parent;
	}
	
	
	public void directoryOpened(DirectoryEvent arg0) {
	}
	
	public void directoryOpenFailed(DirectoryEvent arg0) {
		notification.addMessage("Failed to open SameTime directory", null);
		if (notification != null)
			parent.setStatusOfSend(notification, "failed");

	}
	public void entriesQueried(DirectoryEvent event) {
		STObject[] objects = event.getEntries();
		for (int i = 0; i < objects.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("objects["+i+"]="+objects[i]);
			if (objects[i] instanceof STUser) {
				STUser user = (STUser)objects[i];
				BrokerFactory.getLoggingBroker().logDebug("user="+user.getName());
				if (user.getName().equals (recipient)) {
					Im im = imService.createIm(user, 
	                        EncLevel.ENC_LEVEL_NONE,
	                        ImTypes.IM_TYPE_CHAT);
					im.addImListener(this);
					im.addImListener(parent);
					im.open();
					return;
				}
			}
		}
		
		if (!event.isAtEnd()) {
			directory.queryEntries(false);
		} else {
			directory.removeDirectoryListener(this);
			notification.addMessage("Could not find account in SameTime directory", null);
			if (notification != null)
				parent.setStatusOfSend(notification, "failed");
		}
	}
	public void entriesQueryFailed(DirectoryEvent arg0) {
		directory.removeDirectoryListener(this);
		notification.addMessage("Could not query SameTime directory", null);
		if (notification != null)
			parent.setStatusOfSend(notification, "failed");
	}
	
	
	public void dataReceived(ImEvent arg0) {
	}
	public void imClosed(ImEvent arg0) {
	}
	public void imOpened(ImEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("Im Opened");
		STUserStatus status = arg0.getIm().getAcceptingSideStatus();
		BrokerFactory.getLoggingBroker().logDebug(recipient+"'s SameTime status = "+status);
		if ((status != null) &&
				((status.isStatus(STUserStatus.ST_USER_STATUS_DND)) || 
				(status.isStatus(STUserStatus.ST_USER_STATUS_OFFLINE)))) {
			notification.addMessage(recipient+" is not accepting SameTime messages", null);
			if (notification != null)
				parent.setStatusOfSend(notification, "failed");
		} else {
			arg0.getIm().sendText(false, text);
			directory.removeDirectoryListener(this);
			if (notification != null)
				parent.setStatusOfSend(notification, "succeeded");
		}

	}

	public void openImFailed(ImEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("Im Open Failed");
		if (notification != null) {
			notification.addMessage("Sending IM to "+recipient+" via SameTime failed.  "+recipient+" is probably not logged in.", null);
			if (notification != null)
				parent.setStatusOfSend(notification, "failed");
		}

	}
	public void textReceived(ImEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("Text Received");

	}
}