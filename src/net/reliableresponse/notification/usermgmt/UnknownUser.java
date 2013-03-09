/*
 * Created on Oct 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.usermgmt;

import java.util.Date;
import java.util.Hashtable;

import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class UnknownUser extends User {

	private String email;
	public UnknownUser() {
		email = ""; 
	}
	
	
	public synchronized boolean addDevice(Device device) {
		return false;
	}
	
	public void addEmailAddress(String emailAddress) {
		email = emailAddress;
	}
	
	public boolean equals(Object other) {
		return (other instanceof UnknownUser);
	}
	public Hashtable getAllInformation() {
		return new Hashtable();
	}
	public boolean getAutocommit() {
		return false;
	}
	public String getDepartment() {
		return "Reliable Response Notification";
	}
	public Device[] getDevices() {
		return new Device[0];
	}
	
	public Device getDeviceWithUuid(String uuid) {
		return null;
	}
	public String getEmailAddress() {
		return email;
	}
	public Date getEndTime() {
		return new Date();
	}
	public String getFirstName() {
		return "Unknown";
	}
	public Group[] getGroups() {
		return new Group[0];
	}
	public String getInformation(String type) {
		return null;
	}
	public String getLastName() {
		return "User";
	}
	public String getPagerEmail() {
		return null;
	}
	public String[] getPagerNumbers() {
		return new String[0];
	}
	public String getPhoneNumber() {
		return null;
	}
	public Date getStartTime() {
		return new Date();
	}
	public int getType() {
		// TODO Auto-generated method stub
		return super.getType();
	}
	public String getUuid() {
		return "000000";
	}
	public boolean isInPermanentCache() {
		return false;
	}
	public boolean removeDevice(Device device) {
		return false;
	}
	public boolean removePager(String pagerNumber) {
		return false;
	}
	public void setAutocommit(boolean autocommit) {
	}
	public void setDepartment(String string) {
	}
	public void setEmailAddress(String emailAddress) {
		this.email = emailAddress;
	}
	public void setEmailPrefix(String prefix) {
	}
	public void setEndTime(Date time) {
	}
	public void setFirstName(String string) {
	}
	public void setInformation(String type, String value) {
	}
	public void setInPermanentCache(boolean inPermanentCache) {
	}
	public void setLastName(String string) {
	}
	public void setPagerEmail(String string) {
	}
	public void setPhoneNumber(String string) {
	}
	public void setStartTime(Date time) {
	}
	public void setUuid(String string) {
	}
	public String toString() {
		if (!StringUtils.isEmpty(email)) {
			return email;
		}
		return "Unknown User";
	}
}
