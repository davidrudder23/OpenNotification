/*
 * Created on Aug 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
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
import net.reliableresponse.notification.sender.UserSender;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SendNotificationServlet extends HttpServlet {

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
		
		// Figure out who we're supposed to be sending to.  If blank, redirect to the input form
		Vector users = (Vector)request.getSession().getAttribute("pendingUsers");
		
		Vector groups = (Vector)request.getSession().getAttribute("pendingGroups");
		if (groups != null) {
			if (users == null) {
				users= new Vector(); 
			} else {
				users = new Vector(users);
			}
			users.addAll(groups);
		}
			
		String summary = request.getParameter("subject");
		if (summary == null) summary = "";
		String message = request.getParameter("message");
		if ((message == null) ||
				(message.length() <= 0)) {
			request.getSession().setAttribute("Error", "You must include a message to send");
			response.sendRedirect("index.jsp");
			return;
		}
		
		// Check to make sure we have at least one person or group to notification
		if ((users == null) ||
				(users.size() == 0)) {
			request.getSession().setAttribute("Error", "You must specify at least one user or group to notification");
			response.sendRedirect("index.jsp");
			return;
		} else {
			int count = 0;
			for (int i = 0; i < users.size(); i++) {
				Member member = (Member)users.elementAt(i);
				Notification page = new Notification (null, member, null, summary, message);
				User sender = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getSession().getAttribute("user"));
				if (sender != null) {
					page.setSender(new UserSender(sender));
				}
				try {
					SendNotification.getInstance().doSend(page);
					count++;
				} catch (NotificationException e) {
					String error = (String)request.getSession().getAttribute("Error");
					if (error == null) error = "";
					request.getSession().setAttribute("Error", error+"\n"+e.getMessage());
					response.sendRedirect("index.jsp");
				}
			}
			request.getSession().setAttribute("Info", count+" messages sent");
		}
		String returnURL = "index.jsp";
		String userPageNum = request.getParameter("userPageNum");
		BrokerFactory.getLoggingBroker().logDebug("userPageNum="+userPageNum);
		if (userPageNum != null) {
			returnURL += "?userPageNum="+userPageNum;
		}
		response.sendRedirect(returnURL);
	}
}
