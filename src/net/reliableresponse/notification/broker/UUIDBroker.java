/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface UUIDBroker {
	
	public String getUUID(Object object);
	public String getUUID();
	
	public boolean isUuidValid(String uuid);
	
	public final static int UUID_LENGTH = 7;

}
