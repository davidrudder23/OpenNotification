/*
 * Created on Nov 5, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class GroupSettingsAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Group Settings Action running");
		
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user"));
		int numEscalationGroups = 0;
		int numBroadcastGroups = 0;
		int numOnCallGroups = 0;
		
		Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].getType() == Member.ESCALATION) numEscalationGroups++;
			if (groups[i].getType() == Member.BROADCAST) numBroadcastGroups++;
			if (groups[i].getType() == Member.ONCALL) numOnCallGroups++;
		}
		
		String groupSettingsTitle = "Group Settings</td>";
		groupSettingsTitle += "<td align=\"right\" class=\"headercell\"><font color=\"#666666\">";
		groupSettingsTitle += numBroadcastGroups+":Broadcast "+numEscalationGroups+":Escalation "+numOnCallGroups+":OnCall";
		groupSettingsTitle += "<img src=\"images/spacer.gif\" width=\"10\" height=\"1\"></font><a href=\"#\"></td>";

		String groupMessage = request.getParameter("add_group_system_error");
		if ((groupMessage != null) && (groupMessage.length()>0)) {
			groupSettingsTitle = groupSettingsTitle+"</tr><tr><td class=\"headercell\">";
			groupSettingsTitle = groupSettingsTitle+"<span class=\"systemalert\">"+groupMessage+"</span></td>";
		}
		
		actionRequest.addParameter("groupSettingsTitle", groupSettingsTitle);
		return actionRequest;
	}

}
