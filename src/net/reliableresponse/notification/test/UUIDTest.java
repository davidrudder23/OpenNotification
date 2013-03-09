/*
 * Created on Aug 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class UUIDTest extends TestCase{

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUniqueness() {
		User user1 = new User();
		user1.setFirstName("user1");
		user1.setLastName("user1");
		String uuid1 = user1.getUuid();
		System.out.println ("uuid1="+uuid1);

		User user2 = new User();
		user2.setFirstName("user2");
		user2.setLastName("user2");
		String uuid2 = user2.getUuid();
		System.out.println ("uuid2="+uuid2);
		
		assertTrue ("Two uuid's, for users "+user1+" and "+user2+" which should have been unique, were not", !(uuid1.equals(uuid2)));
		
	}
	
	public static void main(String[] args) throws Exception {
		TestSuite suite = new TestSuite(UUIDTest.class);
		junit.textui.TestRunner.run(suite);
	}
}
