/*
 * Created on Nov 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.wctp.ClientResponse;
import net.reliableresponse.notification.wctp.WctpException;
import net.reliableresponse.notification.wctp.WctpLibrary;

public class SNPPNotificationProvider extends AbstractNotificationProvider {

	String host;
	int port = 444;
	String messageTag;
	String passCode;
	boolean twoway = true;
	boolean mcre = true;
	String[] options; 
	Vector responses;
	
	public SNPPNotificationProvider (String host, int port) {
		init (host, port);
	}
	
	public void init (Hashtable params) {
		String host = (String)params.get("host");
		String portString = (String)params.get("port");
		int port = 444;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
		}
		
		init (host, port);
	}
	
	public void init (String host, int port) {
		this.host = host;
		this.port = port;
		
		messageTag = "";
		passCode = "";
		
		responses = new Vector();
	}
	
	public Hashtable getParameters (Notification notification, Device device) {
		Hashtable params = new Hashtable();

		if (host == null) host = "";
		params.put ("host", host);
		params.put ("port", port+"");
		return params;
	}
	
	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException {
		int statusCode = 0;
		User user = (User)notification.getRecipient();
		String summary = notification.getSubject();
		options = notification.getSender().getAvailableResponses(notification);
		String message = notification.getDisplayText();

		try {
			Socket socket = new Socket (host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line = in.readLine();
			statusCode = getStatusCode(line);
			
			if (statusCode != 220) {
				throw new NotificationException(NotificationException.FAILED, 
						"Did not get an SNPP capable server.  Message received was \""+line+"\"");
			}

			out.println("2WAY");
			line = in.readLine();
			statusCode = getStatusCode(line);
			if (statusCode != 250) {
				twoway = false;
			}
			
			out.println("PAGE "+((PagerDevice)device).getNormalizedNumber());
			line = in.readLine();
			statusCode = getStatusCode(line);
			if (statusCode != 250) {
				throw new NotificationException(NotificationException.FAILED, 
						"Pager number not accepted.  Message received was \""+line+"\"");
			}

			out.println("ACKR 1");
			line = in.readLine();

			out.println("data");
			out.println(message);
			out.println(".");
			line = in.readLine();
			statusCode = getStatusCode(line);
			if (statusCode != 250) {
				throw new NotificationException(NotificationException.FAILED, 
						"Message not accepted.  Message received was \""+line+"\"");
			}

			out.println("RTYP multichoice");
			line = in.readLine();
			statusCode = getStatusCode(line);
			if (statusCode != 250) {
				mcre = false;
			}
			
			if (mcre) {
				for (int i = 0; i < options.length; i++) {
					out.println("MCRE "+i+" "+options[i]);
					line = in.readLine();
				}
			}
			
			out.println("SEND");
			line = in.readLine();
			statusCode = getStatusCode(line);
			if (statusCode != 960) {
				throw new NotificationException(NotificationException.FAILED, 
						"Message not sent.  Message received was \""+line+"\"");
			}
			StringTokenizer tok = new StringTokenizer (line, " ");
			if (tok.countTokens()>=3) {
				tok.nextElement();
				messageTag = (String)tok.nextElement();
				passCode = (String)tok.nextElement();
			}
			
			in.close();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		Hashtable params = new Hashtable();
		params.put ("host", host);
		params.put ("port", ""+port);
		params.put ("host", host);
		params.put ("host", host);
		
		return params;
	}

	/**
	 * @param line
	 */
	private int getStatusCode(String line) {
		int statusCode = 0;
		
		try {
			statusCode = Integer.parseInt(line.substring(0, line.indexOf(" ")));
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return statusCode;
	}


	/**
	 * 
	 * @param pageId
	 *            The ID of the notification previously sent
	 * @return A english-readable status. null if the message is unknown
	 */
	public int getStatus(Notification notification) {
		
		try {
			Socket socket = new Socket (host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.println("RTYP multichoice");
			String line = in.readLine();
			int statusCode = getStatusCode(line);
			
			switch (statusCode) {
			case 860: // Delivered, Awaiting Read Confirmation
			case 861: // Delivered, Awaiting Reply (MCR)
			case 870: // Delivered, Read, Awaiting Reply (MCR)
			case 880: // Message Delivered (No Reply Pending)
			case 881: // Message Delivered and Read by Recipient
				return Notification.PENDING;	
			case 500: // Command Not Implemented
			case 550: // Unknown or Illegal Message_Tag or Pass_Code
				return Notification.NORMAL;	
			case 888: // <Reply_Code> MCR Reply Received
			case 889: // <Full_Text_Response>
			case 960: // Message Queued; Awaiting Delivery
				return Notification.DELIVERED;	
			case 780: // MESSAGE EXPIRED Before Delivery!
				return Notification.EXPIRED;	
			case 421: // Gateway Service Unavailable (terminate connection)
			case 554: // Error, failed (technical reason)
				return Notification.FAILED;	
			}
		} catch (UnknownHostException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return Notification.PENDING;
	}
	
	public String[] getResponses(Notification notification) {
		Vector clientResponses = new Vector();
		return (String[]) clientResponses.toArray(new String[0]);
	}

	/**
	 * 
	 * @param pageId
	 * @return Whether the cancellation was successfull
	 */
	public boolean cancelPage(Notification page) {
		try {
			Socket socket = new Socket (host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.println("KTAG "+messageTag+" "+passCode);
			String line = in.readLine();
			int statusCode = getStatusCode(line);
			
			return (statusCode == 250);
		} catch (UnknownHostException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return false;
	}
	
	public String getName() {
		return "Simple Network Pager Provider";
	}	
	
	public String toString() {
		return "Two-Way Pager";
	}
}
