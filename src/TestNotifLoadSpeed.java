import java.io.FileInputStream;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.User;

/*
 * Created on Mar 28, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class TestNotifLoadSpeed {
	
	public static long getUsedMemory(Runtime rt) {
		return (rt.totalMemory() - rt.freeMemory());
	}

	public static void main (String[] args) throws Exception {

		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		User[] users = new User[1];
		BrokerFactory.getUserMgmtBroker().getUsers(1, 0, users);
		User user = users[0];
		NotificationBroker broker = BrokerFactory.getNotificationBroker();
		long start = System.currentTimeMillis();
		int size = 100000;
		for (int i = 0; i < size; i++) {
			Notification notification = new Notification(null, user, new EmailSender("drig@noses.org"), "test"+System.currentTimeMillis(), "testing load speed - "+i);
			broker.addNotification(notification);
			if ((i%100) == 0) BrokerFactory.getLoggingBroker().logInfo("Created "+i+" notifs");
		}
		long end = System.currentTimeMillis();
		BrokerFactory.getLoggingBroker().logInfo("Creating "+size+" notifs took "+(end-start)+" millis");
		
		long nextstart = System.currentTimeMillis();
		broker.getNotificationsSince(nextstart - start);
		end = System.currentTimeMillis();
		BrokerFactory.getLoggingBroker().logInfo("Loading "+size+" notifs took "+(end-nextstart)+" millis");
	}
}
