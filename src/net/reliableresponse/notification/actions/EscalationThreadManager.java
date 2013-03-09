/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.actions;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Member;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EscalationThreadManager {

	private static EscalationThreadManager manager = null;

	private Hashtable threads;

	private EscalationThreadManager() {
		threads = new Hashtable();
	}

	public static EscalationThreadManager getInstance() {
		if (manager == null) {
			manager = new EscalationThreadManager();
		}

		return manager;
	}

	/**
	 * 
	 * @param group
	 * @param notification
	 * @return An id to reference this escalation and confirm with
	 */
	public String addEscalation(Notification notification) {
		BrokerFactory.getLoggingBroker().logAction(
				"Starting escalation thread for notification " + notification.getUuid()+" - To: "+notification.getRecipient()+", Re: "+notification.getSubject());
		EscalationGroup group = (EscalationGroup) notification.getRecipient();
		EscalationThread escThread = new EscalationThread(group, notification);

		String id = notification.getUltimateParent().getUuid();
		BrokerFactory.getLoggingBroker().logAction(
				"Created new escalation thread;  " + escThread);
		threads.put(id, escThread);

		escThread.start();

		return id;
	}

	public EscalationThread getEscalationThread(String id) {
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(id);
		EscalationThread escThread = (EscalationThread)threads.get(id);
		while (escThread == null) {
			if (notification == null) {
				return null;
			}
			String parentID = notification.getParentUuid();
			if (parentID == null) {
				return null;
			}
			escThread = (EscalationThread)threads.get(parentID);
			notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(parentID);
		}
		return escThread;
	}

	public String[] getEscalationThreadIDsForMember(Member member) {
		Vector memberThreads = new Vector();
		Enumeration keys = threads.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key != null) {
				EscalationThread thread = (EscalationThread) threads.get(key);
				if (thread != null) {
					if (thread.getGroup().isMember(member)) {
						memberThreads.addElement(key);
					}
				}
			}
		}

		return (String[]) memberThreads.toArray(new String[0]);
	}
	
	public String getEscalationThreadIDForThread(EscalationThread thread) {
		Enumeration keys = threads.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			EscalationThread testThread = (EscalationThread)threads.get(key);
			if (testThread.equals (thread)) {
				return key;
			}
		}
		return null;
	}

	public String[] getEscalationThreadIDs() {
		Vector returnThreads = new Vector();
		Enumeration keys = threads.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key != null) {
				returnThreads.addElement(key);
			}
		}

		return (String[]) returnThreads.toArray(new String[0]);
	}
}
