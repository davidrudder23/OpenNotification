/*
 * Created on Nov 2, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.actions.ActionRequest;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class ForgotPasswordServlet extends HttpServlet {
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BrokerFactory.getLoggingBroker().logDebug("Authentication Servlet");
		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);
		RequestDispatcher loginPage = request.getRequestDispatcher("login.jsp");
		
		String token = request.getParameter("token");
		if (token == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		
		User user = BrokerFactory.getAuthenticationBroker().getUserByPasswordToken(token);
		if (user == null) {
			actionRequest.addParameter("authentication.message", "The request you made had an error.  Please check the URL and try again.");
			loginPage.forward(actionRequest, response);
			return;
		}
		
		// Now, delete the stored token
		BrokerFactory.getAuthenticationBroker().getPasswordChangeToken(user);
		
		try {
			String newPassword =""; 
			MessageDigest md5 = MessageDigest.getInstance("MD5"); 
			md5.digest((user.getUuid()+System.currentTimeMillis()+"newpassword").getBytes());
			if (newPassword.length() > 8) {
				newPassword = newPassword.substring(0, 8);
			}
			BrokerFactory.getAuthenticationBroker().changePassword(user, newPassword);
			String message = "Your password has been changed to "+newPassword+"\n"+
							"Please change it as soon as you can.";
			Notification notification = new Notification(null, user, 
					new EmailSender("passwordchange@reliableresponse.net"), "New password", message);
			SendNotification.getInstance().doSend(notification);
			BrokerFactory.getLoggingBroker().logDebug("Reset "+user+"'s password");
			
			actionRequest.addParameter("authentication.message", "A new password was generated and sent to you through your notification devices");
			loginPage.forward(actionRequest, response);
			return;
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
			actionRequest.addParameter("authentication.message", "An error occured while resetting your password.  Please try again later.");
			loginPage.forward(actionRequest, response);
			return;
		}
	}
}