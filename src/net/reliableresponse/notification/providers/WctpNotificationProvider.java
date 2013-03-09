/*
 * TODO: The instancesByTrackingNumber will increase unbounded unless fixed
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.device.TAPDevice;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
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
public class WctpNotificationProvider extends AbstractNotificationProvider {

	String url, parameter, contentType;
	
	String trackingNumber;
	
	String sender;
	
	String username, password;

	WctpLibrary library;

	long lastUpdated;

	long pollTime;
	
	int status = Notification.PENDING;

	private static Hashtable libraries;
	
	private static Hashtable responses;

	public WctpNotificationProvider(String url, String parameter, String contentType) {
		init (url, parameter, contentType, null, null);
	}

	public WctpNotificationProvider(String url, String parameter, String contentType, String username, String password) {
		init (url, parameter, contentType, username, password);
	}
	
	public WctpNotificationProvider() {	
	}
	
	public void init (Hashtable params) {
		String url = (String)params.get("url");
		String parameter = (String)params.get("parameter");
		String contentType = (String)params.get("contentType");
		String trackingNumber = (String)params.get("tracking number");
		String username = (String)params.get("username");
		String password = (String)params.get("password");
		setTrackingNumber(trackingNumber);
		init (url, parameter, contentType, username, password);
	}
	
	public void init (String url, String parameter, String contentType, String username, String password) {
		this.sender = BrokerFactory.getConfigurationBroker().getStringValue("wctp.replyto");
		if ((sender == null) || (sender.length() <= 0)) {
			sender = "Reliable Response Notification";
		}
		this.url = url;
		this.parameter = parameter;
		this.contentType = contentType;
		this.username = username;
		this.password = password;
		
		if (libraries == null) {
			libraries = new Hashtable();
		}
		
		library = (WctpLibrary)libraries.get (url);
		library = new WctpLibrary(url, parameter, contentType, username, password);
		if (library == null) {
			library = new WctpLibrary(url, parameter, contentType, username, password);
			libraries.put (url, library);
		}

		if (responses == null) {
			responses = new Hashtable();
		}

		lastUpdated = 0;
		pollTime = 60000;
	}
	
	public Hashtable getParameters (Notification notification, Device device) {
		Hashtable params = new Hashtable();
		
		if (url == null) url = "";
		params.put ("url", url);
		if (parameter == null) parameter = "";
		params.put ("parameter", parameter);
		if (contentType == null) contentType= "";
		params.put ("contentType", contentType);
		params.put ("tracking number", "000000");
		if (username != null) {
			params.put ("username", username);
		}
		if (password != null) {
			params.put ("password", password);
		}
		return params;
	}
	
	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException {
		User user = (User)notification.getRecipient();
		String summary = notification.getSubject();
		String[] optionsArray = notification.getSender().getAvailableResponses(notification);
		Vector options = new Vector();
		for (int i = 0; i < optionsArray.length; i++) {
			options.addElement(optionsArray[i]);
		}
		String message = notification.getDisplayText();
		if (StringUtils.isEmpty(message)) {
			message = notification.getSubject();
		}

		String response = null;
		try {
			response = library.sendMessage(((PagerDevice) device)
					.getNormalizedNumber(), sender, message, options);
		} catch (IOException e) {
			if (((PagerDevice)device).doFailoverToModem()) {
				TAPDevice tapDevice = new TAPDevice();
				tapDevice.initialize(getParameters(notification, device));
				tapDevice.getNotificationProvider().sendNotification(notification, tapDevice);
			} else {
				throw new NotificationException(NotificationException.TEMPORARILY_FAILED, e.getMessage());
			}
		}

		try {
			ClientResponse cr = library.readClientResponse(response);
			if (((int)cr.getCode()/100) == 4) {
				throw new NotificationException(cr.getCode(), cr.getStatus());
			}
			if (((int)cr.getCode()/100) == 3) {
				throw new NotificationException(cr.getCode(), cr.getStatus());
			}

			Hashtable params = new Hashtable();
			setTrackingNumber(cr.getTrackingNumber());

			params.put ("url", url);
			if (parameter != null)
				params.put ("parameter", parameter);
			params.put ("contentType", contentType);
			params.put ("tracking number", getTrackingNumber());
			if (username != null) {
				params.put ("username", username);
			}
			if (password != null) {
				params.put ("password", password);
			}
			return params;
		} catch (WctpException e1) {
			//BrokerFactory.getLoggingBroker().logError(e1);
			throw new NotificationException(400, e1.getMessage());
		}
	}

	private synchronized ClientResponse getClientResponse(Notification page) {
		ClientResponse clientResponse = null;
		if (System.currentTimeMillis() > (lastUpdated + pollTime)) {
			try {
				Device[] devices = ((User) page.getRecipient())
						.getDevices();
				String recipientPager = "";
				if (devices.length > 0) {
					int i = 0;
					while ((i < devices.length) && (!(devices[i] instanceof PagerDevice))) i++;
					if (i<devices.length) {
						recipientPager = ((PagerDevice)devices[i]).getNormalizedNumber();
					}	
				}

				String clientQuery = library.formatWCTPClientQuery(recipientPager,
					 sender, getTrackingNumber());
				BrokerFactory.getLoggingBroker().logDebug("query  = "+clientQuery);
				String response = library.sendMessage(clientQuery);
				BrokerFactory.getLoggingBroker().logDebug("response = " + response);
				clientResponse = library.readClientQueryResponse(response);
				if ((clientResponse.getCode()/100) == 4) {
					page.addMessage("Querying notification status failed because: "+clientResponse.getStatus()+".  Two-way communication may not work", null);
				}
				lastUpdated = System.currentTimeMillis();
			} catch (DOMException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (WctpException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		} else {
			BrokerFactory.getLoggingBroker().logDebug("Waiting " +((lastUpdated + pollTime) - System.currentTimeMillis())+" millis before checking wctp again");
		}

		return clientResponse;
	}

	/**
	 * 
	 * @param pageId
	 *            The ID of the notification previously sent
	 * @return A english-readable status. null if the message is unknown
	 */
	public int getStatus(Notification notification) {
		
		// Check to see if the send failed
		if (trackingNumber == null) trackingNumber = "000000";
		if (trackingNumber.equals("000000")) {
			return Notification.NORMAL;
		}
		ClientResponse response = getClientResponse(notification);
		BrokerFactory.getLoggingBroker().logDebug("Wctp Message #"+trackingNumber+" response = "+response);
		if (response == null) return status;
		
		status = getStatusFromResponse(response);
		
		return status;
	}
	
	private int getStatusFromResponse(ClientResponse response) {
		String[] messages = response.getMessages();
		for (int i = 0; i < messages.length; i++) {
			if (messages[i].toLowerCase().equals ("confirm")) {
				status= Notification.CONFIRMED;
				return status;
			}
			if (messages[i].toLowerCase().equals ("pass")) {
				status= Notification.CONFIRMED;
				return status;
			}
		}

		String statusString = response.getStatus();
		BrokerFactory.getLoggingBroker().logDebug("Wctp Message #"+trackingNumber+" returned "+statusString);
		if (statusString != null) {
			if (statusString.toLowerCase().equals ("confirmed")) {
				status = Notification.CONFIRMED; 
				return status;
			} else if (statusString.equalsIgnoreCase("queued")) {
				status = Notification.PENDING;
				return status;
			} else if (statusString.equalsIgnoreCase("delivered")) {
				status = Notification.DELIVERED;
				return status;
			} else if (statusString.toLowerCase().indexOf("failed")>=0) {
				status = Notification.FAILED;
				return status;
			}
		} 
		return status;
	}

	public String[] getResponses(Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("WCTP Getting responses");
		Vector clientResponses = new Vector();
		ClientResponse clientResponse = getClientResponse(notification);
		if (clientResponse != null) {
			
			status = getStatusFromResponse(clientResponse);
			Vector knownResponses = (Vector) responses.get(notification);
			if (knownResponses == null) knownResponses = new Vector();

			String[] messages = clientResponse.getMessages();
			BrokerFactory.getLoggingBroker().logDebug("We have "+knownResponses.size()+" known responses and "+messages.length+" messages");
			
			for (int i = knownResponses.size(); i < messages.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug("message["+i+"]="+messages[i]);
				clientResponses.addElement(messages[i]);
				knownResponses.addElement(messages[i]);
			}
			responses.put (notification, knownResponses);
		}
		return (String[]) clientResponses.toArray(new String[0]);
	}

	
	
	public boolean isConfirmed(Notification page) {
		String[] responses = getResponses(page);
		for (int i = 0; i < responses.length; i++) {
			if (responses[i].toLowerCase().equals ("confirm")) {
				return true;
			}
		}
		return false;
	}

	public boolean isPassed(Notification page) {
		String[] responses = getResponses(page);
		for (int i = 0; i < responses.length; i++) {
			if (responses[i].toLowerCase().equals ("pass")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param pageId
	 * @return Whether the cancellation was successfull
	 */
	public boolean cancelPage(Notification page) {
		//TODO: First fill in the required functions to the WctpLibary
		return false;
	}
	
	

	public String getTrackingNumber() {
		return trackingNumber;
	}
	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}
	public String getName() {
		return "Online Pager Provider";
	}	
	
	public String toString() {
		return "Two-Way Pager";
	}
	
	public WctpLibrary getWctpLibrary() {
		return library;
	}

}
