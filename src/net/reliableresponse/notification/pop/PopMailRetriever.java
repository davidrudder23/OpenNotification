/*
 * Created on Nov 15, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.pop;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.device.CellPhoneEmailDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class PopMailRetriever implements StatefulJob {
	String address;
	List<String> hostnames;
	List<String> usernames;
	List<String> passwords;
	String sslPort;
	boolean useSSL;
	boolean catchAll;
	
	List<Folder> folders;

	public PopMailRetriever() {
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		address = config.getStringValue("email.pop.address");
		hostnames = config.getStringValues("email.pop.hostname");
		usernames = config.getStringValues("email.pop.username");
		passwords = config.getStringValues("email.pop.password");
		useSSL = config.getBooleanValue("email.pop.usessl", false);
		sslPort = config.getStringValue("email.pop.sslport", "995");
		
		catchAll = config.getBooleanValue("email.pop.catchall");
		folders = new ArrayList<Folder>();
	}
	
	private Member findSenderFromEmail (String email) {
		BrokerFactory.getLoggingBroker().logDebug("Looking for sender with email "+email);
		User[] users= BrokerFactory.getUserMgmtBroker().getUsersWithEmailAddress(email);
		
		if ((users != null) && (users.length>0)) {
			BrokerFactory.getLoggingBroker().logDebug("We found member "+users[0]+" with email address "+email);
			return users[0];
		}

		BrokerFactory.getLoggingBroker().logDebug(email+" is not a known email address");
		if (CellPhoneEmailDevice.isCellPhoneAddress(email)) {
			BrokerFactory.getLoggingBroker().logDebug(email+" is a cell phone email address");
			users = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType("net.reliableresponse.notification.device.CellPhoneEmailDevice");
			if (users != null) {
				for (int i = 0; i < users.length; i++) {
					Device[] devices = users[i].getDevices();
					for (int d = 0; d < devices.length; d++) {
						if (devices[d] instanceof CellPhoneEmailDevice) {
							CellPhoneEmailDevice cellDevice = (CellPhoneEmailDevice)devices[d];
							if (cellDevice.getEmailAddress().equalsIgnoreCase(email)) {
								return users[i];
							}
						}
					}
				}
			}
		}
		UnknownUser unknownUser = new UnknownUser();
		unknownUser.setEmailAddress(email);
		return unknownUser;
	}

	private Message[] getMessages() {
		List<Message> messages = new ArrayList<Message>();
		for (int i = 0; i < hostnames.size(); i++) {
			String hostname = hostnames.get(i);
			String username = usernames.get(i);
			String password = passwords.get(i);
			BrokerFactory.getLoggingBroker().logDebug("Connecting to pop server at "+hostname+" with "+username);
			try {
				Properties props = new Properties();
				
				if (useSSL) {
					BrokerFactory.getLoggingBroker().logDebug("Using SSL to connect to POP");
					props.setProperty( "mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					props.setProperty( "mail.pop3.socketFactory.fallback", "false");
					props.setProperty( "mail.pop3.port", sslPort);
					props.setProperty( "mail.pop3.socketFactory.port", sslPort);
				}
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore("pop3");
				store.connect(hostname, username, password);
				Folder folder = store.getFolder("INBOX");
				folder.open(Folder.READ_WRITE);
				int numMessages = folder.getMessageCount();
				BrokerFactory.getLoggingBroker().logDebug("num messages = "+numMessages);
				
				for (int m = 0; m < numMessages; m++) {
					try {
						BrokerFactory.getLoggingBroker().logDebug("Adding message "+m);
						messages.add(folder.getMessage(m+1));
					} catch (Exception e) {
						BrokerFactory.getLoggingBroker().logDebug("Got a bad email");
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}

				if (!folders.contains(folder)) {
					folders.add(folder);
				}

			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		return (Message[])messages.toArray(new Message[0]);
	}
	
	
	/**
	 * Checks for the bounce suffix, and removes the email from processing
	 * @param messages
	 * @return
	 */

	private Message[] handleBounces(Message[] messages) {
		List<Message> newMessages = new ArrayList<Message>();

		for (int messageNum = 0; messageNum < messages.length; messageNum++) {
			Message message = messages[messageNum];
			try {
				Address[] recipients = message.getAllRecipients();
				if (recipients == null) {
					BrokerFactory.getLoggingBroker().logWarn ("Null recipients");
					recipients = new Address[0];
				}
				for (int t = 0; t < recipients.length; t++) {
					String to = ((InternetAddress)recipients[t]).getAddress();
					BrokerFactory.getLoggingBroker().logDebug("Recipient["+t+"]="+to);
					if (isBounce(to)) {
						BrokerFactory.getLoggingBroker().logDebug("Is bounce");						
						// Bounce address is in format
						// notification_device-bounce@notification.server.com
						String bounceSuffix = BrokerFactory.getConfigurationBroker().getStringValue("email.bounce.suffix", "-bounce");
						String addressPart = to.substring(0, to.indexOf(bounceSuffix));
						String notificationUuid = "";
						String deviceUuid = "";
						if (addressPart.indexOf("_")>0) {
							notificationUuid = addressPart.substring(0, addressPart.indexOf("_"));
							deviceUuid = addressPart.substring(notificationUuid.length()+1);
							BrokerFactory.getLoggingBroker().logDebug("Bounce notification uuid="+notificationUuid);
							BrokerFactory.getLoggingBroker().logDebug("Bounce device uuid="+deviceUuid);
						}
						Notification bouncedNotif = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
						Device bouncedDevice = BrokerFactory.getDeviceBroker().getDeviceByUuid(deviceUuid);
						if ((bouncedNotif != null) && (bouncedDevice != null)) {
							bouncedNotif.addMessage(bouncedDevice.toString()+" bounced and should be removed", null);
						}
						// TODO: What now?  Should we actually remove the device?
					} else {
						// if this isn't a bounce, add it back
						newMessages.add(message);
					}
				}
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logWarn(e);
			}
		}		
		return (Message[])newMessages.toArray(new Message[0]);
	}	

	private PopMessage[] getNewNotifications(Message[] messages) {
		// This works by finding all the messages with a recipient in the subject or body.
		List<PopMessage> newMessages = new ArrayList<PopMessage>();

		for (int i = 0; i < messages.length; i++) {
			try {
				boolean found = false;
				String subject = (String) messages[i].getSubject();
				String content = getContent(messages[i]);

				BrokerFactory.getLoggingBroker().logDebug(
						"Subject = " + subject);
				BrokerFactory.getLoggingBroker().logDebug(
						"Content = " + content);

				// Look to see if this is a catch-all address with the recipient in the "to" or "cc" field
				if (catchAll) {
					Address[] recipients = messages[i].getAllRecipients();
					if (recipients == null) {
						BrokerFactory.getLoggingBroker().logWarn ("Null recipients");
						recipients = new Address[0];
					}
					for (int t = 0; t < recipients.length; t++) {
						String to = ((InternetAddress)recipients[t]).getAddress();
						BrokerFactory.getLoggingBroker().logDebug("Recipient["+t+"]="+to);
						if (!to.equals(address)) {
							Member toMember = Notification.findRecipient(to);
							BrokerFactory.getLoggingBroker().logDebug("Recipient member = "+toMember);
							if (toMember != null) {
								PopMessage message = new PopMessage(messages[i]);
								message.setParam("recipient-uuid", toMember.getUuid());
								newMessages.add(message);
								found = true;
							}
						}
					}
				}
				// Look in the subject for the recipient
				if ((!found) && (subject != null) && (subject.length() > 0)) {
					String recipient = findRecipientInString(subject);
					BrokerFactory.getLoggingBroker().logDebug("Found recipient in subject: "+recipient);
					if (recipient != null) {
						found = true;
						PopMessage message = new PopMessage(messages[i]);
						message.setParam("recipient-uuid", recipient);
						newMessages.add(message);
					}
				}

				if ((!found) && (content != null) && (content.length() > 0)) {
					int eolIndex = content.indexOf("\r");
					if (eolIndex<0) {
						eolIndex = content.indexOf("\n");
					}
					if (eolIndex<0) {
						eolIndex = content.length();
					}
					String firstLine = content.substring(0, eolIndex);
					String recipient = findRecipientInString(firstLine);
					BrokerFactory.getLoggingBroker().logDebug("Found recipient in 1st line: "+recipient);
					if (recipient != null) {
						found = true;
						PopMessage message = new PopMessage(messages[i]);
						message.setParam("recipient-uuid", recipient);
						newMessages.add(message);
					}
				}
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		return (PopMessage[]) (newMessages.toArray(new PopMessage[0]));
	}

	/**
	 * @param messages
	 * @param i
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String getContent(Message message) throws IOException, MessagingException {
		Object contentParts = message.getContent();
		String content = "";
		BrokerFactory.getLoggingBroker().logDebug("contentParts="+contentParts);
		if (contentParts instanceof String) {
			content = (String) contentParts;
		} else if (contentParts instanceof MimeMultipart) {
			MimeMultipart multiPart = (MimeMultipart) contentParts;
			int parts = multiPart.getCount();
			for (int p = 0; p < parts; p++) {
				if (multiPart.getBodyPart(p).getContentType()
						.toLowerCase().indexOf("text/plain") >= 0) {
					Object bodyPart =  multiPart.getBodyPart(p).getContent();
					if (bodyPart instanceof String) {
						content = (String) bodyPart;
					} else if (bodyPart instanceof InputStream) {
						BufferedReader in = new BufferedReader (new InputStreamReader((InputStream)bodyPart));
						String line = null;
						while ( (line = in.readLine()) != null) {
							content += line+"\n";
						}
					} else {
						BrokerFactory.getLoggingBroker().logWarn ("Don't know how to handle body part "+bodyPart);
					}
				}
			}
		} else {
			BrokerFactory.getLoggingBroker().logWarn("Do not know how to handle this email's content");
		}
		return content;
	}
	private BodyPart[] getAttachments(Message message) throws IOException, MessagingException {
		List<BodyPart> bodyParts = new ArrayList<BodyPart>();
		
		Object contentParts = message.getContent();
		BrokerFactory.getLoggingBroker().logDebug("contentParts="+contentParts);
		if (contentParts instanceof MimeMultipart) {
			MimeMultipart multiPart = (MimeMultipart) contentParts;
			int parts = multiPart.getCount();
			BrokerFactory.getLoggingBroker().logDebug("We have "+parts+" parts");
			for (int p = 0; p < parts; p++) {
				BodyPart bodyPart = multiPart.getBodyPart(p);
				BrokerFactory.getLoggingBroker().logDebug("content-type["+p+"]="+bodyPart.getContentType());
				
				if ((p>0) || (!bodyPart.getContentType().toLowerCase().startsWith("text"))) {  // don't return the 1st attachment if its text, because it's actually the body
				byte[] content;
				Object bodyPartContents =  bodyPart.getContent();

				if (bodyPartContents instanceof String) {
						content = ((String) bodyPartContents).getBytes();
				} else if (bodyPartContents instanceof InputStream) {
					InputStream in = (InputStream)bodyPartContents;
					ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					int size = 0;
					while ( (size = in.read(buf, 0, buf.length))>=0) {
						bytesOut.write (buf,0, size);
					}
				} else if (bodyPartContents instanceof byte[]) {
					content = (byte[])bodyPartContents;
				}
				
				bodyParts.add(bodyPart);
				}
			}
		}
		return (BodyPart[])bodyParts.toArray(new BodyPart[0]);
	}
	
	private byte[] getContent(BodyPart bodyPart) throws IOException, MessagingException {
		byte[] content = new byte[0];
		Object bodyPartContents =  bodyPart.getContent();
		if (bodyPartContents instanceof String) {
			content = ((String) bodyPartContents).getBytes();
		} else if (bodyPartContents instanceof InputStream) {
			InputStream in = (InputStream)bodyPartContents;
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int size = 0;
			while ( (size = in.read(buf, 0, buf.length))>=0) {
				bytesOut.write (buf,0, size);
			}
			content = bytesOut.toByteArray();
		} else if (bodyPartContents instanceof byte[]) {
			content = (byte[])bodyPartContents;
		}

		return content;
	}
	
	private void addAttachments(Message message, Notification notification) throws IOException, MessagingException {
		BodyPart[] bodyParts = getAttachments(message);
		for (int bodyPartNum = 0; bodyPartNum<bodyParts.length; bodyPartNum++) {
			BodyPart bodyPart = bodyParts[bodyPartNum];
			
			NotificationMessage notifMessage = new NotificationMessage
			(getContent(bodyPart), notification.getSender().toString(), new Date(), bodyPart.getContentType());
			if (!StringUtils.isEmpty(bodyPart.getFileName())) {
				notifMessage.setFilename(bodyPart.getFileName());
			}
			notification.addMessage(notifMessage);
		}
	}

	/**
	 * @param line 
	 * @return
	 */
	private String findRecipientInString(String line) {
		// Check to see if this is a bounce
		if (isBounce(line)) {
			//String bounceSuffix = BrokerFactory.getConfigurationBroker().getStringValue("email.bounce.suffix", "-bounce");
			return null;
		}
		// Check the first and last name
		if (line.indexOf(" ") > 0) {
			String firstName = line.substring(0, line.indexOf(" "));
			String lastName = line.substring(line.indexOf(" ") + 1, line
					.length());
			BrokerFactory.getLoggingBroker().logDebug(
					"checking name " + firstName + " " + lastName);
			User[] users = BrokerFactory.getUserMgmtBroker().getUsersByName(
					firstName, lastName);
			if ((users != null) && (users.length > 0)) {
				BrokerFactory.getLoggingBroker().logDebug(
						"found user " + firstName + " " + lastName);
				return users[0].getUuid();
			}
		}

		// Check for user's uuid as subject
		Member member = Notification.findRecipient(line);
		if (member != null) {
			return member.getUuid();
		} else {
			return null;
		}
	}
	
	/**
	 * Send in the same string that gave a positive match from "findRecipientInString"
	 * @param line
	 * @return
	 */
	private boolean isBounce (String line) {
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("email.bounce.enable", true)) {
			String bounceSuffix = BrokerFactory.getConfigurationBroker().getStringValue("email.bounce.suffix", "-bounce");
			if ((line.endsWith(bounceSuffix)) || (line.indexOf (bounceSuffix+"@")>0)) {
				return true;
			}
		}
		
		return false;
	}

	private PopMessage[] getMessagesWithResponse(Message[] messages,
			String response) {
		Vector newMessages = new Vector();
		for (int i = 0; i < messages.length; i++) {
			try {
				PopMessage message = new PopMessage(messages[i]);
				String subject = (String) messages[i].getSubject();
				String content = getContent(messages[i]);
				BrokerFactory.getLoggingBroker().logDebug(
						"Checking for response " + response
								+ " from message with subject: " + subject);
				newMessages.addAll(findCommandInString(response, message, subject));
				if (content.indexOf("\n")>0) {
					content = content.substring(0, content.indexOf("\n"));
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Checking for response " + response
								+ " from message with content: " + content);
				newMessages.addAll(findCommandInString(response, message, content));
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		return (PopMessage[]) (newMessages.toArray(new PopMessage[0]));
	}

	/**
	 * @param response
	 * @param newMessages
	 * @param message
	 * @param text
	 */
	private Vector findCommandInString(String response, PopMessage message, String text) {
		Vector newMessages = new Vector();
		// Look in the subject for the recipient
		if ((text != null) && (text.length() > 0)) {
			Pattern pattern = Pattern.compile("\\b(?i)" + response
					+ "\\b");
			if (pattern.matcher(text).find()) {
				int startIndex = text.toLowerCase().indexOf(
						response.toLowerCase());
				if (startIndex >= 0) {
					startIndex += response.length();
					while ((startIndex < text.length()) && (text.charAt(startIndex) == ' '))
						startIndex++;
					int endIndex = -1;
					
					if (text.length() >= (startIndex+7)) 
						endIndex = startIndex+7;
					
					if (endIndex < startIndex)
						text.indexOf(" ", startIndex);
					if (endIndex < startIndex)
						endIndex = text.indexOf("\r", startIndex);
					if (endIndex < startIndex)
						endIndex = text.indexOf("\n", startIndex);
					if (endIndex < startIndex)
						endIndex = text.length();
					String uuid = text.substring(startIndex,
							endIndex);
					System.out.println ("We think the uuid="+uuid+" from text "+text);
					if (uuid != null) {
					Notification notification = BrokerFactory
							.getNotificationBroker()
							.getNotificationByUuid(uuid);
					if (notification != null) {
						message.setParam(response.toLowerCase()
								+ "-uuid", uuid);
						message.setParam("text", text);
						newMessages.addElement(message);
					} else {
						if (catchAll) {
							try {
								Address[] recipients = message.message.getAllRecipients();
								for (int t = 0; t < recipients.length; t++) {
									String to = recipients[t].toString();
									
									BrokerFactory.getLoggingBroker().logDebug("to="+to);

									int indexOfLessThan = to.indexOf("<");
									int indexOfGreaterThan = to.indexOf(">");
									if ((indexOfLessThan>=0) && (indexOfGreaterThan>0) && (indexOfGreaterThan>indexOfLessThan)) {
										to = to.substring(indexOfLessThan, indexOfGreaterThan);
									}
									BrokerFactory.getLoggingBroker().logDebug("after to="+to);
										
									while ((to.startsWith(" ")) || (to.startsWith("\""))) {
										to = to.substring (1, to.length());
									}
									while ((to.endsWith(" ")) || (to.endsWith("\""))) {
										to = to.substring (0, to.length()-1);
									}
									if (to.indexOf("@") > 0) to = to.substring (0, to.indexOf("@"));
									if (to.indexOf(" ") > 0) to = to.substring (0, to.indexOf(" ")); // Blackberry hack...it sometimes munges the return address
									byte[] toChars = to.getBytes();
									int start = 0;
									while ((start<toChars.length) && ((toChars[start]<((int)'0')) || (toChars[start]>((int)'9')))) start++;
									if (start>0) 
										to = to.substring (start, to.length());
										
									BrokerFactory.getLoggingBroker().logDebug("Notif["+t+"]="+to);
									notification = BrokerFactory
									.getNotificationBroker()
									.getNotificationByUuid(to);
									if (notification != null) {
										message.setParam(response.toLowerCase()
												+ "-uuid", to);
										message.setParam("text", text);
										newMessages.addElement(message);
									}
								}
							} catch (MessagingException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}


					}
					}
				}
			}
		}
		return newMessages;
	}

	public void deleteMessages(Message[] messages) {
		try {
			for (int i = 0; i < messages.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Setting " + messages[i] + " to deleted");
				messages[i].setFlag(Flags.Flag.DELETED, true);
			}
		} catch (MessagingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		for (int i = 0; i < folders.size(); i++) {
			Folder folder = (Folder)folders.get(i);
			try {
				folder.close(true);
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jeContext)
			throws JobExecutionException {

		// Add the start time
		JobDataMap map = jeContext.getJobDetail().getJobDataMap();

		long startMillis = System.currentTimeMillis();
		Date startDate = new Date();
		Vector startTimes = (Vector) map.get("starttimes");
		if (startTimes == null) {
			startTimes = new Vector();
		}
		startTimes.addElement(startDate);
		map.put("starttimes", startTimes);
		jeContext.getJobDetail().setJobDataMap(map);

		BrokerFactory.getLoggingBroker().logDebug("POPjob running");
		Message[] messages = getMessages();
		messages = handleBounces(messages);
		PopMessage[] newNotifications = getNewNotifications(messages);
		BrokerFactory.getLoggingBroker().logDebug("Got "+newNotifications.length+" new notifs to send");

		for (int i = 0; i < newNotifications.length; i++) {
			try {
				PopMessage popMessage = newNotifications[i];
				Message message = popMessage.getMailMessage();
				Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						popMessage.getParam("recipient-uuid"));
				if (member == null) {
					member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
							popMessage.getParam("recipient-uuid"));
				}
				String from = "";
				try {
					from = ((InternetAddress)message.getReplyTo()[0]).getAddress();
				} catch (MessagingException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
				if (StringUtils.isEmpty(from)) {
					try {
						from = ((InternetAddress)message.getFrom()[0]).getAddress();
					} catch (MessagingException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
				
				Notification notification = new Notification(null, member,
						new EmailSender(from), message.getSubject(),
						getContent(message));
				
				// Look for the sender by applying the regexp to it
				int regexpIndex = 1;
				boolean doSend = true;
				String regexp = BrokerFactory.getConfigurationBroker().getStringValue(
						"sender.regexp." + regexpIndex);
				String senderClass = BrokerFactory.getConfigurationBroker()
						.getStringValue("sender.regexp.class." + regexpIndex++);
				boolean found = false;
				while ((regexp != null) && (!found)) {
					BrokerFactory.getLoggingBroker().logDebug ("Looking for regexp sender "+senderClass+" for subject "+notification.getSubject()+" with regexp "+regexp);
					if (notification.getSubject().matches(regexp)) {
						BrokerFactory.getLoggingBroker().logDebug ("Looking for regexp sender "+senderClass+" found");
						try {
							NotificationSender sender = (NotificationSender) Class
									.forName(senderClass).newInstance();
							doSend = sender.getVariablesFromNotification(notification);
							notification.setSender(sender);
							found = true;
						} catch (Exception anyExc) {
							BrokerFactory.getLoggingBroker().logWarn(anyExc);
						}
					}

					regexp = BrokerFactory.getConfigurationBroker().getStringValue(
							"sender.regexp." + regexpIndex);
					senderClass = BrokerFactory.getConfigurationBroker()
							.getStringValue("sender.regexp.class." + regexpIndex++);
				}
				
				notification.setAutocommit(false);
				addAttachments(message, notification);
				notification.setAutocommit(true);
				SendNotification.getInstance().doSend(notification);
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (Error e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		
		// Now look for any notifications that have an actual response
		List<Notification> pendingNotifications = BrokerFactory.getNotificationBroker().getNotificationsSince(8640000L);
		Vector responses = new Vector();
		for (Notification pendingNotification: pendingNotifications) {
			NotificationSender sender = pendingNotification.getSender();
			String[] respArray = sender
					.getAvailableResponses(pendingNotification);
			for (int r = 0; r < respArray.length; r++) {
				if (!responses.contains(respArray[r])) {
					responses.addElement(respArray[r]);
				}
			}
		}
		
		Vector<Message> trimmedMessages = new Vector<Message>();
		for (int messageNum = 0; messageNum < messages.length; messageNum++) {
			trimmedMessages.add(messages[messageNum]);
		}
		// Remove the new notifs from the trimmed list
		for (int newMessageNum = 0; newMessageNum < newNotifications.length; newMessageNum++) {
			Message newMessage = newNotifications[newMessageNum].getMailMessage();
			trimmedMessages.remove(newMessage);
		}

		for (int i = 0; i < responses.size(); i++) {
			PopMessage[] responseMessages = getMessagesWithResponse(messages,
					(String) responses.elementAt(i));
			BrokerFactory.getLoggingBroker().logDebug("Got "+responseMessages.length+" notifs to respond to");
			for (int m = 0; m < responseMessages.length; m++) {
				Message message = responseMessages[m].getMailMessage();
				// Since we have a response to this, trim it from the unhandled list
				trimmedMessages.remove(responseMessages[m].getMailMessage());
				String response = (String) responses.elementAt(i);
				Notification notification = BrokerFactory
						.getNotificationBroker().getNotificationByUuid(
								responseMessages[m].getParam(response
										.toLowerCase()
										+ "-uuid"));
				try {
					doResponse(message, response, notification);
				} catch (IOException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (MessagingException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		
		// Now, for all those messages that were neither new notifs, nor had a known response
		// Look to see if it has a comment
		// We define comments as emails sent to the proper uuid@hostname.com where uuid==a valid notification
		for (int messageNum = 0; messageNum < trimmedMessages.size(); messageNum++) {
			Message message = trimmedMessages.get(messageNum);
			try {
				Address[] recipients = message.getAllRecipients();
				for (int recipientNum = 0; recipientNum < recipients.length; recipientNum++) {
					String to = recipients[recipientNum].toString();
					if (to.indexOf("@") > 0) to = to.substring (0, to.indexOf("@"));
					
					Notification notif = BrokerFactory.getNotificationBroker().getNotificationByUuid(to);
					if (notif != null) {
						doResponse (message, "", notif);
					}
				}
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (RuntimeException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		
		
		deleteMessages(messages);

		long totalMillis = System.currentTimeMillis() - startMillis;

		BrokerFactory.getLoggingBroker().logDebug(
				"Finished importing from POP Retrieval");
		// Add the run time
		Hashtable runTimes = (Hashtable) map.get("runtimes");
		if (runTimes == null) {
			runTimes = new Hashtable();
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"POP Retrieval ran for " + totalMillis + " millis");
		runTimes.put(startDate, new Long(totalMillis));
		map.put("runtimes", runTimes);
		jeContext.getJobDetail().setJobDataMap(map);
	}

	private void doResponse(Message message, String response,
			Notification notification) throws IOException, MessagingException {
		if (notification != null) {
			String from = "";
			try {
				from = ((InternetAddress)message.getReplyTo()[0]).getAddress();
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			if (StringUtils.isEmpty(from)) {
				try {
					from = ((InternetAddress)message.getFrom()[0]).getAddress();
				} catch (MessagingException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			// Add the attachments as notif messages
			// TODO: Do we need to trigger a response on each attachment?
			try {
				BodyPart[] bodyParts = getAttachments(message);
				BrokerFactory.getLoggingBroker().logDebug(response+" got "+bodyParts.length+" attachments");
				for (int bodyPartNum = 0; bodyPartNum < bodyParts.length; bodyPartNum++) {
					BodyPart bodyPart = bodyParts[bodyPartNum];
					NotificationMessage notifMessage = new NotificationMessage
						(getContent(bodyPart), from, new Date(), bodyPart.getContentType()); 
					notifMessage.setFilename(bodyPart.getFileName());
					notification.addMessage(notifMessage);
				}
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (MessagingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}

			
			String text = message.getSubject()+"\n"+getContent(message);
			BrokerFactory.getLoggingBroker().logDebug("Handling response for notif "+notification.getUuid()+" with text"+text);
			notification.getSender().handleResponse(
					notification,
					findSenderFromEmail(from),
					response, text);
			
		}
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		PopMailRetriever pop = new PopMailRetriever();
		pop.execute(null);
	}

}
