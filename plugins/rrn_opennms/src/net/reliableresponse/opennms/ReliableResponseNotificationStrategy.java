/*
 * Created on Apr 19, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.opennms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.opennms.core.utils.Argument;
import org.opennms.netmgt.notifd.NotificationStrategy;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ReliableResponseNotificationStrategy implements
		NotificationStrategy {
	
	String eventID;
	String recipient;
	String message;
	String subject;
	
	public int send(List arguments) {
		parseArguments(arguments);
		
		try {
			URL url = new URL ("http://10.10.10.2:8080/notification/SendSOAPNotification.jws");
			Call call = new Call(url);
			call.setOperation("sendNotification");
			call.setEncodingStyle(Constants.NS_URI_AXIS);
			call.addParameter("memberName", Constants.XSD_STRING, ParameterMode.IN);
			call.addParameter("summary", Constants.XSD_STRING, ParameterMode.IN);
			call.addParameter("message", Constants.XSD_STRING, ParameterMode.IN);
			
			String[] ret = (String[]) call.invoke( new Object[] {recipient, subject, message } );
			// TODO Auto-generated method stub

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void parseArguments(List arguments) {
		// Setup defaults
		recipient = "0000001";
		subject = "OpenNMS Alert";
		message = "No message specified";

		Argument[] argArray = (Argument[]) arguments.toArray(new Argument[0]);
		for (int i = 0; i < argArray.length; i++) {
			System.out.println("Argument["+i+"]="+argArray[i].getSwitch()+":"+argArray[i].getValue());
			String name = argArray[i].getSwitch();
			
			if (name.equals("-tm")) {
				message = argArray[i].getValue();
			} else if (name.equals("eventid")) {
				eventID = argArray[i].getValue();
			} else if (name.equals("-subject")) {
				subject = argArray[i].getValue();
			}
		}
	}
}