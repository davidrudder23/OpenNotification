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
import net.reliableresponse.notification.web.util.JSPHelper;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class JobsAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Jobs Action running");
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user")));
		
		String[] triggerJobNames = JSPHelper.getParameterEndings(actionRequest, "action_trigger_");
		for (int jobNum = 0; jobNum < triggerJobNames.length; jobNum++) {
			BrokerFactory.getLoggingBroker().logDebug("Triggering job "+triggerJobNames[jobNum]);
			BrokerFactory.getJobsBroker().triggerJob(triggerJobNames[jobNum]);
		}
		String action = request.getParameter("action");
		if (action == null) action = "";
		if (action.startsWith ("action_trigger_")) {
			String jobToTrigger = action.substring ("action_trigger_".length(), action.length());
			BrokerFactory.getJobsBroker().triggerJob (jobToTrigger);
		}

		
		String[] stopJobNames = JSPHelper.getParameterEndings(actionRequest, "action_stop_");
		for (int jobNum = 0; jobNum < stopJobNames.length; jobNum++) {
			BrokerFactory.getLoggingBroker().logDebug("Stopping job "+stopJobNames[jobNum]);
			BrokerFactory.getJobsBroker().stopJob(stopJobNames[jobNum]);
		}

		if (action.startsWith ("action_stop_")) {
			String jobToStop = action.substring ("action_stop_".length(), action.length());
			BrokerFactory.getJobsBroker().stopJob(jobToStop);
		}
				
		return actionRequest;
	}

}
