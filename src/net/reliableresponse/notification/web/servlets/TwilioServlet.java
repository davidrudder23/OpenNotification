package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.IPUtil;
import net.reliableresponse.notification.util.StringUtils;

public class TwilioServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7651817805737819417L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req, resp);
	}

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
		
		if (action.equals("twiml")) {
			String uuid = peices[restOffset+2];
			handleTwiMLRequest(request, response, uuid);
		} else if (action.equals("respond")) {
			String uuid = peices[restOffset+2];
			handleTwilioResponse(request, response, uuid);
		}
		
		//super.service(request, response);
	}
	
	protected void handleTwilioResponse(HttpServletRequest request, HttpServletResponse response, String uuid) throws ServletException, IOException {
		BrokerFactory.getLoggingBroker().logDebug("Twilio uuid="+uuid);
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			BrokerFactory.getLoggingBroker().logDebug("Param: "+name+": "+request.getParameter(name));
		}
	}
	
	protected void handleTwiMLRequest(HttpServletRequest request, HttpServletResponse response, String uuid) throws ServletException, IOException {
		BrokerFactory.getLoggingBroker().logDebug("Twilio uuid="+uuid);
		if (StringUtils.isEmpty(uuid)) {
			response.sendError(500, "No uuid");
			return;
		}

		try {
			Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
			if (notification == null) {
				response.sendError(500);
				return;
			}
			
			String message = "You have a new notification from " + notification.getSender() + ".  The subject is " + notification.getSubject()
					+ ".  The message is " + notification.getMessages()[0].getMessage() + ".";
			
			message = "Test";
			TwiMLResponse twilioResponse = new TwiMLResponse();
			Say messageSay = new Say(message);
			twilioResponse.append(messageSay);
			
			Gather gather = new Gather();
			gather.setAction(IPUtil.getExternalBaseURL()+"/TwilioServlet/respond/"+notification.getUuid());
			gather.setNumDigits(1);
			Say gatherSay = new Say("Press 1 to do something");
			gather.append(gatherSay);
			twilioResponse.append(gather);
			
			BrokerFactory.getLoggingBroker().logDebug(twilioResponse.toXML());
			BrokerFactory.getLoggingBroker().logDebug("We're good up to here");
			
	        response.setContentType("application/xml");
			response.getWriter().print(twilioResponse.toXML());
			//response.getOutputStream().write(twilioResponse.toXML().getBytes());
			//response.flushBuffer();
		} catch (TwiMLException e) {
			// TODO Auto-generated catch block
			response.sendError(23, e.getMessage());
			e.printStackTrace();
		}

	}
	
	

}
