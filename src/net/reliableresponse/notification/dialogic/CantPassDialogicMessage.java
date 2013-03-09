/*
 * Created on Aug 19, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.dialogic;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class CantPassDialogicMessage extends AbstractDialogicMessage {

	DialogicMessage previous;
	public CantPassDialogicMessage(DialogicMessage previous) {
		this.previous = previous;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"cantpass.wav";
	}

	public String getAsteriskFilename() {
		return "cantpass";
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
