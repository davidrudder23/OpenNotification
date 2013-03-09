/*
 * Created on Aug 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.test;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SOAPSendNotificationTest extends TestCase {

	public void testSendNotificationToOne() {
		try {
			String endpoint = "http://localhost:8080/paging/SendNotification.jws";

			Service service = new Service();
			Call call = (Call) service.createCall();

			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			call.setOperationName(new QName("http://soapinterop.org/",
					"sendPage"));
			
			String[] ret = (String[])call.invoke(new Object[] {"drig@reliableresponse.net",
																"Test SOAP", "Test SOAP Notification from JUnit"});
			
			for (int i = 0; i < ret.length; i++) {
				System.out.println (ret[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		TestSuite suite = new TestSuite(SOAPSendNotificationTest.class);
		junit.textui.TestRunner.run(suite);
	}
}