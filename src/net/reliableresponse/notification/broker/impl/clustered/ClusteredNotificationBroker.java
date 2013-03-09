/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.clustered;

import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.broker.impl.caching.CacheException;
import net.reliableresponse.notification.broker.impl.caching.CachingNotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;

/**
 * @author drig
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ClusteredNotificationBroker extends CachingNotificationBroker {
	public ClusteredNotificationBroker(NotificationBroker realBroker) {
		super (realBroker);
	}

	public void addMessage(Notification notification,
			NotificationMessage message) {
		super.addMessage(notification, message);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateNotification", notification.getUuid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#addProviderInformation(net.reliableresponse.notification.Notification,
	 *      net.reliableresponse.notification.providers.NotificationProvider,
	 *      java.util.Hashtable)
	 */
	public void addProviderInformation(Notification notification,
			NotificationProvider provider, Hashtable parameters, String status) {
		super.addProviderInformation(notification, provider, parameters,
				status);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateNotification", notification.getUuid());
	}

	
	public void setNotificationStatus(Notification notification, String status) {
		super.setNotificationStatus(notification, status);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateNotification", notification.getUuid());
	}

	public void setOwner(Notification notification, String owner) {
		super.setOwner(notification, owner);
		invalidateNotification(notification.getUuid());
	}
	
	public void invalidateNotification(String uuid) {
		Object object = notifications.getByUuid(uuid);
		if (object == null) return;
		
		if (object instanceof Notification) {
			Notification notification = (Notification)object;
			try {
				notification.refreshObject(super.realBroker.getNotificationByUuid(uuid));
				return;
			} catch (CacheException e) {
				BrokerFactory.getLoggingBroker().logWarn(e);
			}
		}
		
		notifications.remove(object);
	}
}