/*
 * Created on Nov 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractSchedule implements Schedule,
		UniquelyIdentifiable {

	String uuid = null;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.UniquelyIdentifiable#getUuid()
	 */
	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		
		return uuid;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.UniquelyIdentifiable#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
