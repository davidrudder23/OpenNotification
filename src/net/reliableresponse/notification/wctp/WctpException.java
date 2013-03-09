/*
 * Created on May 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.wctp;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WctpException extends Exception {
	
	public WctpException (String code, String message) {
		super ("Code: "+code+", "+message);
	}

	public WctpException (int code, String message) {
		super ("Code: "+code+", "+message);
	}

}
