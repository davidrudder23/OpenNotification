package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

public class IPNServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost (request, response);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuffer params = new StringBuffer();
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String)paramNames.nextElement();
			String[] values = request.getParameterValues(paramName);
			for (int i = 0; i < values.length; i++) {
				params.append (paramName+"="+values[i]+"\n");
			}
		}
		BrokerFactory.getLoggingBroker().logInfo("IPN http params: "+params.toString());
		
		String secret = request.getParameter("secret");
		if (StringUtils.isEmpty(secret)) {
			BrokerFactory.getLoggingBroker().logWarn("Paypal secret empty");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("text/plain");
			response.getOutputStream().write("Authentication Failed".getBytes());
		}
		String accountUuid = request.getParameter("custom");
		if (StringUtils.isEmpty(accountUuid)) {
			BrokerFactory.getLoggingBroker().logWarn("Paypal txn_type empty");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("text/plain");
			response.getOutputStream().write("Authentication Failed".getBytes());
		}
		String txn_type = request.getParameter("txn_type");
		if (StringUtils.isEmpty(txn_type)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("text/plain");
			response.getOutputStream().write("Authentication Failed".getBytes());
		}
		Account account = BrokerFactory.getAccountBroker().getAccountByUuid(accountUuid);
		if (account== null) {
			BrokerFactory.getLoggingBroker().logWarn("Could not find account in paypal's IPN");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("text/plain");
			response.getOutputStream().write("Authentication Failed".getBytes());
		}
		
		if (!account.getPaymentSecret().equals(secret)) {
			BrokerFactory.getLoggingBroker().logWarn("Paypal secret did not match");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("text/plain");
			response.getOutputStream().write("Authentication Failed".getBytes());
		}
		
		
		// Okay, we've verified the basaic stuff, now check the txn_type for
		// what we're doing
		if (txn_type.equalsIgnoreCase("subscr_payment")) {
			String payment_status = request.getParameter("payment_status");
			if (StringUtils.isEmpty(payment_status)) {
				BrokerFactory.getLoggingBroker().logWarn("Paypal payment_status empty");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("text/plain");
				response.getOutputStream().write("Authentication Failed".getBytes());
			}
			
			if ((payment_status.equalsIgnoreCase("completed")) ||
					(payment_status.equalsIgnoreCase("verified"))) {
				BrokerFactory.getLoggingBroker().logWarn(account.getName()+" has paid for his/her subscription");
				account.setAuthorized(true);
			} else if (payment_status.equalsIgnoreCase("failed")) {
				BrokerFactory.getLoggingBroker().logWarn(account.getName()+" has failed to pay for his/her subscription");
				account.setAuthorized(false);
			}
		} else if (txn_type.equalsIgnoreCase("subscr_modify")) {
			// Subscription is modified, so figure out what the
			// customer has bought
			String item_number = request.getParameter("item_number");
			Vector<User> newTelephoneUsers = new Vector<User>();
			StringTokenizer tok = new StringTokenizer(item_number, "+");
			while (tok.hasMoreElements()) {
				String element = (String)tok.nextElement();
				if (element.startsWith("phone")) {
					String userId = element.substring (6, element.length());
					BrokerFactory.getLoggingBroker().logDebug("IPN says "+userId+" has telephone access");
					newTelephoneUsers.addElement(BrokerFactory.getUserMgmtBroker().getUserByUuid(userId));
				}
				
				// Remove from all who have, but don't any longer
				Member[] accountMembers = BrokerFactory.getAccountBroker().getAccountMembers(account);
				for (int i = 0; i < accountMembers.length;i++) {
					if (accountMembers[i] instanceof User) {
						User accountUser = (User)accountMembers[i];
						if (BrokerFactory.getAuthorizationBroker().isUserInRole(accountUser, Roles.TELEPHONE_USER)) {
							if (!newTelephoneUsers.contains(accountUser)) {
								BrokerFactory.getAuthorizationBroker().removeMemberFromRole(accountUser, Roles.TELEPHONE_USER);
								accountUser.removeAllDevicesOfType("net.reliableresponse.notification.device.VoiceShotDevice");
							}
						}
					}
				}
				
				// Add all who don't have, but do now
				for (int i = 0; i < newTelephoneUsers.size(); i++) {
					User newTelephoneUser = newTelephoneUsers.elementAt(i);
					if (!BrokerFactory.getAuthorizationBroker().isUserInRole(newTelephoneUser, Roles.TELEPHONE_USER)) {
						BrokerFactory.getAuthorizationBroker().addUserToRole(newTelephoneUser, Roles.TELEPHONE_USER);
					}
				}
			}
		} else if (txn_type.equalsIgnoreCase("subscr_signup")) {
			BrokerFactory.getLoggingBroker().logInfo(account.getName()+" has signed up");
		} else if (txn_type.equalsIgnoreCase("subscr_cancel")) {
			BrokerFactory.getLoggingBroker().logWarn(account.getName()+" has canceled his/her subscription");
			// TODO:  erase?
			account.setAuthorized(false);
		} else if (txn_type.equalsIgnoreCase("subscr_eot")) {
			BrokerFactory.getLoggingBroker().logWarn(account.getName()+" has come to the end of his/her subscription");
			// TODO:  erase?
			account.setAuthorized(false);
		} else if (txn_type.equalsIgnoreCase("new_case")) {
			BrokerFactory.getLoggingBroker().logWarn("New PayPal dispute");
			Notification disputeNotif = new Notification (null, BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001"),
					new EmailSender("support@reliableresponse.net"), "New PayPal Dispute",
					params.toString());
			try {
				SendNotification.getInstance().doSend(disputeNotif);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		// Everything went well, return an OK message
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		response.getOutputStream().write("OK".getBytes());
	}
}
