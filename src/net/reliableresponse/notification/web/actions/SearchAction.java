/*
 * Created on Oct 26, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.util.FindSearchMembers;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SearchAction implements Action {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		boolean doAction = false;

		String[] uuidArray = new String[0];
		// Process the search buttoencoden submission
		if (request.getParameter("action_search_recipients.x") != null) {
			FindSearchMembers fsm = new FindSearchMembers(actionRequest);
			Vector uuids = fsm.findSearchMembers();
			uuidArray = (String[]) uuids.toArray(new String[0]);
			// See if we added users through the lists
		} else {
			// If we don't have the action, slurp in the previous values
			uuidArray = request.getParameterValues("found_recipient");
			if (uuidArray == null)
				uuidArray = new String[0];
		}

		actionRequest.setParameter("recipient_search_uuids", uuidArray);

		// Add the found users
		SortedVector members = new SortedVector();

		for (int i = 0; i < uuidArray.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("uuid=" + uuidArray[i]);
			// TODO: This should be extracted into a utility class
			Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(
					uuidArray[i]);
			if (member == null) {
				member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
						uuidArray[i]);
			}
			if (member != null) {
				members.addElement(member, false);
			}
		}
		members.sort();

		for (int i = 0; i < members.size(); i++) {
			Member member = (Member) members.elementAt(i);
			String title = "<td><strong>" + StringUtils.htmlEscape(member.toString())
					+ "</strong></td>";
			title += "<td>";
			if (member instanceof Group) {
				int numMembers = ((Group) member).getMembers().length;
				if (numMembers == 1) {
					title += numMembers;
					title += " member";
				} else if (numMembers == 0) {
					title += "<font color=\"#990000\"> "+numMembers+" members</font>";
				} else {
					title += numMembers;
					title += " members";
				}
			}
			title += "</td><td width=\"40%\">";
			switch (member.getType()) {
			case Member.USER:
				title += "Individual";
				title += "</td><td width=\"10\"><input name=\"add_user_"
						+ member.getUuid()
						+ "\" type=\"checkbox\" id=\"add\" value=\"dynamic\" onchange=\"setCheckUsersDevices("+
						member.getUuid()+", this)\"></td>";
				break;
			case Member.ESCALATION:
				title += "Escalation Group";
				title += "</td><td width=\"10\"><input name=\"add_group_"
						+ member.getUuid()
						+ "\" type=\"checkbox\" id=\"add\" value=\"dynamic\"</td>";
				break;
			case Member.BROADCAST:
				title += "Broadcast Group";
				title += "</td><td width=\"10\"><input name=\"add_group_"
						+ member.getUuid()
						+ "\" type=\"checkbox\" id=\"add\" value=\"dynamic\"></td>";
				break;
			case Member.ONCALL:
				title += "On Call Group";
				title += "</td><td width=\"10\"><input name=\"add_group_"
						+ member.getUuid()
						+ "\" type=\"checkbox\" id=\"add\" value=\"dynamic\"></td>";
				break;
			default: 
				title += "Group";
				title += "</td><td width=\"10\"><input name=\"add_group_"
					+ member.getUuid()
					+ "\" type=\"checkbox\" id=\"add\" value=\"dynamic\"></td>";
			break;
			}

			BrokerFactory.getLoggingBroker().logDebug(
					"Adding recipient parameter recipient_search_found_"
							+ ((Member) members.elementAt(i)).getUuid() + "="
							+ title);
			actionRequest.setParameter("recipient_search_found_"
					+ ((Member) members.elementAt(i)).getUuid(), title);
		}

		// Limit to 100 members
		if (uuidArray.length > 100) {
			actionRequest
					.addParameter(
							"search_system_message",
							"Your search returned more than 100 records.  Please limit the scope of your search.");
		}

		// Look for added members
		if (request.getParameter("add_selected.x") != null) {
			Hashtable userDevices = new FindSearchMembers(actionRequest).getUserDeviceList();
			BrokerFactory.getLoggingBroker().logDebug("userDevice = "+userDevices);
			String[] userUuids = (String[]) userDevices.keySet().toArray(new String[0]);
			String[] groupUuids = JSPHelper.getParameterEndings(request,
					"add_group_");

			for (int i = 0; i < userUuids.length; i++) {
				User newUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						userUuids[i]);
				if (newUser != null) {
					actionRequest.setParameter("selected_user_" + userUuids[i],
							newUser.toString());
				}
				
				Vector devices = (Vector) userDevices.get(userUuids[i]);
				for (int d = 0; d < devices.size(); d++) {
					String deviceUuid = (String) devices.elementAt(d);
					actionRequest.addParameter("selected_userdevice_" + userUuids[i], deviceUuid);
				}
			}
			for (int i = 0; i < groupUuids.length; i++) {
				Group newGroup = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(groupUuids[i]);
				if (newGroup != null) {
					actionRequest.setParameter("selected_group_"
							+ groupUuids[i], newGroup.toString());
				}
			}
		} else if (request.getParameter("action_add_from_list.x") != null) {
			String[] userUuids = actionRequest
					.getParameterValues("add_users_from_list");
			String[] groupUuids = actionRequest
					.getParameterValues("add_groups_from_list");
			
			//String[] sendtoByDefault = BrokerFactory.getConfigurationBroker().getStringValues("sendto.bydefault");

			if (userUuids != null) {
				for (int i = 0; i < userUuids.length; i++) {
					User newUser = BrokerFactory.getUserMgmtBroker()
							.getUserByUuid(userUuids[i]);
					if (newUser != null) {
						actionRequest.setParameter("selected_user_"
								+ userUuids[i], newUser.toString());
						
						List<Device> devices = newUser.getDevices();
						for (Device device: devices) {
							actionRequest.addParameter("selected_userdevice_" + userUuids[i], device.getUuid());
						}
					}
				}
			}

			if (groupUuids != null) {
				for (int i = 0; i < groupUuids.length; i++) {
					Group newGroup = BrokerFactory.getGroupMgmtBroker()
							.getGroupByUuid(groupUuids[i]);
					if (newGroup != null) {
						actionRequest.setParameter("selected_group_"
								+ groupUuids[i], newGroup.toString());
					}
				}
			}
		}
		return actionRequest;
	}

}