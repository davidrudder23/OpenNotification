/*
 * Created on Mar 25, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class HPOVNNMSender extends AbstractNotificationSender {

	public static final int UUID=1;

	String uuid;
	Vector receivedResponses;

	public HPOVNNMSender() {
	}
	
	public void addVariable(int index, String value) {
		switch (index) {
		case UUID: uuid = value;
				break;
		}
	}
	
	public String[] getAvailableResponses(Notification notification) {
		String[] escalationOptions = {"Confirm", "Corrected", "Pass"};
		String[] options = {"Confirm", "Corrected"};
		if (notification.getUltimateParent().getRecipient() instanceof EscalationGroup) {
			return escalationOptions;
		} else {
			return options;
		}
	}

	public String[] getVariables() {
		return new String[] {uuid};
	}
	
	public void handleResponse(Notification notification, Member responder, String response, String text) {
		BrokerFactory.getLoggingBroker().logDebug("Sending "+response+" to HP OpenView Network Node Manager with uuid = "+uuid);
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory.getLoggingBroker().logInfo(responder+" tried to confirm an expired notification with uuid "+notification.getUuid());
			return;
		}
		String executableName = BrokerFactory.getConfigurationBroker().getStringValue("ov.installpath", "/opt/OV")+"/bin/";
		
		String[] ovArgs = null;
		if (response.equalsIgnoreCase("confirm")) {
			executableName += "opcannoadd";
			ovArgs = new String[2];
			ovArgs[0] = uuid;
			ovArgs[1] = "Alert confirmed by "+responder.toString();
		} else if (response.toLowerCase().indexOf("corrected")>=0) {
			executableName += "opcackmsg";
			ovArgs = new String[3];
			ovArgs[0] = "-u";
			ovArgs[1] = responder.toString();
			ovArgs[2] = uuid;
		} 
		if (ovArgs != null) {
			Runtime runTime = Runtime.getRuntime();
			try {
				Process process = runTime.exec(executableName, ovArgs);
				process.waitFor();
				int exitValue = process.exitValue();
				BrokerFactory.getLoggingBroker().logInfo(
						"HP OpenView's ovevent returned with " + exitValue);
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		
		if (text != null) {
			executableName = BrokerFactory.getConfigurationBroker().getStringValue("ov.installpath", "/opt/OV")+"/bin/opcannoadd";
			ovArgs = new String[2];
			ovArgs[0] = uuid;
			ovArgs[1] = text;
			Runtime runTime = Runtime.getRuntime();
			try {
				Process process = runTime.exec(executableName, ovArgs);
				process.waitFor();
				int exitValue = process.exitValue();
				BrokerFactory.getLoggingBroker().logInfo(
						"HP OpenView's ovevent returned with " + exitValue);
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		
		notification.addMessage(text, responder);
		if (response.equalsIgnoreCase("confirm")) {
			notification.getUltimateParent().addMessage("Notification confirmed", responder);
			notification.setStatus(Notification.CONFIRMED, responder);
			BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
		} else if (response.toLowerCase().indexOf("corrected")>=0) {
				notification.getUltimateParent().addMessage("Notification marked as corrected", responder);
				notification.setStatus(Notification.CONFIRMED, responder);
				BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
		} else if (response.equalsIgnoreCase("pass")) {
			notification.addMessage("Notification passed", responder);
			EscalationThread escThread = EscalationThreadManager.getInstance().getEscalationThread(notification.getUuid());
			if (escThread != null) {
				notification.setStatus(Notification.PENDING, responder);
				escThread.pass(responder);
			}
		} else {
			notification.getUltimateParent().addMessage(response, responder);
		}
	}
	
	public String getConfirmEquivalent(Notification notification) {
		return "Confirm";
	}
	
	public String getPassEquivalent(Notification notification) {
		return "Pass";
	}
	
	public String toString() {
		return "HP OpenView Network Node Manager";
	}
}
 	