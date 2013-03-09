/*
 * Created on Dec 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.snmp;

import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SNMPSysNameGetHandler implements SNMPGetHandler {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.snmp.SNMPGetHandler#handleGet(java.lang.String)
	 */
	public SnmpVarBind handleGet(String oid) {
		SnmpVarBind outVar = new SnmpVarBind(oid);
		SnmpSyntax value = new SnmpOctetString("Reliable Response Notification".getBytes());
		outVar.setValue(value);
		return outVar;
	}

}
