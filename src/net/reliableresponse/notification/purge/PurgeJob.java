/*
 * Created on Nov 2, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.purge;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.Stoppable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.ldap.LDAPLibrary;
import net.reliableresponse.notification.ldap.LDAPSetting;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PurgeJob implements StatefulJob, Stoppable {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.Stoppable#stop()
	 */
	public void stop() {
		// TODO Auto-generated method stub

	}

	public void doPurge(int purgePeriodInDays) {
		long beforeTime = purgePeriodInDays*(1000*60*60*24);
		beforeTime = System.currentTimeMillis()-beforeTime;
		Date date = new Date (beforeTime);
		BrokerFactory.getNotificationBroker().deleteNotificationsBefore(date);
		
		BrokerFactory.getUserMgmtBroker().purgeUsersBefore(date);
		BrokerFactory.getGroupMgmtBroker().purgeGroupsBefore(date);
	}
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jeContext) throws JobExecutionException {
		BrokerFactory.getLoggingBroker().logDebug("Running Purge Job");
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		int purgePeriod = config.getIntValue("purge.days", -1);
		if (purgePeriod < 0) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Notification purging turned off");
			return;
		}
		
		JobDataMap map = jeContext.getJobDetail().getJobDataMap();

		// Add the start time
		long startMillis = System.currentTimeMillis();
		Date startDate = new Date();
		Vector startTimes = (Vector) map.get("starttimes");
		if (startTimes == null) {
			startTimes = new Vector();
		}
		startTimes.addElement(startDate);
		map.put("starttimes", startTimes);

		doPurge(purgePeriod);

		
		long totalMillis = System.currentTimeMillis() - startMillis;

		// Add the run time
		Hashtable runTimes = (Hashtable) map.get("runtimes");
		if (runTimes == null) {
			runTimes = new Hashtable();
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"Purge job ran for " + totalMillis + " millis");
		runTimes.put(startDate, new Long(totalMillis));
		map.put("runtimes", runTimes);
		jeContext.getJobDetail().setJobDataMap(map);

	}

}
