/*
 * Created on Oct 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NonValidatingSocketFactory extends javax.net.ssl.SSLSocketFactory {

	SSLContext sslCtx;
	SSLSocketFactory factory;
	
	public NonValidatingSocketFactory() {
	}
	
	   public static synchronized SocketFactory getDefault() {
	   	return new NonValidatingSocketFactory();
	   }
	
	private void init() {
		try {
			TrustManager[] myTM = new TrustManager [] { 
			        new NonValidatingTrustManager() };
			sslCtx = SSLContext.getInstance("TLS");
			sslCtx.init(null, myTM, null);
			
			factory = sslCtx.getSocketFactory();
		} catch (KeyManagementException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (NoSuchAlgorithmException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		try {
			init();

			return factory.createSocket(arg0, arg1);

		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		return null;
	}
	
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return createSocket(arg0, arg1, InetAddress.getLocalHost(), 0); 
	}
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		// TODO Auto-generated method stub
		return createSocket(InetAddress.getByName(arg0), arg1, arg2, arg3); 
	}

	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		return createSocket(InetAddress.getByName(arg0), arg1, InetAddress.getLocalHost(), 0); 
	}
	
	public Socket createSocket(String arg0, Integer arg1) throws IOException,
	UnknownHostException {
		return createSocket(InetAddress.getByName(arg0), arg1.intValue(), InetAddress.getLocalHost(), 0); 
	}
	
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
			throws IOException {
		init();
		return factory.createSocket(arg0, arg1, arg2, arg3);
	}
	
	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites();
	}
	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites();
	}
	
	
	public Socket createSocket() throws IOException {
		init();
		return factory.createSocket();
	}
	
	
}

class NonValidatingTrustManager implements X509TrustManager {
	
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
	}
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {

	}
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
