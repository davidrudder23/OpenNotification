/*
 * Created on Sep 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.device.Device;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface DeviceBroker {
	
	public String[] getDeviceClassNames();
	
	public Device getDeviceByUuid(String uuid);
	
	public void addDeviceType (String className, String name);
	
	public void removeDeviceType (String name);
	
	public void removeDevice (String uuid);
	
	public void updateSetting (Device device, String setting, String value);
	
}
