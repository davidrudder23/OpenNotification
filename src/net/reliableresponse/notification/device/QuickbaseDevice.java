/*
 * Created on Oct 18, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.QuickBaseNotificationProvider;
import net.reliableresponse.notification.util.StringUtils;

public class QuickbaseDevice extends AbstractDevice {
	
	String dbid, username, password, relatedToFID, addedByFID, subjectFID, messageFID, photoFID, videoFID, documentFID;
	boolean sendConfirmation;

	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[11];
		settings[0] = new DeviceSetting ("DBID", String.class, null, true, null);
		settings[1] = new DeviceSetting ("User Name", String.class, null, true, null);
		settings[2] = new DeviceSetting ("Password", String.class, null, true, null);
		settings[3] = new DeviceSetting ("Parent Relation Field Name", String.class, null, true, null);
		settings[4] = new DeviceSetting ("Added By Field Name", String.class, null, true, null);
		settings[5] = new DeviceSetting ("Subject Field Name", String.class, null, true, null);
		settings[6] = new DeviceSetting ("Message Field Name", String.class, null, true, null);
		settings[7] = new DeviceSetting ("Photo Field Name", String.class, null, true, null);
		settings[8] = new DeviceSetting ("Video Field Name", String.class, null, true, null);
		settings[9] = new DeviceSetting ("Document Field Name", String.class, null, true, null);
		settings[10] = new DeviceSetting ("Send Confirmation", Boolean.class, null, true, null);
		return settings;
	}

	public String getDescription() {
		return "A device which sends notifications into QuickBase";
	}

	public String getName() {
		return "QuickBase";
	}

	public NotificationProvider getNotificationProvider() {
		// TODO Auto-generated method stub
		return new QuickBaseNotificationProvider();
	}

	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();
		settings.put("DBID", dbid);
		settings.put("User Name", username);
		settings.put("Password", password);
		settings.put("Parent Relation Field Name", relatedToFID);
		settings.put("Added By Field Name", addedByFID);
		settings.put("Subject Field Name", subjectFID);
		settings.put("Message Field Name", messageFID);
		settings.put("Photo Field Name", photoFID);
		settings.put("Video Field Name", videoFID);
		settings.put("Document Field Name", documentFID);
		settings.put("Send Confirmation", new Boolean(sendConfirmation));

		return settings;
	}

	public String getShortIdentifier() {
		return "QuickBase";
	}

	public void initialize(Hashtable options) {
		dbid = (String)options.get("DBID");
		username = (String)options.get("User Name");
		password = (String)options.get("Password");
		relatedToFID = (String)options.get("Parent Relation Field Name");
		addedByFID = (String)options.get("Added By Field Name");
		messageFID = (String)options.get("Message Field Name");
		subjectFID = (String)options.get("Subject Field Name");
		photoFID = (String)options.get("Photo Field Name");
		videoFID = (String)options.get("Video Field Name");
		documentFID = (String)options.get("Document Field Name");
		BrokerFactory.getLoggingBroker().logDebug("send confirmation var="+options.get("Send Confirmation"));
		Object sendConfirmObj = options.get("Send Confirmation");
		if (sendConfirmObj == null) {
			sendConfirmObj = Boolean.FALSE;
		}
		if (sendConfirmObj instanceof Boolean) {
			sendConfirmation = ((Boolean)sendConfirmObj).booleanValue();
		} else if (sendConfirmObj instanceof String) {
			String sendConfirmString = (String)sendConfirmObj;
			if (StringUtils.isEmpty(sendConfirmString)) {
				sendConfirmString = "false";
			}
			sendConfirmation = sendConfirmString.equalsIgnoreCase("true");
		}
		BrokerFactory.getLoggingBroker().logDebug("send confirmation="+sendConfirmation);
	}

	public boolean supportsDeviceStatus() {
		return true;
	}

	public String getDbid() {
		return dbid;
	}

	public void setDbid(String dbid) {
		this.dbid = dbid;
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

	public String getMessageFID() {
		return messageFID;
	}

	public void setMessageFID(String messageFID) {
		this.messageFID = messageFID;
	}

	public String getSubjectFID() {
		return subjectFID;
	}

	public void setSubjectFID(String subjectFID) {
		this.subjectFID = subjectFID;
	}

	public String getPhotoFID() {
		return photoFID;
	}

	public void setPhotoFID(String photoFID) {
		this.photoFID = photoFID;
	}
	
	public String getRelatedToFID() {
		return relatedToFID;
	}

	public void setRelatedToFID(String relatedToFID) {
		this.relatedToFID = relatedToFID;
	}

	public String getAddedByFID() {
		return addedByFID;
	}

	public void setAddedByFID(String addedByFID) {
		this.addedByFID = addedByFID;
	}

	public String getVideoFID() {
		return videoFID;
	}

	public void setVideoFID(String videoFID) {
		this.videoFID = videoFID;
	}

	public String getDocumentFID() {
		return documentFID;
	}

	public void setDocumentFID(String documentFID) {
		this.documentFID = documentFID;
	}
	
	

	public boolean sendConfirmation() {
		return sendConfirmation;
	}

	public void setSendConfirmation(boolean sendConfirmation) {
		this.sendConfirmation = sendConfirmation;
	}

	public boolean supportsMessageStatus() {
		return true;
	}

	public boolean supportsReceivingAudio() {
		return false;
	}

	public boolean supportsReceivingImages() {
		return true;
	}

	public boolean supportsReceivingText() {
		return true;
	}

	public boolean supportsReceivingVideo() {
		return false;
	}

	public boolean supportsSendingAudio() {
		return false;
	}

	public boolean supportsSendingImages() {
		return true;
	}

	public boolean supportsSendingText() {
		return true;
	}

	public boolean supportsSendingVideo() {
		return true;
	}
	
	public String toString() {
		return "QuickBase database "+dbid;
	}

}
