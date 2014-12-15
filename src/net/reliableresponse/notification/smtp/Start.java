// Wait for a connection on a given port

package net.reliableresponse.notification.smtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import net.reliableresponse.notification.broker.BrokerFactory;

public class Start extends Thread {
	int intPort;

	public Start(int intPassedPort) {
		intPort = intPassedPort;
	}

	public void run() {
		ServerSocket serverSocket = null; // the socket for the server
		Socket clientSocket = null; // a client's connection to the server

		try {
			// listen to the given port
			String serverIP = BrokerFactory.getConfigurationBroker().getStringValue("smtp.bindaddress");
			if (serverIP == null) {
				serverSocket = new ServerSocket(intPort);
			} else {
				try {
					serverSocket = new ServerSocket(intPort, 50, InetAddress.getByName(serverIP));
				} catch (UnknownHostException e) {
					BrokerFactory.getLoggingBroker().logError(e);
					serverSocket = new ServerSocket(intPort);					
				}
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logWarn(
					"Could not listen on port: " + intPort);
		}

		// i'm ready
		BrokerFactory.getLoggingBroker().logWarn(
				"smtp server is ready on port " + intPort);

		// go on lisining forever - ctrl-c will quit the program 
		while (!SMTP.shutdown) {
			try {
				BrokerFactory.getLoggingBroker().logWarn(
						"smtp is waiting for a connection on port " + intPort);
				clientSocket = serverSocket.accept();
				// wait for a connection and when one comes in then 
				// tell process to go process it
				Process process = new Process(clientSocket);
				process.setDaemon(true);
				process.start();
			} catch (IOException e) {
			}
		}
	}
}

