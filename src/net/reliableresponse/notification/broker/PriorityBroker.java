/*
 * Created on Nov 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface PriorityBroker {

	public void addPriority (User user, Device device, int priorityNumber, Priority priority);

	public void updatePriority (User user, Device device, int priorityNumber, Priority priority);
	
	public Priority getPriority (User user, Device device, int priority);
	
	public int getPriorityOfGroup (User user, Group group);

	public void setPriorityOfGroup (User user, Group group, int priority);
}
