/*
 * Created on Dec 8, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TwilioNotificationProvider;

public class TwilioDevice extends TelephoneDevice {

	public NotificationProvider getNotificationProvider() {
		TwilioNotificationProvider twilioProvider = new TwilioNotificationProvider();
		return twilioProvider;
	}


}
