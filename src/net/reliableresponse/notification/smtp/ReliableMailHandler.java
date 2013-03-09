/*
 * Created on Nov 4, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.meterware.httpunit.Base64;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 * 
 *         Copyright 2004 - David Rudder
 */
public class ReliableMailHandler implements MailHandler {
	Member recipient;

	String sender;

	String subject;

	StringBuffer message;

	boolean inHeader;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.reliableresponse.notification.smtp.MailHandler#CheckToUser(java.lang
	 * .String)
	 */
	public boolean CheckToUser(String strUser) {
		recipient = BrokerFactory.getUserMgmtBroker().getUserByEmailAddress(
				strUser);
		if (recipient == null) {
			recipient = BrokerFactory.getGroupMgmtBroker().getGroupByEmail(
					strUser);
		}

		// TODO: Give groups email addresses, too
		BrokerFactory.getLoggingBroker().logDebug("recipient=" + recipient);

		if (recipient == null)
			return false;
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.reliableresponse.notification.smtp.MailHandler#CheckFromUser(java
	 * .lang.String)
	 */
	public boolean CheckFromUser(String strUser) {
		if ((strUser == null) || (strUser.length() == 0))
			return false;
		sender = strUser;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#Start()
	 */
	public boolean Start() {
		message = new StringBuffer();
		inHeader = true;
		subject = "";
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.reliableresponse.notification.smtp.MailHandler#Line(java.lang.String)
	 */
	public void Line(String strLine) {
		if (inHeader) {
			if (strLine.equals("")) {
				inHeader = false;
			} else if (strLine.toLowerCase().startsWith("subject:")) {
				subject = strLine.substring(8, strLine.length());
				subject = subject.trim();
				
				BrokerFactory.getLoggingBroker().logDebug("subject: "+subject);
				// Check for UTF-8 encoding
				if (subject.toLowerCase().startsWith("=?utf-8?b?")) {
					BrokerFactory.getLoggingBroker().logDebug("UTF-8 encoded subject: "+subject);
					subject = subject.substring("=?utf-8?b?".length(), subject.length());
					subject = subject.substring(0, subject.length()-2);
					BrokerFactory.getLoggingBroker().logDebug("Stripped subject: "+subject);
					subject = Base64.decode(subject);
					BrokerFactory.getLoggingBroker().logDebug("Decoded subject: "+subject);
				}
			}
		}
		message.append(strLine);
		message.append("\n");
	}

	private String getFromPart(BodyPart part) {
		try {
			BrokerFactory.getLoggingBroker().logDebug(
					"part content type=" + part.getContentType());
			BrokerFactory.getLoggingBroker().logDebug(
					"part content=" + part.getContent());
		} catch (MessagingException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		} catch (IOException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream in = part.getInputStream();
			byte[] b = new byte[1024];
			int size = 0;
			while ((size = in.read(b, 0, b.length)) > 0) {
				buffer.append(new String(b, 0, size));
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (MessagingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return buffer.toString();
	}

	private String getFromMultiPart(Multipart multipart) {
		try {
			int count = multipart.getCount();
			StringBuffer messageContents = new StringBuffer();
			BrokerFactory.getLoggingBroker().logDebug(
					"Mail is a multipart with " + count + " parts");
			for (int i = 0; i < count; i++) {
				BrokerFactory.getLoggingBroker().logDebug(
						"part " + i + " type="
								+ multipart.getBodyPart(i).getContentType());
				if (multipart.getBodyPart(i).getContentType().toLowerCase()
						.indexOf("text/plain") >= 0) {
					messageContents
							.append(getFromPart(multipart.getBodyPart(i)));
				} else if (multipart.getBodyPart(i).getContentType()
						.toLowerCase().indexOf("multipart/alternative") >= 0) {
					messageContents
							.append(getFromMultiPart((Multipart) (multipart
									.getBodyPart(i).getContent())));
				}
			}
			return messageContents.toString();
		} catch (MessagingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#End()
	 */
	public void End() {
		Notification notification = null;
		try {
			Session session = Session
					.getDefaultInstance(new Properties(), null);
			StringBufferInputStream in = new StringBufferInputStream(message
					.toString());
			MimeMessage mime = new MimeMessage(session, in);

			Object content = mime.getContent();
			BrokerFactory.getLoggingBroker().logDebug("content=" + content);
			if (content instanceof String) {
				BrokerFactory.getLoggingBroker().logDebug(
						"content is an instance of string");
				notification = new Notification(null, recipient,
						new EmailSender(sender), subject, (String) content);
			} else if (content instanceof MimeMultipart) {
				MimeMultipart multipart = (MimeMultipart) content;

				String messageContents = getFromMultiPart(multipart);
				if (messageContents.length() == 0) {
					messageContents = getFromPart(multipart.getBodyPart(0));
				}
				notification = new Notification(null, recipient,
						new EmailSender(sender), subject, messageContents
								.toString());
			}
		} catch (Exception e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
			notification = new Notification(null, recipient, new EmailSender(
					sender), subject, message.toString());
		}

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

		if (doSend) {
		try {
			SendNotification.getInstance().doSend(notification);
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.smtp.MailHandler#Abort()
	 */
	public void Abort() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.reliableresponse.notification.smtp.MailHandler#ConnectionClosed(boolean
	 * )
	 */
	public void ConnectionClosed(boolean bCleanExit) {
		// TODO Auto-generated method stub

	}

}