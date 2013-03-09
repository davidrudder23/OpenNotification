/*
 * Created on Nov 1, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.escada;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class EscadaListener extends Thread {
	int port;
	public EscadaListener () {
		port = BrokerFactory.getConfigurationBroker().getIntValue("escada.listener.port");
	}

	public EscadaListener (int port) {
		this.port = port;
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			while (true) {
				Socket socket = ss.accept();
				InetAddress escadaAddress = socket.getInetAddress();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				out.println ("220");
				String line = in.readLine();
				if (line.indexOf("221")<0) {
					BrokerFactory.getLoggingBroker().logWarn("Got a bad eSCADA input from "+
							escadaAddress+
							".  Dropping connection");
				} else {
					// TODO: Connect to eSCADA
				}
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public static void main(String[] args) {
		EscadaListener listener = new EscadaListener(8021);
		listener.start();
	}
}
