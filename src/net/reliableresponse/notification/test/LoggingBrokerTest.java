/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LoggingBrokerTest extends TestCase {

	public void testErrorLog() {
		BrokerFactory.getLoggingBroker().logError("TestLog");
	}
	
	public static void main (String[] args) throws Exception {
		TestSuite suite = new TestSuite (LoggingBrokerTest.class);
		junit.textui.TestRunner.run (suite);
	}
}
