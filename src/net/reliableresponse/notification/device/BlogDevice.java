/*
 * Created on Dec 8, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.providers.BlogNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;

public class BlogDevice extends AbstractDevice {

	private String server, blogID, username, password;
	
	public void initialize(Hashtable options) {
		server = (String)options.get("Server Name");
		blogID = (String)options.get("Blog ID");
		username = (String)options.get("User Name");
		password = (String)options.get("Password");
	}

	public String getName() {
		return "Blog";
	}

	public String getDescription() {
		return "A Blogger-compatible blog";
	}

	public boolean supportsSendingText() {
		return true;
	}

	public boolean supportsSendingAudio() {
		return false;
	}

	public boolean supportsSendingImages() {
		return true;
	}

	public boolean supportsSendingVideo() {
		return false;
	}

	public boolean supportsReceivingText() {
		// TODO: Figure out how to get comments
		return false;
	}

	public boolean supportsReceivingAudio() {
		return false;
	}

	public boolean supportsReceivingImages() {
		return false;
	}

	public boolean supportsReceivingVideo() {
		return false;
	}

	public boolean supportsDeviceStatus() {
		return false;
	}

	public boolean supportsMessageStatus() {
		return false;
	}

	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[4];
		settings[0] = new DeviceSetting ("Server Name", String.class, null, true, null);
		settings[1] = new DeviceSetting ("Blog ID", String.class, null, true, null);
		settings[2] = new DeviceSetting ("User Name", String.class, null, true, null);
		settings[3] = new DeviceSetting ("Password", String.class, null, true, null);
		return settings;
	}

	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (server == null) {
			server = "";
		}
		
		if (blogID == null) {
			blogID = "";
		}

		if (username == null) {
			username = "";
		}
		if (password == null) {
			password = "";
		}

		settings.put ("Server Name", server);
		settings.put ("Blog ID", blogID);
		settings.put ("User Name", username);
		settings.put ("Password", password);
		return settings;
	}
	
	

	public String getBlogID() {
		return blogID;
	}

	public void setBlogID(String blogID) {
		this.blogID = blogID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public NotificationProvider getNotificationProvider() {
		return new BlogNotificationProvider();
	}

	public String getShortIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

}
