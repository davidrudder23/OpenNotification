/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.template;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;

public abstract class AbstractTemplate implements Template {

	
	/**
	 * Returns the interface which all recipients should derive from.  This means
	 * that any Template class that does not override this method will apply
	 * to all recipients
	 */
	public String getMemberTypeClassname() {
		return "net.reliableresponse.notification.usermgmt.Member";
	}

	/**
	 * Returns the interface which all senders should derive from.  This means
	 * that any Template class that does not override this method will apply
	 * to all senders
	 */
	public String getSenderClassname() {
		return "net.reliableresponse.notification.sender.NotificationSender";
	}

	/**
	 * Returns the default template contents, which is just the message
	 */
	public String getTemplateContents() {
		return "%m";
	}

	/** 
	 * Checks if this template applies to this notification 
	 */
	public boolean isValid(Notification notification) {
		
		try {
			// Check that the recipient is of the right type
			if (Class.forName(getMemberTypeClassname()).isInstance(notification.getRecipient())) {
				// Check that the sender is of the right type
				if (Class.forName(getSenderClassname()).isInstance(notification.getSender())) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return false;
	}

}
