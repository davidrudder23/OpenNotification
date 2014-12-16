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

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PersonalInfoAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Personal Info Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));
		
		String actionString = request.getParameter("action_personal_save.x");
		if (actionString!= null) {
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String department = request.getParameter("department");
			String startHourString = request.getParameter("starthour");
			String startMinutesString = request.getParameter("startminutes");
			String startAM = request.getParameter("startampm");
			String endHourString = request.getParameter("endhour");
			String endMinutesString = request.getParameter("endminutes");
			String endAM = request.getParameter("endampm");
			boolean vacation = request.getParameter("vacation")!= null;
			String freebusyURL = request.getParameter("freebusyURL");
			int priority = 3;
			String priorityString = request.getParameter("priority");
			if (!StringUtils.isEmpty(priorityString)) {
				try {
					priority = Integer.parseInt(priorityString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			String password = request.getParameter("password");
			String confirmPassword = request.getParameter("confirm_password");
			if ((password !=null) && (password.length()>0)){ 
				if (confirmPassword == null) {
					actionRequest.setParameter("add_user_system_message", "Your passwords do not match");
					return actionRequest;					
				}
				if (!password.equals(confirmPassword)) {
					actionRequest.setParameter("add_user_system_message", "Your passwords do not match");
					return actionRequest;
				}
				
				BrokerFactory.getAuthenticationBroker().changePassword(user, password);
			}
	
			if (firstName != null)
				user.setFirstName(firstName);
			if (lastName != null)
				user.setLastName(lastName);
			if (department != null)
				user.setDepartment(department);
			user.setOnVacation(vacation);
			
			user.setInformation("priority", ""+priority);
			
			if ((startHourString != null) && (startMinutesString != null) && (startAM != null)) {
				try {
					Calendar startDate = Calendar.getInstance();
					int startHour = Integer.parseInt(startHourString);
					if (startAM.toLowerCase().equals("pm")) {
						startHour += 12;
					}
					startDate.set(Calendar.HOUR_OF_DAY, startHour);
					
					startDate.set(Calendar.MINUTE, Integer.parseInt (startMinutesString));
					user.setStartTime(startDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			if ((endHourString != null) && (endMinutesString != null) && (endAM != null)) {
				try {
					Calendar endDate = Calendar.getInstance();
					int endHour = Integer.parseInt(endHourString);
					if (endAM.toLowerCase().equals("pm")) {
						endHour += 12;
					}
					endDate.set(Calendar.HOUR_OF_DAY, endHour);
					
					endDate.set(Calendar.MINUTE, Integer.parseInt (endMinutesString));
					user.setEndTime(endDate.getTime());
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			
			user.setDeviceEscalation(request.getParameter("deviceEscalationPolicy"), 
					StringUtils.getInteger(request.getParameter("deviceEscalationTime"), 5)	);
			
			user.setInformation("freebusyURL", freebusyURL);
		}
		return actionRequest;
	}

}
