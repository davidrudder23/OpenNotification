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
public class WelcomeDialogicMessage extends AbstractDialogicMessage {

	public WelcomeDialogicMessage() {
		addMessage(new ConfirmDialogicMessage(), "1");
		addMessage(new PassDialogicMessage(), "2");
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"welcome.wav";
	}
	
	public String getAsteriskFilename() {
		return "welcome";
	}


	public int getExpectedDigits() {
		return 1;
	}
}
