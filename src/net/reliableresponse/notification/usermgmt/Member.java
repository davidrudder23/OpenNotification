/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import net.reliableresponse.notification.UniquelyIdentifiable;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface Member extends UniquelyIdentifiable{
	public static final int USER = 0;
	public static final int ESCALATION = 1;
	public static final int ONCALL = 2;
	public static final int BROADCAST = 3;

	public String getUuid();
	
	public void setUuid(String uuid);
	
	public int getType();
	
	public boolean isDeleted();

	public void setDeleted(boolean deleted);
	
	public void setInPermanentCache(boolean inPermanentCache);
	
	public boolean isInPermanentCache();
}
