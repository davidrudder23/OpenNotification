/*
 * Created on Nov 2, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.providers;

import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ProviderStatusLoop extends Thread {

	Vector notificationsToCheck;
	private static ProviderStatusLoop instance;
	
	private ProviderStatusLoop() {
		notificationsToCheck = new Vector();
	}
	
	public static ProviderStatusLoop getInstance() {
		if (instance == null) {
			instance = new ProviderStatusLoop();
			instance.setDaemon(true);
			instance.start();
		}
		return instance;
	}
	
	public void addNotification (Notification notification) {
		notificationsToCheck.addElement(notification);
	}

	public void run() {
		while (true) {
			BrokerFactory.getLoggingBroker().logDebug("Provider Status Loop running");
			try {
				
				// Loopover each notif
			for (int n = 0; n < notificationsToCheck.size(); n++) {
				Notification notification = (Notification)notificationsToCheck.elementAt(n);
				BrokerFactory.getLoggingBroker().logDebug("Provider Status Loop checking "+notification.getUuid()+" with status "+notification.getStatus());
				
				if (notification.getStatus() == Notification.EXPIRED) {
					// If it's expired, remove it from the queue
					notificationsToCheck.remove (notification);
				} else if (notification.getTime().getTime() < (System.currentTimeMillis()-(60*60*1000*8))) {
					// If the notification is greater than a day old, mark it as expired
					if ((notification.getStatus() == Notification.PENDING) || (notification.getStatus() == Notification.NORMAL)) {
						notification.setStatus(Notification.EXPIRED);
					}
					notificationsToCheck.remove(notification);
				} else {
					// If it's not expired, then we need to check it
					// Loop over each provider
					NotificationProvider[] providers = notification
							.getNotificationProviders();
					for (int p = 0; p < providers.length; p++) {
						BrokerFactory.getLoggingBroker().logDebug("Provider Status Loop checking provider "+providers[p]);
						if (providers[p] != null) {
							// Make sure we were successful when sending it
							if (providers[p].getStatusOfSend(notification)
								.toLowerCase().startsWith("succee")) {
								
								// Call getResponses().  If the device doesn't support polling, it'll
								// just returnan empty array
								String[] responses = providers[p].getResponses(notification);
								if (responses == null)
									responses = new String[0];
								for (int r = 0; r < responses.length; r++) {
									// Now, handle the responses
									BrokerFactory.getLoggingBroker().logDebug(
											"Handling response " + responses[r]);
									notification.getSender().handleResponse(
										notification,
										notification.getRecipient(),
										responses[r], null);
								}
							}
						}
					}
				}
			}
			try {
				// Here we sleep for 1 minute.  Unfortunately, this
				// is a hardcoded value for all providers.
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logError(anyExc);
			} catch (Error anyErr) {
				BrokerFactory.getLoggingBroker().logError(anyErr);
			}
		}
	}
}
