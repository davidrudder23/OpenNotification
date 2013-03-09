/*
 * Created on Oct 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.util;

import java.util.Vector;

import net.reliableresponse.notification.Notification;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadNotificationHierarchyUtil {
	
	public static Vector sortNotifications(Notification[] notifications) {
		Vector existing = new Vector();
		for (int i = 0; i < notifications.length; i++) {
			existing.addElement(notifications[i]);
		};
		Vector sorted = getTopLevelNotifications(notifications);
		for (int i = 0; i < sorted.size(); i++) {
			existing.removeElement(sorted.elementAt(i));
		}
		
		while (existing.size()>0) {
			for (int e = 0; e < existing.size(); e++) {
				for (int s = 0; s < sorted.size(); s++) {
					if (((Notification)existing.elementAt(e)).getParentUuid().equals
							(((Notification)sorted.elementAt(s)).getUuid())) {
						sorted.insertElementAt(existing.elementAt(e), s+1);
						existing.removeElementAt(e);
						break;
					}
				}
			}
		}
		return sorted;
	}

	public static Vector getTopLevelNotifications(Notification[] existingNotifications) {
		Vector vector = new Vector();

		for (int notifNum = 0; notifNum < existingNotifications.length; notifNum++) {
			Notification notification = existingNotifications[notifNum];
			if (notification.getParentUuid() == null) {
				vector.addElement(notification);
			} else {
				String parent = notification.getParentUuid();
				boolean found = false;
				for (int compareNum = 0; compareNum < existingNotifications.length; compareNum++) {
					if (parent.equals ((existingNotifications[compareNum]).getUuid())) {
						found = true;
						break;
					}
				}
				
				if (!found) vector.addElement(notification); 
			}
		}
		return vector;
	}
	
	
}
