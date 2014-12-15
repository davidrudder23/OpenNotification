package net.reliableresponse.notification.broker.impl.clustered;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.StringUtils;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * This class controls which active service, like IM or POP, is tied to
 * @author David Rudder &lt; david@reliableresponse.net &gt;
 * 
 * Copyright 2005 - Reliable Response, LLC
 */
public class ClusteredServiceManager {
	
	static ClusteredServiceManager instance = null;
	
	Hashtable owners;
	String myHost;
	
	private ClusteredServiceManager () {
		owners = new Hashtable();
		myHost = BrokerFactory.getConfigurationBroker().getStringValue("cluster.name", "");
	}
	
	public static ClusteredServiceManager getInstance() {
		if (instance == null) {
			instance = new ClusteredServiceManager();
		}
		return instance;
	}
	
	public boolean isOwner(String identifier) {
		
		String knownHost = (String)owners.get(identifier);
		if ((knownHost != null) && (knownHost.equalsIgnoreCase(myHost))) {
			return true;
		}
		return false;
		
	}
	
	private boolean checkServer (String url, String identifier) {
		try {
			String endpoint =url+"/ClusterManager.jws";
			  
			Service service = new Service();
			Call call = (Call) service.createCall();
				  
			call.setTargetEndpointAddress( new java.net.URL(endpoint) );
			call.setOperationName(new QName("http://soapinterop.org/", "doYouOwn"));
				  
			boolean ret = ((Boolean) call.invoke( new Object[] { identifier } )).booleanValue();
			
			if (ret) {
				owners.put (identifier, url);
			}
			BrokerFactory.getLoggingBroker().logDebug (url +" will "+(ret?"not":"")+" run "+identifier);
			
			return ret;
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e.getMessage());
		}
		
		return false;
	}

	public boolean willRun (String identifier) {
		BrokerFactory.getLoggingBroker().logDebug("Checking if this machine, "+myHost+" owns "+identifier);
		if (myHost == null) return true;

		if (isOwner(identifier)) {
			BrokerFactory.getLoggingBroker().logDebug("This machine owns "+identifier);
			return true;
		}
		String knownHost = (String)owners.get(identifier);

		BrokerFactory.getLoggingBroker().logDebug("This machine thinks "+knownHost+" owns "+identifier);
		if (!StringUtils.isEmpty(knownHost)) {
			if (checkServer(knownHost, identifier)) {
				BrokerFactory.getLoggingBroker().logDebug(knownHost+" owns "+identifier);
				return false;
			}
		}
		List<String> urls = BrokerFactory.getConfigurationBroker().getStringValues("cluster.server");
		for (String url: urls) {
			if (!StringUtils.isEmpty(url)) {
				BrokerFactory.getLoggingBroker().logDebug("Checking if "+url+" owns "+identifier);
				if (checkServer(url, identifier)) {
					BrokerFactory.getLoggingBroker().logDebug(url+" owns "+identifier);
					return false;
				}
			}			
		}

		owners.put (identifier, myHost);
		return true;
	}
	
	public String getURLForService (String identifier) {
		if (isOwner(identifier)) return myHost;
		String knownHost = (String)owners.get(identifier);

		if (!StringUtils.isEmpty(knownHost)) {
			if (checkServer(knownHost, identifier))
				return knownHost;
		}
		List<String> urls = BrokerFactory.getConfigurationBroker().getStringValues("cluster.server");
		for (String url: urls) {
			if (!StringUtils.isEmpty(url)) {
				if (checkServer(url, identifier))
					return url;
			}			
		}

		owners.put (identifier, myHost);
		return myHost;
	}
	
	public void sendNotificationToDevice (
			Notification notification,
			String serviceName, 
			String message,
			String deviceUuid) {
		
		String recipientUuid = notification.getRecipient().getUuid();
		String subject = notification.getSubject();
		String senderName = notification.getSender().toString();
		if (notification.isPersistent()) {
			sendPersistrentNotificationToDevice(serviceName, notification.getUuid(), deviceUuid);
		} else {
			sendNonpersistrentNotificationToDevice(serviceName, recipientUuid, subject, message, senderName, deviceUuid);
		}
	} 

	public void sendPersistrentNotificationToDevice (
			String serviceName,
			String notificationUuid,
			String deviceUuid) {
		try {
			String url = getURLForService(serviceName);
			String endpoint =url+"/ClusterManager.jws";
			  
			Service service = new Service();
			Call call = (Call) service.createCall();
				  
			call.setTargetEndpointAddress( new java.net.URL(endpoint) );
			call.setOperationName(new QName("http://soapinterop.org/", "sendPersistentNotificationToDevice"));
				  
			call.invoke( new Object[] { notificationUuid, deviceUuid } );
			
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
		}
		
	}
	public void sendNonpersistrentNotificationToDevice (
			String serviceName, 
			String recipientUuid,
			String subject, String message,
			String senderName,
			String deviceUuid) {
		try {
			String url = getURLForService(serviceName);
			String endpoint =url+"/ClusterManager.jws";
			  
			Service service = new Service();
			Call call = (Call) service.createCall();
				  
			call.setTargetEndpointAddress( new java.net.URL(endpoint) );
			call.setOperationName(new QName("http://soapinterop.org/", "sendNonpersistentNotificationToDevice"));
				  
			call.invoke( new Object[] { recipientUuid, subject, message, senderName, deviceUuid } );
			
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
