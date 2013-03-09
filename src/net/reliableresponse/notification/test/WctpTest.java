/*
 * Created on May 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.test;

import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.wctp.ClientResponse;
import net.reliableresponse.notification.wctp.WctpException;
import net.reliableresponse.notification.wctp.WctpLibrary;

import org.w3c.dom.DOMException;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WctpTest extends TestCase {
	WctpLibrary l;
	String message;
	public WctpTest() {
		l = new WctpLibrary("http://wctp.skytel.com/wctp", null, "text/xml", null, null);
		message = "";
	}

	public void testFormatMessage() {
		try {
			message =
				l.formatWCTPMessage(
					"test@noses.org",
					"sender@noses.org",
					"JUnit unit case at " + System.currentTimeMillis(),
					null);
			BrokerFactory.getLoggingBroker().logDebug(message);
		} catch (DOMException e) {
			assertTrue(e.getMessage(), false);
		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
		}
	}

	public void testReadResponse() {
		String response = "<?xml version=\"1.0\"?>";
		response
			+= "<!DOCTYPE wctp-Operation SYSTEM \"http://wctp.arch.com/DTD/wctpv1-0.dtd\">";
		response += "<wctp-Operation wctpVersion=\"1.0\">";
		response += "<wctp-SubmitClientResponse>";
		response
			+= "<wctp-ClientSuccess successCode=\"200\" successText=\"Accepted\" trackingNumber=\"0013610059\">";
		response
			+= "Your message for 3032013132 has been accepted for delivery.";
		response += "</wctp-ClientSuccess>";
		response += "</wctp-SubmitClientResponse>";
		response += "</wctp-Operation>";

		try {
			ClientResponse cr = l.readClientResponse(response);
			assertEquals( cr.getCode(), 200);
			assertEquals( cr.getStatus(), "Accepted");
			assertEquals( cr.getTrackingNumber(), "0013610059");
		} catch (WctpException e) {
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void testSendPage() {
		try {
			l.sendMessage("3032013132", "drig@noses.org", "JUnit test notification, no choices", null);
		} catch (Exception e) {
			assertTrue(e.getMessage(), false);
		}
	}
	public void testSendPageWithChoices() {
		try {
			Vector choices = new Vector();
			choices.addElement ("choice1");
			choices.addElement ("choice2");
			l.sendMessage("3032013132", "drig@noses.org", 
				"JUnit test notification, no choices", choices);
		} catch (Exception e) {
			assertTrue(e.getMessage(), false);
		}
	}

	public static void main(String[] args) throws Exception {
		TestSuite suite = new TestSuite(WctpTest.class);
		junit.textui.TestRunner.run(suite);
	}
}
