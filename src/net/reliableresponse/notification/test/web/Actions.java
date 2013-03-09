/*
 * Created on May 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.test.web;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.usermgmt.User;

import org.xml.sax.SAXException;

import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class Actions {
	public static WebResponse login (WebConversation wc, String username, String password) throws IOException, SAXException {
		WebResponse loginResponse = wc.getResponse ("http://localhost:8080/notification/index.jsp");
		WebForm loginForm = loginResponse.getForms()[0];
		
		loginForm.setParameter("username", username);
		loginForm.setParameter("password", password);

		SubmitButton loginButton = (SubmitButton)loginForm.getSubmitButton("Submit");
		WebResponse notifsPage = loginForm.submit(loginButton);

		return notifsPage;
	}

	/**
	 * @param mainForm
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static WebResponse openTab(WebResponse page, String tabName) throws IOException, SAXException {
		return toggleTab (page, tabName, true);
	}
	
	public static WebResponse closeTab(WebResponse page, String tabName) throws IOException, SAXException {
		return toggleTab (page, tabName, false);
	}

	public static WebResponse toggleTab(WebResponse page, String tabName, boolean open) 
	throws IOException, SAXException {
		WebResponse settingsPage = null;
		WebForm mainForm = Actions.getMainForm(page);
		assertNotNull("Could not find main form", mainForm);
		assertNotNull("The name of the tab to toggle was not supplied", tabName);
		String tabParam = mainForm.getParameterValue("opened."+tabName);
		assertNotNull ("Could not find tab, "+tabName+" to toggle", tabParam);
		if (tabParam.equals(open?"false":"true")) {
			BrokerFactory.getLoggingBroker().logDebug("The "+tabName+" tab is "+(open?"closed":"open")+", so we're "+(open?"opening":"closing")+" it");
			// Open devices tab
			settingsPage = clickSubmitButton(page, "action_toggle_collapseable."+tabName);
		}
		if (settingsPage == null) return page;
		return settingsPage;
	}

	/**
	 * @param mainForm
	 * @param buttonName
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static WebResponse clickSubmitButton(WebResponse page, String buttonName) 
	throws IOException, SAXException{
		WebForm mainForm = page.getFormWithName("mainform");
		return clickSubmitButton(mainForm, buttonName);
	}

	public static WebResponse clickSubmitButton(WebForm form, String buttonName) 
	throws IOException, SAXException{
		SubmitButton sendButton = form.getSubmitButton(buttonName);
		assertNotNull("Couldn't find "+buttonName+" button",
				sendButton);
		WebResponse page = form.submit(sendButton);
		checkPage(page);
		return page;
	}
	/**
	 * @param subject
	 */
	public static void checkForEmail(String subject) {
		try {
			int waitSecs = 10;
			BrokerFactory.getLoggingBroker().logDebug("Waiting "+waitSecs+" seconds to allow email to be sent and received");
			// Check to make sure the mail made it
			Thread.sleep(1000*waitSecs);
		} catch (InterruptedException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		
		String password;
		// Use POP to check that the mail made it
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		String address = config.getStringValue("email.pop.address");
		String hostname = config.getStringValue("email.pop.hostname");
		String username = "notif-test";
		password = "n0t1f";
		
		try {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("pop3");
			store.connect(hostname, username, password);
			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();

			boolean subjectFound = false;
			for (int i = 0; i < messages.length; i++) {
				String messageSubject = messages[i].getSubject();
				BrokerFactory.getLoggingBroker().logDebug("POP found message "+messageSubject);
				if (messageSubject.equals(subject)) {
					subjectFound = true;
				}
			}

			assertTrue("We didn't find the subject of the sent notification.  We were looking for "+subject, subjectFound);
			
		} catch (NoSuchProviderException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (MessagingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/**
	 * @param notifsPage
	 * @param firstName
	 * @param lastName
	 * @throws SAXException
	 */
	public static void addUserToSend(WebResponse notifsPage, String firstName, String lastName) throws SAXException {
		WebForm mainForm;
		String[] paramNames;
		String uuid;
		User user;
		mainForm = notifsPage.getFormWithName("mainform");
		paramNames = mainForm.getParameterNames();
		String newUserCheck = null;
		for (int i = 0; i < paramNames.length; i++) {
			if (paramNames[i].startsWith("add_user_")) {
				newUserCheck = paramNames[i];
				break;
			}
		}
		uuid = newUserCheck.substring(newUserCheck.lastIndexOf("_")+1, newUserCheck.length());
		BrokerFactory.getLoggingBroker().logDebug("Found uuid = "+uuid);
		user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		assertNotNull("Given uuid was not found in the system", user);
		assertEquals("First name didn't match", user.getFirstName(), firstName);
		assertEquals("Last name didn't match", user.getLastName(), lastName);
	}

	/**
	 * @param notifsPage
	 * @param mainForm
	 * @throws SAXException
	 */
	public static WebResponse search(WebResponse page, String substring) throws SAXException, IOException {
		WebForm form = Actions.getMainForm(page);
		form = page.getFormWithName("mainform");
		form.setParameter("recipient_search_substring", substring);
		form.setParameter("recipient_search_type", "any");
		
		return form.submit(form.getSubmitButton("action_search_recipients"));
	}

	/**
	 * @param page
	 * @throws SAXException
	 */
	public static boolean checkPage(WebResponse page) throws SAXException {
		if (page.getResponseCode() != 200) return false;
		if (!(page.getTitle().equals("Reliable Response Notification"))) return false;
		return true;
	}
	
	public static WebForm getMainForm (WebResponse page) throws SAXException{
		WebForm form = page.getFormWithName("mainform");
		assertNotNull("Couldn't find main form on page "+page, form);
		return form;
	}

	public static WebResponse switchToPage(WebResponse currentPage, String nextPage) throws IOException, SAXException {
		// Switch to Administration page and add user
		WebLink link = currentPage.getLinkWithImageText(nextPage);
		//assertNotNull ("Couldn't find "+nextPage+" tab", link);
		if (link != null) {
			WebResponse newPage = link.click();
			
			Actions.checkPage(newPage);
			return newPage;
		} else {
			return currentPage;
		}
	}
	
	public static WebResponse addUser (WebResponse page, String firstName, String lastName) throws IOException, SAXException {
		BrokerFactory.getLoggingBroker().logInfo("Adding a new user "+firstName+" "+lastName);
		WebResponse newPage = Actions.switchToPage(page, "Administration");
		newPage = Actions.closeTab(newPage, "jobs");
		newPage = Actions.openTab(newPage, "addUser");
		Actions.checkPage(newPage);
		
		WebForm form = Actions.getMainForm(newPage);
		form.setParameter("addFirstName", firstName);
		form.setParameter("addLastName", lastName);
		form.setParameter("addDepartment", "Test");
		form.setParameter("addPassword", firstName);
		form.setParameter("addConfirmPassword", firstName);
		newPage = Actions.clickSubmitButton(form, "action_addnew_save");
		
		return newPage;
	}
	
	public static WebResponse addGroup(WebResponse page, String groupName, boolean escalation) throws IOException, SAXException {
		BrokerFactory.getLoggingBroker().logInfo("Adding a new group "+groupName);
		WebResponse newPage = Actions.switchToPage(page, "Settings");
		newPage = Actions.closeTab(newPage, "personalSettings");
		newPage = Actions.openTab(newPage, "groupSettings");
		Actions.checkPage(newPage);
		
		newPage = Actions.openTab(newPage, "addGroup");
		WebForm form = Actions.getMainForm(newPage);
		form.setParameter("group_name", groupName);
		form.setParameter("group_description", groupName+" automatically added by test script");
		form.setParameter("group_type", escalation?"escalation":"broadcast");
		newPage = Actions.clickSubmitButton(form, "action_group_save");
		
		return newPage;
	}
	
	public static WebResponse addUserToGroup(WebResponse page, String groupUuid, String userUuid) throws IOException, SAXException {
		BrokerFactory.getLoggingBroker().logInfo("Adding "+userUuid+" to "+groupUuid);
		WebResponse newPage = Actions.switchToPage(page, "Settings");
		newPage = Actions.closeTab(newPage, "personalSettings");
		newPage = Actions.openTab(newPage, "groupSettings");
		Actions.checkPage(newPage);
		
		newPage = Actions.openTab(newPage, "individualGroupSettings_"+groupUuid+"_0");
		newPage = Actions.openTab(newPage, "add_new_recipients_"+groupUuid);
		
		WebForm form = Actions.getMainForm(newPage);
		form.setParameter("recipient_search_substring", "");
		form.setParameter("recipient_search_type", "any");
		
		newPage = form.submit(form.getSubmitButton("action_search_recipients"));
		form = Actions.getMainForm(newPage);
		assertContains(form.getParameterNames(), "add_user_"+userUuid);

		form.setCheckbox("add_user_"+userUuid, true);
		newPage = Actions.clickSubmitButton(form, "add_selected_"+groupUuid);
		newPage = Actions.clickSubmitButton(newPage, "action_group_save_"+groupUuid);
		
		return newPage;
	}
	
	public static WebResponse addEmail (WebResponse page, String address) throws SAXException, IOException {
		BrokerFactory.getLoggingBroker().logInfo("Adding new email address "+address);
		WebResponse settingsPage = Actions.switchToPage(page, "Settings");
		Actions.checkPage(settingsPage);
		WebForm mainForm = Actions.getMainForm(settingsPage);

		settingsPage = Actions.openTab(settingsPage, "deviceSettings");
		mainForm = settingsPage.getFormWithName("mainform");

		// Open add a new email tab
		settingsPage = Actions.openTab (settingsPage, "addNewEmail");
		mainForm = settingsPage.getFormWithName("mainform");

		mainForm.setParameter("Address_devicesetting", address);
		settingsPage = Actions.clickSubmitButton(settingsPage, "action_device_save");
		mainForm = settingsPage.getFormWithName("mainform");
		
		settingsPage = Actions.closeTab (settingsPage, "addNewEmail");

		return settingsPage;
	}
	
	public static void logoff (WebResponse page) throws SAXException, IOException{
		WebLink logOff = page.getLinkWith("LOG OFF");
		assertNotNull("Couldn't find log off button", logOff);
		logOff.click();
	}

	public static void assertTrue (String message, boolean bool) {
		if (!bool) throw new AssertionException(message);
	}
	
	public static void assertEquals (String message, String string1, String string2) {
		if (!string1.equals(string2)) throw new AssertionException(message);
	}
	
	public static void assertNotNull (String message, Object obj) {
		if (obj == null) throw new AssertionException(message);
	}
	
	public static void assertContains (String[] responses, String value) {
		for (int i = 0; i < responses.length; i++) {
			if (responses[i].equals(value)) return;
		}
		throw new AssertionError("Did not find "+value+" in web responses list");
	}	
	
}
