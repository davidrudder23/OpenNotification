/*
 * Created on Aug 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ConfirmNotificationServlet extends HttpServlet {

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
		
		BrokerFactory.getLoggingBroker().logDebug("confirmer uuid is "+(String)request.getSession().getAttribute("user"));
		User confirmer = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getSession().getAttribute("user"));
		String id = request.getParameter("id");
		if ((id == null) || (id.length() == 0)) {
			request.getSession().setAttribute("pending_notification_message", "You must specify a message id");
			response.sendRedirect("index.jsp");
			return;
		}
		
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(id);
		
		String action = request.getParameter("action");
		if ((action != null) && (action.toLowerCase().startsWith("pass"))) {
			BrokerFactory.getLoggingBroker().logDebug(confirmer+" is passing notification "+id);
			EscalationThread escThread = EscalationThreadManager.getInstance().getEscalationThread(id);
			if (escThread != null) {
				escThread.pass(confirmer);
				request.getSession().setAttribute("pending_notification_message", "Message "+id+" passed successfully");
			} else {
				request.getSession().setAttribute("pending_notification_message", "Message "+id+" expired already");
			}
		} else {
			BrokerFactory.getLoggingBroker().logDebug(confirmer+" is confirming notification "+id);
			notification.setStatus(Notification.CONFIRMED);
			request.getSession().setAttribute("pending_notification_message", "Message "+id+" confirm successfully");
			
		}
		response.sendRedirect("index.jsp");
	}
}
