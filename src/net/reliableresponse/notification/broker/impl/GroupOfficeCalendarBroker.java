/*
 * Created on Sep 12, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CalendarBroker;
import net.reliableresponse.notification.usermgmt.User;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class GroupOfficeCalendarBroker implements CalendarBroker {
	String url;
	boolean enabled;
	
	public GroupOfficeCalendarBroker() {
		boolean enabled = BrokerFactory.getConfigurationBroker().getBooleanValue("groupoffice", false);
		if (enabled) {
			url = BrokerFactory.getConfigurationBroker().getStringValue("groupoffice.url", "http://www.reliableresponse.net/groupoffice/freebusy.php");
		}
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.CalendarBroker#isCalendaringEnabled()
	 */
	public boolean isCalendaringEnabled() {
		return enabled;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.CalendarBroker#isInMeeting(net.reliableresponse.notification.usermgmt.User)
	 */
	public boolean isInMeeting(User user) {
		try {
			Service  service = new Service();
			Call     call    = (Call) service.createCall();
			call.setTargetEndpointAddress( new java.net.URL(url) );
			call.setOperationName(new QName("http://soapinterop.org/", "freebusy"));
			boolean ret = ((Boolean)(call.invoke (new Object[]{new Date()}))).booleanValue();
			return ret;
		} catch (MalformedURLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (RemoteException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ServiceException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.CalendarBroker#isOutOfOffice(net.reliableresponse.notification.usermgmt.User)
	 */
	public boolean isOutOfOffice(User user) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.CalendarBroker#isFree(net.reliableresponse.notification.usermgmt.User)
	 */
	public boolean isFree(User user) {
		return !(isInMeeting(user));
	}

	public static void main(String[] args ) {
		try {
			Service  service = new Service();
			Call     call    = (Call) service.createCall();
			call.setTargetEndpointAddress( new java.net.URL("http://www.reliableresponse.net/groupoffice/freebusy.php") );
			call.setOperationName(new QName("freebusy"));
			boolean ret = ((Boolean)(call.invoke (new Object[]{new Date()}))).booleanValue();
			System.out.println ("get "+ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
