/*
 * Created on Oct 26, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class RespondAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Respond Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));

		String action = request.getParameter("action");
		if (action == null) {
			action = "";
		}
		if (action.equalsIgnoreCase("action_confirm_marked")) {
			String[] uuidsToConfirm = request.getParameterValues("notification_list");
			if (uuidsToConfirm != null) {
				for (int i = 0; i < uuidsToConfirm.length; i++) {
					Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid (uuidsToConfirm[i]);
					
					if (notification != null) {
						notification.getSender().handleResponse(notification, user, 
								notification.getSender().getConfirmEquivalent(notification), null);
					}
					
				}
				String message = "Confirmed "+uuidsToConfirm.length+" messages";
				actionRequest.addParameter("pending_notification_message", message);
			}
		}
		
		if (action.equalsIgnoreCase("action_pass_marked")) {
			int numPassed = 0;
			String[] uuidsToPass = request.getParameterValues("notification_list");
			if (uuidsToPass != null) {
				for (int i = 0; i < uuidsToPass.length; i++) {
					Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid (uuidsToPass[i]);
					notification.getSender().handleResponse(notification, user, 
							notification.getSender().getPassEquivalent(notification), null);
				}
				String message = "Passed "+numPassed+" messages";
				actionRequest.addParameter("pending_notification_message", message);
			}
		}

		if (action.equalsIgnoreCase("action_release_marked")) {
			int numPassed = 0;
			String[] uuidsToRelease = request.getParameterValues("notification_list");
			if (uuidsToRelease != null) {
				for (int i = 0; i < uuidsToRelease.length; i++) {
					Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid (uuidsToRelease[i]);
					if (notification.getStatus() == Notification.ONHOLD) {
						notification.getSender().handleResponse(notification, user, 
								"release", null);
					}
				}
				String message = "Released "+numPassed+" messages";
				actionRequest.addParameter("pending_notification_message", message);
			}
		}

		if (JSPHelper.getParameterEndings(request, "action_respond_text_").length>0) {
		// Handle the individual responses
		int sinceHours = 2;
		try {
			String displayPast = request.getParameter("display_past");
			if (displayPast == null) displayPast = "2";
			sinceHours = Integer.parseInt(displayPast);
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		long sinceMillis = sinceHours*3600000;
		List<Notification> pendingNotifications =BrokerFactory.getNotificationBroker().getNotificationsSince(sinceMillis);
		pendingNotifications.addAll(BrokerFactory.getNotificationBroker().getAllPendingNotifications());
		for (Notification pendingNotification: pendingNotifications) {
			String[] responses = pendingNotification.getSender().getAvailableResponses(pendingNotification);
			for (int r = 0; r < responses.length; r++) {
				if (request.getParameter ("action_"+responses[r].toLowerCase()+"_"+pendingNotification.getUuid()+".x") != null) {
					String respondText = request.getParameter("action_respond_text_"+pendingNotification.getUuid());
					BrokerFactory.getLoggingBroker().logDebug("respondText="+respondText);
					pendingNotification.getSender().handleResponse(pendingNotification, user, responses[r], respondText);
				}
			}
		}
		// Handle the comment action
		String commentUuid = JSPHelper.getUUIDFromAction(request, "action_comment_");
		if (commentUuid != null) {
			
			SimpleDateFormat dateFormatter = JSPHelper.getDateFormatter();
			
			String respondText = request.getParameter("action_respond_text_"+commentUuid);
			
			Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid (commentUuid);
			
			if (notification != null) {
				if (respondText != null) {
					notification.addMessage (respondText, user);
				}	
			}
		}
		
		// Handle the forward action
		String forwardUuid = JSPHelper.getUUIDFromAction(request, "action_forward_");
		if (forwardUuid != null) {
			
			SimpleDateFormat dateFormatter = JSPHelper.getDateFormatter();
			
			String respondText = request.getParameter("action_respond_text_"+forwardUuid);
			
			Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid (forwardUuid);
			
			if (notification != null) {
				if (respondText != null) {
					notification.addMessage (respondText, user);
				}
				actionRequest.setParameter("send_subject", "Fwd: "+notification.getSubject());
				
				String message = respondText;
				NotificationMessage[] messages = notification.getMessages();
				for (int m = 0; m < messages.length; m++) {
					message += "\n\n";
					String addedby = messages[m].getAddedby();
				    if ((addedby != null) && (addedby.length() > 0)) {
				    	message += addedby;
				    	message += " ";
				    	message += dateFormatter.format(messages[m].getAddedon())+" - ";
				    }
				    message += messages[m].getMessage();
				}
				actionRequest.setParameter("send_message", message);
				actionRequest.setParameter("opened.sendNotification", "true");
				
				String systemMessage = "Notification with subject \""+notification.getSubject()+"\" is open for forwarding below";
				actionRequest.addParameter("pending_notification_message", systemMessage);
			}

		}
		}
		return actionRequest;
	}

}
