/*
 * Created on Apr 20, 2009
 *
 *Copyright Reliable Response, 2009
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TwitterNotificationProvider;

public class TwitterDevice extends AbstractDevice {
	
	private String username;
	private String password;

	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Username", String.class, null, true, null);
		settings[1] = new DeviceSetting ("Password", String.class, "", true, null);
		return settings;
	}

	public String getDescription() {
		return "A device for posting notifications to Twitter";
	}

	public String getName() {
		return "Twitter";
	}
	
	public String toString() {
		return getName();
	}

	public NotificationProvider getNotificationProvider() {
		return TwitterNotificationProvider.getInstance();
	}

	public Hashtable getSettings() {

		Hashtable settings = new Hashtable();
		
		settings.put ("Username", username);
		settings.put ("Password", password);
		return settings;
		
	}

	public String getShortIdentifier() {
		return "Twitter";
	}

	public void initialize(Hashtable options) {
		username = (String)options.get("Username");
		password = (String)options.get("Password");
		
		if (username==null) username="";
		if (password==null) password="True";
		

	}

	public boolean supportsDeviceStatus() {
		return false;
	}

	public boolean supportsMessageStatus() {
		return false;
	}

	public boolean supportsReceivingAudio() {
		return false;
	}

	public boolean supportsReceivingImages() {
		return false;
	}

	public boolean supportsReceivingText() {
		return false;
	}

	public boolean supportsReceivingVideo() {
		return false;
	}

	public boolean supportsSendingAudio() {
		return false;
	}

	public boolean supportsSendingImages() {
		return false;
	}

	public boolean supportsSendingText() {
		return true;
	}

	public boolean supportsSendingVideo() {
		return false;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
