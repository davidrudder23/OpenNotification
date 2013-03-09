/*
 * Created on Dec 13, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.test.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SearchWebHttpUnitTest extends TestCase{

	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	public void testBasicWeb() {
		try {
			WebConversation wc = new WebConversation();

			// Login
			WebResponse notifsPage = Actions.login(wc, "admin", "password");

			// Get Notifications page
			
			Actions.checkPage(notifsPage);

			// Search for "admin"
			WebForm mainForm = null;
			
			notifsPage = Actions.search(notifsPage, "admin");
			
			Actions.checkPage(notifsPage);

			// Click the "Administrator, System" checkbox to add to the send list
			mainForm = notifsPage.getFormWithName("mainform");
			String[] paramNames = mainForm.getParameterNames();
			String adminCheck = null;
			for (int i = 0; i < paramNames.length; i++) {
				if (paramNames[i].startsWith("add_user_")) {
					adminCheck = paramNames[i];
					break;
				}
			}
			assertTrue(adminCheck != null);

			String uuid = adminCheck.substring(adminCheck.lastIndexOf("_")+1, adminCheck.length());
			BrokerFactory.getLoggingBroker().logDebug("Found uuid = "+uuid);
			User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
			assertNotNull("Given uuid was not found in the system", user);
			assertEquals("First name didn't match", user.getFirstName(), "System");
			assertEquals("Last name didn't match", user.getLastName(), "Administrator");

			mainForm.setCheckbox(adminCheck, true);

			notifsPage = Actions.clickSubmitButton(notifsPage, "add_selected");

			Actions.checkPage(notifsPage);
			mainForm = notifsPage.getFormWithName("mainform");

			// Check to make sure the name was added to the send list
			String[] selectedUsers = mainForm.getOptions("recipient_list");
			assertTrue("We should have only found 1 user", selectedUsers.length == 1);
			assertTrue("The selected user wasn't System Administrator, it was "+selectedUsers[0], selectedUsers[0].equals ("Administrator, System"));
		
			// Switch to Administration page and add user
			WebLink adminLink = notifsPage.getLinkWithImageText("Administration");
			assertNotNull ("Couldn't find Administration tab", adminLink);
			WebResponse adminPage = adminLink.click();
			Actions.checkPage(adminPage);
			mainForm = adminPage.getFormWithName("mainform");
			
			String firstName = "user"+System.currentTimeMillis();
			String lastName = "last"+System.currentTimeMillis();
			String password = "password"+System.currentTimeMillis();
			mainForm.setParameter("addFirstName", firstName);
			mainForm.setParameter("addLastName", lastName);
			mainForm.setParameter("addDepartment", "httpunit test");
			mainForm.setParameter("addPassword", password);
			mainForm.setParameter("addConfirmPassword", password);

			adminPage = Actions.clickSubmitButton(adminPage, "action_addnew_save");
			BrokerFactory.getLoggingBroker().logDebug("Added user "+lastName+", "+firstName);
			Actions.checkPage(adminPage);
			mainForm = notifsPage.getFormWithName("mainform");
			
			// Log Off
			WebLink logOff = adminPage.getLinkWith("LOG OFF");
			assertNotNull("Couldn't find log off button", logOff);
			logOff.click();

		
			// Login as the newly created user
			notifsPage = Actions.login (wc, firstName+"."+lastName, password);
			
			Actions.checkPage(notifsPage);

			// Search for the newly created user to make sure it's there
			mainForm = notifsPage.getFormWithName("mainform");
			notifsPage = Actions.search (notifsPage, firstName);
			Actions.checkPage(notifsPage);

			// Click the "Administrator, System" checkbox to add to the send list
			Actions.addUserToSend(notifsPage, firstName, lastName);

			// Add the email device
			WebResponse settingsPage = Actions.addEmail(notifsPage, "notif-test@reliableresponse.net");
			

			// Switch to notification page
			notifsPage = Actions.switchToPage(settingsPage, "My Notifications");
			mainForm = Actions.getMainForm(notifsPage);
			
			// Search for the newly added user
			mainForm.setParameter("recipient_search_substring", firstName);
			mainForm.setParameter("recipient_search_type", "any");
			
			notifsPage = Actions.clickSubmitButton(notifsPage, "action_search_recipients");

			// Click the "Administrator, System" checkbox to add to the send list
			mainForm = notifsPage.getFormWithName("mainform");
			paramNames = mainForm.getParameterNames();
			String userCheck = null;
			for (int i = 0; i < paramNames.length; i++) {
				if (paramNames[i].startsWith("add_user_")) {
					userCheck = paramNames[i];
					break;
				}
			}
			assertTrue(userCheck != null);

			uuid = userCheck.substring(adminCheck.lastIndexOf("_")+1, adminCheck.length());
			BrokerFactory.getLoggingBroker().logDebug("Found uuid = "+uuid);
			user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
			assertNotNull("Given uuid was not found in the system", user);
			assertEquals("First name didn't match", user.getFirstName(), firstName);
			assertEquals("Last name didn't match", user.getLastName(), lastName);

			mainForm.setCheckbox(userCheck, true);

			notifsPage = Actions.clickSubmitButton(notifsPage, "add_selected");
			mainForm = notifsPage.getFormWithName("mainform");

			// Check to make sure the name was added to the send list
			selectedUsers = mainForm.getOptions("recipient_list");
			assertTrue("We should have only found 1 user", selectedUsers.length == 1);
			assertTrue("The selected user wasn't the new user, "+lastName+", "+firstName+", it was "+selectedUsers[0], 
					selectedUsers[0].equals (lastName+", "+firstName));

			// Send a notification to the new user
			String subjectToPerson = "test notification to "+lastName+", "+firstName;
			String messageToPerson = "this is a test notification sent to "+lastName+", "+firstName+".";
			mainForm.setParameter("send_subject", subjectToPerson);
			mainForm.setParameter("send_message", messageToPerson);

			BrokerFactory.getLoggingBroker().logDebug("Sending notification to "+lastName+", "+firstName);
			notifsPage = Actions.clickSubmitButton(notifsPage, "action_send_notification");
			
			// Make sure we get a positive answer
			assertTrue("Didn't get confirmation message", (notifsPage.getText().indexOf("Sent message to "+lastName+", "+firstName) > 0));
			mainForm = notifsPage.getFormWithName("mainform");
			
			Actions.checkForEmail(subjectToPerson);
			
			// Clear out recipients
			String[] optionNames = mainForm.getOptionValues("recipient_list");
			mainForm.setParameter("recipient_list", optionNames);
			notifsPage = Actions.clickSubmitButton(notifsPage, "action_remove_recipients");
			mainForm = notifsPage.getFormWithName("mainform");
			optionNames = mainForm.getOptions("recipient_list");
			if (optionNames != null) {
				for (int i = 0; i < optionNames.length; i++) {
					BrokerFactory.getLoggingBroker().logDebug("optionName["+i+"] = "+optionNames[i]);
				}
			}
			if ((optionNames.length == 1) && (optionNames[0].equals ("Add Recipients Below"))) {
				// We have that weird "add recipients below" hack
			} else {
				assertTrue ("We should not have users left in recipient list, but we do", ((optionNames == null) || (optionNames.length == 0)));
			}

			// Close then open the search tab, so we clear it out
			notifsPage = Actions.closeTab (notifsPage, "searchRecipients");
			mainForm = notifsPage.getFormWithName("mainform");
			notifsPage = Actions.openTab (notifsPage, "searchRecipients");
			mainForm = notifsPage.getFormWithName("mainform");
			
			// Search for the new user again
			notifsPage = Actions.search(notifsPage, lastName);
			Actions.checkPage(notifsPage);
			
			// Open the user's tab
			notifsPage = Actions.openTab(notifsPage, "collapsetag_recipient_search_found_"+uuid);
			Actions.checkPage (notifsPage);
			mainForm = notifsPage.getFormWithName("mainform");
			
			// Check the user's device
			String[] params = mainForm.getParameterNames();
			String param = null;
			for (int i = 0; i < params.length; i++) {
				if (params[i].startsWith("add_device_notification_"+uuid)) {
					param = params[i];
				}
			}
			assertNotNull("Couldn't find the device tab", param);
			mainForm.setCheckbox(param, true);
			notifsPage = Actions.clickSubmitButton(notifsPage, "add_selected");
			Actions.checkPage(notifsPage);
			mainForm = notifsPage.getFormWithName("mainform");

			// Check to make sure the name was added to the send list
			selectedUsers = mainForm.getOptions("recipient_list");
			assertTrue("We should have only found 1 user", selectedUsers.length == 1);
			assertTrue("The selected user wasn't the new user, it was "+selectedUsers[0], selectedUsers[0].equals (lastName+", "+firstName));

			// Send a notification to the new user
			String subjectToDevice = "test notification to "+lastName+", "+firstName+"'s device";
			String messageToDevice = "this is a test notification sent to "+lastName+", "+firstName+"'s device.";
			mainForm.setParameter("send_subject", subjectToDevice);
			mainForm.setParameter("send_message", messageToDevice);

			BrokerFactory.getLoggingBroker().logDebug("Sending notification to "+lastName+", "+firstName);
			notifsPage = Actions.clickSubmitButton(notifsPage, "action_send_notification");
			
			// Make sure we get a positive answer
			assertTrue("Didn't get confirmation message", (notifsPage.getText().indexOf("Sent message to "+lastName+", "+firstName) > 0));
			mainForm = notifsPage.getFormWithName("mainform");
			
			Actions.checkForEmail(subjectToDevice);
			
			// Log Off
			Actions.logoff (notifsPage);

		} catch (MalformedURLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SAXException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		TestSuite suite = new TestSuite (SearchWebHttpUnitTest.class);
		junit.textui.TestRunner.run (suite);
	}
	
}
