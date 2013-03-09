/*
 * Created on Dec 3, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class AbstractDialogicMessage implements DialogicMessage {
	Hashtable messages;
	

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getNextMessage(java.lang.String)
	 */
	public DialogicMessage getNextMessage(String identifier) {
		BrokerFactory.getLoggingBroker().logDebug("Getting message for "+identifier);
		if (messages == null) messages = new Hashtable();
		DialogicMessage message =(DialogicMessage) messages.get(identifier);
		if (message == null) {
			return this;
		}
		BrokerFactory.getLoggingBroker().logDebug("Returning "+message);
		return  message;
	}
	
	public void addMessage (DialogicMessage message, String identifier) {
		if (messages == null) messages = new Hashtable();
		messages.put (identifier, message);
	}
	
	public String getSoundsDirectory() {
		String dir = BrokerFactory.getConfigurationBroker().getStringValue("telephone.sounds.directory", 
				BrokerFactory.getConfigurationBroker().getStringValue("tomcat.location") +
			"/webapps/notification/sounds/");
		return dir;
	}
}
