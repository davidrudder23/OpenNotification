/*
 * Created on Nov 16, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AdministrationAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Personal Info Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));

		String addUserTitle = "Add A New User";
		String addUserMessage = request.getParameter("add_user_system_message");
		if ((addUserMessage != null) && (addUserMessage.length()>0)) {
			addUserTitle = addUserTitle+"<br>";
			addUserTitle = addUserTitle+addUserMessage;
		}
		addUserMessage = request.getParameter("add_user_system_error");
		if ((addUserMessage != null) && (addUserMessage.length()>0)) {
			addUserTitle = addUserTitle+"<br><span class=\"systemalert\">";
			addUserTitle = addUserTitle+addUserMessage+"</span>";
		}
		addUserTitle = addUserTitle+"</td>";
		actionRequest.setParameter("addUserTitle", addUserTitle);
		
		String editUserTitle = "Edit A User";
		String editUserMessage = request.getParameter("edit_user_system_message");
		if ((editUserMessage != null) && (editUserMessage.length()>0)) {
			editUserTitle = editUserTitle+"<br>";
			editUserTitle = editUserTitle+editUserMessage;
		}
		editUserTitle = editUserTitle+"</td>";
		actionRequest.setParameter("editUserTitle", editUserTitle);

		String editRoleTitle = "Edit Roles";
		String editRoleMessage = request.getParameter("edit_role_system_message");
		if ((editRoleMessage != null) && (editRoleMessage.length()>0)) {
			editRoleTitle = editRoleTitle+"<br>";
			editRoleTitle = editRoleTitle+editRoleMessage;
		}
		editRoleTitle = editRoleTitle+"</td>";
		actionRequest.setParameter("editRoleTitle", editRoleTitle);

		return actionRequest;
	}

}
