/*
 * Created on Dec 3, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface DialogicMessage {
	
	/**
	 * Use this to get the next message, given a keypress
	 * The identifier should be 0-9, * or #
	 * 
	 * @param identifier The phone touchpad key pressed
	 * @return The next message
	 */
	public DialogicMessage getNextMessage (String identifier);
	
	public String getWaveFilename();
	
	public String getAsteriskFilename();
	
	public int getExpectedDigits();
		
}
