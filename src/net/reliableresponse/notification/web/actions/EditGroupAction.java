/*
 * Created on Nov 8, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.PriorityBroker;
import net.reliableresponse.notification.sender.NonResponseSender;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class EditGroupAction implements Action {

	private void notifyGroupMembers(Member recipient, String subject, String message) {
		if (recipient instanceof Group) {
			Group group = (Group) recipient;
			Member[] members = group.getMembers();
			for (int i = 0; i < members.length; i++) {
				notifyGroupMembers(members[i], subject,  message);
			}
		} else {
			User user = (User)recipient;
			
			BrokerFactory.getLoggingBroker().logDebug(
					"Telling " + user+" that his or her group has changed");
			Notification updateNotification = new Notification(null,
					user, new NonResponseSender(
							"Notification Update"), subject, message);
			updateNotification.setPersistent(false);
			try {
				SendNotification.getInstance().doSend(
						updateNotification);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request,
			ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Edit Group Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));

		String removeGroupUuid = JSPHelper.getUUIDFromAction(request,
				"action_remove_group_");

		BrokerFactory.getLoggingBroker().logDebug(
				"Remove group uuid = " + removeGroupUuid);
		if ((removeGroupUuid != null) && (removeGroupUuid.length() > 0)) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					removeGroupUuid);
			if ((group != null) && (group.isOwner(user, true))){
				// Notify the members that their group has been deleted
				notifyGroupMembers(group, "Your group \"" + group
						+ "\" has been deleted",
						"Your group \"" + group
						+ "\" has been deleted by " + user);
				try {
					BrokerFactory.getGroupMgmtBroker().deleteGroup(group);
				} catch (NotSupportedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			return actionRequest;
		}

		String addRecipientsUuid = JSPHelper.getUUIDFromAction(request,
				"action_add_recipients_");
		if ((addRecipientsUuid != null) && (addRecipientsUuid.length() > 0)) {
			actionRequest.setParameter("opened.add_new_recipients_"
					+ addRecipientsUuid, "true");
		}

		String saveUuid = JSPHelper.getUUIDFromAction(request,
				"action_group_save_");

		if ((saveUuid != null) && (saveUuid.length() > 0)) {
			// Save the edited user
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					saveUuid);
			if ((group != null) && (group.isOwner(user, true))){
				group.setAutocommit(false);
				String groupname = request
						.getParameter("groupname_" + saveUuid);

				if (groupname != null)
					group.setGroupName(groupname);

				String description = request.getParameter("description_"
						+ saveUuid);
				if (description != null)
					group.setDescription(description);

				// Check the escalation times
				if (group instanceof EscalationGroup) {
					EscalationGroup escGroup = (EscalationGroup) group;
					if (request.getParameter("loop_"+group.getUuid())!= null) {
						escGroup.setLoopCount(0);
					} else {
						escGroup.setLoopCount(1);
					}
					Member[] members = escGroup.getMembers();
					for (int i = 0; i < members.length; i++) {
						String timeString = null;

						timeString = request.getParameter("esctime_"
								+ group.getUuid() + "_" + i);

						if (timeString != null) {
							try {
								group.setAutocommit(true);
								escGroup.setEscalationTime(i, Integer
										.parseInt(timeString));
								group.setAutocommit(false);
							} catch (NumberFormatException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}
					}
				}
				BrokerFactory.getGroupMgmtBroker().updateGroup(group);
				group.setAutocommit(true);
				
				Member[] members = group.getMembers();
				Vector owners = new Vector();
				for (int m = 0; m < members.length; m++) {
					String paramName = "owner_"+group.getUuid()+"_"+m;
					if (!StringUtils.isEmpty(request.getParameter(paramName))) {
						owners.addElement(members[m].getUuid());
					}
				}
				for (int m = 0; m < members.length; m++) {
					if (owners.contains(members[m].getUuid())) {
						group.setOwner(m);
					} else {
						group.unsetOwner(m);
					}
				}
			}
		}

		String addUuid = JSPHelper.getUUIDFromAction(request, "add_selected_");

		if ((addUuid != null) && (addUuid.length() > 0)) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					addUuid);
			if ((group != null) && (group.isOwner(user, true))) {

				String[] userUuids = JSPHelper.getParameterEndings(request,
						"add_user_");
				String[] groupUuids = JSPHelper.getParameterEndings(request,
						"add_group_");

				for (int i = 0; i < userUuids.length; i++) {
					User newUser = BrokerFactory.getUserMgmtBroker()
							.getUserByUuid(userUuids[i]);
					if (newUser != null) {
						PriorityBroker prioBroker = BrokerFactory.getPriorityBroker();
						int priority = prioBroker.getPriorityOfGroup(user, group);
						if (priority == 0) {
							Member[] members = group.getMembers();
							if (members.length > 0) {
								for (int m = members.length - 1; m >= 0; m--) {
									if (members[m] instanceof User) {
										priority = prioBroker.getPriorityOfGroup((User)members[m], group);
									}
								}
							}
						}
						
						if (priority == 0) {
							priority = 3;
						}
							
						try {
							group.addMember(newUser, group.getMembers().length);
							notifyGroupMembers(newUser, "You have been added to group \"" + group
									+ "\"",
									"You have been added to group \"" + group
									+ "\" by " + user);
							prioBroker.setPriorityOfGroup(newUser, group, priority);
						} catch (InvalidGroupException e) {
							BrokerFactory.getLoggingBroker().logError(e);
							actionRequest.addParameter(
									"add_group_system_error", e.getMessage());
						}
					}
				}
				for (int i = 0; i < groupUuids.length; i++) {
					Group newGroup = BrokerFactory.getGroupMgmtBroker()
							.getGroupByUuid(groupUuids[i]);
					if (newGroup != null) {
						try {
							Member member = BrokerFactory.getGroupMgmtBroker()
									.getGroupByUuid(groupUuids[i]);
							group.addMember(member, group.getMembers().length);
						} catch (InvalidGroupException e) {
							BrokerFactory.getLoggingBroker().logError(e);
							actionRequest.addParameter(
									"add_group_system_error", e.getMessage());
						}
					}
				}
			}

		}

		String removeInfo = JSPHelper.getUUIDFromAction(request, "action_remove_selected_");
		String groupUuid = null;
		String memberNumString = null;
		int memberNum = -1;
		BrokerFactory.getLoggingBroker().logDebug("Remove info="+removeInfo);
		if ((removeInfo != null) && (removeInfo.length() > 7)) {
			groupUuid = removeInfo.substring(0, 7);
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					groupUuid);
			if (group != null) {
				memberNumString = removeInfo.substring(8, removeInfo.length());
				BrokerFactory.getLoggingBroker().logDebug(
						"Remove group=" + groupUuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"Remove mem num=" + memberNumString);
				if ((memberNumString != null) && (memberNumString.length() > 0)) {
					try {
						memberNum = Integer.parseInt(memberNumString);
					} catch (NumberFormatException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
			}

		}

		if ((groupUuid != null) && (groupUuid.length() > 0) && (memberNum >= 0)) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					groupUuid);
			if ((group != null) && ((group.isOwner(user, true)) || (user.equals(group.getMembers()[memberNum])))) {
				notifyGroupMembers(group.getMembers()[memberNum], "You have been removed from group \"" + group
						+ "\"",
						"You have been removed from group \"" + group
						+ "\" by " + user);
				group.removeMemberFromGroup(memberNum);
			}
		}

		String moveUpInfo = JSPHelper.getUUIDFromAction(request,
				"action_move_up_");
		groupUuid = null;
		memberNumString = null;
		memberNum = -1;
		BrokerFactory.getLoggingBroker().logDebug("Move Up info="+moveUpInfo);
		if ((moveUpInfo != null) && (moveUpInfo.length()>7)) {
			groupUuid = moveUpInfo.substring(0,7);
			memberNumString = moveUpInfo.substring(8,moveUpInfo.length());			
			BrokerFactory.getLoggingBroker().logDebug("Move Up group="+groupUuid);
			BrokerFactory.getLoggingBroker().logDebug("Move Up mem num="+memberNumString);
			if ((memberNumString != null) && (memberNumString.length()>0)) {
				try {
					memberNum = Integer.parseInt(memberNumString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		if ((groupUuid != null) && (groupUuid.length() > 0) && (memberNum>=0)) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					groupUuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"Moving user " + memberNum + " in group "+group+" up");
				if (memberNum > 0) {
					group.moveMemberUp(memberNum);
				}
		}

		String moveDownInfo = JSPHelper.getUUIDFromAction(request,
				"action_move_down_");
		groupUuid = null;
		memberNumString = null;
		memberNum = -1;
		BrokerFactory.getLoggingBroker().logDebug("Move Down info="+moveDownInfo);
		if ((moveDownInfo != null) && (moveDownInfo.length()>7)) {
			groupUuid = moveDownInfo.substring(0,7);
			memberNumString = moveDownInfo.substring(8,moveDownInfo.length());			
			BrokerFactory.getLoggingBroker().logDebug("Move Down group="+groupUuid);
			BrokerFactory.getLoggingBroker().logDebug("Move Down mem num="+memberNumString);
			if ((memberNumString != null) && (memberNumString.length()>0)) {
				try {
					memberNum = Integer.parseInt(memberNumString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		if ((groupUuid != null) && (groupUuid.length() > 0) && (memberNum>=0)) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					groupUuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"Moving user " + memberNum + " in group "+group+" down");
				if (memberNum < (group.getMembers().length-1)) {
					group.moveMemberDown(memberNum);
				}
		}

		// Check the priority
		String[] priorityUuids = JSPHelper.getParameterEndings(request,
				"priority_");

		BrokerFactory.getLoggingBroker().logDebug(
				"We have " + priorityUuids.length + " priority uuids");
		if (priorityUuids != null) {
			for (int i = 0; i < priorityUuids.length; i++) {
				String groupUuidString = priorityUuids[i];
				int indexOfUnderbar = groupUuidString.indexOf("_");
				if (indexOfUnderbar>=0) {
					groupUuidString = groupUuidString.substring (indexOfUnderbar+1, groupUuidString.length());
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"priorityUuids[" + i + "]=" + groupUuidString);
				Group group = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(groupUuidString);
				String priorityString = request.getParameter("priority_"
						+ priorityUuids[i]);
				BrokerFactory.getLoggingBroker().logDebug(
						"Priority string = " + priorityString);
				int priority = 3;
				try {
					priority = Integer.parseInt(priorityString);
					BrokerFactory.getPriorityBroker().setPriorityOfGroup(user,
							group, priority);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}

		return actionRequest;
	}

}