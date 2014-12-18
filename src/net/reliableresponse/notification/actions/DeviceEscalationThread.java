/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeviceEscalationThread extends Thread {
	Notification notification;

	boolean confirmed;

	public DeviceEscalationThread(Notification notification) {
		this.notification = notification;
		confirmed = false;
	}
	
	private List<Device> getOrderedDevices(User user) {
		
		List<Device> devices = user.getDevices();
		Collections.sort(devices, new Comparator<Device>() {

			@Override
			public int compare(Device o1, Device o2) {
				return (o2.getDeviceOrder()-o1.getDeviceOrder());
			}
		});
		
		return devices;
	}

	public void run() {
		Member member = notification.getRecipient();
		if (!(member instanceof User)) {
			return;
		}
		User user = (User) member;
		List<Device> devices = getOrderedDevices(user);
		BrokerFactory.getLoggingBroker().logDebug("Doing escalation for "+user.getUuid()+"'s "+devices.size()+" devices");

		for (Device device: devices) {
			BrokerFactory.getLoggingBroker().logDebug("Sending to "+user.getUuid()+"'s device "+device.getUuid());
			List<Device> deviceList = new ArrayList<Device>();
			deviceList.add(device);
			Notification individualNotification = new Notification(notification.getUuid(), user, deviceList, notification.getSender(), notification.getSubject(), new NotificationMessage[0]);
			try {
				BrokerFactory.getNotificationBroker().setNotificationStatus(individualNotification, "pending");
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logWarn(anyExc);
			}
			individualNotification.setAutocommit(false);
			individualNotification.setPriority(notification.getPriority());

			try {
				List<NotificationProvider> originalProviders = notification.getNotificationProviders();
				if (originalProviders != null) {
					for (NotificationProvider originalProvider : originalProviders) {
						individualNotification.addNotificationProvider(originalProvider);
					}
				}

				individualNotification.addOption("Confirm");
				individualNotification.addOption("Pass");
				individualNotification.setAutocommit(true);
				individualNotification.setPersistent(notification.isPersistent());
				SendNotification.getInstance().doSend(individualNotification);

				int totalTime = 0;
				if (User.DEVICE_ESCALATION_STATIC_TIMING.equals(user.getDeviceEscalationPolicy())) {
					totalTime = user.getDeviceEscalationTime()*1000*60;
				} else if (User.DEVICE_ESCALATION_PROPORTIONAL_TIMING.equals(user.getDeviceEscalationPolicy())) {
					BrokerFactory.getLoggingBroker().logWarn("Proportional device escalation not implemented");
					totalTime = 2 * 1000 *60;
				} else {
					totalTime = 0;
				}
				
				BrokerFactory.getLoggingBroker().logDebug("Sleeping for "+totalTime+" millis");
				int spentTime = 0;
				while ((spentTime < totalTime) && (!isConfirmed())) {
					Thread.sleep(1000);
					spentTime += 1000;
					checkConfirmed();
				}
				
				if (isConfirmed()) return;
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError("Could not send escalation notification: " + e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException intExc) {
				BrokerFactory.getLoggingBroker().logError("Could not send escalation notification: " + intExc.getMessage());
				intExc.printStackTrace();
			}
		}
	}

	public void confirm(Member confirmer) {
		confirmed = true;
		// getNotification().setStatus(Notification.CONFIRMED);
	}

	public void checkConfirmed() {
		Notification notification = getNotification();
		if (notification.getStatus() == Notification.CONFIRMED) {
			confirm(notification.getRecipient());
			return;
		}

	}

	/**
	 * 
	 * @return Whether this escalation notification has been confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	public Notification getNotification() {
		return notification;
	}


}
