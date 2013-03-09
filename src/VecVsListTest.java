import java.io.FileInputStream;
import java.util.ArrayList;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;

/*
 * Created on Oct 12, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class VecVsListTest {

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		int count = 2000;

		SortedVector vector = new SortedVector();
		ArrayList list = new ArrayList();
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setFirstName(i+"");
			user.setLastName(i+"");
			vector.addElement(user, false);
		}
		long end = System.currentTimeMillis();
		System.out.println ("Vec add took "+(end-start)+" millis");
		start = System.currentTimeMillis();
		vector.sort();
		end = System.currentTimeMillis();
		System.out.println ("Sort took "+(end-start)+" millis");

		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setFirstName(i+"");
			user.setLastName(i+"");
			list.add(user);
		}
		end = System.currentTimeMillis();
		System.out.println ("List add took "+(end-start)+" millis");

		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			vector.elementAt(i);
		}
		end = System.currentTimeMillis();
		System.out.println ("Vec get took "+(end-start)+" millis");

		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			list.get(i);
		}
		end = System.currentTimeMillis();
		System.out.println ("List get took "+(end-start)+" millis");

	}
}
