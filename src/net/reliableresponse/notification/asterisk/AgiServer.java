package net.reliableresponse.notification.asterisk;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.ClassNameMappingStrategy;
import org.asteriskjava.fastagi.DefaultAgiServer;

public class AgiServer {

	static AgiServer instance;
	
	private AgiServer() {
		DefaultAgiServer server = new DefaultAgiServer();
		ClassNameMappingStrategy strategy = new ClassNameMappingStrategy();
		server.setMappingStrategy(strategy);
		AgiServerThread thread = new AgiServerThread(server);
		thread.startup();
		BrokerFactory.getLoggingBroker().logDebug("AGI Server started");
	}
	
	public static AgiServer getInstance() {
		if (instance == null){
			instance = new AgiServer();
		}
		return instance;
	}
}
