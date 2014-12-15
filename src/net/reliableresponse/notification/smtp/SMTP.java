// smtp - start listning for smtp clients

package net.reliableresponse.notification.smtp;


import java.util.List;

import net.reliableresponse.notification.broker.BrokerFactory;

public class SMTP extends Thread {
	static List<String> vNamesOfPlugIns = null; // the names of each plugin

	static List<String> vServerNames; // the names of all the servers
	
	public static boolean shutdown = false;

	// that are local

	public void run() {
		int i;
		List<String> vPorts = null;
		String strCurrentLine;
		String strTemp;

		// load the names of all the plugins from the config file
		vNamesOfPlugIns = BrokerFactory.getConfigurationBroker()
				.getStringValues("smtp.plugin");

		// load the names of all the local servers
		vServerNames = BrokerFactory.getConfigurationBroker().getStringValues(
				"smtp.server.hostname");
		// make sure at least one plugin is wanted
		if ((vServerNames == null) || (vServerNames.size() == 0)) {
			BrokerFactory.getLoggingBroker().logError(
					"SMTP: No servers are local.  Mail will NOT be received");
			return;
		}

		// load what ports to listen to
		vPorts = BrokerFactory.getConfigurationBroker().getStringValues(
				"smtp.port");
		if ((vPorts == null) || (vPorts.size() == 0)) {
			BrokerFactory.getLoggingBroker().logInfo(
					"SMTP: no ports to listen to.  Mail will NOT be received");
			return;
		}

		for (String vPort: vPorts) {
			Start start = new Start(Integer.parseInt(vPort));
			start.setDaemon(true);
			start.start();
		}
	}
}

