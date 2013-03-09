/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NotificationException extends Exception {
	
	/**
	 * Errors within 400-499 are non-recoverable
	 */
	public static final int FAILED = 400;
	public static final int INTERNAL_ERROR = 401;
	public static final int NOT_FOUND = 404;
	public static final int NOT_ALLOWED = 405;
	public static final int NOT_ACCEPTABLE = 406;
	
	/**
	 * Errors within 300-399 are recoverable
	 */
	public static final int TEMPORARILY_FAILED = 300;
	public static final int BUSY = 301;
	public static final int UNAVAILABLE = 302;
	
	int code;
	public NotificationException (int code, String message) {
		super (message);
		
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

}
