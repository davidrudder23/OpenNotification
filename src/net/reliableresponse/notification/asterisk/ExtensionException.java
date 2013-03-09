package net.reliableresponse.notification.asterisk;

import org.asteriskjava.fastagi.AgiException;

public class ExtensionException extends AgiException {
	public static final long serialVersionUID = 0;
	
	public ExtensionException(String message) {
		super (message);
	}
}
