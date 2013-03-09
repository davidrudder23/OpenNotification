/*
 * Created on Dec 11, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.actions;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;

public class EventStormManager {

	private static EventStormManager instance;
	int maxNotifs;
	int maxTime;
	
	public static EventStormManager getInstance() {
	
		if (instance == null) {
			instance = new EventStormManager();
		}
		return instance;
	}
	
	private EventStormManager() {
		maxNotifs = BrokerFactory.getConfigurationBroker().getIntValue("transmit.limit.num", 50);
		maxTime = BrokerFactory.getConfigurationBroker().getIntValue("transmit.limit.seconds", 600)*1000;
		
	}
	
	public boolean willSend (Notification notification) {
		if (notification.isReleased()) {
			return true;
		}
		
		if (notification.isPersistent()) {
			// This section is for event storm protection
			int numNotifsSent = BrokerFactory.getNotificationBroker().countPastNotifs(notification.getRecipient(), maxTime);
			if (numNotifsSent >= maxNotifs) {
				BrokerFactory.getLoggingBroker().logDebug("Notification "+notification.getUuid()+
					" is onhold because the number of messages exceeds the storm protection threshold");
				BrokerFactory.getNotificationBroker().addNotification(notification);
				notification.setAutocommit(true);
				notification.addMessage("Message on hold due to event storm protection", null);
				notification.setStatus(Notification.ONHOLD);
				return false;
			}
		}
		return true;
	}

}
