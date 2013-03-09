/*
 * Created on May 1, 2004
 *
 * Copyright 2004 - David Rudder
 */

package net.reliableresponse.notification.broker.impl.clustered;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingGroupMgmtBroker;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 * 
 * This is a simple in-memory broker, mostly used for testing
 */
public class ClusteredGroupMgmtBroker extends CachingGroupMgmtBroker {

	public ClusteredGroupMgmtBroker(GroupMgmtBroker realBroker) {
		super (realBroker);
	}

	public void deleteGroup(Group group) throws NotSupportedException {
		super.deleteGroup(group);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}
	
	/**
	 * 
	 * @param member
	 *            The member to add to the group
	 * @param group
	 *            The group to add the member to
	 */
	public void addMemberToGroup(Member member, Group group) {
		super.addMemberToGroup(member, group);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void removeMemberFromGroup(int memberNum, Group group) {
		super.removeMemberFromGroup(memberNum, group);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void moveMemberDown(Group group, int memberNum) {
		super.moveMemberDown(group, memberNum);
		invalidateGroup(group.getUuid());
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void moveMemberUp(Group group, int memberNum) {
		super.moveMemberUp(group, memberNum);
		invalidateGroup(group.getUuid());
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void setEscalationTime(EscalationGroup group, int memberNum, int time) {
		super.setEscalationTime(group, memberNum, time);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void updateGroup(Group group) {
		super.updateGroup(group);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateGroup", group
				.getUuid());
	}

	public void invalidateGroup(String uuid) {
		for (int i = 0; i < groups.size(); i++) {
			Group group = (Group) groups.elementAt(i);
			if (group.getUuid().equals(uuid)) {
				groups.remove(i);
			}
			if (group.isInPermanentCache()) {
				getGroupByUuid(group.getUuid());
			}
		}

	}
}