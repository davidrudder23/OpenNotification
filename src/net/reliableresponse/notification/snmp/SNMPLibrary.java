/*
 * Created on Dec 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.snmp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.opennms.protocols.snmp.SnmpAgentHandler;
import org.opennms.protocols.snmp.SnmpAgentSession;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SNMPLibrary implements SnmpAgentHandler{
	
	private static SNMPLibrary instance;
	private int port;
	private SnmpAgentSession session;
	long startTime;
	
	private SNMPLibrary() {
		try {
			boolean doSNMP = BrokerFactory.getConfigurationBroker().getBooleanValue("snmp");
			if (doSNMP) {
			
				startTime = System.currentTimeMillis();

				int port = BrokerFactory.getConfigurationBroker().getIntValue(
						"snmp.port");
				if (port == -1) {
					BrokerFactory
							.getLoggingBroker()
							.logWarn(
									"Could not read value of snmp.port, defaulting SNMP port to 2161");
					port = 2161;
				}
				BrokerFactory.getLoggingBroker().logDebug("SNMP Server startup on port "+port);
				session = new SnmpAgentSession(this, new SnmpPeer(null, port));
			}
		} catch (SocketException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}		
	}
	
	public void reset() {
		try {
			boolean doSNMP = BrokerFactory.getConfigurationBroker().getBooleanValue("snmp");
			if (doSNMP) {
			
				if (session != null) session.close();
				
				startTime = System.currentTimeMillis();

				int port = BrokerFactory.getConfigurationBroker().getIntValue(
						"snmp.port");
				if (port == -1) {
					BrokerFactory
							.getLoggingBroker()
							.logWarn(
									"Could not read value of snmp.port, defaulting SNMP port to 2161");
					port = 2161;
				}
				BrokerFactory.getLoggingBroker().logDebug("SNMP Server startup on port "+port);
				session = new SnmpAgentSession(this, new SnmpPeer(null, port));
			}
		} catch (SocketException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
	}
	
	public static SNMPLibrary getInstance() {
		if (instance == null) {
			instance = new SNMPLibrary();
		}
		
		return instance;
	}
	
	public void sendTrap (int priority, String message) {
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("snmp.traps")) {
			String hostname = BrokerFactory.getConfigurationBroker().getStringValue("snmp.traps.hostname");
			int port = BrokerFactory.getConfigurationBroker().getIntValue("snmp.traps.port");
			if (port == -1) port = 161;
			
			//SnmpTrapSession trapSession = new SnmpTrapSession()
		}		
	}

	
	public void SnmpAgentSessionError(SnmpAgentSession arg0, int arg1,
			Object arg2) {
	}
	public SnmpPduRequest snmpReceivedGet(SnmpPduPacket packet, boolean arg1) {
		
		BrokerFactory.getLoggingBroker().logDebug("Got snmp get request = "+packet);
		
		SnmpPduRequest request = new SnmpPduRequest(SnmpPduRequest.RESPONSE);
		
		SnmpVarBind[] returnBinds = BrokerFactory.getSnmpBroker().handleGet(packet);
		BrokerFactory.getLoggingBroker().logDebug("Got "+returnBinds.length+" return binds");
		if (returnBinds.length == 0) return null;
		
		for (int i = 0; i < returnBinds.length; i++) {
			request.addVarBind(returnBinds[i]);
		}
		
		return request;
	}
	
	public void snmpReceivedPdu(SnmpAgentSession arg0, InetAddress arg1,
			int arg2, SnmpOctetString arg3, SnmpPduPacket arg4) {
	}

	public SnmpPduRequest snmpReceivedSet(SnmpPduPacket arg0) {
		return null;
	}
	
	public void shutdown() {
		System.out.println("SNMP library shutdown");
		if (session != null)
			session.close();
	}
	
	public long getStartTime() {
		return startTime;
	}

	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		SNMPLibrary snmp = SNMPLibrary.getInstance();
		System.out.print ("Press enter to stop");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		snmp.shutdown();
	}

}
