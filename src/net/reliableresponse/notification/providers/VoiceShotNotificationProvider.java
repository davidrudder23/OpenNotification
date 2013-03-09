/*
 * TODO: The instancesByTrackingNumber will increase unbounded unless fixed
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.AbstractDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.device.TAPDevice;
import net.reliableresponse.notification.device.VoiceShotDevice;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.wctp.ClientResponse;
import net.reliableresponse.notification.wctp.WctpException;
import net.reliableresponse.notification.wctp.WctpLibrary;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VoiceShotNotificationProvider extends AbstractNotificationProvider {

	int status = Notification.PENDING;
	
	private String phoneNumber;
	
	private boolean hasResponded;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public VoiceShotNotificationProvider() {	
	}
	
	public Hashtable getParameters (Notification notification, Device device) {
		Hashtable params = new Hashtable();
		
		return params;
	}
	
	public void init(Hashtable params) throws NotificationException {
		// We don't need no stinkin' initialization
	}

	public Hashtable sendNotification(Notification notification, Device device) throws NotificationException {
		if (!(device instanceof VoiceShotDevice)) {
			throw new NotificationException (NotificationException.FAILED, "Supplied device is not a VoiceShot device");
		}
		String[] responses = notification.getSender().getAvailableResponses(notification);
		String message ="";
		if ((!StringUtils.isEmpty(notification.getSender().getNotificationType()) && (!notification.getSender().getNotificationType().equalsIgnoreCase("blank")))) {
				message += "You have a new "+notification.getSender().getNotificationType()+" from "+notification.getSender()+".  ";

		}
		if (!StringUtils.isEmpty(notification.getSubject())) {
			if  (!notification.getSender().getNotificationType().equalsIgnoreCase("blank")) {
				message += "The subject is ";
			}
			message += notification.getSubject()+".  ";
		}
		message +=notification.getMessages()[0].getMessage()+".  ";
		
		if (notification.isPersistent()) {
			for (int i = 0; i < responses.length; i++) {
				message += "Please press " + (i+1) + " to respond with " + responses[i]
						+ ".  ";
			}
		}

		VoiceShotDevice voiceShotDevice = (VoiceShotDevice)device;
		setPhoneNumber(voiceShotDevice.getPhoneNumber());
		StringBuffer xml = new StringBuffer();
		
		xml.append ("<campaign menuid=\"");
		xml.append (BrokerFactory.getConfigurationBroker().getStringValue("voiceshot.campaign", "0"));
		xml.append ("\" action=\"0\" >\n");
		xml.append ("<phonenumbers>\n");
		xml.append ("<phonenumber number=\"");
		xml.append (voiceShotDevice.getPhoneNumber());
		xml.append ("\" callid=\"");
		xml.append (notification.getUuid());
		xml.append ("\" callerid=\"");
		xml.append (BrokerFactory.getConfigurationBroker().getStringValue("voiceshot.callerid", "3035551212"));
		xml.append ("\">\n");
		xml.append ("<prompts>\n");
		xml.append ("<prompt promptid=\"1\" tts=\"");
		xml.append (StringUtils.escapeForXML(message));
		xml.append ("\" />\n");
		for (int i = 0; i < responses.length; i++) {
			xml.append ("<prompt promptid=\""+(i+2)+"\" tts=\""+notification.getSender().getResponseMessage(responses[i])+"\" />\n");
		}
		xml.append ("<prompt promptid=\"11\" tts=\"Thank you for using Reliable Response Notification\" />\n");
		xml.append ("</prompts>\n");
		xml.append ("</phonenumber>\n");
		xml.append ("</phonenumbers>\n");
		xml.append ("</campaign>\n");
				
		BrokerFactory.getLoggingBroker().logDebug("Submitting XML to VoiceShot: "+xml.toString());
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod("http://api.voiceshot.com/ivrapi.asp");
		post.setParameter("Content-Type", "text/xml");
		post.setRequestBody(xml.toString());
		
		try {
			int result = httpClient.executeMethod(post);
			
			BrokerFactory.getLoggingBroker().logDebug("VoiceShot responded "+post.getResponseBodyAsString());
			if (result != 200) {
				throw new NotificationException(NotificationException.FAILED, "Got a bad return code from the server: "+result);				
			}
			hasResponded = false;
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new NotificationException(NotificationException.TEMPORARILY_FAILED, e.getMessage());
		}

		Hashtable params = new Hashtable();
		
		return params;
	}

	/**
	 * 
	 * @param pageId
	 *            The ID of the notification previously sent
	 * @return A english-readable status. null if the message is unknown
	 */
	public int getStatus(Notification notification) {		
		return status;
	}
	
	public String[] getResponses(Notification notification) {
		Vector responses = new Vector();
		if (hasResponded) {
			return new String[0];
		}
		StringBuffer xml = new StringBuffer();
		xml.append ("<campaign menuid=\"20862-2\" action=\"3\" >\n");
		xml.append ("<phonenumbers>\n");
		xml.append ("<phonenumber number=\"");
		xml.append (getPhoneNumber());
		xml.append ("\" callid=\"");
		xml.append (notification.getUuid());
		xml.append ("\" callerid=\"3035551212\">\n");
		xml.append ("</phonenumber>\n");
		xml.append ("</phonenumbers>\n");
		xml.append ("</campaign>\n");

		BrokerFactory.getLoggingBroker().logDebug("Submitting XML to VoiceShot: "+xml.toString());
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod("http://api.voiceshot.com/ivrapi.asp");
		post.setParameter("Content-Type", "text/xml");
		post.setRequestBody(xml.toString());
		
		try {
			int result = httpClient.executeMethod(post);
			
			String response = post.getResponseBodyAsString();
			BrokerFactory.getLoggingBroker().logDebug("VoiceShot responded "+response);
			

			if (result != 200) {
				throw new NotificationException(NotificationException.FAILED, "Got a bad return code from the server: "+result);				
			}
			
			
			// This is the XML parsing
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					response)));
			
			Element campaign = document.getDocumentElement();
			if (campaign == null) {
				BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
						" has no campaign");
				return new String[0];
			}
			NodeList phoneNumbers = campaign.getElementsByTagName("phonenumbers");
			if (phoneNumbers == null) {
				BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
				" has no phone numbers");
				return new String[0];
			}
			for (int phoneNumberNum = 0; phoneNumberNum < phoneNumbers.getLength(); phoneNumberNum++) {
				Element phoneNumberElement = (Element)phoneNumbers.item(phoneNumberNum);
				if (phoneNumberElement == null) {
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
					" has no phone number");
					return new String[0];
				}
				
				BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
						" phone num element= "+phoneNumberElement.getTagName());
				String statusString = phoneNumberElement.getAttribute("status");
				BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+" status = "+statusString);
				
				NodeList prompts = phoneNumberElement.getElementsByTagName("prompts");
				if ((prompts == null) || (prompts.getLength()<0)) {
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
					" has no prompts");
					return new String[0];
				}
				
				Element promptsElement = (Element)prompts.item(0);
				if ((prompts == null) || (prompts.getLength()<0)) {
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
					" has no prompts element");
					return new String[0];
				}
				
				if (promptsElement == null) {
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
					" has no prompts element");
					return new String[0];
				}
				NodeList promptList = promptsElement.getElementsByTagName("prompt");
				if ((promptList == null) || (promptList.getLength()<0)) {
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
					" has no prompt elements");
					return new String[0];
				}
				
				for (int promptNum = 0; promptNum< promptList.getLength(); promptNum++) {
					Element promptElement = (Element)promptList.item(promptNum);
					String promptID = promptElement.getAttribute("promptid");
					String keypress = promptElement.getAttribute("keypress");
					BrokerFactory.getLoggingBroker().logDebug("VoiceShot notification "+notification.getUuid()+
							" prompt id = "+promptID+",keypress="+keypress);
					
					if ( (promptID != null) && (keypress != null)) {
						if (promptID.equals("1")) {
							int responseNum = -1;
							
							try {
								responseNum = Integer.parseInt(keypress);
								hasResponded = true;
								// We got the number that was press.  But, remember
								// it starts at 1, not 0, so we have to -1
								responses.addElement(notification.getSender().getAvailableResponses(notification)[responseNum-1]);
							} catch (NumberFormatException nfExc) {
								
							}
						}
					}
				}
			}				
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		
		return (String[])responses.toArray(new String[0]);
	}

	
	
	public boolean isConfirmed(Notification page) {
		String[] responses = getResponses(page);
		for (int i = 0; i < responses.length; i++) {
			if (responses[i].toLowerCase().equals ("confirm")) {
				return true;
			}
		}
		return false;
	}

	public boolean isPassed(Notification notification) {
		String[] responses = getResponses(notification);
		for (int i = 0; i < responses.length; i++) {
			if (responses[i].toLowerCase().equals ("pass")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param pageId
	 * @return Whether the cancellation was successfull
	 */
	public boolean cancelPage(Notification page) {
		return false;
	}
		
	public String getName() {
		return "Online Text to Speech";
	}	
	
	public String toString() {
		return getName();
	}

}
