/*
 * Created on Apr 20, 2009
 *
 *Copyright Reliable Response, 2009
 */
package net.reliableresponse.notification.providers;

import java.util.Hashtable;

import winterwell.jtwitter.Twitter;

import net.kano.joscar.snaccmd.loc.GetInfoCmd;
import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.TwitterDevice;

public class TwitterNotificationProvider extends AbstractNotificationProvider {

	public static TwitterNotificationProvider instance = null;
	
	public TwitterNotificationProvider() {
		
	}
	
	public static TwitterNotificationProvider getInstance() {
		if (instance == null) {
			instance = new TwitterNotificationProvider();
		}
		
		return instance;
	}
	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		return "Twitter";
	}

	public Hashtable getParameters(Notification notification, Device device) {
		return new Hashtable();
	}

	public String[] getResponses(Notification notification) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(Hashtable params) throws NotificationException {
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		if (!(device instanceof TwitterDevice)) {
			throw new NotificationException(NotificationException.NOT_ACCEPTABLE, "Supplied device is not a Twitter device");
		}
		
		TwitterDevice twit = (TwitterDevice)device;
		
		Twitter twitter = new Twitter(twit.getUsername(), twit.getPassword());
		Twitter.Status status = twitter.setStatus(notification.getSubject()+": "+notification.getDisplayText());
		Hashtable tracking = new Hashtable();
		tracking.put ("ID", status.getId()+"");
		tracking.put ("Text", status.getText());
		BrokerFactory.getLoggingBroker().logDebug("Twitter status for update to "+twit.getUsername()+" is "+status.getId()+":"+status.getText());
		return tracking;
	}

}
