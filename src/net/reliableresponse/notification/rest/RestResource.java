package net.reliableresponse.notification.rest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotificationException;

public interface RestResource {
	
	public String getRepresentation(String contentType, String method, HttpServletRequest request) throws NotificationException;
	
	public void doUpdate (String contentType, String method, ServletRequest request) throws NotificationException;

	public void doAdd (String contentType, String method, ServletRequest request) throws NotificationException;

	public void doDelete (String contentType, String method, ServletRequest request) throws NotificationException;

	boolean isValidResource();
	

}
