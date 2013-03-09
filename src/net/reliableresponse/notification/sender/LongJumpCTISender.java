/*
 * Created on Dec 16, 2008
 *
 *Copyright Reliable Response, 2008
 */
package net.reliableresponse.notification.sender;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.UnknownUser;

public class LongJumpCTISender extends AbstractNotificationSender {

	String[] responses = {"Yes", "No"};
	
	public void setResponses (String[] responses) {
		this.responses = responses;
	}

	public String[] getAvailableResponses(Notification notification) {
		return responses;
	}
	
	

	public void handleResponse(Notification notification, Member responder,
			String response, String text) {
		
		notification.setStatus(Notification.CONFIRMED);
		NotificationMessage message = new NotificationMessage(response.getBytes(), responder.toString(), new Date(), "x-application/longjumpresponse");
		notification.addMessage(message);
		
	}

	public String getConfirmEquivalent(Notification notification) {
		return "Confirm";
	}

	public String getPassEquivalent(Notification notification) {
		return "Pass";
	}

	public String toString() {
		return "Long Jump";
	}

	public void addVariable(int index, String value) {
		// We don't have any variables
	}

	public String[] getVariables() {
		// We don't have any variables
		return new String[0];
	}

}
