/*
 * Created on Aug 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.device;

import java.util.Vector;

/**
 * This is a storage class for allowing devices to specify what options they need
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class DeviceSetting {
	String name;
	Class type;
	Object defaultValue;
	boolean required;
	Vector options;
	
	public DeviceSetting (String name, Class type, Object defaultValue, boolean required, Vector options) {
		this.name = name;
		this.type = type;
		this.required = required;
		this.defaultValue = defaultValue;
		this.options = options;
		
	}
	
	public String getName() {
		return name;
	}
	
	public Class getType() {
		return type;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public Vector getOptions() {
		return options;
	}

}
