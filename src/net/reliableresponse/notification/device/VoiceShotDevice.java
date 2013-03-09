/*
 * Created on Feb 16, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.AsteriskVoIPNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TelephoneNotificationProvider;
import net.reliableresponse.notification.providers.VoiceShotNotificationProvider;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class VoiceShotDevice extends TelephoneDevice {

	public NotificationProvider getNotificationProvider() {
		VoiceShotNotificationProvider voiceShotProvider = new VoiceShotNotificationProvider();
		return voiceShotProvider;
	}

}
