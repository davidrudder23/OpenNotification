import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

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
public class IndexTimeTest {

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		int numUsers = BrokerFactory.getUserMgmtBroker().getNumUsers();
		if (numUsers>200) numUsers=200;
		long start = System.currentTimeMillis();
		String[] uuids = new String[numUsers];
		BrokerFactory.getUserMgmtBroker().getUuids(numUsers, 0, uuids);
	
		for (int i = 0; i < numUsers; i++) {
			BrokerFactory.getUserMgmtBroker().getUserByUuid(uuids[i]);
		}
		long end = System.currentTimeMillis();
		System.out.println ("Loaded "+numUsers+" users in "+(end-start)+" millis");
	}
}
