/*
 * Created on Oct 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.reliableresponse.notification.Stoppable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.JobsBroker;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class QuartzJobsBroker implements JobsBroker {
	
	Scheduler sched;
	
	public QuartzJobsBroker() {
	}
	
	public void initialize (InputStream in) {
		try {
			StdSchedulerFactory schedFact = new StdSchedulerFactory();
			schedFact.initialize(in);
			sched = schedFact.getScheduler();
			sched.start();
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.JobsBroker#addJob(java.lang.String, java.lang.String, java.lang.Class, java.lang.String)
	 */
	public void addJob(String name, String triggerName, Class clazz, String cron) {
		try {
			BrokerFactory.getLoggingBroker().logDebug("Adding job "+name);
			JobDetail jobDetail = new JobDetail(name,
					Scheduler.DEFAULT_GROUP, clazz);
			jobDetail.setRequestsRecovery(false);
			CronTrigger trigger = new CronTrigger(triggerName,
					Scheduler.DEFAULT_GROUP, cron);
			trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
			sched.scheduleJob(jobDetail, trigger);
			BrokerFactory.getLoggingBroker().logDebug(name+" added");
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} 

	}
	
	public void removeJob (String name) {
		BrokerFactory.getLoggingBroker().logDebug("Removing job "+name);
		try {
			sched.removeJobListener(name);
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.JobsBroker#getJobNames()
	 */
	public String[] getJobNames() {
		try {
			return sched.getJobNames(Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new String[0];
		}
	}
	
	public String[] getTriggerNames(String jobName) {
		try {
			return sched.getTriggerNames(jobName);
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new String[0];
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.JobsBroker#getStatus(java.lang.String, java.lang.String)
	 */
	public String[] getStatuses(String name) {
		String[] triggers = getTriggerNames(name);
		String[] statuses = new String[triggers.length];
		for (int i = 0; i < triggers.length; i++) {
			statuses[i] = getStatus(name, triggers[i]);
		}
		return statuses;
	}
	
	
	public String getStatus(String jobName, String triggerName) {
		String status = "UNKNOWN";
		try {
			Trigger trigger = sched.getTrigger(jobName, triggerName);
			int state = sched.getTriggerState(jobName, Scheduler.DEFAULT_GROUP);
			switch (state) {
			case Trigger.STATE_BLOCKED:
				status = "BLOCKED";
				break;
			case Trigger.STATE_COMPLETE:
				status = "COMPLETE";
				break;
			case Trigger.STATE_ERROR:
				status = "ERROR";
				break;
			case Trigger.STATE_NONE:
				status = "UNKNOWN";
				break;
			case Trigger.STATE_NORMAL:
				status = "NORMAL";
				break;
			case Trigger.STATE_PAUSED:
				status = "PAUSED";
				break;
			}
			
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return status;
	}
	
	public Date getNextRuntime (String jobName, String triggerName) {
		try {
			Trigger trigger = sched.getTrigger(jobName, triggerName);
			return trigger.getNextFireTime();
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new Date();
		}
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.JobsBroker#triggerJob(java.lang.String, java.lang.String)
	 */
	public void triggerJob(String name) {
		try {
			sched.triggerJob(name, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public void stopJob (String jobName) {
		try {
			ArrayList jobs = new ArrayList((List)sched.getCurrentlyExecutingJobs());
			for (int i = 0; i < jobs.size(); i++) {
				JobDetail detail = ((JobExecutionContext)jobs.get(i)).getJobDetail();
				BrokerFactory.getLoggingBroker().logDebug("Quartz Broker checking "+detail.getName());
				if (detail.getName().equals(jobName)) {
					Job job = ((JobExecutionContext)jobs.get(i)).getJobInstance();
					if (job instanceof Stoppable) {
						BrokerFactory.getLoggingBroker().logDebug("Quartz Broker stopping "+jobName);
						((Stoppable)job).stop();
					}	
				}				
			}
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}	
	public String[] getHistory(String jobName) {
		Vector history = new Vector();
		try {
			JobDataMap map = sched.getJobDetail(jobName,
					Scheduler.DEFAULT_GROUP).getJobDataMap();
			Vector startTimes = (Vector) map.get("starttimes");
			Hashtable runTimes = (Hashtable) map.get("runtimes");
			if (runTimes == null) runTimes = new Hashtable();
			BrokerFactory.getLoggingBroker().logDebug("startTimes = "+startTimes);
			BrokerFactory.getLoggingBroker().logDebug("runTimes = "+runTimes);
			if ((startTimes != null) && (runTimes != null)) {
				for (int s = 0; s < startTimes.size(); s++) {
					Date startTime = (Date) startTimes.elementAt(s);
					Long runTime = (Long) runTimes.get(startTime);
					if (runTime != null) {
						double seconds = ((double)runTime.longValue()) / 1000.0;
						history.addElement(jobName + " started at " + startTime
								+ " and ran for " + seconds + " seconds");
					} else {
						history.addElement(jobName + " started at " + startTime
								+ " and did not finish");
					}
				}
			} else {
				String[] excuses = new String[1];
				excuses[0] = "No history found for "+jobName;
				return excuses;
			}
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return (String[]) history.toArray(new String[0]);
	}
	
	
	
	public String[] getNamesOfCurrentlyRunningJobs() {
		try {
			ArrayList jobs = new ArrayList((List)sched.getCurrentlyExecutingJobs());
			Vector jobNames = new Vector();
			
			for (int i = 0; i < jobs.size(); i++) {
				jobNames.addElement(((JobExecutionContext)jobs.get(i)).getJobDetail().getName());
			}
			return (String[])jobNames.toArray(new String[0]);
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new String[0];
		}
	}
	
	public void shutdown() {
		try {
			sched.shutdown();
		} catch (SchedulerException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
}
