/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.sender.UserSender;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.IPUtil;

/**
 * @author drig
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SMTPNotificationProvider extends AbstractNotificationProvider {

	private static SMTPNotificationProvider provider;

	public SMTPNotificationProvider() {
	}

	public void init(Hashtable params) {

	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put("tracking number", "000000");
		return params;
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		User user = (User) notification.getRecipient();
		NotificationSender sender = notification.getSender();
		String summary = notification.getSubject();
		Vector options = notification.getOptions();
		String messageText = notification.getDisplayText();

		// Add links to attachments
		NotificationMessage[] messages = notification.getMessages();
		boolean hasHeader = false;
		if (messages.length > 1) {
			String baseURL = BrokerFactory.getConfigurationBroker()
					.getStringValue("base.url");

			if (BrokerFactory.getConfigurationBroker().getBooleanValue(
					"show.attachments.email",
					BrokerFactory.getConfigurationBroker().getBooleanValue(
							"show.attachments", true))) {
				for (int msgNum = 1; msgNum < messages.length; msgNum++) {
					if ((messages[msgNum].getContentType().toLowerCase()
							.indexOf("text/plain") < 0)
							&& (!messages[msgNum]
									.getContentType()
									.equals(
											NotificationMessage.NOTIFICATION_CONTENT_TYPE))) {
						if (!hasHeader) {
							messageText += "\n\nAttachments:\n\n";
							hasHeader = true;
						}
						String link = baseURL + "/AttachmentServlet?uuid="
								+ notification.getUuid() + "&messageID="
								+ msgNum;
						messageText += messages[msgNum].getFilename() + ": "
								+ link + "\n";
					}
				}
			}
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"Sender has "
						+ sender.getAvailableResponses(notification).length
						+ " available responses");
		if ((notification.isPersistent())
				&& (sender.getAvailableResponses(notification).length > 0)) {
			messageText += "\n\n\n";
			boolean isSpaceConstrained = device.getMaxCharactersSize() < 10240;
			if (isSpaceConstrained) {
				messageText += "Reply With:\n";
			} else {
				messageText += "You may reply to this message by entering any of these responses in the subject\n\n";
			}
			String[] responses = notification.getSender()
					.getAvailableResponses(notification);
			BrokerFactory.getLoggingBroker().logDebug("Device = " + device);
			for (int i = 0; i < responses.length; i++) {
				messageText += "    "
						+ device.getFormattedResponse(responses[i],
								notification);
				if (!BrokerFactory.getConfigurationBroker().getBooleanValue(
						"email.pop.catchall", true)) {
					messageText += " " + notification.getUuid();
				}
				
				messageText += "\n";
			}
			messageText += "Or you may click on any of these links:\n";
			for (int i = 0; i < responses.length; i++) {
				messageText += " - "+IPUtil.getExternalBaseURL()+
						BrokerFactory.getConfigurationBroker().getStringValue("contextPath","")+
						"/ResponseServlet/"+notification.getUuid()+"/"
						+ URLEncoder.encode(responses[i]);
				messageText += "\n";
			}
			BrokerFactory.getLoggingBroker().logDebug(
					"Sending message w/ text: " + messageText);
		}

		String to = ((EmailDevice) device).getEmailAddress();

		Hashtable params = SMTPNotificationProvider.sendEmail(
				(EmailDevice) device, notification, sender, summary,
				messageText, to);
		return params;
	}

	/**
	 * @param notification
	 * @param sender
	 * @param summary
	 * @param messageText
	 * @param to
	 * @return
	 * @throws NotificationException
	 */
	public static Hashtable sendEmail(Device device, Notification notification,
			NotificationSender sender, String summary, String messageText,
			String to) throws NotificationException {
		// Get system properties
		Properties props = System.getProperties();

		if (props == null)
			props = new Properties();
		List<String> smtpServers = BrokerFactory.getConfigurationBroker().getStringValues("smtp.server", Arrays.asList("localhost"));
		
		int smtpServerNum = 0;
		boolean succeeded = false;
		int errorNum = 0;
		String error = "";
		String id = "unknown_" + System.currentTimeMillis();

		while ((smtpServerNum < smtpServers.size()) && (!succeeded)) {
			try {
				String smtpServer = smtpServers.get(smtpServerNum);
				BrokerFactory.getLoggingBroker().logDebug(
						"Trying SMTP Server num " + smtpServerNum + ": "
								+ smtpServer + ".");
				smtpServerNum++;
				// Setup mail server
				props.put("mail.smtp.host", smtpServer);

				// Set the "from" address to the bounce address
				if (BrokerFactory.getConfigurationBroker().getBooleanValue(
						"email.bounce.enable", true)) {
					props.put("mail.smtp.from", notification.getUuid()
							+ "_"
							+ device.getUuid()
							+ BrokerFactory.getConfigurationBroker()
									.getStringValue("email.bounce.suffix",
											"-bounce") + "@"
							+ getDomainPartOfFrom());
					BrokerFactory.getLoggingBroker().logDebug(
							"Bounce address is " + props.get("mail.smtp.from"));
				}

				// Get session
				Session session = Session.getDefaultInstance(props, null);
				session.getProperties().setProperty("mail.smtp.host",
						smtpServer);

				// Define message
				MimeMessage message = new MimeMessage(session);

				InternetAddress returnAddress = getReturnEmailAddress(device,
						notification);
				BrokerFactory.getLoggingBroker().logDebug(
						"SMTP from address=" + returnAddress);
				message.setFrom(returnAddress);
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(to));

				// String[] parts = splitMessage(messageText,
				// device.getMaxCharactersSize(), device.getMaxMessages());
				List<String> parts = splitMessage(messageText, 10240, device.getMaxMessages());
				int partNum = 0;
				for (String part: parts) {
					partNum++;
					if (parts.size()< 2) {
						message.setSubject(summary);
					} else {
						message.setSubject((partNum + 1) + ": " + summary);
					}
					message.setText(part);

					if (BrokerFactory.getConfigurationBroker().getBooleanValue(
							"email.attachments.attach", false)) {
						NotificationMessage[] attachments = notification
								.getMessages();
						Multipart attachmentParts = new MimeMultipart();
						MimeBodyPart attachmentPart = new MimeBodyPart();
						attachmentPart.setText(part);
						attachmentParts.addBodyPart(attachmentPart);
						
						for (int i = 1; i < attachments.length; i++) {
							NotificationMessage attachment = attachments[i];
							if (!attachment.getContentType().equals(NotificationMessage.NOTIFICATION_CONTENT_TYPE)) {
								attachmentPart = new MimeBodyPart();
								DataSource source = new ByteArrayDataSource(attachment.getContent(), attachment.getContentType());
								attachmentPart.setDataHandler(new DataHandler(source));
								attachmentPart.setFileName(attachment.getFilename());
								attachmentParts.addBodyPart(attachmentPart);
							}
						}
						
						message.setContent(attachmentParts);

					}
					// Send message
					BrokerFactory.getLoggingBroker().logInfo(
							"Sending SMTP Email:\n" + message.toString());
					// Check for STMP authentication
					// If we don't find it, just use the Transport's static
					// methods
					// If we do, use the more complicated method that allows
					// authn
					String smtpUsername = BrokerFactory
							.getConfigurationBroker().getStringValue("smtp.username");
					String smtpPassword = BrokerFactory
							.getConfigurationBroker().getStringValue("smtp.password");
					if ((smtpUsername == null) || (smtpPassword == null)) {
						Transport.send(message);
					} else {
						Transport tr = session.getTransport("smtp");
						tr.connect(smtpServer, smtpUsername, smtpPassword);
						message.saveChanges(); // don't forget this
						tr.sendMessage(message, message.getAllRecipients());
						tr.close();
					}
				}
				id = message.getMessageID();

				succeeded = true;
			} catch (AddressException e) {
				e.printStackTrace();
				error = e.getMessage();
				errorNum = NotificationException.FAILED;
				notification.getSender().handleBounce(device);
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (MessagingException e) {
				e.printStackTrace();
				error = e.getMessage();
				errorNum = NotificationException.TEMPORARILY_FAILED;
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		if (!succeeded) {
			throw new NotificationException(errorNum, error);
		}

		Hashtable params = new Hashtable();
		params.put("tracking number", id);
		return params;
	}

	public static String getDomainPartOfFrom() {
		String mailMethod = BrokerFactory.getConfigurationBroker()
				.getStringValue("email.method");
		if (mailMethod == null)
			mailMethod = "pop";
		mailMethod = mailMethod.toLowerCase();

		if (mailMethod.equals("smtp")) {
			return BrokerFactory.getConfigurationBroker().getStringValue(
					"smtp.server.hostname");
		} else {
			String hostname = BrokerFactory.getConfigurationBroker()
					.getStringValue("email.pop.address");
			if (hostname.indexOf("@") > 0) {
				hostname = hostname.substring(hostname.indexOf("@") + 1,
						hostname.length());
			}
			return hostname;
		}

	}

	/**
	 * @param device
	 * @param notification
	 * @param sender
	 * @param message
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public static InternetAddress getReturnEmailAddress(Device device,
			Notification notification) throws AddressException,
			MessagingException {
		NotificationSender sender = notification.getSender();
		String realName = "";

		InternetAddress fromAddress = null;
		if (sender instanceof EmailSender) {
			fromAddress = new InternetAddress(((EmailSender) sender)
					.getAddress());
		} else if (sender instanceof UserSender) {
			User user = ((UserSender) sender).getUser();
			realName = user.getFirstName() + " " + user.getLastName();
			try {
				fromAddress = new InternetAddress(user.getEmailAddress(),
						realName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		InternetAddress replyToAddress = null;

		String mailMethod = BrokerFactory.getConfigurationBroker()
				.getStringValue("email.method");
		if (mailMethod == null)
			mailMethod = "pop";
		mailMethod = mailMethod.toLowerCase();

		if (mailMethod.equals("smtp")) {
			try {
				replyToAddress = new InternetAddress(notification.getUuid()
						+ "@"
						+ BrokerFactory.getConfigurationBroker()
								.getStringValue("smtp.server.hostname"),
						realName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		} else {
			boolean checkAll = BrokerFactory.getConfigurationBroker()
					.getBooleanValue("email.pop.catchall", true);
			if (checkAll) {
				String hostname = BrokerFactory.getConfigurationBroker()
						.getStringValue("email.pop.address");
				if (hostname.indexOf("@") > 0) {
					hostname = hostname.substring(hostname.indexOf("@") + 1,
							hostname.length());
				}
				try {
					replyToAddress = new InternetAddress(notification.getUuid()
							+ "@" + hostname, realName);
				} catch (UnsupportedEncodingException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			} else {
				try {
					replyToAddress = new InternetAddress(BrokerFactory
							.getConfigurationBroker().getStringValue(
									"email.pop.address"), realName);
				} catch (UnsupportedEncodingException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"Reply To address = " + replyToAddress);
		boolean doReplyTo = false;
		if (device instanceof EmailDevice) {
			doReplyTo = ((EmailDevice) device).useReplyTo();
		}
		if (doReplyTo) {
			Address[] replyTo = new Address[1];
			replyTo[0] = replyToAddress;
			if (replyToAddress != null) {
				return replyToAddress;
			} else {
				return fromAddress;
			}
		} else {
			return replyToAddress;
		}
	}

	public boolean isConfirmed(Notification page) {
		return false;
	}

	public boolean isPassed(Notification page) {
		return false;
	}

	public String[] getResponses(Notification page) {
		return new String[0];
	}

	/**
	 * 
	 * @param pageId
	 * @return Whether the cancellation was successfull
	 */
	public boolean cancelPage(Notification page) {
		// I don't think we know how to cancel email messages
		return false;
	}

	public String getName() {
		return "Email";
	}

	public String toString() {
		return "SMTP Email";
	}

}
