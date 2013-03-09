/*
 * Created on Nov 8, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.web.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.PriorityBroker;
import net.reliableresponse.notification.sender.NonResponseSender;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class EditScheduleAction implements Action {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request,
			ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Edit Schedule Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));

		String saveScheduleIDs = JSPHelper.getUUIDFromAction(request,
				"action_save_schedule_");
		System.out.println ("saveScheduleIDs="+saveScheduleIDs);
		if (saveScheduleIDs == null) {
			return actionRequest;
		}
		String groupUuid = saveScheduleIDs.substring(0, saveScheduleIDs.indexOf("_"));
		String memberNumString = saveScheduleIDs.substring(saveScheduleIDs.indexOf("_")+1, saveScheduleIDs.length());

		String scheduleID = groupUuid+"_"+memberNumString;
		
		BrokerFactory.getLoggingBroker().logDebug(
				"Schedule group ids = " + groupUuid);
		BrokerFactory.getLoggingBroker().logDebug(
				"Schedule member uuid = " + memberNumString);
		
		if ((groupUuid != null) && (groupUuid.length() > 0) &&
			(memberNumString != null) && (memberNumString.length() > 0)) {
			int memberNum = -1;
			try {
				memberNum = Integer.parseInt(memberNumString);
			} catch (NumberFormatException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}

			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					groupUuid);
			if ((group != null) && (group.isOwner(user, true)) && 
				(group instanceof OnCallGroup) && (memberNum>=0)){
				// Create the schedule
				OnCallGroup onCallGroup = (OnCallGroup)group;
				OnCallSchedule schedule = new OnCallSchedule();
				
				// All Day checkbox
				String allDay = request.getParameter("allday_"+scheduleID);
				
				SimpleDateFormat format = new SimpleDateFormat("MMddyyyyhhmma");
				// Get the From date
				String fromDay = request.getParameter("fromDate_"+scheduleID);
				String fromMonth = request.getParameter("fromMonth_"+scheduleID);
				String fromYear = request.getParameter("fromYear_"+scheduleID);
				String fromHour = request.getParameter("fromHours_"+scheduleID);
				String fromMinutes = request.getParameter("fromMinutes_"+scheduleID);
				String fromAMPM = request.getParameter("fromAMPM_"+scheduleID);
				Date fromDate = null;
				try {
					fromDate = format.parse(fromMonth+fromDay+fromYear+fromHour+fromMinutes+fromAMPM);
				} catch (ParseException e) {
					BrokerFactory.getLoggingBroker().logError(e);
					return actionRequest;
				}
				BrokerFactory.getLoggingBroker().logDebug("Read from date: "+fromDate);
				
				// Get the To date
				String toDay = request.getParameter("toDate_"+scheduleID);
				String toMonth = request.getParameter("toMonth_"+scheduleID);
				String toYear = request.getParameter("toYear_"+scheduleID);
				String toHour = request.getParameter("toHours_"+scheduleID);
				String toMinutes = request.getParameter("toMinutes_"+scheduleID);
				String toAMPM = request.getParameter("toAMPM_"+scheduleID);
				Date toDate = null;
				try {
					toDate = format.parse(toMonth+toDay+toYear+toHour+toMinutes+toAMPM);
				} catch (ParseException e) {
					BrokerFactory.getLoggingBroker().logError(e);
					return actionRequest;
				}
				BrokerFactory.getLoggingBroker().logDebug("Read to date: "+toDate);
				
				// Handle the repeat
				String repeatString = request.getParameter("repeat_"+scheduleID);
				int repeat = OnCallSchedule.REPEAT_DAILY;
				if (repeatString != null) {
					try {
						repeat = Integer.parseInt(repeatString);
					} catch (NumberFormatException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						return actionRequest;
					}
				}
				
				String repeatCountString = request.getParameter("repcount_"+scheduleID);
				int repcount = 0;
				if (repeatCountString != null) {
					try {
						repcount = Integer.parseInt(repeatCountString);
					} catch (NumberFormatException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						return actionRequest;
					}
				}
				
				schedule.setFromDate(fromDate);
				schedule.setToDate(toDate);
				schedule.setAllDay(!StringUtils.isEmpty(allDay));
				schedule.setRepetition(repeat);
				schedule.setRepetitionCount(repcount);
				onCallGroup.getOnCallSchedules();
				onCallGroup.setOnCallSchedule(schedule, memberNum);
			}
		}
		return actionRequest;
	}

}
