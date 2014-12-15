package net.reliableresponse.notification.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

public class ResponseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7273776203931909855L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User)BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getSession().getAttribute("user"));
		if (user == null) {
			response.sendRedirect("/login.jsp");
			return;	
		}

		String action = request.getParameter("action");
		if (StringUtils.isEmpty(action)) {
			response.getOutputStream().write("No action supplied".getBytes());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		String uuid = request.getParameter("uuid");
		if (StringUtils.isEmpty(uuid)) {
			response.getOutputStream().write("No uuid supplied".getBytes());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
		if (notification == null) {
			response.getOutputStream().write("Could not find notification".getBytes());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		notification.getSender().handleResponse(notification, user,null, null);
	}
}
