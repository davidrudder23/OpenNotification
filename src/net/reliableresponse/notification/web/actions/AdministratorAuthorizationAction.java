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
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AdministratorAuthorizationAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Administrator's Authorization Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));
		if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR)) {
			actionRequest.setParameter("pending_notification_message", "You do not have permissions to access that page");
			actionRequest.setParameter("page", "/index.jsp");
			return actionRequest;
		}
		
		return actionRequest;
	}

}
