/*
 * Created on Dec 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.snmp;

import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SNMPSysUptimeGetHandler implements SNMPGetHandler {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.snmp.SNMPGetHandler#handleGet()
	 */
	public SnmpVarBind handleGet(String oid) {
		SnmpVarBind outVar = new SnmpVarBind(oid);
		long millis = System.currentTimeMillis() - SNMPLibrary.getInstance().getStartTime();
		millis = millis/10; // convert from 1000th of a second to 100th
		SnmpSyntax value = new SnmpUInt32(millis);
		outVar.setValue(value);
		return outVar;
	}

}
