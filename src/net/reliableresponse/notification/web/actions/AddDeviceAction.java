/*
 * Created on Nov 11, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.util.Hashtable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.DeviceSetting;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddDeviceAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Add Device Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));
		if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR)) {
			user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getParameter("user"));
			if (user == null) {
				user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						(String) actionRequest.getSession().getAttribute("user"));
			}
		}		
		
		String[] classNames = JSPHelper.getParameterEndings(request, "action_device_add_");
		if (classNames.length > 0) {
			String className = classNames[0];
			Device device = null;
			
			try {
				device = (Device)Class.forName(className).newInstance();
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			
			DeviceSetting[] settings = device.getAvailableSettings();
			Hashtable settingsTable = new Hashtable();
			for (int i = 0; i < settings.length; i++) {
				String key = settings[i].getName();
				if (key != null) {
					String value = request.getParameter(key+"_devicesetting");
					BrokerFactory.getLoggingBroker().logDebug("Adding "+key+"="+value+" to "+device.getName());
					if (value != null) {
						settingsTable.put (key, value);
					}
				}
			}
			
			device.initialize(settingsTable);
			
			BrokerFactory.getLoggingBroker().logDebug("Action adding device "+device.getUuid());
			user.addDevice(device);
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(user);
			} catch (NotSupportedException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		
			// Handle the priorities
			for (int num = 1; num <= 3; num++) {
				Priority priority = new Priority(user);
				if (request.getParameter("prioritylist_"+num+"_oh") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OffHoursSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_onh") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OnHoursSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_inf") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_ooo") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OutOfOfficeSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_va") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.VacationSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_m") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InMeetingSchedule"));
				}
				if (request.getParameter("prioritylist_"+num+"_never") != null) {
					priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.DontUseSchedule"));
				}
				device.addPriority(user, priority, num);
			}
		}
		
		return actionRequest;
	}

}
