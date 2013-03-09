/*
 * Created on Jan 3, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.test;

import java.io.FileInputStream;
import java.util.Hashtable;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.AIMDevice;
import net.reliableresponse.notification.providers.AIMNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AIMTest extends TestCase {
	
	public void testAIM() {
		try {
			AIMDevice device = new AIMDevice();
			Hashtable options = new Hashtable();
			options.put ("Account Name", "drig23");
			device.initialize(options);
			NotificationProvider provider = new AIMNotificationProvider();
			User[] users = new User[1];
			BrokerFactory.getUserMgmtBroker().getUsers(1,0, users);
			Notification notif = new Notification(null, users[0], new EmailSender("drig23@aol.com"), "test aim", "testing aim");
			provider.sendNotification(notif, device);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}				
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			assertTrue(e.getMessage(), false);
		}
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		TestSuite suite = new TestSuite (AIMTest.class);
		junit.textui.TestRunner.run (suite);
		
	}
}
