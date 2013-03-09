/*
 * Created on Aug 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.device;


/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class OneWayPagerDevice extends PagerDevice implements Device {

	public OneWayPagerDevice () {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "A device to send pages to a one-way alphanumberic pagers at "+getPagerNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.device.Device#supportsSendingText()
	 */
	public boolean supportsSendingText() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingText()
	 */
	public boolean supportsReceivingText() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.device.Device#supportsDeviceStatus()
	 */
	public boolean supportsDeviceStatus() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.device.Device#supportsMessageStatus()
	 */
	public boolean supportsMessageStatus() {
		return false;
	}
}