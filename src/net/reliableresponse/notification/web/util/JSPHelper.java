/*
 * Created on Oct 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.util;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletRequest;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class JSPHelper {

	static SimpleDateFormat formatter = null;
	
	public static String getUUIDFromAction(ServletRequest request, String actionName) {
		// Look for pending actions
		Enumeration parameterNames = request.getParameterNames();
		String uuid = null;
		while ((parameterNames.hasMoreElements()) && (uuid == null)) {
			String name = (String)parameterNames.nextElement();
			if (name.startsWith (actionName)) {
				uuid = name.substring (actionName.length(), name.length());
				if ((uuid.endsWith(".x")) ||(uuid.endsWith(".y"))) {
					uuid = uuid.substring (0, uuid.length()-2);
				}
			}
		}
		
		return uuid;
	}
	
	public static String[] getParameterEndings(ServletRequest request, String beginning) {
		// Look for pending actions
		Enumeration parameterNames = request.getParameterNames();
		Vector endings = new Vector();
		while (parameterNames.hasMoreElements()) {
			String name = (String)parameterNames.nextElement();
			if (name.startsWith (beginning)) {
				name = name.substring (beginning.length(), name.length());
				if (name.endsWith(".x")) {
					name = name.substring (0, name.length()-2);
				}
				
				if (!name.endsWith(".y")) {
					endings.addElement(name);					
				}
			}
		}
		
		return (String[])endings.toArray(new String[0]);
	}
	
	public static String getPageNumFromAction(ServletRequest request, String actionName) {
		return getUUIDFromAction(request, actionName);
	}
	
	public static String getVariableFromAction(ServletRequest request, String actionName) {
		return getUUIDFromAction(request, actionName);
	}
	
	public static SimpleDateFormat getDateFormatter() {
		if (formatter == null) {
			formatter = new SimpleDateFormat ("HH:mm:ss z");

		}
		return formatter;
	}
}
