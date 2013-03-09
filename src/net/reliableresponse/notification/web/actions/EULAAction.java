/*
 * Created on Jan 13, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.actions;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class EULAAction implements Action {

	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug(
				"EULA Action running");
		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		String confirmationToken = request.getParameter("confirmation");

		if (confirmationToken != null) {
			String accept = request.getParameter("Accept");
			if ((accept != null) && (accept.equals("Accept"))) {
				User user = (User) RegisterAction.pendingUsers
						.get(confirmationToken);
				if (user != null) {
					try {
						BrokerFactory.getUserMgmtBroker().addUser(user);
						String loginName = user.getFirstName() + "."
								+ user.getLastName();
						BrokerFactory.getAuthenticationBroker().addUser(
								loginName,
								(String) RegisterAction.passwords.get(user
										.getUuid()), user);
						RegisterAction.pendingUsers.remove(confirmationToken);
						RegisterAction.passwords.remove(user.getUuid());

						NotificationSender from;
						String mailMethod = BrokerFactory
								.getConfigurationBroker().getStringValue(
										"email.method");
						if (mailMethod == null)
							mailMethod = "pop";
						mailMethod = mailMethod.toLowerCase();

						if (mailMethod.equals("smtp")) {
							from = new EmailSender("noreply@"
									+ BrokerFactory.getConfigurationBroker()
											.getStringValue(
													"smtp.server.hostname"));
						} else {
							from = new EmailSender(BrokerFactory.getConfigurationBroker()
									.getStringValue("email.pop.address"));
						}

						Notification newUserNotif = new Notification(
								null,
								user,
								from,
								"Your Reliable Response Notification account is active",
								"Thank you for registering with Reliable Response Notification.\n"
										+ "Your account is now active.  You may login as "
										+ loginName);
						try {
							SendNotification.getInstance().doSend(newUserNotif);
						} catch (NotificationException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
						actionRequest.setParameter("authentication.message",
								"Your account is now active.  You can login as "
										+ loginName);
						actionRequest.setParameter("page", "/login.jsp");
					} catch (NotSupportedException e) {
						actionRequest
								.setParameter("authentication.message",
										"A system error has occurred.  Please try again later.");
						actionRequest.setParameter("page", "/login.jsp");
						BrokerFactory.getLoggingBroker().logError(e);
						
					}
				} else {
					actionRequest
							.setParameter("authentication.message",
									"We could not find the confirmation number you supplied.");
					actionRequest.setParameter("page", "/login.jsp");
				}

			} else {
				User user = (User) RegisterAction.pendingUsers
						.get(confirmationToken);
				if (user != null) {
					RegisterAction.passwords.remove(user.getUuid());
				}
				RegisterAction.pendingUsers.remove(confirmationToken);
				actionRequest
						.setParameter("authentication.message",
								"Your account has been removed.  Thank you for your interest");
				actionRequest.setParameter("page", "/login.jsp");
			}
		}

		return actionRequest;

	}

}