/*
 * Created on Oct 18, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intuit.quickbase.util.FileAttachment;
import com.intuit.quickbase.util.QuickBaseClient;
import com.intuit.quickbase.util.QuickBaseException;
import com.lotus.sametime.core.util.enc.DiffieHellman;
import com.lotus.sametime.post.PostEvent;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.QuickbaseDevice;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

public class QuickBaseNotificationProvider extends AbstractNotificationProvider {
	
	public static Pattern digitsPattern;
	
	static {
		digitsPattern = Pattern.compile ("^.*\\b+(\\S+)\\s*\\#(\\d+).*");
	}

	public boolean cancelPage(Notification notification) {
		return false;
	}

	public String getName() {
		return "QuickBase";
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		return params;
	}

	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	public void init(Hashtable params) throws NotificationException {
	}

	/**
	 * Determines whether a QuickBase field is a file attachment
	 * 
	 * @param fid
	 *            - The ID or name to look for
	 * @param dbid
	 *            - The DBID
	 * @param qbc
	 *            - An initialized QBC
	 * @return boolean, true means it is a file attachment field
	 * @throws QuickBaseException
	 * @throws Exception
	 */
	private boolean isFileField(String fid, String dbid, QuickBaseClient qbc)
			throws QuickBaseException, Exception {

		Document document = qbc.getSchema(dbid);
		NodeList fields = document.getElementsByTagName("field");
		for (int fieldNum = 0; fieldNum < fields.getLength(); fieldNum++) {
			Node fieldNode = fields.item(fieldNum);
			Node attribNode = fieldNode.getAttributes().getNamedItem("id");
			if (attribNode != null) {
				String fieldID = attribNode.getNodeValue();
				String label = "";
				NodeList children = fieldNode.getChildNodes();
				for (int childNum = 0; childNum < children.getLength(); childNum++) {
					Node child = children.item(childNum);
					BrokerFactory.getLoggingBroker().logDebug("child=" + child);
					BrokerFactory.getLoggingBroker().logDebug(
							"child name=" + child.getNodeName());
					if (child.getNodeName().equalsIgnoreCase("label")) {
						label = child.getFirstChild().getNodeValue();
						BrokerFactory.getLoggingBroker().logDebug(
								"label=" + label);
					}
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"FID=" + fieldID + ", label=" + label);
				if (label == null)
					label = "";
				if (fieldID.equals(fid) || label.equals(fid)) {
					String fieldType = fieldNode.getAttributes().getNamedItem(
							"field_type").getNodeValue();
					if (fieldType.equals("file")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {

		NotificationMessage[] messages = notification.getMessages();

		StringBuffer messageText = new StringBuffer();
		String photoURL = null;

		String dbid, username, password, relatedToFID, addedByFID, subjectFID, messageFID, photoFID, videoFID, documentFID;
		boolean sendConfirmation = false;

		StringBuffer confirmationMessage = new StringBuffer(
				"Your message with subject \"" + notification.getSubject()
						+ "\"\n");

		if (device instanceof QuickbaseDevice) {
			QuickbaseDevice qbDevice = (QuickbaseDevice) device;
			dbid = qbDevice.getDbid();
			username = qbDevice.getUsername();
			password = qbDevice.getPassword();
			relatedToFID = qbDevice.getRelatedToFID();
			addedByFID = qbDevice.getAddedByFID();
			subjectFID = qbDevice.getSubjectFID();
			messageFID = qbDevice.getMessageFID();
			photoFID = qbDevice.getPhotoFID();
			videoFID = qbDevice.getVideoFID();
			documentFID = qbDevice.getDocumentFID();
			sendConfirmation = qbDevice.sendConfirmation();
		} else {
			return new Hashtable();
		}

		QuickBaseClient qbc = new QuickBaseClient(username, password);
		String parentRID = notification.getSubject();

		// Get the message text
		for (int messageNum = 0; messageNum < messages.length; messageNum++) {
			NotificationMessage message = messages[messageNum];
			BrokerFactory.getLoggingBroker().logDebug(
					"Content type=" + message.getContentType());
			if ((message.getContentType()
					.equalsIgnoreCase(NotificationMessage.NOTIFICATION_CONTENT_TYPE))
					|| (message.getContentType().toLowerCase()
							.startsWith("text/plain"))) {
				messageText.append(message.getMessage());
			} else if ( (StringUtils.isEmpty(messageText.toString())) && (message.getContentType().toLowerCase()
							.startsWith("text/"))) {
				messageText.append(message.getMessage());
			}
		}

		// Get the attachments
		boolean foundAttachment = false;
		for (int messageNum = 0; messageNum < messages.length; messageNum++) {
			NotificationMessage message = messages[messageNum];
			if (message.getContentType().toLowerCase().startsWith("image/")) {
				foundAttachment = true;
				HashMap recorddata = new HashMap<String, String>();
				BrokerFactory.getLoggingBroker().logDebug(
						"Adding to QB Record - " + messageFID + ": "
								+ messageText);

				recorddata.put(subjectFID, notification.getSubject());
				recorddata.put(relatedToFID, parentRID);

				try {
					if (isFileField(photoFID, dbid, qbc)) {

						BrokerFactory.getLoggingBroker().logDebug(
								"Adding file to QB Record - " + photoFID + ": "
										+ photoURL);
						
						recorddata.put(photoFID, new FileAttachment(message
								.getFilename(), message.getContent()));
					} else {
						photoURL = BrokerFactory.getConfigurationBroker()
								.getStringValue("base.url")
								+ "/AttachmentServlet?uuid="
								+ notification.getUuid()
								+ "&messageID="
								+ messageNum;

						if (!StringUtils.isEmpty(photoURL)) {
							BrokerFactory.getLoggingBroker().logDebug(
									"Adding url to QB Record - " + photoFID + ": "
											+ photoURL);
							recorddata.put(photoFID, photoURL);
						}
					}
					String newRecord = qbc.addRecord(dbid, recorddata);
					notification.addMessage("Adding record #" + newRecord
							+ " to QuickBase", notification.getRecipient());
					confirmationMessage.append("Has an image at https://www.quickbase.com/db/"+dbid+"?a=er&rid="+newRecord+"\n");
				} catch (Exception e) {
					confirmationMessage.append("Was not able to attach an image named "+message.getFilename());
					BrokerFactory.getLoggingBroker().logError(e);
					throw new NotificationException(
							NotificationException.FAILED, e.getMessage());
				}

			} else if (message.getContentType().toLowerCase().startsWith(
					"video/")) {
				foundAttachment = true;
				photoURL = BrokerFactory.getConfigurationBroker()
						.getStringValue("base.url")
						+ "/AttachmentServlet?uuid="
						+ notification.getUuid()
						+ "&messageID=" + messageNum;
				HashMap recorddata = new HashMap<String, String>();
				BrokerFactory.getLoggingBroker().logDebug(
						"Adding to QB Record - " + messageFID + ": "
								+ messageText);
				recorddata.put(subjectFID, notification.getSubject());
				recorddata.put(relatedToFID, parentRID);
				recorddata.put(messageFID, messageText.toString().replaceAll("\\p{Cntrl}", ""));
				recorddata.put(addedByFID, notification.getSender().toString());
				try {
					if (isFileField(videoFID, dbid, qbc)) {

						BrokerFactory.getLoggingBroker().logDebug(
								"Adding file to QB Record - " + photoFID + ": "
										+ photoURL);
						
						recorddata.put(videoFID, new FileAttachment(message
								.getFilename(), message.getContent()));
					} else {
						if (!StringUtils.isEmpty(photoURL)) {
							BrokerFactory.getLoggingBroker().logDebug(
									"Adding URL to QB Record - " + photoFID + ": "
											+ photoURL);
							recorddata.put(videoFID, photoURL);
						}
					}
					String newRecord = qbc.addRecord(dbid, recorddata);
					notification.addMessage("Adding record #" + newRecord
								+ " to QuickBase", notification.getRecipient());
					confirmationMessage.append("Has a video at https://www.quickbase.com/db/"+dbid+"?a=er&rid="+newRecord+"\n");
				} catch (Exception e) {
					confirmationMessage.append("Was not able to attach a video named "+message.getFilename());
					BrokerFactory.getLoggingBroker().logError(e);
					throw new NotificationException(
							NotificationException.FAILED, e.getMessage());
				}
			} else if (!(message.getContentType().toLowerCase().startsWith(
					"text/") && (!message.getContentType().equalsIgnoreCase(
					NotificationMessage.NOTIFICATION_CONTENT_TYPE)))) {
				// If it's not text, not a picture, not a video, not a
				// notification message, then add it as
				// a "document", which is a generic binary data type
				foundAttachment = true;
				photoURL = BrokerFactory.getConfigurationBroker()
						.getStringValue("base.url")
						+ "/AttachmentServlet?uuid="
						+ notification.getUuid()
						+ "&messageID=" + messageNum;
				HashMap recorddata = new HashMap<String, String>();
				BrokerFactory.getLoggingBroker().logDebug(
						"Adding to QB Record - " + messageFID + ": "
								+ messageText);
				recorddata.put(subjectFID, notification.getSubject());
				recorddata.put(relatedToFID, parentRID);
				recorddata.put(messageFID, messageText.toString().replaceAll("\\p{Cntrl}", ""));
				recorddata.put(addedByFID, notification.getSender().toString());
				Matcher digitsMatcher = QuickBaseNotificationProvider.digitsPattern.matcher(notification.getSubject());
				if (digitsMatcher.matches()) {
					recorddata.put ("Related "+digitsMatcher.group(1), digitsMatcher.group(2));
					BrokerFactory.getLoggingBroker().logDebug("Relating subject \""+notification.getSubject()+"\" to field Related "+digitsMatcher.group(1)+": "+digitsMatcher.group(2));
				} else  {
					BrokerFactory.getLoggingBroker().logDebug("Subject \""+notification.getSubject()+"\" didn't match");					
				}
				recorddata.put(messageFID, messageText.toString().replaceAll("\\p{Cntrl}", ""));
				recorddata.put(addedByFID, notification.getSender().toString());


				try {
					if (isFileField(documentFID, dbid, qbc)) {
						BrokerFactory.getLoggingBroker().logDebug(
								"Adding file to QB Record - " + photoFID + ": "
										+ photoURL);
						recorddata.put(documentFID, new FileAttachment(message
								.getFilename(), message.getContent()));
					} else {
						if (!StringUtils.isEmpty(photoURL)) {
							BrokerFactory.getLoggingBroker().logDebug(
									"Adding url to QB Record - " + photoFID + ": "
											+ photoURL);
							recorddata.put(documentFID, photoURL);
						}
					}
					String newRecord = qbc.addRecord(dbid, recorddata);
					notification.addMessage("Adding record #" + newRecord
								+ " to QuickBase", notification.getRecipient());
					confirmationMessage.append("Has a document at https://www.quickbase.com/db/"+dbid+"?a=er&rid="+newRecord+"\n");
				} catch (Exception e) {
					confirmationMessage.append("Was not able to attach a video named "+message.getFilename());
					BrokerFactory.getLoggingBroker().logError(e);
					throw new NotificationException(
							NotificationException.FAILED, e.getMessage());
				}

			}
		}

		if (!foundAttachment) {
			// If we didn't find any attachments, go ahead and make a new record
			// w/ no attachment data
			HashMap recorddata = new HashMap<String, String>();
			BrokerFactory.getLoggingBroker().logDebug(
					"Adding to QB Record - " + messageFID + ": " + messageText);
			recorddata.put(subjectFID, notification.getSubject());
			recorddata.put(relatedToFID, parentRID);
			recorddata.put(messageFID, messageText.toString().replaceAll("\\p{Cntrl}", ""));
			recorddata.put(addedByFID, notification.getSender().toString());
			Matcher digitsMatcher = QuickBaseNotificationProvider.digitsPattern.matcher(notification.getSubject());
			if (digitsMatcher.matches()) {
				recorddata.put ("Related "+digitsMatcher.group(1), digitsMatcher.group(2));
				BrokerFactory.getLoggingBroker().logDebug("Relating subject \""+notification.getSubject()+"\" to field Related "+digitsMatcher.group(1)+": "+digitsMatcher.group(2));
			} else  {
				BrokerFactory.getLoggingBroker().logDebug("Subject \""+notification.getSubject()+"\" didn't match");					
			}
			recorddata.put(messageFID, messageText.toString().replaceAll("\\p{Cntrl}", ""));
			recorddata.put(addedByFID, notification.getSender().toString());

			try {
				String newRecord = qbc.addRecord(dbid, recorddata);
				notification.addMessage("Adding record #" + newRecord
						+ " to QuickBase", notification.getRecipient());
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
				throw new NotificationException(NotificationException.FAILED, e
						.getMessage());
			}
			confirmationMessage.append("Had no attachments added");
		}

		// Send out the confirmatiuon message
		NotificationSender sender = notification.getSender();
		if (sendConfirmation && (sender instanceof EmailSender)) {
			try {
				EmailSender emailSender = (EmailSender) sender;
				User recipient = (User) notification.getRecipient();
				InternetAddress[] returnAddress = new InternetAddress[1];
				returnAddress[0] = new InternetAddress("noreply-"
						+ recipient.getEmailAddress());
				InternetAddress to = new InternetAddress(emailSender
						.getAddress());
				Session session = Session.getDefaultInstance(System
						.getProperties(), null);
				session.getProperties().setProperty(
						"mail.smtp.host",
						BrokerFactory.getConfigurationBroker().getStringValue(
								"smtp.server", "localhost"));
				MimeMessage message = new MimeMessage(session);
				message.addFrom(returnAddress);
				message.addRecipient(MimeMessage.RecipientType.TO, to);
				message.setSubject("Upload to QuickBase confirmation");
				message.setText(confirmationMessage.toString());
				Transport.send(message);
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logWarn(
						"Couldn't send confirmation to " + sender);
			}
		}

		return new Hashtable();
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		QuickBaseNotificationProvider qb = new QuickBaseNotificationProvider();
		Matcher matcher = QuickBaseNotificationProvider.digitsPattern.matcher("Contact #123: foo");
		System.out.println (matcher.matches());
		System.out.println (matcher.group(1));
		System.out.println (matcher.group(2));
		
	}
}
