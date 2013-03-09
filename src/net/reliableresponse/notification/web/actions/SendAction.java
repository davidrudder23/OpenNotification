/*
 * Created on Oct 26, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.sender.UserSender;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SendAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Send Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);

		User sendingUser = BrokerFactory.getUserMgmtBroker().getUserByUuid((String) actionRequest.getSession().getAttribute("user"));
		
		// Process the cancel button
		if (request.getParameter("action_send_cancel.x") != null) {
			actionRequest.removeParameter("send_subject");
			actionRequest.removeParameter("send_message");
			actionRequest.setParameter("opened.sendNotification", "false");
		}	
		
		// Process the remove button
		if (request.getParameter("action_remove_recipients.x") != null) {
			String[] uuidsToRemove = request.getParameterValues("recipient_list");
			if (uuidsToRemove != null) {
				for (int i = 0; i < uuidsToRemove.length; i++) {
					actionRequest.removeParameter("selected_user_"+uuidsToRemove[i]);
				}
				for (int i = 0; i < uuidsToRemove.length; i++) {
					actionRequest.removeParameter("selected_group_"+uuidsToRemove[i]);
				}
			}
		}	

	
		boolean doSendNotification = ((request.getParameter("action_send_notification") != null) ||
									(request.getParameter("action_send_notification.x") != null));
		
		if (doSendNotification) {
			
			String subject = request.getParameter ("send_subject");
			if (subject == null) subject = "";
			
			String message = request.getParameter ("send_message");
			if (message == null) message = "";

			
			SortedVector foundMembers = new SortedVector();

			
			String[] foundUuids = JSPHelper.getParameterEndings(request, "selected_user_");
			if (foundUuids != null) {
				for (int fu = 0; fu < foundUuids.length; fu++) {
					User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(foundUuids[fu]);
					if (!foundMembers.contains (user)) {
						foundMembers.addElement (user);
					}
				}
			}

			foundUuids = JSPHelper.getParameterEndings(request, "selected_group_");
			if (foundUuids != null) {
				for (int fu = 0; fu < foundUuids.length; fu++) {
					Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(foundUuids[fu]);
					if (!foundMembers.contains (group)) {
						foundMembers.addElement (group);
					}
				}
			}
			
			User sender = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user"));
			int count = 0;
			for (int fm = 0; fm < foundMembers.size(); fm++) {
				Member member = (Member)foundMembers.elementAt(fm);
				Vector devices = new Vector();
				String[] deviceUuids = request.getParameterValues("selected_userdevice_"+member.getUuid());
				if (deviceUuids != null) {
					for (int i = 0; i < deviceUuids.length; i++) {
						Device device = BrokerFactory.getDeviceBroker().getDeviceByUuid(deviceUuids[i]);
						devices.addElement(device);
					}
				}
				if (devices.size() == 0) devices = null;
				Notification sendNotification = new Notification (null, member, devices, 
						new UserSender(sendingUser), subject, message);
				actionRequest.addParameter("send_system_message", "sent notification "+sendNotification.getUuid());
				
				if (sender != null) {
					sendNotification.setSender(new UserSender(sender));
				}
				try {
					SendNotification.getInstance().doSend(sendNotification);
					count++;
				} catch (Exception e) {
					actionRequest.addParameter("send_system_message", e.getMessage());
				}
			}
			
			if (count == 1) {
				actionRequest.addParameter("send_system_message", "Sent message to "+foundMembers.elementAt(0));
			} else {
				actionRequest.addParameter("send_system_message", "Sent "+count+" messages");
			}
		}
		
		return actionRequest;
	}

}
