/*
 * Created on May 16, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.actions;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.ProviderStatusLoop;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SendNotification {
	private static SendNotification sendPage;

	public static SendNotification getInstance() {
		if (sendPage == null) {
			sendPage = new SendNotification();
		}
		return sendPage;
	}

	private void doSend(Notification notification, Member member)
			throws NotificationException {
		if (notification.isPersistent()) {
			try {
				BrokerFactory.getNotificationBroker().addNotification(
						notification);
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logWarn(anyExc);
			}
			try {
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						notification, "pending");
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logWarn(anyExc);
			}
		}
		if (member.getType() == Member.USER) {
			User user = (User) member;
			
			Device[] devices = notification.getDevices();
			
			BrokerFactory.getLoggingBroker().logDebug("Sending notification to "+member+" with "+devices.length+" devices");

			for (int i = 0; i < devices.length; i++) {
				if (devices[i].willSend(user, notification.getUltimateParent().getPriority(), notification)) {
					NotificationProvider provider = devices[i]
							.getNotificationProvider();
					Hashtable<String, String> params = new Hashtable<String, String>();
					try {
						try {
							BrokerFactory.getLoggingBroker().logInfo(
									"Sending notification to " + user + " via "
											+ provider);
						} catch (Exception anyExc) {
							BrokerFactory.getLoggingBroker().logWarn(anyExc);
						}
						notification.addNotificationProvider(provider);
						provider.setStatusOfSend(notification, "succeeded");
						params = provider.sendNotification(notification,
								devices[i]);
						if (notification.isPersistent()) {
							ProviderStatusLoop.getInstance().addNotification(notification);
							BrokerFactory.getNotificationBroker()
									.addProviderInformation(notification,
											provider, params, "succeeded");
							BrokerFactory.getNotificationLoggingBroker()
									.logNotification(notification,
											notification.getRecipient(),
											devices[i], "succeeded");
						}
						notification.addMessage("Notification delivered to "
								+ notification.getRecipient().toString()
								+ " via device " + devices[i].toString(), null, NotificationMessage.NOTIFICATION_CONTENT_TYPE);
					} catch (NotificationException nfExc) {
						// Check to see if it's a temporary failure
						if ((nfExc.getCode() >= 300) && (nfExc.getCode()<400)) {
							FailedNotificationThread.getInstance().addNotification(new FailedNotification(notification, devices[i], provider, user));
							SendNotification.flagError(notification, devices[i], provider, nfExc, "temporarily failed");
						} else {
							SendNotification.flagError(notification, devices[i], provider, nfExc, "failed");
						}						
					} catch (Exception anyExc) {
						SendNotification.flagError(notification, devices[i], provider, anyExc, "failed");
					}
				} else {
					BrokerFactory.getLoggingBroker().logInfo(
							"Not sending notification to " + user + " via "
									+ devices[i]
									+ " because the priority blocks it");
					
					notification.addMessage(
							"Not sending notification to " + user + " via "
							+ devices[i]
							+ " because the priority blocks it", null);
				}
			}
			BrokerFactory.getNotificationBroker().setNotificationStatus(
					notification, "sent");
		} else {

			// Recurse into the group
			Notification sendPage = new Notification(notification.getUuid(),
					member, notification.getSender(),
					notification.getSubject(), notification.getMessages());
			int priority = notification.getPriority();
			sendPage.setPriority(priority);
			sendPage.setRequireConfirmation(notification
					.isRequireConfirmation());
			sendPage.setIncludeTimestamp(notification.isIncludeTimestamp());

			for (int i = 0; i < notification.getOptions().size(); i++) {
				sendPage.addOption((String) notification.getOptions()
						.elementAt(i));
			}
			doSend(sendPage);
		}
	}

	/**
	 * @param notification
	 * @param devices
	 * @param i
	 * @param provider
	 * @param anyExc
	 */
	public static void flagError(Notification notification, Device device, NotificationProvider provider, Exception anyExc, String status) {
		Hashtable<String, String> params;
		if (notification.isPersistent()) {
			BrokerFactory.getNotificationLoggingBroker()
					.logNotification(notification,
							notification.getRecipient(),
							device, status);
			BrokerFactory.getLoggingBroker()
					.logAction(
							"Notification not sent to "
									+ provider + " because "
									+ anyExc.getMessage());
			params = provider.getParameters(notification,
					device);
			BrokerFactory.getNotificationBroker()
					.addProviderInformation(notification,
							provider, params, status);
		}
		provider.setStatusOfSend(notification, status);
		anyExc.printStackTrace();
		NotificationMessage errorMessage = new NotificationMessage(
				"ERROR Sending to " + device + "\n\n"
						+ anyExc.getMessage(), "system",
				new Date());
		notification.addMessage(errorMessage);
	}

	/**
	 * Tells this class to send the notification
	 * 
	 * @return The IDs of the messages sent
	 */
	public void doSend(Notification notification) throws NotificationException {
		Member recipient = notification.getRecipient();

		if (!EventStormManager.getInstance().willSend(notification)) {
			return;
		}
		if (recipient.getType() == Member.BROADCAST) {
			if (notification.isPersistent()) {
				BrokerFactory.getNotificationBroker().addNotification(notification);
			}
			Member[] members = ((BroadcastGroup) recipient).getMembers();

			for (int memNum = 0; memNum < members.length; memNum++) {
				Notification directNotification = new Notification(notification
						.getUuid(), members[memNum], notification.getSender(),
						notification.getSubject(), notification.getMessages());
				directNotification.setPersistent(notification.isPersistent());
				int priority = notification.getPriority();
				if (members[memNum].getType() == Member.USER) {
					priority = BrokerFactory.getUserMgmtBroker()
							.getPriorityOfGroup((User) members[memNum],
									(Group) recipient);
				}
				directNotification.setPriority(priority);
				doSend(directNotification, members[memNum]);
			}
		} else if (recipient.getType() == Member.ESCALATION) {
			if (notification.isPersistent()) {
				BrokerFactory.getNotificationBroker().addNotification(
						notification);
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						notification, "sent");
				EscalationThreadManager.getInstance().addEscalation(
						notification);
			} else {
				doSend(notification, recipient);
			}
		} else if (recipient.getType() == Member.ONCALL) {
			if (notification.isPersistent()) {
				BrokerFactory.getNotificationBroker().addNotification(notification);
			}
			Member[] members = ((OnCallGroup)recipient).getOnCallMembers(new Date());	

			for (int memNum = 0; memNum < members.length; memNum++) {
				Notification directNotification = new Notification(notification
						.getUuid(), members[memNum], notification.getSender(),
						notification.getSubject(), notification.getMessages());
				directNotification.setPersistent(notification.isPersistent());
				int priority = notification.getPriority();
				if (members[memNum].getType() == Member.USER) {
					priority = BrokerFactory.getUserMgmtBroker()
							.getPriorityOfGroup((User) members[memNum],
									(Group) recipient);
				}
				directNotification.setPriority(priority);
				doSend(directNotification, members[memNum]);
			}
		} else if (recipient.getType() == Member.USER) {
			doSend(notification, recipient);
		}
	}
	
	public static void addFailedNotification (Notification notification, Device device, NotificationProvider provider, User user) {
		FailedNotification failedNotif = new FailedNotification(notification, device, provider, user);
		FailedNotificationThread.getInstance().addNotification(failedNotif);
	}

}

class FailedNotificationThread extends Thread {
	private Vector<FailedNotification> failedNotifications;
	
	private static FailedNotificationThread instance = null;
	
	private FailedNotificationThread() {
		failedNotifications = new Vector<FailedNotification>();		
	}
	
	public void addNotification (FailedNotification notification) {
		BrokerFactory.getLoggingBroker().logDebug("Adding failed notification to retry thread: "+notification.getNotification().getUuid());
		failedNotifications.addElement(notification);
	}
	
	public static FailedNotificationThread getInstance() {
		if (instance == null) {
			instance = new FailedNotificationThread();
			instance.start();
		}
		
		return instance;
	}
	
	public void run() {
		while (true) {
			// Run once a minute
			try {
				Thread.sleep(60*1000);
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logWarn(e);
			}
			
			BrokerFactory.getLoggingBroker().logDebug("Running Failed Notification thread on "+failedNotifications.size()+" notifs");
			for (int n = 0; n < failedNotifications.size(); n++) {
				FailedNotification failedNotification = (FailedNotification)failedNotifications.elementAt(n);
				Device device = failedNotification.getDevice();
				Notification oldNotification = failedNotification.getNotification();
				Vector<Device> deviceToSendTo = new Vector<Device>();
				deviceToSendTo.addElement(device);
				Notification notification = new Notification(oldNotification.getUuid(), 
											failedNotification.getUser(),
											deviceToSendTo,
											oldNotification.getSender(), 
											oldNotification.getSubject(), 
											oldNotification.getMessages()[0].getMessage());
				notification.setPersistent(false);
				
				BrokerFactory.getLoggingBroker().logDebug("Resending "+notification);
				NotificationProvider provider = failedNotification.getProvider();
				User user = failedNotification.getUser();
				
				if (device.willSend(user, notification.getPriority(), notification)) {
					Hashtable<String, String> params = new Hashtable<String, String>();
					try {
						try {
							BrokerFactory.getLoggingBroker().logInfo(
								"Sending notification to " + user + " via "
								+ provider);
						} catch (Exception anyExc) {
							BrokerFactory.getLoggingBroker().logWarn(anyExc);
						}
						params = provider.sendNotification(notification,
							device);
						provider.setStatusOfSend(notification, "succeeded");
						if (notification.isPersistent()) {
							ProviderStatusLoop.getInstance().addNotification(notification);
							BrokerFactory.getNotificationBroker()
								.addProviderInformation(notification,
										provider, params, "succeeded");
							BrokerFactory.getNotificationLoggingBroker()
								.logNotification(notification,
										notification.getRecipient(),
										device, "succeeded");
						}
						notification.addMessage("Notification delivered to "
							+ notification.getRecipient().toString()
							+ " via device " + device.toString(), null);
						failedNotifications.removeElementAt(n);
					} catch (NotificationException nfExc) {
                                               // Check to see if it's a temporary failure
                                                if ((nfExc.getCode() >= 300) && (nfExc.getCode()<400)) {
                                                        SendNotification.flagError(notification, device, provider, nfExc, "temporarily failed");
                                                } else {
                                                        SendNotification.flagError(notification, device, provider, nfExc, "failed");
							failedNotifications.removeElementAt(n);
						}
					} catch (Exception anyExc) {
						SendNotification.flagError(notification, device, provider, anyExc, "failed");					
					}
				} else {
					BrokerFactory.getLoggingBroker().logInfo(
						"Not sending notification to " + user + " via "
						+ device
						+ " because the priority blocks it");
				}
			}
		}
	}
}

class FailedNotification {
	long time;
	Notification notification;
	Device device;
	NotificationProvider provider;
	int retryCount;
	User user;
	
	public FailedNotification (Notification notification, Device device, NotificationProvider provider, User user) {
		this.time = System.currentTimeMillis();
		this.notification = notification;
		this.device = device;
		this.provider = provider;
		this.retryCount = 0;
		this.user = user;
	}
	
	public boolean doRetry() {
		return doRetry(BrokerFactory.getConfigurationBroker().getIntValue("notification.retryseconds", 10*60));
	}
	
	public boolean doRetry(int seconds) {
		long now = System.currentTimeMillis();
		long difference = now - time;
		difference = difference/1000;
		if (difference > seconds) {
			retryCount++;
			return true;
		}
		return false;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public NotificationProvider getProvider() {
		return provider;
	}

	public void setProvider(NotificationProvider provider) {
		this.provider = provider;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
