/*
 * Created on Dec 5, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.actions;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

public class RoleAction implements Action {

	public ServletRequest doAction(ServletRequest request,
			ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Role Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));

		// Check to make sure the user is allowed to run this
		if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR)) {
			actionRequest.setParameter("pending_notification_message", "You do not have permissions to access that page");
			actionRequest.setParameter("page", "/index.jsp");
			return actionRequest;
		}
		
		// What role are we dealing with?
		String role = request.getParameter("role");
		BrokerFactory.getLoggingBroker().logDebug("role from request="+role);
		
		// What type of member in the left-hand box?
		String memberType = request.getParameter("member_type");
		if ((memberType == null) || (memberType.equals(""))) {
			memberType="users";
		}
		
		// Are we adding members?
		if (request.getParameter("add_members_to_role.x") != null) {
			if ((role == null) || (role.length() ==0)) {
				// The form should prevent this from happening
				actionRequest.setParameter("edit_role_system_message", "Please specify a role to edit");
				return actionRequest;
			}

			BrokerFactory.getLoggingBroker().logDebug("member type="+memberType);
			String[] memberUuids = request.getParameterValues(memberType+"_to_add");
			if (memberUuids != null) {
				for (int i = 0; i < memberUuids.length; i++) {
					BrokerFactory.getLoggingBroker().logDebug("Trying to add "+memberUuids[i]+" to "+role);
					Member memberToAdd = null;
					if (memberType.equalsIgnoreCase("users")) {
						memberToAdd = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuids[i]);
					} else {
						memberToAdd = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(memberUuids[i]);
					}
					if (memberToAdd != null) {
						BrokerFactory.getAuthorizationBroker().addUserToRole(memberToAdd, role);
					}
				}
			}
		}
		
		// Are we removing members?
		if (request.getParameter("remove_members_from_role.x") != null) {
			if ((role == null) || (role.length() ==0)) {
				// The form should prevent this from happening
				actionRequest.setParameter("edit_role_system_message", "Please specify a role to edit");
				return actionRequest;
			}

			int numMembers =BrokerFactory.getAuthorizationBroker().getMembersInRole(role).length; 
			BrokerFactory.getLoggingBroker().logDebug("num members in role = "+numMembers);
			String[] memberUuids = request.getParameterValues("members_in_role");
			BrokerFactory.getLoggingBroker().logDebug("num members to remove = "+memberUuids.length);
			if (memberUuids != null) {
				// Make sure we're not emptying out the role.
				if (numMembers <= memberUuids.length) {
					actionRequest.setParameter("edit_role_system_message", "You may not delete all members of the role");
					return actionRequest;
				}
				// Remove them
				for (int i = 0; i < memberUuids.length; i++) {
					BrokerFactory.getLoggingBroker().logDebug("Trying to remove "+memberUuids[i]+" from "+role);
					Member memberToRemove = null;
					memberToRemove = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuids[i]);
					if (memberToRemove == null){
						memberToRemove = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(memberUuids[i]);
					}
					if (memberToRemove != null) {
						BrokerFactory.getAuthorizationBroker().removeMemberFromRole(memberToRemove, role);
					}
				}
			}
		}
		
		String newRole = request.getParameter("new_role");
		if ((newRole != null) && (newRole.length()>0)) {
			BrokerFactory.getAuthorizationBroker().addUserToRole(user, newRole);
		}

		return actionRequest;
	}

}
