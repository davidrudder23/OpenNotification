/*
 * Created on Oct 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import java.io.InputStream;

/**
 * This Broker defines the methods for accessing the jobs
 * defined by the Quartz job scheduler.  Since jobs scheduling 
 * is a fairly proprietary process, this is pretty much tied
 * to the <a href="http://www.quartzscheduler.org/">Quartz system</a>
 * 
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface JobsBroker {

	public void initialize (InputStream in);
	
	public void addJob (String jobName, String triggerName, Class clazz, String cron);
	
	public void removeJob (String jobName);
	
	public String[] getJobNames();
	
	public String[] getTriggerNames(String jobName);
	
	public String[] getStatuses (String jobName);
	
	public String getStatus (String jobName, String triggerName);
	
	public void triggerJob (String jobName);
	
	public String[] getHistory (String jobName);
	
	public String[] getNamesOfCurrentlyRunningJobs();
	
	public void stopJob (String jobName);
	
	public void shutdown();
}
