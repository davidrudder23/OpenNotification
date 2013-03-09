/*
 * Created on Jan 12, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class RegisterAction implements Action {

	public static Hashtable pendingUsers = new Hashtable();

	public static Hashtable passwords = new Hashtable();

	public RegisterAction() {
	}

	private void sendEmail(String to, String url) throws Exception {
		try {

			//		Get system properties
			Properties props = System.getProperties();

			if (props == null)
				props = new Properties();
			String smtpServer = BrokerFactory.getConfigurationBroker()
					.getStringValue("smtp.server");
			if (smtpServer == null)
				smtpServer = "smtp.comcast.net";
			//		Setup mail server
			props.put("mail.smtp.host", smtpServer);

			//		Get session
			Session session = Session.getDefaultInstance(props, null);
			session.getProperties().setProperty("mail.smtp.host", smtpServer);

			//		Define message
			MimeMessage message = new MimeMessage(session);
			message
					.setFrom(new InternetAddress("noreply@reliableresponse.net"));
			Address[] replyTo = new Address[1];

			String mailMethod = BrokerFactory.getConfigurationBroker()
					.getStringValue("email.method");
			if (mailMethod == null)
				mailMethod = "pop";
			mailMethod = mailMethod.toLowerCase();

			if (mailMethod.equals("smtp")) {
				replyTo[0] = new InternetAddress("noreply@"
						+ BrokerFactory.getConfigurationBroker()
								.getStringValue("smtp.server.hostname"));
			} else {
				replyTo[0] = new InternetAddress(BrokerFactory
						.getConfigurationBroker().getStringValue(
								"email.pop.address"));
			}
			BrokerFactory.getLoggingBroker().logDebug(
					"Setting reply to " + replyTo[0]);
			message.setReplyTo(replyTo);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject("New user registration");

			String messageText = "Welcome to Reliable Response Notification\n"
					+ "To finish your registration, please go to this address\n"
					+ url;

			message.setText(messageText);
			String id = message.getMessageID();
			//		Send message
			Transport.send(message);
			id = message.getMessageID();

		} catch (AddressException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new Exception("Your email address was invalid");
		} catch (MessagingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new Exception(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Register User Action running");
		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		String addUserAction = request.getParameter("action_addnew_save.x");
		BrokerFactory.getLoggingBroker().logDebug("addUserAction="+addUserAction);
		if (addUserAction != null) {
			User user = new User();
			String addFirstName = request.getParameter("addFirstName");
			String addLastName = request.getParameter("addLastName");
			String addDepartment = "OnLine Registration";
			String addEmail = request.getParameter("addEmail");
			String addStartHourString = request.getParameter("addStartHour");
			String addStartMinutesString = request
					.getParameter("addStartMinutes");
			String startAM = request.getParameter("addStartAMPM");
			String addEndHourString = request.getParameter("addEndHour");
			String addEndMinutesString = request.getParameter("addEndMinutes");
			String endAM = request.getParameter("addEndAMPM");

			String addPassword = request.getParameter("addPassword");
			String addConfirmPassword = request
					.getParameter("addConfirmPassword");

			if (addPassword != null) {
				if (addConfirmPassword == null) {
					actionRequest.setParameter("add_user_system_message",
							"Your passwords do not match");
					return actionRequest;
				}
				if (!addPassword.equals(addConfirmPassword)) {
					actionRequest.setParameter("add_user_system_message",
							"Your passwords do not match");
					return actionRequest;
				}
			}

			if (addFirstName != null)
				user.setFirstName(addFirstName);
			if (addLastName != null)
				user.setLastName(addLastName);
			if (addDepartment != null)
				user.setDepartment(addDepartment);

			if ((addStartHourString != null) && (addStartMinutesString != null)
					&& (startAM != null)) {
				try {
					Calendar startDate = Calendar.getInstance();
					int addStartHour = Integer.parseInt(addStartHourString);
					if (startAM.toLowerCase().equals("pm")) {
						addStartHour += 12;
					}
					startDate.set(Calendar.HOUR_OF_DAY, addStartHour);

					startDate.set(Calendar.MINUTE, Integer
							.parseInt(addStartMinutesString));
					user.setStartTime(startDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}

			if ((addEndHourString != null) && (addEndMinutesString != null)
					&& (endAM != null)) {
				try {
					Calendar endDate = Calendar.getInstance();
					int addEndHour = Integer.parseInt(addEndHourString);
					if (endAM.toLowerCase().equals("pm")) {
						addEndHour += 12;
					}
					endDate.set(Calendar.HOUR_OF_DAY, addEndHour);

					endDate.set(Calendar.MINUTE, Integer
							.parseInt(addEndMinutesString));
					user.setEndTime(endDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}

			if (BrokerFactory.getUserMgmtBroker().getUsersByName(addFirstName,
					addLastName).length > 0) {
				actionRequest.setParameter("add_user_system_message",
						"A user with that name already exists");
				return actionRequest;
			}

			Enumeration keys = pendingUsers.keys();
			while (keys.hasMoreElements()) {
				User test = (User) pendingUsers
						.get((String) keys.nextElement());
				if ((test.getFirstName().equals(addFirstName))
						&& (test.getLastName().equals(addLastName))) {
					actionRequest.setParameter("add_user_system_message",
							"A user with that name already exists");
					return actionRequest;
				}
			}

			// Add the user's email as a new device
			EmailDevice device = new EmailDevice();
			Hashtable options = new Hashtable();
			options.put("Address", addEmail);
			device.initialize(options);
			user.addDevice(device);

			String token = "";

			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				digest.update(("" + System.currentTimeMillis()).getBytes());
				digest.update(addFirstName.getBytes());
				digest.update(addLastName.getBytes());
				byte[] digestBytes = digest.digest();

				for (int i = (digestBytes.length - 7); i < digestBytes.length; i++) {
					int byteVal = digestBytes[i];
					if (byteVal < 0)
						byteVal += 256;
					token += (byteVal % 10);
				}

			} catch (NoSuchAlgorithmException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				actionRequest
						.setParameter("add_user_system_message",
								"A system error has occurred.  Please try again later.");
				return actionRequest;
			}
			pendingUsers.put(token, user);
			passwords.put(user.getUuid(), addPassword);

			String url = "";
			
			try {
				url = BrokerFactory.getConfigurationBroker().getStringValue(
						"base.url")
						+ "/ActionServlet?page=/register.jsp&confirmation="
						+ URLEncoder.encode(token, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
			BrokerFactory.getLoggingBroker().logDebug("url=" + url);
			try {
				sendEmail(addEmail, url);
				actionRequest
						.setParameter("authentication.message",
								"An email has been sent to you with confirmation instructions.");
				actionRequest.setParameter("page", "/login.jsp");
			} catch (Exception anyExc) {
				actionRequest.setParameter("add_user_system_message", anyExc
						.getMessage());
				return actionRequest;
			}
			return actionRequest;
		}

		String confirmationToken = request.getParameter("confirmation");
		BrokerFactory.getLoggingBroker().logDebug("confirmation="+confirmationToken);
		if (confirmationToken != null) {
			actionRequest.setParameter("page", "/eula.jsp");
		}

		return actionRequest;
	}

}