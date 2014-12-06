/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.device.Device;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface NotificationProvider {

	public void init (Hashtable<String, String> params) throws NotificationException;
	/**
	 * 
	 * @param notification The notification to send
	 * @param device Which device to use
	 * @return A hashtable with enough information to recreate the active provider
	 * @throws NotificationException
	 */
	
	public Hashtable<String, String> sendNotification(Notification notification, Device device) throws NotificationException;
	
	/**
	 * If sendNotification throws an exception, use this function to get the params
	 * 
	 * @param notification
	 * @param device
	 * @return
	 */
	public Hashtable<String, String> getParameters (Notification notification, Device device);
	
	/**
	 * 
	 * @param notificationId The ID of the notification previously sent
	 * @return A english-readable status.  null if the message is unknown
	 */
	public int getStatus (Notification notification);

	/**
	 * Gets the responses to this notification. 
	 * @param notification The notification to inspect
	 * @return The responses to the notification
	 */
	public String[] getResponses(Notification notification);

	/**
	 * 
	 * @param notificationId
	 * @return Whether the cancellation was successfull
	 */

	public boolean cancelPage(Notification notification);

	public void setStatusOfSend (Notification notification, String status);
	
	/**
	 * This shows whether the initial send worked or not, and hopefully a little description
	 * although the contents depend on the provider 
	 * @param notification
	 * @return
	 */
	public String getStatusOfSend (Notification notification);
	
	/**
	 * 
	 * @return A descriptive name of this provider
	 */
	public String getName();

}
