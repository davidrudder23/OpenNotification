/*
 * Created on Aug 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.providers;

import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.sasl.SASLMechanism;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.JabberDevice;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 *         Copyright 2004 - David Rudder
 */
public class JabberNotificationProvider extends AbstractNotificationProvider
		implements PacketListener, PacketFilter {
	static XMPPConnection session = null;
	String serverName, accountName, password;
	String recipient;

	public JabberNotificationProvider(String serverName, String accountName,
			String password) {
		init(serverName, accountName, password);
	}

	public JabberNotificationProvider() {
	}

	public void init(Hashtable params) {
		String serverName = BrokerFactory.getConfigurationBroker()
				.getStringValue("jabber.server");
		String accountName = BrokerFactory.getConfigurationBroker()
				.getStringValue("jabber.account");
		String password = BrokerFactory.getConfigurationBroker()
				.getStringValue("jabber.password");

		init(serverName, accountName, password);
	}

	public void init(String serverName, String accountName, String password) {
		this.serverName = serverName;
		this.accountName = accountName;
		this.password = password;

		try {
			getSession();
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	private XMPPConnection getSession() throws NotificationException {
		if (session != null) {
			if (!session.isConnected()) {
				BrokerFactory
						.getLoggingBroker()
						.logInfo(
								"Jabber provider is not connected, but session is not null");
				session = null;
			}
		}
		if (session == null) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Creating Jabber Context for " + accountName + "/"
							+ password + " on server " + serverName);
			
			/*
			try {
				boolean tls = BrokerFactory.getConfigurationBroker()
						.getBooleanValue("jabber.tls", false);
				int port = BrokerFactory.getConfigurationBroker().getIntValue(
						"jabber.port", 5222);
				SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				ConnectionConfiguration config = new ConnectionConfiguration(
						serverName, port, "gmail.com");
				config.setSASLAuthenticationEnabled(true);
				session = new XMPPConnection(config);
				session.connect();
				session.login(accountName, password);
				if (session == null) {
					throw new NotificationException(
							NotificationException.FAILED,
							"Could not login to Jabber server");
				}
				session.addPacketListener(this, this);
				Presence presence = new Presence(Type.available);
				presence.setStatus("Notifying and Acknowledging");
				presence.setPriority(1);
				session.sendPacket(presence);
			} catch (XMPPException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				throw new NotificationException(NotificationException.FAILED, e
						.getMessage());
			}
*/
		}

		BrokerFactory.getLoggingBroker()
				.logDebug("Jabber Session = " + session);
		BrokerFactory.getLoggingBroker().logDebug(
				"Jabber Session ConnectionID = " + session.getConnectionID());
		BrokerFactory.getLoggingBroker().logDebug(
				"Jabber Session is anon= " + session.isAnonymous());
		return session;
	}

	public static NotificationProvider getInstance(Hashtable params) {
		String serverName = (String) params.get("serverName");
		String accountName = (String) params.get("accountName");
		String password = (String) params.get("password");
		String recipient = (String) params.get("recipient");

		JabberNotificationProvider provider = new JabberNotificationProvider(
				serverName, accountName, password);
		provider.setRecipient(recipient);
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.reliableresponse.notification.providers.NotificationProvider#cancelPage
	 * (net.reliableresponse.notification.Notification)
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
	 * @see
	 * net.reliableresponse.notification.providers.NotificationProvider#sendPage
	 * (net.reliableresponse.notification.usermgmt.User,
	 * net.reliableresponse.notification.device.Device, java.lang.String,
	 * java.lang.String, java.lang.String, java.util.Vector)
	 */
	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		if (!ClusteredServiceManager.getInstance().willRun("Jabber")) {
			ClusteredServiceManager.getInstance().sendNotificationToDevice(
					notification, "Jabber", notification.getDisplayText(),
					device.getUuid());
			return new Hashtable();
		}
		User user = (User) notification.getRecipient();
		NotificationSender sender = notification.getSender();
		String summary = notification.getSubject();
		Vector options = notification.getOptions();
		String messageText = notification.getDisplayText();

		String message = summary + "\n" + messageText;
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

		if (device instanceof JabberDevice) {
			sendMessage(device, message);
			return params;
		} else {
			throw new NotificationException(
					NotificationException.INTERNAL_ERROR,
					"Supplied device does not support Jabber");
		}
	}

	/**
	 * @param device
	 * @param message
	 * @throws SendMessageFailedException
	 */
	private void sendMessage(Device device, String message)
			throws NotificationException {
		JabberDevice jabberDevice = (JabberDevice) device;
		sendMessage(jabberDevice.getJID(), message);
	}

	private void sendMessage(String jid, String message)
			throws NotificationException {
		BrokerFactory.getLoggingBroker().logDebug(
				"Sending Jabber message to " + jid + ": " + message);
		Message msg = new Message(jid, Message.Type.CHAT);
		msg.setBody(message);
		getSession().sendPacket(msg);
	}

	public void processPacket(Packet packet) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Got Jabber Packet: " + packet);
		if (packet instanceof Message) {
			Message messagePacket = (Message) packet;

			if (messagePacket.getType() == Message.Type.ERROR) {
				BrokerFactory.getLoggingBroker().logWarn(
						"Got error packet from Jabber, resetting session - "
								+ messagePacket.getError());
				session.close();
				session = null;
				return;
			}
			String message = messagePacket.getBody();
			String from = messagePacket.getFrom();
			if (from.indexOf("/") >= 0) {
				from = from.substring(0, from.lastIndexOf("/"));
			}
			BrokerFactory.getLoggingBroker().logInfo(
					"Private Message received from " + from + ": " + message);

			User user = null;
			User[] usersWithJabber = BrokerFactory
					.getUserMgmtBroker()
					.getUsersWithDeviceType(
							"net.reliableresponse.notification.device.JabberDevice");
			BrokerFactory.getLoggingBroker().logDebug(
					"Got " + usersWithJabber.length + " users with Jabber");
			for (int i = 0; i < usersWithJabber.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug(
						"user w/ jabber [" + i + "]=" + usersWithJabber[i]);
				Device[] devices = usersWithJabber[i].getDevices();
				for (int d = 0; d < devices.length; d++) {
					if (devices[d] instanceof JabberDevice) {
						BrokerFactory.getLoggingBroker().logDebug(
								"User's JID = "
										+ ((JabberDevice) devices[d]).getJID());
						if (((JabberDevice) devices[d]).getJID().equals(from)) {
							user = usersWithJabber[i];
						}
					}
				}
			}

			// We don't know who this is
			if (user == null) {
				BrokerFactory.getLoggingBroker().logInfo(
						"Got Jabber message from unknown source, " + from
								+ " - " + message);
				user = new UnknownUser();
				user.setFirstName(from);
				user.setLastName("");
			}
			List<Notification> pendingNotifications = BrokerFactory.getNotificationBroker().getNotificationsSince(8640000L);
			Vector responses = new Vector();
			for (Notification pendingNotification: pendingNotifications) {
				NotificationSender sender = pendingNotification.getSender();
				String[] respArray = sender
						.getAvailableResponses(pendingNotification);
				for (int r = 0; r < respArray.length; r++) {
					if (!responses.contains(respArray[r])) {
						responses.addElement(respArray[r]);
					}
				}
			}

			boolean notifFound = false;
			for (int i = 0; i < responses.size(); i++) {
				String response = (String) responses.elementAt(i);
				Pattern pattern = Pattern.compile("\\b(?i)" + response + "\\b");
				if (pattern.matcher(message).find()) {
					for (Notification pendingNotification: pendingNotifications) {
						if (message.indexOf(pendingNotification.getID()) >= 0) {
							notifFound = true;
							NotificationSender sender = pendingNotification.getSender();
							if (sender != null) {
								BrokerFactory.getLoggingBroker().logDebug("Responding to "
												+ pendingNotification
												+ " with \"" + response
												+ "\"via Jabber message from "
												+ from);
								sender.handleResponse(pendingNotification,
										user, response,
										"Notification confirmed by Jabber message: "
												+ message);
								try {
									sendMessage(from,
											"Responded to notification " + pendingNotification.getID() + " with " + response);
								} catch (NotificationException e) {
									BrokerFactory.getLoggingBroker()
											.logError(e);
								}
							}
						}
					}
				}
			}

			if (!notifFound) {
				if (message.equalsIgnoreCase("list")) {
					List<Notification> activeNotifs = BrokerFactory.getNotificationBroker().getMembersUnconfirmedNotifications(user);
					StringBuffer response = new StringBuffer();
					for (Notification activeNotif: activeNotifs) {
						response.append(activeNotif.getStatusAsString());
						response.append(": ");
						response.append(activeNotif.getSubject());
						response.append("\n");
					}
					try {
						sendMessage(from, response.toString());
					} catch (NotificationException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}

				} else {
					try {
						sendMessage(from, "Notification not found");
					} catch (NotificationException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
			}
		}

	}

	public boolean accept(Packet arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put("serverName", serverName);
		params.put("accountName", accountName);
		params.put("password", password);
		params.put("recipient", "");
		return params;
	}

	public boolean isConfirmed(Notification page) {
		return false;
	}

	public boolean isPassed(Notification page) {
		return false;
	}

	public static void clearSessions() {
		if (session != null) {
			session.close();
		}
		session = null;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getName() {
		return "Jabber IM";
	}

	public static void main(String[] args) throws Exception {

		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		JabberNotificationProvider provider = new JabberNotificationProvider();
		provider.init(new Hashtable());
		provider.sendMessage("drig23@gmail.com", "this is a test");

		while (true) {
			Thread.sleep(1000);
		}
	}

}