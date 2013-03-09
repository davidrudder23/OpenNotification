/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EscalationGroup extends Group {
	Vector escalationTimes;
	int loopCount;

	public EscalationGroup() {
		escalationTimes = new Vector();
	}
	
	public int getType() {
		return ESCALATION;
	}

	/**
     * Returns the number of times to loop.  <=0 for infinite
     * @return
     */
	
	public int getLoopCount() {
		if (getMembers().length < 1) {
			return 1;
		}
		return loopCount;
	}

    /**
     * Sets the number of times to loop.  <=0 for infinite
     * @return
     */
	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}

	/**
	 * Gets all the escalation times for all members of this group
	 * @return A list of all the escalation times
	 */
	
	public int[] getEscalationTimes() {
		int[] times = new int[members.size()];
		for (int i = 0; i < times.length; i++) {
			Integer time = (Integer)escalationTimes.elementAt(i);
			if (time == null) {
				times[i] = 15;
			} else {
				times[i] = time.intValue();
			}
		}
		return times;
	}
	
	public void addMember(Member member, int order)
	throws InvalidGroupException {
		if ((order < 0) || (order > escalationTimes.size())) {
			escalationTimes.addElement(new Integer(15));
		} else {
			escalationTimes.insertElementAt(new Integer(15), order);
		}
		super.addMember(member, order);
	}


	/**
	 * Sets the escalation time for a particular member
	 * @param member The member who's time to set 
	 * @param time The time
	 */
	public void setEscalationTime (int memberNum, int time) {
		
		if ((memberNum < 0) || (memberNum >= getMembers().length)) {
			return;
		}

		escalationTimes.setElementAt(new Integer(time), memberNum);

		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().setEscalationTime(this, memberNum, time);
		}
	}

	/**
	 * Gets the escalation time for a particular member
	 * @param member The member to look for
	 * @return The escalation time
	 */
	public int getEscalationTime (int memberNum) {
		if ((memberNum < 0) || (memberNum >= getMembers().length)) {
			return -1;
		}
		
		Integer time = (Integer)escalationTimes.elementAt(memberNum);
		if (time == null) {
			setEscalationTime(memberNum, 15);
			return 15;
		}
		return time.intValue();
	}
	
//	public void moveMemberUp(int memberNum) {
//		BrokerFactory.getLoggingBroker().logDebug("Moving member "+memberNum+" up");
//		super.moveMemberUp(memberNum);
//		if (memberNum < members.size()) {
//			int temp = getEscalationTime(memberNum);
//			setEscalationTime(memberNum, getEscalationTime(memberNum-1));
//			setEscalationTime(memberNum-1, temp);
//		}
//	}
//	
//	public void moveMemberDown(int memberNum) {
//		BrokerFactory.getLoggingBroker().logDebug("Moving member "+memberNum+" down");
//		super.moveMemberDown(memberNum);
//		if (memberNum < (members.size()-1)) {
//			int temp = getEscalationTime(memberNum);
//			setEscalationTime(memberNum, getEscalationTime(memberNum+1));
//			setEscalationTime(memberNum+1, temp);
//		}
//	}
	
	public void removeMemberFromGroup(int memberNum) {
		super.removeMemberFromGroup(memberNum);
		escalationTimes.removeElementAt(memberNum);
	}
}
