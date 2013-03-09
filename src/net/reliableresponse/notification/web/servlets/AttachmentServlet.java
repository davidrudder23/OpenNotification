/*
 * Created on Aug 30, 2004
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

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AttachmentServlet extends HttpServlet {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
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
		BrokerFactory.getLoggingBroker().logDebug("Attachment Servlet");
		int messageID = 0;
		try {
			messageID = Integer.parseInt(request.getParameter("messageID"));
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logWarn("Bad message ID: "+messageID+", not parseable");
			response.sendError(500, "Bad message ID: "+messageID+", not parseable");
			return;
		}
		String notificationUuid = request.getParameter("uuid");
		if (StringUtils.isEmpty(notificationUuid)) {
			BrokerFactory.getLoggingBroker().logWarn("Bad notification UUID: "+notificationUuid);
			response.sendError(500, "Bad notification UUID: "+notificationUuid);
			return;			
		}

		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
		if (notification == null) {
			BrokerFactory.getLoggingBroker().logWarn("Notification "+notificationUuid+" not found");
			response.sendError(500, "Notification "+notificationUuid+" not found");
			return;			
			
		}
		NotificationMessage[] messages = notification.getMessages();
		if (messageID>=messages.length) {
			BrokerFactory.getLoggingBroker().logWarn("Message ID "+messageID+" for notification "+notificationUuid+" too big");
			response.sendError(500, "Message ID "+messageID+" for notification "+notificationUuid+
					" too big.  It only has "+messages.length+" messages");
			return;			
		}
		NotificationMessage message = messages[messageID];
		byte[] content = message.getContent();
		response.setContentLength(content.length);
		response.setContentType(message.getContentType());
		response.setHeader("Content-Disposition","inline;filename="+message.getFilename());

		response.getOutputStream().write(content);
		
	}
}
