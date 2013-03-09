/*
 * Created on Dec 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.SNMPBroker;
import net.reliableresponse.notification.snmp.SNMPGetHandler;

import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PropertiesSNMPBroker implements SNMPBroker {

	public SnmpVarBind[] handleGet(SnmpPduPacket packet) {
		Vector binds = new Vector();
		
		for (int i = 0; i < packet.getLength(); i++) {
			SnmpVarBind var = packet.getVarBindAt(i);
			String oid = var.getName().toString();
			BrokerFactory.getLoggingBroker().logDebug("Received SNMP Get "+oid);

			String className = BrokerFactory.getConfigurationBroker().getStringValue("snmp.get.handler."+oid);
			BrokerFactory.getLoggingBroker().logDebug("SNMP Get class name = "+className);
			if (className != null) {
				try {
					Class clazz = Class.forName(className);
					SNMPGetHandler getHandler = (SNMPGetHandler) clazz
							.newInstance();
					binds.addElement(getHandler.handleGet(oid));
				} catch (ClassNotFoundException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (InstantiationException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (IllegalAccessException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		
		return (SnmpVarBind[])binds.toArray(new SnmpVarBind[0]);
	}
}
