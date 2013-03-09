/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class EmailPagerNotificationProvider extends AbstractNotificationProvider {
	String id;
	
	public EmailPagerNotificationProvider() {
	}
	
	public void init(Hashtable params) {	
	}	
	
	public abstract String getEmailAddress (PagerDevice device);
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#sendPage(net.reliableresponse.notification.usermgmt.User, java.lang.String, java.util.Vector)
	 */
	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException{
			User user = (User)notification.getRecipient();
			NotificationSender sender = notification.getSender(); 
			String summary = notification.getSubject();
			Vector options = notification.getOptions();
			String messageText = notification.getDisplayText();
			
			if (!(device instanceof PagerDevice)) {
				return null;
			}
			Hashtable parameters = SMTPNotificationProvider.sendEmail(device, notification, sender, summary, messageText, getEmailAddress((PagerDevice)device));

			return parameters;
	}
	
	

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable parameters = new Hashtable();
		parameters.put ("id", "000000");
		return parameters;
	}
	/**
	 * 
	 * @param pageId The ID of the notification previously sent
	 * @return A english-readable status.  null if the message is unknown
	 */
	public int getStatus (Notification page) {
		return Notification.PENDING;
	}
	
	public String[] getResponses(Notification page) {
		return new String[0];
	}
	
	public boolean isConfirmed(Notification page) {
		return false;
	}

	public boolean isPassed(Notification page) {
		return false;
	}
	
	/**
	 * 
	 * @param pageId
	 * @return Whether the cancellation was successfull
	 */
	public boolean cancelPage (Notification page) {
		// If it was sent, too late.  If not, then we can't cancel anyway
		return false;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
