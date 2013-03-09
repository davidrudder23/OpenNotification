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
public class SNMPFreeMemoryGetHandler implements SNMPGetHandler {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.snmp.SNMPGetHandler#handleGet(java.lang.String)
	 */
	public SnmpVarBind handleGet(String oid) {
		System.gc();
		SnmpVarBind outVar = new SnmpVarBind(oid);
		long freeMem = Runtime.getRuntime().freeMemory();
		SnmpSyntax value = new SnmpUInt32(freeMem);
		outVar.setValue(value);
		return outVar;
	}

}
