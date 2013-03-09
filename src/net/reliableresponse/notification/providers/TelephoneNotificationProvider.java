/*
 * Created on Feb 16, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.io.FileOutputStream;
import java.io.StringBufferInputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.AsteriskDevice;
import net.reliableresponse.notification.dialogic.DialogicAudioMessage;
import net.reliableresponse.notification.dialogic.DialogicOutgoing;
import net.reliableresponse.notification.sip.SipOutgoing;
import net.reliableresponse.notification.tts.FreeTTS;

import org.w3c.dom.Document;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class TelephoneNotificationProvider extends AbstractNotificationProvider {

	String phoneNumber;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#init(java.util.Hashtable)
	 */
	public void init(Hashtable params) throws NotificationException {
		phoneNumber = (String)params.get("Phone Number");
	}
	
	private String removeSpaces(String message) {
		while (message.indexOf("  ")>=0) {
			message.replaceAll("  ", " ");
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#sendNotification(net.reliableresponse.notification.Notification, net.reliableresponse.notification.device.Device)
	 */
	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		if (device instanceof AsteriskDevice) {
			try {
				AsteriskDevice phone = (AsteriskDevice)device;
				String message =
					"<jsml>You have a new notification from <sayas class=\"net:email\">"+notification.getSender()+"</sayas>.  The subject is "+
					notification.getSubject()+".  The message is "+notification.getMessages()[0].getMessage()+".  ";
				
				if (notification.isPersistent()) {
					String[] responses = notification.getSender()
							.getAvailableResponses(notification);
					for (int i = 0; i < responses.length; i++) {
						message += "Please press " + (i+1) + " to " + responses[i]
								+ ".  ";
					}
					message += "</jsml>";
				}
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        factory.setValidating(false);
		        Document doc = factory.newDocumentBuilder().parse
					(new StringBufferInputStream
						(message));
				byte[] wav = new FreeTTS().getWav(doc);
				BrokerFactory.getLoggingBroker().logDebug("got a wav file "+wav.length+" bytes long");
				String fileName = BrokerFactory.getConfigurationBroker().getStringValue("tomcat.location")+
				"/webapps/notification/sound_output/"+
				BrokerFactory.getUUIDBroker().getUUID(this)+".wav";
				BrokerFactory.getLoggingBroker().logDebug("writing wav to file "+fileName);
				FileOutputStream out = new FileOutputStream (fileName);
				out.write (wav, 0, wav.length);
				out.flush();
				out.close();
				
				
				DialogicAudioMessage audioMessage= new DialogicAudioMessage(phoneNumber, fileName);

				audioMessage.setNotification(notification);
				audioMessage.setNotificationProvider(this);
				if (false) {
					DialogicOutgoing dialogic = DialogicOutgoing.getInstance();
					dialogic.addMessage(audioMessage);
				} else {
					SipOutgoing sip = SipOutgoing.getInstance();
					sip.addMessage(audioMessage);
				}
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
				notification.addMessage("Sending to telephone at "+phoneNumber+" failed", null);
			}		
		} else {
			BrokerFactory.getLoggingBroker().logWarn("Telephone provider got a non-telephone device");
		}
		return new Hashtable();	
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getParameters(net.reliableresponse.notification.Notification, net.reliableresponse.notification.device.Device)
	 */
	public Hashtable getParameters(Notification notification, Device device) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getResponses(net.reliableresponse.notification.Notification)
	 */
	public String[] getResponses(Notification notification) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#cancelPage(net.reliableresponse.notification.Notification)
	 */
	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#isConfirmed(net.reliableresponse.notification.Notification)
	 */
	public boolean isConfirmed(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#isPassed(net.reliableresponse.notification.Notification)
	 */
	public boolean isPassed(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getName()
	 */
	public String getName() {
		return "Telephone";
	}

}
