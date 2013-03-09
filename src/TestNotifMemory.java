import java.io.FileInputStream;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
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
public class TestNotifMemory {
	
	public static long getUsedMemory(Runtime rt) {
		return (rt.totalMemory() - rt.freeMemory());
	}

	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		Runtime rt = Runtime.getRuntime();
		User[] users = new User[1];
		BrokerFactory.getUserMgmtBroker().getUsers(1, 0, users);
		User user = users[0];
		Vector notifications = new Vector();
		long initialUsed = getUsedMemory(rt);
		int count = 0;
		while (true) {
			Notification notification = new Notification(null, user, new EmailSender("drig@noses.org"), "test"+System.currentTimeMillis(), "testing memory usage - "+rt.freeMemory());
			notifications.addElement(notification);
			if ((count % 100) == 0) {
				System.out.println ("Used Memory: "+getUsedMemory(rt));
				int per = (int)((getUsedMemory(rt) - initialUsed)/notifications.size());
				System.out.println ("Per notif: "+per);
				count = 0;
			}
			count++;
		}
	}
}
