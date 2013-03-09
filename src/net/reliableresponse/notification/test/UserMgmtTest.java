/*
 * Created on Aug 11, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UserMgmtTest extends TestCase {

	int numUsersTotal;

	private static boolean initialized = false;

	public UserMgmtTest() {
		if (!initialized) {
			initialized = true;
			// Create a list of users
			numUsersTotal = 51;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {

	}

	public void testRetrieveAllUsers() {
		User[] users = new User[numUsersTotal];
		int size = BrokerFactory.getUserMgmtBroker().getUsers(users.length, 0,
				users);

		for (int i = 0; i < users.length; i++) {
			assertTrue("Broker didn't return all the users in the array",
					users[i] != null);
		}
	}

	public void testPageSize() {
		int totalSize = BrokerFactory.getUserMgmtBroker().getNumUsers();
		assertTrue(
				"Broker didn't return the correct number of total users.  "
						+ "Was " + totalSize+ " but should have been "
						+ numUsersTotal, totalSize == numUsersTotal);
		
		User[] users = new User[10];

		for (int page = 0; page < (numUsersTotal / users.length); page++) {
			int size = BrokerFactory.getUserMgmtBroker().getUsers(users.length,
					page, users);

			for (int i = 0; i < users.length; i++) {
				// Make sure there are users in all slots
				assertTrue("Broker didn't return all the users in the array",
						users[i] != null);

				// Do a sanity check on the name
				assertTrue("Broker returned the wrong user, " + users[i]
						+ " in slot " + page + "," + i, users[i].getFirstName()
						.equals("User" + ((page * users.length) + i)));
			}

		}
	}

	public static void main(String[] args) throws Exception {
		TestSuite suite = new TestSuite(UserMgmtTest.class);
		junit.textui.TestRunner.run(suite);
	}
}