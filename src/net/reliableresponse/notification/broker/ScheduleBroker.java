/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ScheduleBroker {
	
	public Schedule[] getSchedules();
	
	public Schedule getSchedule (String className);

	public void addSchedule (Schedule schedule);
	
	public Member[] getOnCallMembers (OnCallGroup group);
}
