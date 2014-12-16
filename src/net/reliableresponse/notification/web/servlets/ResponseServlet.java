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

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contextPath = request.getContextPath();
		BrokerFactory.getConfigurationBroker().setStringValue("contextPath", contextPath);
		BrokerFactory.getLoggingBroker().logDebug("Context path = "+contextPath);
		int restOffset = contextPath.split("\\/").length;
		BrokerFactory.getLoggingBroker().logDebug("restOffset = "+restOffset);
		
		String requestURI = request.getRequestURI();
		BrokerFactory.getLoggingBroker().logDebug("Twilio request uri="+requestURI);
		
		String[] peices = requestURI.split("\\/");
		for (int i = 0; i < peices.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("peice["+i+"]: "+peices[i]);
		}
		
		String action = peices[restOffset+1];
		BrokerFactory.getLoggingBroker().logDebug("action="+action);
		
		if (action.equals("respond")) {
			String uuid = peices[restOffset+2];
			String responseText = peices[restOffset+3];
			handleResponse(request, response, uuid, responseText);
		}
	}
	
	protected void handleResponse(HttpServletRequest request, HttpServletResponse response, String uuid, String responseText)
			throws ServletException, IOException {
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
		if (notification == null) {
			response.sendError(500, "No notification found");
		}
		
    	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getSession().getAttribute("user"));
		
		notification.getSender().handleResponse(notification, user, responseText, "");
	}
}
