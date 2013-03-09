/*
 * Created on Nov 9, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
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
public class EditDeviceAction implements Action {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Edit Device Action running");

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

		
		String deviceUuid = JSPHelper.getUUIDFromAction(actionRequest,
				"action_device_edit_");
		if (deviceUuid != null) {
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = (String) paramNames.nextElement();
				if (paramName.endsWith("_devicesetting_" + deviceUuid)) {
					String key = paramName.substring(0, paramName.length()
							- (deviceUuid.length() + 15));
					String value = request.getParameter(paramName);

					Device device = user.getDeviceWithUuid(deviceUuid);
					Hashtable settings = device.getSettings();
					settings.put(key, value);
					device.initialize(settings);
					
					for (int num = 1; num <= 3; num++) {
						Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, device, num);

						if (priority == null) priority = new Priority(user);
						// 1st, remove all the old schedules
						Schedule[] schedules = priority.getSchedules();
						for (int s = 0; s < schedules.length; s++) {
							priority.removeSchedule(schedules[s]);
						}

						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_oh") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OffHoursSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_onh") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OnHoursSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_inf") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_ooo") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.OutOfOfficeSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_va") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.VacationSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_m") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InMeetingSchedule"));
						}
						if (request.getParameter("prioritylist_"+num+"_"+device.getUuid()+"_never") != null) {
							priority.addSchedule(BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.DontUseSchedule"));
						}
						Priority existingPriority = BrokerFactory
								.getPriorityBroker().getPriority(user, device,
										num);
						if (existingPriority == null) {
							BrokerFactory.getPriorityBroker().addPriority(user,
									device, num, priority);
						} else {
							BrokerFactory.getPriorityBroker().updatePriority(
									user, device, num, priority);
						}
					}
					
					try {
						BrokerFactory.getUserMgmtBroker().updateUser(user);
					} catch (NotSupportedException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
			}
		}

		deviceUuid = JSPHelper.getUUIDFromAction(actionRequest,
		"action_device_remove_");
		if (deviceUuid != null) {
			Device device = BrokerFactory.getDeviceBroker().getDeviceByUuid(deviceUuid);
			BrokerFactory.getLoggingBroker().logDebug("Deleting device "+device);
			if (device != null) {
				user.setAutocommit(true);
				user.removeDevice(device);
			}
		}
		return actionRequest;
	}

}