package net.reliableresponse.notification.aggregation;

import java.util.Calendar;
import java.util.Date;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.StringUtils;

public class Squelch {
	
	private String notificationUuid;
	private Date startDate;
	private Date endDate;
	
	private int distancePercentage = 80;
	public static final int MIN_CHARACTERS = 8;
	
	public Squelch (Notification notification, Date startDate, int numberOfMinutes) {
		if (notification != null) {
			this.notificationUuid = notification.getUuid();
		}
		
		this.startDate = startDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.MINUTE, numberOfMinutes);
		endDate = calendar.getTime();
	}
	
	public Notification getNotification() {
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notificationUuid = notification.getUuid();
	}

	public boolean isExpired() {
		Date now = new Date();
		return now.after(endDate);
	}
	
	public String getLoggingMessage() {
		return notificationUuid+" starts at "+startDate+" and ends at "+endDate;
	}
	
	public boolean shouldSquelch(Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("Running shouldSquelch for squelched notif "+notificationUuid+" vs supplied notif "+notification.getUuid()+" with recipient "+notification.getRecipient().getUuid());
		
		// Quickly see if we can discard this notification
		if (notification == null) return false;
		if (notification.getSubject() == null) return false;
		if (notification.getSubject().length()<Squelch.MIN_CHARACTERS) return false;
		
		Notification squelchedNotif = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
		if (squelchedNotif == null) return false;
		
		if (squelchedNotif.getSubject() == null) return false;
		if (squelchedNotif.getSubject().length()<Squelch.MIN_CHARACTERS) return false;
		
		// it passes sanity, now check if it should squelch
		int distance = notification.getSubject().length() - StringUtils.distance(notification.getSubject(), squelchedNotif.getSubject());
		int thisDistancePercent = (distance*100)/notification.getSubject().length();
		boolean shouldSquelch = thisDistancePercent>=distancePercentage;
		
		BrokerFactory.getLoggingBroker().logDebug("Notification \""+notification.getSubject()+"\" has distance "+distance+
		" from notification with subject \""+squelchedNotif.getSubject()+"\"");
		BrokerFactory.getLoggingBroker().logDebug("Notification "+notification.getUuid()+
				" has distance percentage "+thisDistancePercent+
				" with subject "+notification.getSubject()+" count "+notification.getSubject().length());
		BrokerFactory.getLoggingBroker().logDebug("Notification "+notification.getUuid()+" will "+(shouldSquelch?"":"not ")+" squelch");

		return shouldSquelch;
	}
}
