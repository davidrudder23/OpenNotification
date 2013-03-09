/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PagingTest extends TestCase {
	User user;
	EscalationGroup escalationGroup;
	
	public void setUp() {
		try {
			UserMgmtBroker userBroker = BrokerFactory.getUserMgmtBroker();
			user = new User();
			user.setDepartment("IT");
			user.addEmailAddress("drig@noses.org");
			user.setFirstName("David");
			user.setLastName("Rudder");
			userBroker.addUser(user);
		} catch (NotSupportedException e) {
			e.printStackTrace(); 
		}
		
		try {
			GroupMgmtBroker groupMgmtBroker = BrokerFactory.getGroupMgmtBroker();
			escalationGroup = new EscalationGroup();
			escalationGroup.setGroupName("Test EscalationGroup");
			groupMgmtBroker.addEscalationGroup(escalationGroup);
			groupMgmtBroker.addMemberToGroup(user, escalationGroup);
			groupMgmtBroker.addMemberToGroup(user, escalationGroup);
		} catch (NotSupportedException e1) {
			e1.printStackTrace();
		}
	}
	
	public void testHasDevices() {
		int numDevices = user.getDevices().length; 
		assertTrue("User does not have the same number of devices as were set.  User has "+numDevices,
				numDevices == 1);
	}
	
	public void testSendPage() {

				try {
			Notification page = new Notification(null, user, null,
				"Testing Sending Notification", "This is a test of the\nsend notification");
				page.setSender(new EmailSender("drig@noses.org"));
			page.setRequireConfirmation(false);
			SendNotification.getInstance().doSend(page);
		} catch (NotificationException e) {
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void testSendEscalation() {
		try {
			Notification page = new Notification (null, escalationGroup, null,
				"Testing Escalation", "This is a test of an escalation group");
				page.setSender(new EmailSender("drig@noses.org"));
			SendNotification.getInstance().doSend(page);
			
			// confirm after 10 seconds
			try {
				Thread.sleep(1000*10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			EscalationThreadManager.getInstance().getEscalationThread(page.getID()).confirm(user);
		} catch (NotificationException e) {
			assertTrue(e.getMessage(), false);
		}
	}

	public static void main (String[] args) throws Exception {
		TestSuite suite = new TestSuite (PagingTest.class);
		junit.textui.TestRunner.run (suite);
	}
}
