/*
 * Created on Nov 10, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class AddGroupAction implements Action {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request,
			ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Add Group Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));

		if (request.getParameter("add_group_action.x") != null) {
			String[] addUuids = JSPHelper.getParameterEndings(request,
					"add_group_");
			if (addUuids != null) {
				for (int i = 0; i < addUuids.length; i++) {
					Group group = BrokerFactory.getGroupMgmtBroker()
							.getGroupByUuid(addUuids[i]);
					if (group != null) {
						try {
							group.addMember(user, group.getMembers().length);
						} catch (InvalidGroupException e) {
							BrokerFactory.getLoggingBroker().logError(e);
							actionRequest.setParameter(
									"add_group_system_error", e.getMessage());
							return actionRequest;
						}
					}
				}
			}
		}

		if (request.getParameter("action_add_from_list.x") != null) {
			String[] groupUuids = request
					.getParameterValues("add_groups_from_list");
			for (int i = 0; i < groupUuids.length; i++) {
				Group group = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(groupUuids[i]);
				if (group != null) {
					try {
						group.addMember(user, group.getMembers().length);
					} catch (InvalidGroupException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						actionRequest.setParameter("add_group_system_error", e
								.getMessage());
						return actionRequest;
					}

				}
			}
		}

		if (request.getParameter("action_group_save.x") != null) {
			String groupName = request.getParameter("group_name");
			Group existing = BrokerFactory.getGroupMgmtBroker().getGroupByName(
					groupName);
			if (existing != null) {
				if ((existing.isOwner(user, true)) || (existing.getMembers().length<1)) {
				try {
					existing.addMember(user, existing.getMembers().length);
					BrokerFactory.getGroupMgmtBroker().addMemberToGroup(user,
							existing);
					actionRequest.setParameter("add_group_system_error",
							"There is already a group named " + groupName
									+ ".  You have been added to the group.");
				} catch (InvalidGroupException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
				}

			} else {
				existing = BrokerFactory.getGroupMgmtBroker().getDeletedGroup(
						groupName);
				if (existing != null) {
					BrokerFactory.getGroupMgmtBroker().undeleteGroup(existing);
					existing = BrokerFactory.getGroupMgmtBroker()
							.getGroupByUuid(existing.getUuid());
					if (!existing.isMember(user)) {
						try {
							existing.addMember(user, -1);
						} catch (InvalidGroupException e) {
							BrokerFactory.getLoggingBroker().logError(e);
						}
						actionRequest
								.setParameter(
										"add_group_system_error",
										groupName
												+ " was undeleted.  You have been added to the group.");
					} else {
						actionRequest.setParameter("add_group_system_error",
								groupName + " was undeleted.");
					}
				} else {
					String description = request
							.getParameter("group_description");
					String type = request.getParameter("group_type");

					Group group = null;

					if (type == null)
						type = "escalation";
					type = type.toLowerCase();

					if (type.equals("escalation")) {
						group = new EscalationGroup();
					} else if (type.equals("oncall")) {
						group = new OnCallGroup();
					} else {
						group = new BroadcastGroup();
					}
					group.setAutocommit(false);

					group.setGroupName(groupName);
					group.setDescription(description);
					try {
						BrokerFactory.getGroupMgmtBroker().addGroup(group);
					} catch (NotSupportedException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}

					try {
						group.addMember(user, 1);
						BrokerFactory.getGroupMgmtBroker().addMemberToGroup(
								user, group);
					} catch (InvalidGroupException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						actionRequest.setParameter("add_group_system_error", e
								.getMessage());
						return actionRequest;
					}
					group.setAutocommit(true);
					group.setOwner(0);
				}
			}
		}
		return actionRequest;
	}

}
