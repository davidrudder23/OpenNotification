/*
 * Created on Dec 8, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TwilioTelephoneNotificationProvider;

public class TwilioTelephoneDevice extends TelephoneDevice {

	public NotificationProvider getNotificationProvider() {
		TwilioTelephoneNotificationProvider twilioProvider = new TwilioTelephoneNotificationProvider();
		return twilioProvider;
	}


}
