package net.reliableresponse.notification.web.util;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.sip.header.ToHeader;

import org.omg.PortableInterceptor.USER_EXCEPTION;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.StringUtils;

/**
 * This class implements the SSO ticket capabilities of Apache's mod_auth_tkt
 * See http://www.openfusion.com.au/labs/mod_auth_tkt/
 * @author David Rudder &lt; david@reliableresponse.net &gt;
 * 
 * Copyright 2005 - Reliable Response, LLC
 */
public class ModAuthTicket {

	public static String getTicket (String username, String sharedSecret,
			InetAddress clientIP, String[] tokens, String userData, long time) {
		try {
			byte[] ipts = new byte[8];
			
			// Get the client IP into the ipts array
			System.arraycopy (clientIP.getAddress(), 0, ipts, 0, 4);
			
			// Get the date into the ipts array
			byte[] timeArray = new byte[4];
			timeArray[0] = (byte)((time>>24)&0xff);
			timeArray[1] = (byte)((time>>16)&0xff);
			timeArray[2] = (byte)((time>>8)&0xff);
			timeArray[3] = (byte)(time& 0xff);
			System.arraycopy(timeArray, 0, ipts, 4, 4);
			System.out.println (StringUtils.toHexString(ipts));
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update (ipts);
			digester.update(sharedSecret.getBytes());
			digester.update(username.getBytes());
			digester.update("\0".getBytes());
			
			if (tokens != null) {
				for (int i = 0; i < tokens.length; i++) {
					digester.update(tokens[i].getBytes());
				}
			}
			digester.update("\0".getBytes());
			if (userData != null) {
				digester.update(userData.getBytes());
			}
			
			byte[] digest = digester.digest();
			
			digester.reset();
			digester.update(StringUtils.toHexString(digest).getBytes());
			digester.update(sharedSecret.getBytes());
			digest = digester.digest();
			
			String cookie = StringUtils.toHexString(digest)+
			StringUtils.toHexString(timeArray)+
			username+"!";
			if (userData != null) cookie += userData;
			
			return cookie;
		} catch (NoSuchAlgorithmException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		return null;
	}
	
	public static void main (String[] args) throws Exception {
		System.out.println (ModAuthTicket.getTicket("nagiosadmin", "changethistosomethingunique", 
				InetAddress.getByName("127.0.0.1"),
				null, null, System.currentTimeMillis()/1000));
	}
}
