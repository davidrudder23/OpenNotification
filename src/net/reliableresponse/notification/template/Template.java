/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.template;

import java.util.Hashtable;

import net.reliableresponse.notification.Notification;

/**
 * This interface defines a template.  Templates are loaded with their
 * member type and sender specified as class names.  Look at the AbstractTemplate
 * implementation to see an example.
 * 
 * All Template classes must have an empty constructor and the ability to initialize 
 * fully out of the init(String, String) function.  
 * 
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface Template {
	
	public void init (String memberTypeClassname, String senderClassname);
	
	public String getTemplateContents();
	
	public String getMemberTypeClassname();
	
	public String getSenderClassname();
	
	public String processTemplate (Hashtable<String, String> input);

	public String processTemplate (String message);

	public boolean isValid (Notification notification);

}
