/*
 * Created on Dec 3, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

import java.util.Date;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.UnknownUser;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ConfirmDialogicMessage extends AbstractDialogicMessage {

	public ConfirmDialogicMessage() {
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"confirm.wav";
	}
	
	public String getAsteriskFilename() {
		return "confirm";
	}


	public int getExpectedDigits() {
		return 7;
	}

	public DialogicMessage getNextMessage (String digits) {

		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(digits);
		BrokerFactory.getLoggingBroker().logDebug("notification to confirm = "+notification);
		if (notification == null) {
			return new CantFindDialogicMessage(this); // TODO: return CantFindMessage
		}
		if (notification.getStatus() == Notification.EXPIRED) {
			return new AlreadyExpiredDialogicMessage(new WelcomeDialogicMessage());
		}
		
		if (notification.getStatus() == Notification.CONFIRMED) {
			return new AlreadyConfirmedDialogicMessage(new WelcomeDialogicMessage());
		}

		notification.setStatus(Notification.CONFIRMED, new UnknownUser());
		notification.addMessage(new NotificationMessage("Message confirmed via dial-in", "Dial-In", new Date()));
		return new ConfirmedDialogicMessage(new WelcomeDialogicMessage());
	}
}
