/*
 * Created on Dec 1, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.util.Calendar;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class EditUserAction implements Action {
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Edit User Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		String actionString = request.getParameter("action");
		if ((actionString != null) && (actionString.startsWith("action_loginas_"))) {
			String foundLogin = actionString.substring("action_loginas_"
					.length());
			BrokerFactory.getLoggingBroker().logDebug(
					"foundlogin=" + foundLogin);
			if (foundLogin != null) {
				((HttpServletRequest) request).getSession().setAttribute(
						"user", foundLogin);
				actionRequest.setParameter("page", "/index.jsp");
				return actionRequest;
			}
		}
		
		actionString = request.getParameter("action_edituser_save.x");
		if (actionString!= null) {
			String userUuid = request.getParameter("edit_user");
			User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid));

			String editFirstName = request.getParameter("editFirstName");
			String editLastName = request.getParameter("editLastName");
			String editEndDepartment = request.getParameter("editEndDepartment");
			String editEmail = request.getParameter("editEmail");
			String editStartHourString = request.getParameter("editStartHour");
			String editStartMinutesString = request.getParameter("editStartMinutes");
			String startAM = request.getParameter("editStartAMPM");
			String editEndHourString = request.getParameter("editEndHour");
			String editEndMinutesString = request.getParameter("editEndMinutes");
			String editEndAM = request.getParameter("editEndAMPM");
			
			String editAdministratorString = request.getParameter("editAdministrator");
			boolean editAdministrator = false;
			if ((editAdministratorString != null) && (editAdministratorString.equalsIgnoreCase("on"))) {
				editAdministrator = true;
			}
			
			String editObserverString = request.getParameter("editObserver");
			boolean editObserver = false;
			if ((editObserverString != null) && (editObserverString.equalsIgnoreCase("on"))) {
				editObserver = true;
			}
			
			String editCachedString = request.getParameter("editCached");
			boolean editCached = false;
			if ((editCachedString != null) && (editCachedString.equalsIgnoreCase("on"))) {
				editCached = true;
			}

			String password = request.getParameter("editPassword");
			String confirmPassword = request.getParameter("editConfirmPassword");
			if ((password !=null) && (password.length()>0)){ 
				if (confirmPassword == null) {
					actionRequest.setParameter("edit_user_system_message", "Your passwords do not match");
					return actionRequest;					
				}
				if (!password.equals(confirmPassword)) {
					actionRequest.setParameter("edit_user_system_message", "Your passwords do not match");
					return actionRequest;
				}
				
				BrokerFactory.getAuthenticationBroker().changePassword(user, password);
			}
	
			if (editFirstName != null)
				user.setFirstName(editFirstName);
			if (editLastName != null)
				user.setLastName(editLastName);
			if (editEndDepartment != null)
				user.setDepartment(editEndDepartment);
			
			if (editEmail != null)
				user.setEmailAddress(editEmail);

			if ((editStartHourString != null) && (editStartMinutesString != null) && (startAM != null)) {
				try {
					Calendar startDate = Calendar.getInstance();
					int editStartHour = Integer.parseInt(editStartHourString);
					if (startAM.toLowerCase().equals("pm")) {
						editStartHour += 12;
					}
					startDate.set(Calendar.HOUR_OF_DAY, editStartHour);
					
					startDate.set(Calendar.MINUTE, Integer.parseInt (editStartMinutesString));
					user.setStartTime(startDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			BrokerFactory.getLoggingBroker().logDebug("editEndHourString = "+editEndHourString);
			BrokerFactory.getLoggingBroker().logDebug("editEndMinutesString = "+editEndMinutesString);
			BrokerFactory.getLoggingBroker().logDebug("EndAM = "+editEndAM);
			if ((editEndHourString != null) && (editEndMinutesString != null) && (editEndAM != null)) {
				try {
					Calendar endDate = Calendar.getInstance();
					int editEndHour = Integer.parseInt(editEndHourString);
					if (editEndAM.toLowerCase().equals("pm")) {
						editEndHour += 12;
					}
					endDate.set(Calendar.HOUR_OF_DAY, editEndHour);
					
					endDate.set(Calendar.MINUTE, Integer.parseInt (editEndMinutesString));
					user.setEndTime(endDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			BrokerFactory.getLoggingBroker().logDebug("User is admin? "+BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR));
			if ((editAdministrator) && (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR))) {
				BrokerFactory.getAuthorizationBroker().addUserToRole(user, Roles.ADMINISTRATOR);
			}
			
			if ((!editAdministrator )&& (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR))) {
				BrokerFactory.getAuthorizationBroker().removeMemberFromRole(user, Roles.ADMINISTRATOR);
			}
			
			if ((editObserver) && (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.OBSERVER))) {
				BrokerFactory.getAuthorizationBroker().addUserToRole(user, Roles.OBSERVER);
			}
			
			if ((!editObserver )&& (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.OBSERVER))) {
				BrokerFactory.getAuthorizationBroker().removeMemberFromRole(user, Roles.OBSERVER);
			}

			BrokerFactory.getLoggingBroker().logDebug("Setting user in cache = "+editCached);
			user.setInPermanentCache(editCached);
		}

		actionString = request.getParameter("action_edituser_remove.x");
		if (actionString!= null) {
			String userUuid = request.getParameter("edit_user");
			User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid));
			try {
				if (user != null) {
					user.setInformation("Deleted By", "Web");
					BrokerFactory.getUserMgmtBroker().deleteUser(user);
					actionRequest.setParameter("edit_user_system_message", user+" deleted");
					return actionRequest;					
				}
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		return actionRequest;
	}
}