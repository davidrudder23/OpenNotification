package net.reliableresponse.notification.test;

import java.io.FileInputStream;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.caching.CachingNotificationBroker;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.User;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LargeUuidTest extends TestCase {

	public void testLargeUuid() {
		try {
			int numUsers = 10000;
			int numMessages = 10000000 / numUsers;

			BroadcastGroup everyone = new BroadcastGroup();
			everyone.setGroupName("Everyone");
			BrokerFactory.getGroupMgmtBroker().addGroup(everyone);

			BrokerFactory.getLoggingBroker().logInfo(
					"Adding " + numUsers + " users");
			long start = System.currentTimeMillis();
			long cycleStart = start;
			for (int userNum = 0; userNum < numUsers; userNum++) {
				User user = new User();
				user.setFirstName("" + numUsers);
				user.setLastName("" + numUsers);
				try {
					BrokerFactory.getUserMgmtBroker().addUser(user);
				} catch (NotSupportedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
				everyone.addMember(user, -1);
				if (userNum % 1000 == 0) {
					BrokerFactory.getLoggingBroker().logInfo(
							"Added " + userNum + " users");
					BrokerFactory.getLoggingBroker().logInfo(
							"Last uuid was " + user.getUuid());
					long cycleEnd = System.currentTimeMillis();
					BrokerFactory.getLoggingBroker().logInfo(
							"Cycle took " + (cycleEnd - cycleStart));
					cycleStart = cycleEnd;
				}
			}
			long end = System.currentTimeMillis();
			BrokerFactory.getLoggingBroker().logInfo(
					"Added " + numUsers + " users in "
							+ ((int) ((end - start) / 1000)) + " seconds");

			start = System.currentTimeMillis();
			cycleStart = start;
			BrokerFactory.getLoggingBroker().logInfo(
					"Sending " + (numMessages * numUsers) + " messages");
			for (int messageNum = 0; messageNum < numMessages; messageNum++) {
				Notification notification = new Notification(null, everyone,
						new EmailSender("test@reliableresponse.net"), "test "
								+ messageNum, "test " + messageNum);
				SendNotification.getInstance().doSend(notification);
				//if (messageNum % 1000 == 0) {
					BrokerFactory.getLoggingBroker().logInfo(
							"Sent " + ((messageNum+1) * everyone.getMembers().length)+ " messages");
					BrokerFactory.getLoggingBroker().logInfo(
							"Last uuid was " + notification.getUuid());
					long cycleEnd = System.currentTimeMillis();
					BrokerFactory.getLoggingBroker().logInfo(
							"Cycle took " + (cycleEnd - cycleStart));
					cycleStart = cycleEnd;
				//}
			}
			end = System.currentTimeMillis();
			BrokerFactory.getLoggingBroker().logInfo(
					"Added " + numMessages + " messages in "
							+ ((int) ((end - start) / 1000)) + " seconds");
			BrokerFactory.getLoggingBroker().logInfo(
					"Cache is "+((CachingNotificationBroker)BrokerFactory.getNotificationBroker()).getCache().size()+" objects large");
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		TestSuite suite = new TestSuite(LargeUuidTest.class);
		junit.textui.TestRunner.run(suite);

	}

}
