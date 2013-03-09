/*
 * Created on May 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.test.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

import org.xml.sax.SAXException;

import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class DemoScriptTest extends TestCase {
	WebConversation wc;
	WebForm form;
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		wc = new WebConversation();
	}
	
	public void testStep1() throws IOException, SAXException{
		WebResponse page = Actions.login(wc, "admin", "password");
		Actions.checkPage(page);
		
		// Check for our test user "Test User"
		String firstName = "Test";
		String lastName = "User";
		String groupName = "Test Escalation Group";
		
		page = Actions.search(page, firstName);		
		form = Actions.getMainForm(page);
		String[] paramNames = form.getParameterNames();
		User user = null;
		for (int i = 0; i < paramNames.length; i++) {
			if (paramNames[i].startsWith("add_user_")) {
				String uuid = paramNames[i].substring (9, 16);
				BrokerFactory.getLoggingBroker().logDebug("Found uuid "+uuid);
				User foundUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
				if ((foundUser != null) && (foundUser.getFirstName().equals(firstName)) &&
						(foundUser.getLastName().equals(lastName))) {
					user = foundUser;
				}
			}
		}
		if (user == null) {
			// If Test User isn't there, add it
			page = Actions.addUser(page, firstName, lastName);
			page = Actions.switchToPage(page, "My Notifications");
		}
		
		Actions.logoff(page);
		
		page = Actions.login(wc, "Test.User", "Test");
		Actions.checkPage(page);
		
		page = Actions.addEmail(page, "notif-test@reliableresponse.net");
		page = Actions.switchToPage(page, "My Notifications");

		page = Actions.openTab(page, "pendingNotifications");
		Actions.checkPage(page);
		
		page = Actions.clickSubmitButton(page, "toggle_confirmed");
		Actions.checkPage(page);
		
		page = Actions.closeTab(page, "pendingNotifications");
		Actions.checkPage(page);
		

		Group escGroup = null;
		while (escGroup == null) {
			page = Actions.switchToPage(page, "My Notifications");
		page = Actions.openTab(page, "sendNotification");
		Actions.checkPage(page);

		form = Actions.getMainForm(page);
		form.setParameter("recipient_search_type", "any group");
		page = Actions.clickSubmitButton(form, "action_search_recipients");
		
		// Check for "Example Escalation Group"
		
		form = Actions.getMainForm(page);
		paramNames = form.getParameterNames();
		for (int i = 0; i < paramNames.length; i++) {
			if (paramNames[i].startsWith("add_group_")) {
				String uuid = paramNames[i].substring (10, 17);
				BrokerFactory.getLoggingBroker().logDebug("Found uuid "+uuid);
				Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);
				if ((group != null) && (group.getGroupName().equals(groupName))) {
					escGroup = group;
				}
			}
		}
		if (escGroup == null) {
			page = Actions.addGroup(page, groupName, true);
			Actions.checkPage(page);
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByName(groupName);
			assertNotNull(group);
			User testUser = BrokerFactory.getUserMgmtBroker().getUsersByName(firstName, lastName)[0];
			assertNotNull(testUser);
			page = Actions.addUserToGroup(page, group.getUuid(),
					testUser.getUuid());
			page = Actions.addUserToGroup(page, BrokerFactory.getGroupMgmtBroker().getGroupByName(groupName).getUuid(),
					BrokerFactory.getUserMgmtBroker().getUsersByName(firstName, lastName)[0].getUuid());
			page = Actions.switchToPage(page, "My Notifications");
			form = Actions.getMainForm(page);
			form.setParameter("recipient_search_type", "any group");
			page = Actions.clickSubmitButton(form, "action_search_recipients");
		}
		}
		
		form.setCheckbox("add_group_"+escGroup.getUuid(), true);
		page = Actions.clickSubmitButton(form, "add_selected");
		Actions.checkPage(page);
		
		form = Actions.getMainForm(page);
		assertNotNull(form.getParameterValue("selected_group_"+escGroup.getUuid()));
		assertEquals (form.getParameterValue("selected_group_"+escGroup.getUuid()), groupName);
		
		String subject = "Automated message from DemoScriptTest at "+new Date();
		form.setParameter("send_subject", subject);
		form.setParameter("send_message", "This is an automated message from the DemoScriptTest test case at "+new Date());
		Actions.clickSubmitButton(form, "action_send_notification");
		
		Actions.checkForEmail(subject);

		page = Actions.openTab(page, "sentNotifications");
		Actions.checkPage(page);

		page = Actions.closeTab(page, "sendNotification");
		Actions.checkPage(page);
		
		page = Actions.closeTab(page, "log");
		Actions.checkPage(page);

		page = Actions.openTab(page, "pendingNotifications");
		Actions.checkPage(page);
		
		// Search for the UUID of the recently sent notification
		String pageText = page.getText();
		int subjectOffset = pageText.indexOf(subject);
		assertTrue("Could not find recently sent notification", subjectOffset>=0);
		int IDindex = pageText.indexOf("ID: ", subjectOffset);
		String notificationUuid = pageText.substring(IDindex+4, IDindex+11);
		BrokerFactory.getLoggingBroker().logDebug("Notification uuid="+notificationUuid);
		
		// Check to see that the escalation is on the first person
		HTMLElement textarea = page.getElementWithID("recipientlist_"+notificationUuid);
		BufferedReader textareaReader = new BufferedReader(new StringReader(textarea.getText()));
		assertEquals("*"+lastName+", "+firstName+"*", textareaReader.readLine());
		
		page = Actions.closeTab(page, "pendingNotifications");
		Actions.checkPage(page);

		page = Actions.openTab(page, "log");
		Actions.checkPage(page);

		page = Actions.openTab(page, "groupactivity");
		Actions.checkPage(page);
		
		form = Actions.getMainForm(page);
		form.setParameter("groupname", groupName);
		WebResponse reportPage = Actions.clickSubmitButton(form, "action_report_html_groupactivity");
		assertTrue("Couldn't find expected pending messages", reportPage.getText().toLowerCase().indexOf("pending messages")>0);
		
		page = Actions.closeTab(page, "groupactivity");
		Actions.checkPage(page);
		
		page = Actions.closeTab(page, "log");
		Actions.checkPage(page);

		Actions.logoff(page);
		
		page = Actions.login(wc, "admin", "admin");
		
	}
	


	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		BrokerFactory.getConfigurationBroker().setStringValue("log.level", "info");
		TestSuite suite = new TestSuite (DemoScriptTest.class);
		junit.textui.TestRunner.run (suite);	}
}
