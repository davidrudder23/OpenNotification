import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;

/*
 * Created on Nov 23, 2004
 *
 *Copyright Reliable Response, 2004
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class LoadTimeTest {

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		BrokerFactory.getUserMgmtBroker().getUsers(1024, 0, new User[1024]);
		SortedVector vec = new SortedVector();
		long start = System.currentTimeMillis();
		int numLoaded = 0;
		int numGroups = 0;
		
		if (1==1) {
			User[] users = new User[8196];
			numLoaded = BrokerFactory.getUserMgmtBroker().getUsers(users.length, 0, users);
			long end = System.currentTimeMillis();
			System.out.println ("Loaded "+numLoaded+" users in "+(end-start)+" millis");
			start = System.currentTimeMillis();
			Group[] groups = new Group[8196];
			numGroups = BrokerFactory.getGroupMgmtBroker().getGroups(groups.length, 0, groups);
			end = System.currentTimeMillis();
			System.out.println ("Loaded "+numGroups+" groups in "+(end-start)+" millis");
		} else {
			User[] users = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType("net.reliableresponse.notification.device.TwoWayPagerDevice");
			numLoaded = users.length;
		}
	}
}
