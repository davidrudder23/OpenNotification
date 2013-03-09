import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import net.reliableresponse.notification.broker.BrokerFactory;

/*
 * Created on Feb 8, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SimpleProxy {

	public SimpleProxy() {
		 ServerSocket ss = null;
		try {
			ss = new ServerSocket (8889);
		} catch  (Exception anyExc ){ 
			anyExc.printStackTrace();
		}

		while (1==1) {
		try {
			Socket inSocket = ss.accept();
			Socket outSocket = new Socket ("192.168.1.116", 80);
			
			InputStream inIn = inSocket.getInputStream();
			InputStream inOut = outSocket.getInputStream();

			OutputStream outIn = inSocket.getOutputStream();
			OutputStream outOut = outSocket.getOutputStream();
			
			ProxyThread local = new ProxyThread(inIn, outOut);
			ProxyThread remote = new ProxyThread(inOut, outIn);
			
			local.start();
			remote.start();
		} catch (UnknownHostException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		SimpleProxy sp = new SimpleProxy();
	}
}

class ProxyThread extends Thread {
	InputStream in;
	OutputStream out;
	public ProxyThread (InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}
	
	public void run() {
		try {
			byte[] b = new byte[1024];
			int size = 0;
			while ( (size = in.read(b, 0, b.length))>= 0) {
				System.out.write (b, 0, size);
				out.write (b, 0, size);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
