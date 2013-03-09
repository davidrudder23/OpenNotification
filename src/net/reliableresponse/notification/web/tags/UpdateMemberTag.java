/*
 * Created on Oct 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class UpdateMemberTag extends TagSupport {
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();

		// Check to see if the "update user" or "update group" button was clicked	
		String uuid = JSPHelper.getUUIDFromAction(request, "action_update_user_");

		if (uuid != null) {
			User updateUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
			String firstName = request.getParameter ("usermgmt_firstname_"+uuid);
			String lastName = request.getParameter ("usermgmt_lastname_"+uuid);
			String email = request.getParameter ("usermgmt_email_"+uuid);
			updateUser.setFirstName (firstName);
			updateUser.setLastName (lastName);
			updateUser.addEmailAddress (email);
		}

		uuid = JSPHelper.getUUIDFromAction(request, "action_update_group_");
		if (uuid != null) {
			Group updateGroup = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);
			String name = request.getParameter ("groupmgmt_name_"+uuid);
			updateGroup.setGroupName (name);
		}
		
		return super.doStartTag();
	}
}
