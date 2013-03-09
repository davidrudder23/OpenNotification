/*
 * Created on Oct 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NotificationMessage {

	private byte[] message;
	private String addedby;
	private Date addedon;
	String contentType;
	String filename;
	
	public static String NOTIFICATION_CONTENT_TYPE="x-application/rrn-notification";
	
	public NotificationMessage(String message, String addedby, Date addedon) {
		if (message == null) {
			init (new byte[0], addedby, addedon, "text/plain");			
		} else {
			init (message.getBytes(), addedby, addedon, "text/plain");
		}
	}

	public NotificationMessage(byte[] message, String addedby, Date addedon, String contentType) {
		init (message, addedby, addedon, contentType);
	}
	
	private void init (byte[] message, String addedby, Date addedon, String contentType) {
		this.message = message;
		this.addedby = addedby;
		this.addedon = addedon;
		this.contentType = contentType;
		this.filename = "";
	}
	
	
	public String getAddedby() {
		return addedby;
	}
	
	public void setAddedby(String addedby) {
		this.addedby = addedby;
	}
	
	public void setAddedby(User addedby) {
		this.addedby = addedby.getUuid();
	}

	public Date getAddedon() {
		return new Date(addedon.getTime());
	}
	public void setAddedon(Date addedon) {
		this.addedon = addedon;
	}
	public String getMessage() {
		try {
			return new String(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new String (message);
		}
	}
	
	public byte[] getContent() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message.getBytes();
	}
	
	public void setMessage(byte[] message) {
		this.message = message;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilename() {
		if (StringUtils.isEmpty(filename)) {
			return "File";
		}
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String toString() {
		User senderMember = BrokerFactory.getUserMgmtBroker().getUserByUuid(getAddedby());
		String sender = "an unknown sender";
		if (senderMember != null)
			sender = senderMember.toString();
		return "From "+sender+" on "+getAddedon()+": "+getMessage();
	}
}
