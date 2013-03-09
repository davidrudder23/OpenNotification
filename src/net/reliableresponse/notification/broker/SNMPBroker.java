/*
 * Created on Dec 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker;

import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface SNMPBroker {

	public SnmpVarBind[] handleGet (SnmpPduPacket packet);
}
