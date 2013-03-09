/*
 * Created on May 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.wctp;

import java.util.Vector;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClientResponse {

	int code;
	int pollTime;
	Vector messages;
	String status;
	String trackingNumber;
	
	public ClientResponse (int code, String status, String trackingNumber) {
		this.code = code;
		this.status = status;
		this.trackingNumber = trackingNumber;
		messages = new Vector();
		pollTime = 10000;
	}
	
	
	/**
	 * @return
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return
	 */
	public String[] getMessages() {
		return (String[])messages.toArray(new String[0]);
	}

	/**
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return
	 */
	public String getTrackingNumber() {
		return trackingNumber;
	}

	/**
	 * @param i
	 */
	public void setCode(int i) {
		code = i;
	}

	/**
	 * @param string
	 */
	public void addMessage(String string) {
		messages.addElement(string);
	}

	/**
	 * @param string
	 */
	public void setStatus(String string) {
		status = string;
	}

	/**
	 * @param i
	 */
	public void setTrackingNumber(String i) {
		trackingNumber = i;
	}
	
	
	public int getPollTime() {
		return pollTime;
	}
	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}
	public String toString() {
		return "WctpPage number "+getTrackingNumber();
	}

}
