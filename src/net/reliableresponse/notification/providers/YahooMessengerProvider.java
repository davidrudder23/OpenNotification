/*
 * Created on Mar 29, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import org.openymsg.network.FireEvent;
import org.openymsg.network.Session;
import org.openymsg.network.SessionState;
import org.openymsg.network.YahooUser;
import org.openymsg.network.event.SessionChatEvent;
import org.openymsg.network.event.SessionConferenceEvent;
import org.openymsg.network.event.SessionErrorEvent;
import org.openymsg.network.event.SessionEvent;
import org.openymsg.network.event.SessionExceptionEvent;
import org.openymsg.network.event.SessionFileTransferEvent;
import org.openymsg.network.event.SessionFriendEvent;
import org.openymsg.network.event.SessionGroupEvent;
import org.openymsg.network.event.SessionListener;
import org.openymsg.network.event.SessionNewMailEvent;
import org.openymsg.network.event.SessionNotifyEvent;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.YahooMessengerDevice;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class YahooMessengerProvider extends AbstractNotificationProvider implements SessionListener{
	String accountName;
	String password;
	static Session session;
	static int badCount;
	static long lastBad;
	static {
		session = null;
	}
	
	public YahooMessengerProvider(String accountName, String password) {
		init (accountName, password);	
	}
	public YahooMessengerProvider() {
	}
	
	public void init(Hashtable params) {
		String accountName = (String)params.get("accountName");
		String password = (String)params.get("password");		
		
		init (accountName, password);
	}
	
	public void init(String accountName, String password) {
		BrokerFactory.getLoggingBroker().logDebug("Yahoo session="+session);
		this.accountName = accountName;
		this.password = password;
		
		if (session == null) {
			YahooMessengerProvider.badCount++;
			if (YahooMessengerProvider.badCount == 5) {
				YahooMessengerProvider.lastBad = System.currentTimeMillis();
			}
			if (YahooMessengerProvider.badCount >= 5) {
				if ((System.currentTimeMillis() - YahooMessengerProvider.lastBad) > (1000*60*15)) {
					YahooMessengerProvider.badCount = 0;
				} else {
					BrokerFactory.getLoggingBroker().logWarn("Failed login to Yahoo "+badCount+" times.  Yahoo disabled for 15 minutes");
					return;
				}
			}
			BrokerFactory.getLoggingBroker().logDebug("Signing onto Yahoo Messenger as "+accountName);
			BrokerFactory.getLoggingBroker().logDebug("Bad Count = "+YahooMessengerProvider.badCount);
			session = new Session();
		
			session.addSessionListener(this);
			try {
				session.login(accountName, password);
			} catch (Exception e) {
				YahooMessengerProvider.lastBad = System.currentTimeMillis();
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		BrokerFactory.getLoggingBroker().logDebug("Session Status="+session.getSessionStatus().toString());
		if (session.getSessionStatus().compareTo(SessionState.LOGGED_ON)!=0) {
			try {
				session.login(accountName, password);
			} catch (Exception e) {
				YahooMessengerProvider.lastBad = System.currentTimeMillis();
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
		User user = (User)notification.getRecipient();
		NotificationSender sender = notification.getSender(); 
		String summary = notification.getSubject();
		Vector options = notification.getOptions();
		String message = notification.getDisplayText();
		String subject = notification.getSubject();
		
		message = "Notification from Reliable Response Notification, ID="+notification.getID()+"\n"+
					subject+"\n"+
					message;
		
		if (notification.isPersistent()) {

			String[] responses = notification.getSender()
					.getAvailableResponses(notification);
			if (responses.length > 0) {
				message += "\n\nYou may respond with:\n";
				for (int r = 0; r < responses.length; r++) {
					message += "\t \"" + responses[r] + " "
							+ notification.getID() + "\"\n";
				}
			}
		}

		// We need to add this message if it's an escalation thread
		// "To pass, please respond with \"Pass "+notification.getID()+"\"\n";
		
		Hashtable params = new Hashtable ();
		params.put ("accountName", accountName);
		params.put ("password", password);
		
		if (device instanceof YahooMessengerDevice) {
			YahooMessengerDevice yahooDevice = (YahooMessengerDevice)device;
			String accountToSendTo =yahooDevice.getAccount();
			BrokerFactory.getLoggingBroker().logDebug("Sending Yahoo message to "+accountToSendTo);
			try {
				BrokerFactory.getLoggingBroker().logDebug("Yahoo Session = "+session);
				if (session == null) {
					init (accountName, password);
				}
				if (session == null) {
					throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Not logged into Yahoo");
				}
				
				YahooUser yahooUser = session.getUser(accountToSendTo);
				if (yahooUser == null) {
					BrokerFactory.getLoggingBroker().logDebug("Adding "+accountToSendTo+" to buddies list");
					session.addFriend(accountToSendTo, "friends");
					//session.refreshFriends();
					yahooUser = session.getUser(accountToSendTo);
				}
				BrokerFactory.getLoggingBroker().logDebug("Yahoo user="+yahooUser);
				BrokerFactory.getLoggingBroker().logDebug("Use when offline="+yahooDevice.useWhenOffline());
				if ((yahooDevice.useWhenOffline()) || (yahooUser == null) || (yahooUser.isLoggedIn())) {
					session.sendMessage(accountToSendTo, message);
				} else {
					notification.addMessage("Did not send a Yahoo IM to "+notification.getRecipient()+" because the user's Yahoo account is offline", null);
				}
			} catch (IllegalStateException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				throw new NotificationException(NotificationException.FAILED, e.getMessage());
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				throw new NotificationException(NotificationException.FAILED, e.getMessage());
			}
			return params;
		} else {
			return params;
		}
	}
	
	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable ();
		params.put ("accountName", accountName);
		params.put ("password", password);
		return params;
	}
	
	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#cancelPage(net.reliableresponse.notification.Notification)
	 */
	public boolean cancelPage(Notification page) {
		return false;
	}

	public boolean isConfirmed(Notification page) {
		return false;
	}
	
	public boolean isPassed(Notification page) {
		return false;
	}
	
	public String getName() {
		return "Yahoo Messenger";
	}
	
	public void buzzReceived(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("buzz received: "+arg0);
	}
	
	public void chatConnectionClosed(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("chat connection closed received: "+arg0);

	}
	public void chatLogoffReceived(SessionChatEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("chat logoff received: "+arg0);

	}
	public void chatLogonReceived(SessionChatEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("chat logon received: "+arg0);

	}
	public void chatMessageReceived(SessionChatEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("chat message received: "+arg0);
		messageReceived(arg0);

	}
	public void chatUserUpdateReceived(SessionChatEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("chat user updatereceived: "+arg0);

	}
	public void conferenceInviteDeclinedReceived(SessionConferenceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("conference invite declined received: "+arg0);

	}
	public void conferenceInviteReceived(SessionConferenceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("conference invite received: "+arg0);

	}
	public void conferenceLogoffReceived(SessionConferenceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("conference logoff received: "+arg0);

	}
	public void conferenceLogonReceived(SessionConferenceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("conference logon received: "+arg0);

	}
	public void conferenceMessageReceived(SessionConferenceEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("conference message received: "+arg0);

	}
	public void connectionClosed(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logInfo("Yahoo connection closed, will reestablish.");
		session = null;
		//init(accountName, password);
	}
	public void contactRejectionReceived(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logWarn("contact rejection received: "+arg0);

	}
	public void contactRequestReceived(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("contact request received: "+arg0);

	}
	public void errorPacketReceived(SessionErrorEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("error package received: "+arg0);

	}
	public void fileTransferReceived(SessionFileTransferEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("fiule transfer received: "+arg0);

	}
	public void friendAddedReceived(SessionFriendEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("friend added received: "+arg0);

	}
	public void friendRemovedReceived(SessionFriendEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("friend removed received: "+arg0);

	}
	public void friendsUpdateReceived(SessionFriendEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("friend update received: "+arg0);

	}
	public void inputExceptionThrown(SessionExceptionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("input exception received: "+arg0);

	}
	public void listReceived(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("list received: "+arg0);

	}
	public void messageReceived(SessionEvent arg0) {
		
		String buddy = arg0.getFrom();
		String message = arg0.getMessage();
		
		User user = null;

		BrokerFactory.getLoggingBroker().logDebug("got Yahoo message: "+message);
		// Find out who sent the messahe
		User[] usersWithYahoo = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType
			("net.reliableresponse.notification.device.YahooMessengerDevice");
		BrokerFactory.getLoggingBroker().logDebug("Got "+usersWithYahoo.length+" users with Yahoo");
		for (int i = 0; i < usersWithYahoo.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("user w/ Yahoo ["+i+"]="+usersWithYahoo[i]);
			Device[] devices = usersWithYahoo[i].getDevices();
			for (int d = 0; d < devices.length; d++) {
				if (devices[d] instanceof YahooMessengerDevice) {
					if (((YahooMessengerDevice)devices[d]).getAccount().equals(buddy)) {
						user = usersWithYahoo[i];
					}
				}
			}
		}
		
		// We don't know who this is
		if (user == null) {
			BrokerFactory.getLoggingBroker().logInfo("Got Yahoo message from unknown source, "+buddy+
					" - "+message);
			return;
		}

		Notification[] pendingNotifications = BrokerFactory.getNotificationBroker().getNotificationsSince(8640000L);
		Vector responses = new Vector();
		for (int i = 0; i < pendingNotifications.length; i++) {
			NotificationSender sender = pendingNotifications[i].getSender();
			String[] respArray = sender.getAvailableResponses(pendingNotifications[i]);
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
				for (int p = 0; p < pendingNotifications.length; p++) {
					if (message.indexOf(pendingNotifications[p].getID()) >= 0) {
						notifFound = true;
						NotificationSender sender = pendingNotifications[p].getSender();
						if (sender != null) {
							BrokerFactory.getLoggingBroker().logDebug("Responding to "+
									pendingNotifications[p]+" with \""+response+"\"via Yahoo message from "+buddy);
							sender.handleResponse(pendingNotifications[p], user, response, "Notification confirmed by Yahoo message: "+message);
							try {
								if (session == null) {
									init (accountName, password);
								}
								session.sendMessage(buddy, "Responded to notification "+pendingNotifications[p].getID()+" with \""+response+"\"");
							} catch (IllegalStateException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							} catch (IOException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}
					}
				}
			}
		}
		
		if (!notifFound) {
			try {
				if (session == null) {
					init (accountName, password);
				}
				session.sendMessage(buddy, "Notification not found, please try again");
			} catch (IllegalStateException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
	 	}

	}
	public void newMailReceived(SessionNewMailEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("new mail received: "+arg0);
	}
	public void notifyReceived(SessionNotifyEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("notify received: "+arg0);

	}
	public void offlineMessageReceived(SessionEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("offline message received: "+arg0);

	}
	public void dispatch(FireEvent arg0) {
		BrokerFactory.getLoggingBroker().logDebug("dispatch: "+arg0);
		
        final SessionEvent ev = arg0.getEvent();

        switch (arg0.getType()) {
        case LOGOFF:
                connectionClosed(ev);
                break;
        case Y6_STATUS_UPDATE:
                friendsUpdateReceived((SessionFriendEvent) ev);
                break;
        case MESSAGE:
                messageReceived(ev);
                break;
        case X_OFFLINE:
                offlineMessageReceived(ev);
                break;
        case NEWMAIL:
                newMailReceived((SessionNewMailEvent) ev);
                break;
        case CONTACTNEW:
                contactRequestReceived(ev);
                break;
        case CONFDECLINE:
                conferenceInviteDeclinedReceived((SessionConferenceEvent) ev);
                break;
        case CONFINVITE:
                conferenceInviteReceived((SessionConferenceEvent) ev);
                break;
        case CONFLOGON:
                conferenceLogonReceived((SessionConferenceEvent) ev);
                break;
        case CONFLOGOFF:
                conferenceLogoffReceived((SessionConferenceEvent) ev);
                break;
        case CONFMSG:
                conferenceMessageReceived((SessionConferenceEvent) ev);
                break;
        case FILETRANSFER:
                fileTransferReceived((SessionFileTransferEvent) ev);
                break;
        case NOTIFY:
                notifyReceived((SessionNotifyEvent) ev);
                break;
        case LIST:
                listReceived(ev);
                break;
        case FRIENDADD:
                friendAddedReceived((SessionFriendEvent) ev);
                break;
        case FRIENDREMOVE:
                friendRemovedReceived((SessionFriendEvent) ev);
                break;
        case CONTACTREJECT:
                contactRejectionReceived(ev);
                break;
        case CHATDISCONNECT:
                chatConnectionClosed(ev);
                break;
        case CHATMSG:
                chatMessageReceived((SessionChatEvent) ev);
                break;
        case X_CHATUPDATE:
                chatUserUpdateReceived((SessionChatEvent) ev);
                break;
        case X_ERROR:
                errorPacketReceived((SessionErrorEvent) ev);
                break;
        case X_EXCEPTION:
                inputExceptionThrown((SessionExceptionEvent) ev);
                break;
        case X_BUZZ:
                buzzReceived(ev);
                break;
        default:
                throw new IllegalArgumentException(
                                "Don't know how to handle service type '"
                                                + arg0.getType() + "'");
        }

		
	}
	
	 
}
