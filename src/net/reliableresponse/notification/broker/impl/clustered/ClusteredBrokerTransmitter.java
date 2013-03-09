/*
 * Created on Jan 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.clustered;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ClusteredBrokerTransmitter {

	public static void sendInvalidate(String callName, String uuid) {
		BrokerFactory.getLoggingBroker().logDebug("Sending invalidate: "+callName+" uuid="+uuid);
		String[] urls = BrokerFactory.getConfigurationBroker().getStringValues("cluster.server");
		for (int i = 0; i < urls.length; i++) {
            try {
				String endpoint =urls[i]+"/ClusterManager.jws";
					  
				Service service = new Service();
				Call call = (Call) service.createCall();
					  
				call.setTargetEndpointAddress( new java.net.URL(endpoint) );
				call.setOperationName(new QName("http://soapinterop.org/", callName));
					  
				String ret = (String) call.invoke( new Object[] { uuid } );
				BrokerFactory.getLoggingBroker().logDebug("Invalidate return = "+ret);
			} catch (MalformedURLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (RemoteException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ServiceException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
            	  
		}
	}
	
	public static boolean ping (String serverName) {
        try {
			String endpoint =serverName+"/ClusterManager.jws";
				  
			Service service = new Service();
			Call call = (Call) service.createCall();
				  
			call.setTargetEndpointAddress( new java.net.URL(endpoint) );
			call.setOperationName(new QName("http://soapinterop.org/", "ping"));
				  
			Boolean ret = (Boolean) call.invoke( new Object[] {  } );
			return ret.booleanValue();
		} catch (MalformedURLException e) {
			BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
		} catch (RemoteException e) {
			BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
		} catch (ServiceException e) {
			BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
		}
		return false;
	}
}
