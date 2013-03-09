/*
 * Created on Dec 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.dialogic;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CantFindDialogicMessage extends AbstractDialogicMessage {

	DialogicMessage previous;
	public CantFindDialogicMessage(DialogicMessage previous) {
		this.previous = previous;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.dialogic.DialogicMessage#getWaveFilename()
	 */
	public String getWaveFilename() {
		return getSoundsDirectory()+"cantfind.wav";
	}
	
	public String getAsteriskFilename() {
		return "cantfind";
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
