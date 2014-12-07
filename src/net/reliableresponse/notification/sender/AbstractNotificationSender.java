/*
 * Created on Mar 24, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.aggregation.Squelcher;
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
	
	private List<String> individualOptions = new ArrayList<String>(Arrays.asList("Confirm", "Ack", "ConfirmAll"));
	private List<String> escalationOptions = new ArrayList<String>(Arrays.asList("Confirm", "Ack", "ConfirmAll", "Pass"));
	private List<String> expiredOptions = new ArrayList<String>(Arrays.asList());
	private List<String> onHoldOptions = new ArrayList<String>(Arrays.asList("Confirm", "Ack", "ConfirmAll", "Release"));
	
	private String bridgeNumber;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#getAvailableResponses(net.reliableresponse.notification.Notification)
	 */
	public String[] getAvailableResponses(Notification notification) {
		List<String> options;
		BrokerFactory.getLoggingBroker().logDebug("Getting available responses for "+notification.getRecipient());
		if (notification.getStatus() == Notification.ONHOLD) {
			options = new ArrayList(BrokerFactory.getConfigurationBroker().getStringValues("responses.onhold", onHoldOptions));
		} else if (notification.getStatus() == Notification.EXPIRED) {
			options = new ArrayList(BrokerFactory.getConfigurationBroker().getStringValues("responses.expired", expiredOptions));
		}
		
		Member recipient = notification.getRecipient();
		if (recipient instanceof User) {
			if (notification.getParentUuid() != null) {
				Notification notif = BrokerFactory.getNotificationBroker().getNotificationByUuid(notification.getParentUuid());
				recipient = notif.getRecipient();
			}
		}
		if (notification.getUltimateParent().getRecipient() instanceof EscalationGroup) {
			options = new ArrayList(BrokerFactory.getConfigurationBroker().getStringValues("responses.escalation", escalationOptions));
		} else {
			options = new ArrayList(BrokerFactory.getConfigurationBroker().getStringValues("responses.individual", individualOptions));
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Options has "+options.size()+" elems");

		if (recipient.getType() == Member.USER) {
			if (Squelcher.isSquelched(notification)) {
				options.add("Unsquelch");
			} else {
				options.add("Squelch");
			}
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Options has "+options.size()+" elems - "+options.stream().reduce("", (a,b)->a+" "+b));
		return options.toArray(new String[0]);
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
		
		if ("squelch".equalsIgnoreCase(response)) {
			Squelcher.squelch(notification);
			return;
		}
		
		if ("unsquelch".equalsIgnoreCase(response)) {
			Squelcher.unsquelch(notification);
			return;
		}
		
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
			List<Notification> unconfirmedNotifs = BrokerFactory.getNotificationBroker().getMembersUnconfirmedNotifications (recipient);
			BrokerFactory.getLoggingBroker().logDebug ("ConfirmAll found "+unconfirmedNotifs.size()+" unconfirmed notifs");
			for (Notification unconfirmedNotif: unconfirmedNotifs) {
				BrokerFactory.getLoggingBroker().logDebug ("ConfirmAll confirming "+unconfirmedNotif);
				unconfirmedNotif.getSender().handleResponse (unconfirmedNotif, responder, "Confirm", text);
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
	
	public String getVariable(String variableName) {
		return null; 
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
