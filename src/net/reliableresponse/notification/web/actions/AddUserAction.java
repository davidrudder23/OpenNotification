/*
 * Created on Nov 16, 2004
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
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddUserAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Add User Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		
		String addUserAction = request.getParameter("action_addnew_save.x");
		if (addUserAction != null) {
			User user = new User();
			String addFirstName = request.getParameter("addFirstName");
			String addLastName = request.getParameter("addLastName");
			String addDepartment = request.getParameter("addDepartment");
			String addLogin = request.getParameter("addLogin");
			String addStartHourString = request.getParameter("addStartHour");
			String addStartMinutesString = request.getParameter("addStartMinutes");
			String startAM = request.getParameter("addStartAMPM");
			String addEndHourString = request.getParameter("addEndHour");
			String addEndMinutesString = request.getParameter("addEndMinutes");
			String endAM = request.getParameter("addEndAMPM");
			
			String addPassword = request.getParameter("addPassword");
			String addConfirmPassword = request.getParameter("addConfirmPassword");
			
			if (StringUtils.isEmpty(addLogin)) {
				actionRequest.setParameter("add_user_system_error", "The login name can not be blank");
				return actionRequest;
			}
			
			// Check to see if there's a user with this name already
			User[] existing = BrokerFactory.getUserMgmtBroker().getUsersByName(addFirstName, addLastName);
			if ((existing != null) && (existing.length > 0)) {
				actionRequest.setParameter("add_user_system_error", "There is already a user named "+addFirstName+" "+addLastName);
				return actionRequest;
				
			}
			
			// Check to see if the user was previously deleted
			User existingUser = BrokerFactory.getUserMgmtBroker().getDeletedUser(addFirstName, addLastName);
			if (existingUser != null) {
				BrokerFactory.getUserMgmtBroker().undeleteUser(existingUser);
				User undeletedUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(existingUser.getUuid());
				if (undeletedUser != null) {
					undeletedUser.setDepartment(addDepartment);
					user.setInformation("Deleted By", "");
					try {
						BrokerFactory.getUserMgmtBroker().updateUser(undeletedUser);
					} catch (NotSupportedException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					}
					String loginName = BrokerFactory.getAuthenticationBroker().getIdentifierByUser(undeletedUser);
					if (!StringUtils.isEmpty(addLogin)) {
						BrokerFactory.getAuthenticationBroker().removeUser(undeletedUser);
						BrokerFactory.getAuthenticationBroker().addUser(addLogin, addPassword, undeletedUser);
						loginName = addLogin;
					}
					actionRequest.setParameter("add_user_system_error", existingUser+" was undeleted.  Login name is "+loginName);
					return actionRequest;
				} else {
					actionRequest.setParameter("add_user_system_error", existingUser+" was undeleted.");
					return actionRequest;
				}
			}
			if (BrokerFactory.getAuthenticationBroker().getUserByIdentifier(addLogin) != null) {
				actionRequest.setParameter("add_user_system_error", "There is already a user with the login "+addLogin);
				return actionRequest;
			}
			
			if (addPassword !=null) { 
				if (addConfirmPassword == null) {
					actionRequest.setParameter("add_user_system_error", "Your passwords do not match");
					return actionRequest;					
				}
				if (!addPassword.equals(addConfirmPassword)) {
					actionRequest.setParameter("add_user_system_error", "Your passwords do not match");
					return actionRequest;
				}
			}
			
			if (addFirstName != null)
				user.setFirstName(addFirstName);
			if (addLastName != null)
				user.setLastName(addLastName);
			if (addDepartment != null)
				user.setDepartment(addDepartment);
			
			if ((addStartHourString != null) && (addStartMinutesString != null) && (startAM != null)) {
				try {
					Calendar startDate = Calendar.getInstance();
					int addStartHour = Integer.parseInt(addStartHourString);
					if (startAM.toLowerCase().equals("pm")) {
						addStartHour += 12;
					}
					startDate.set(Calendar.HOUR_OF_DAY, addStartHour);
					
					startDate.set(Calendar.MINUTE, Integer.parseInt (addStartMinutesString));
					user.setStartTime(startDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			if ((addEndHourString != null) && (addEndMinutesString != null) && (endAM != null)) {
				try {
					Calendar endDate = Calendar.getInstance();
					int addEndHour = Integer.parseInt(addEndHourString);
					if (endAM.toLowerCase().equals("pm")) {
						addEndHour += 12;
					}
					endDate.set(Calendar.HOUR_OF_DAY, addEndHour);
					
					endDate.set(Calendar.MINUTE, Integer.parseInt (addEndMinutesString));
					user.setEndTime(endDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			try {
				BrokerFactory.getUserMgmtBroker().addUser(user);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				actionRequest.setParameter("add_user_system_error", e.getMessage());
				return actionRequest;
			}
			
			if (addPassword != null) {

				try {
					BrokerFactory.getAuthenticationBroker().addUser(addLogin, addPassword, user);
					actionRequest.addParameter("add_user_system_message", "The new user has been added to the system as "+addLogin);
				} catch (Exception anyExc) {
					BrokerFactory.getLoggingBroker().logDebug("Got exception "+anyExc);
					actionRequest.setParameter("add_user_system_error", anyExc.getMessage());
				}
			} 
		}
		
		return actionRequest;
	}

}
