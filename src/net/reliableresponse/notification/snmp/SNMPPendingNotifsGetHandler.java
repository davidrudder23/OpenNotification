/*
 * Created on Dec 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.snmp;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SNMPPendingNotifsGetHandler implements SNMPGetHandler {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.snmp.SNMPGetHandler#handleGet(java.lang.String)
	 */
	public SnmpVarBind handleGet(String oid) {
		SnmpVarBind outVar = new SnmpVarBind(oid);
		int numNotifs = BrokerFactory.getNotificationBroker().getNumPendingNotifications();
		outVar.setValue(new SnmpUInt32(numNotifs));
		return outVar;
	}

}
