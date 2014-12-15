import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.User;

/*
 * Created on Dec 7, 2004
 *
 *Copyright Reliable Response, 2004
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class GenerateNotificationLog {

	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		NotificationBroker notifBroker = BrokerFactory.getNotificationBroker();
		List<Notification> notifications = notifBroker.getNotificationsSince(new Date((long)0));
		
		StringBuffer output = new StringBuffer();
		for (Notification notification: notifications) {
			output.append ("From: ");
			output.append (notification.getSender());
			output.append ("\n");
			output.append ("To: ");
			output.append (notification.getRecipient());
			output.append ("\n");
			output.append ("Subject: ");
			output.append (notification.getSubject());
			output.append ("\n");
			output.append ("Sent On: ");
			output.append (notification.getTime());
			output.append ("\n");
			List<NotificationProvider> providers = notification.getNotificationProviders();
			for (NotificationProvider provider: providers) {
				output.append ("Device ");
				output.append (provider.getName());
				output.append (" ");
				output.append (provider.getStatusOfSend(notification));
				output.append ("\n");
			}
			
			NotificationMessage[] messages = notification.getMessages();
			for (int m = 0; m < messages.length; m++) {
				output.append ("Message add by ");
				String addedBy = messages[m].getAddedby();
				if (addedBy == null) addedBy = "unknown";
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(addedBy);
				if (user != null) addedBy = user.toString();
				output.append(addedBy);
				output.append(" on ");
				output.append (messages[m].getAddedon());
				output.append ("\n"); 
				output.append (messages[m].getMessage());
				output.append ("\n\n"); 
			}
			output.append ("\n"); 
		}
		System.out.println (output.toString());
	}
}
