/*
 * Created on Nov 30, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.pop;

import java.util.Hashtable;

import javax.mail.Message;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PopMessage {
	Message message;
	Hashtable params;
	
	public PopMessage (Message message) {
		this.message = message;
		params = new Hashtable();
	}
	
	public void setParam(String name, String value) {
		params.put (name, value);
	}
	
	public String getParam(String name) {
		return (String)params.get(name);
	}
	
	public Message getMailMessage() {
		return message;
	}

}
