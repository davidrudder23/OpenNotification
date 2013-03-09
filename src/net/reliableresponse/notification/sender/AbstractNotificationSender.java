/*
 * Created on Mar 24, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class AbstractNotificationSender implements NotificationSender {

	public static final int CONFIRM=1;
	public static final int PASS=2;
	
	private String[] individualOptions = {"Confirm", "Ack", "ConfirmAll"};
	private String[] escalationOptions = {"Confirm", "Ack", "ConfirmAll", "Pass"};
	private String[] expiredOptions = {};
	private String[] onHoldOptions = {"Confirm", "Ack", "ConfirmAll", "Release"};
	
	private String bridgeNumber;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#getAvailableResponses(net.reliableresponse.notification.Notification)
	 */
	public String[] getAvailableResponses(Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("Getting available responses for "+notification.getRecipient());
		if (notification.getStatus() == Notification.ONHOLD) {
			return BrokerFactory.getConfigurationBroker().getStringValues("responses.onhold", onHoldOptions);
		} else if (notification.getStatus() == Notification.EXPIRED) {
			return BrokerFactory.getConfigurationBroker().getStringValues("responses.expired", expiredOptions);
		}
		
		Member recipient = notification.getRecipient();
		if (recipient instanceof User) {
			if (notification.getParentUuid() != null) {
				Notification notif = BrokerFactory.getNotificationBroker().getNotificationByUuid(notification.getParentUuid());
				recipient = notif.getRecipient();
			}
		}
		if (notification.getUltimateParent().getRecipient() instanceof EscalationGroup) {
			return BrokerFactory.getConfigurationBroker().getStringValues("responses.escalation", escalationOptions);
		} else {
			return BrokerFactory.getConfigurationBroker().getStringValues("responses.individual", individualOptions);
		}
	}

	public String getNotificationType() {
		return "notification";
	}
	
	
	
	public String getResponseMessage(String response) {
		// TODO Auto-generated method stub
		return "Thank you for responding with "+response;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#handleResponse(java.lang.String, java.lang.String)
	 */
	public void handleResponse(Notification notification, Member responder, String response, String text) {
		BrokerFactory.getLoggingBroker().logDebug("Handling response "+response);
		if ((notification.getStatus() == Notification.ONHOLD) && (response.equalsIgnoreCase("release"))) {
			notification.setStatus(Notification.PENDING, responder);
			notification.addMessage("Message release from event storm hold", responder);
			try {
				notification.setReleased(true);
				SendNotification.getInstance().doSend(notification);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			return;
		}
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory.getLoggingBroker().logInfo(responder+" tried to confirm an expired notification with uuid "+notification.getUuid());
			return;
		}
		notification.addMessage(text, responder);
		if ((response.equalsIgnoreCase("confirm")) ||(response.equalsIgnoreCase("ack"))) {
			notification.addMessage("Notification confirmed", responder);
			notification.setStatus(Notification.CONFIRMED, responder);
			BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
		} else if (response.equalsIgnoreCase ("confirmall")) {
			Member recipient = responder;
			if ((recipient == null ) || (recipient instanceof UnknownUser)) {
				recipient = notification.getUltimateParent().getRecipient();
			}
			Notification[] unconfirmedNotifs = BrokerFactory.getNotificationBroker().getMembersUnconfirmedNotifications (recipient);
			BrokerFactory.getLoggingBroker().logDebug ("ConfirmAll found "+unconfirmedNotifs.length+" unconfirmed notifs");
			for (int i = 0; i < unconfirmedNotifs.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug ("ConfirmAll confirming "+unconfirmedNotifs[i]);
				unconfirmedNotifs[i].getSender().handleResponse (unconfirmedNotifs[i], responder, "Confirm", text);
			}
			if ((notification.getStatus()==Notification.PENDING) | (notification.getStatus() == Notification.NORMAL)) {
				notification.getSender().handleResponse (notification, responder, "Confirm", text);
			}
	
		} else if (response.equalsIgnoreCase("pass")) {
			notification.addMessage("Notification passed", responder);
			EscalationThread escThread = EscalationThreadManager.getInstance().getEscalationThread(notification.getUuid());
			if (escThread != null) {
				escThread.pass(responder);
			}
		} else if (response.equalsIgnoreCase("comment")) {
		} else {
			if ((text == null) || (text.length()== 0)) {
				notification.addMessage(response, responder);
			}
		}
	} 
	
	public void handleBounce (Device device) {
		BrokerFactory.getLoggingBroker().logInfo(device+" bounced");
	}

	public String getConfirmEquivalent(Notification notification) {
		return "Confirm";
	}
	
	public String getPassEquivalent(Notification notification) {
		return "Pass";
	}
	
	public void setBridgeNumber (String bridgeNumber) {
		this.bridgeNumber = bridgeNumber;
	}
	
	public String getBridgeNumber () {
		return bridgeNumber;
	}
	
	// The default case is to get no variables from the string
	public boolean getVariablesFromNotification (Notification notification) {
		return true;
	}

}
