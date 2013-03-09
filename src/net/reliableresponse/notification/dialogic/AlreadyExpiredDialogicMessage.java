/*
 * Created on Dec 6, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AlreadyExpiredDialogicMessage extends AbstractDialogicMessage {

	DialogicMessage previous;
	public AlreadyExpiredDialogicMessage(DialogicMessage previous) {
		this.previous = previous;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"expired.wav";
	}

	public String getAsteriskFilename() {
		return "expired";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getExpectedDigits()
	 */
	public int getExpectedDigits() {
		return 0;
	}	

	public DialogicMessage getNextMessage(String identifier) {
		return previous;
	}
}
