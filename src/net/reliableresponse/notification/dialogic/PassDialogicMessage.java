/*
 * Created on Dec 3, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PassDialogicMessage extends AbstractDialogicMessage {
	
	public PassDialogicMessage() {
		
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"pass.wav";
	}

	public String getAsteriskFilename() {
		return "pass";
	}

	public int getExpectedDigits() {
		return 7;
	}
	
	public DialogicMessage getNextMessage (String digits) {
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(digits);
		if (notification == null) {
			return new CantFindDialogicMessage(this); 
		}
		if (notification.getStatus() == Notification.EXPIRED) {
			return new AlreadyExpiredDialogicMessage(new WelcomeDialogicMessage());
		}
		
		if (notification.getStatus() == Notification.CONFIRMED) {
			return new AlreadyConfirmedDialogicMessage(new WelcomeDialogicMessage());
		}

		EscalationThread escThread = EscalationThreadManager.getInstance().getEscalationThread(digits);
		if (escThread == null) {
			return new CantPassDialogicMessage(this);
		}
		escThread.pass(((Group)notification.getRecipient()).getMembers()[escThread.getRecipientNumber()]);
		
		return new PassedDialogicMessage(new WelcomeDialogicMessage()); // TODO: return ThankYouPassedMessage
	}
}
