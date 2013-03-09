/*
 * Created on Jul 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.usermgmt;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class InvalidGroupException extends Exception {
	String message;
	
	public InvalidGroupException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String toString() {
		return "InvalidGroupException: "+message;
	}
}
