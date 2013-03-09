/*
 * Created on May 1, 2004
 *
 * Copyright 2004 - David Rudder
 */

package net.reliableresponse.notification.broker.impl.caching;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 * 
 * This is a simple in-memory broker, mostly used for testing
 */
public class CachingGroupMgmtBroker implements GroupMgmtBroker {

	protected Cache groups;

	Hashtable members;
	Hashtable parents;
	
	GroupMgmtBroker realBroker;
	
	public CachingGroupMgmtBroker(GroupMgmtBroker realBroker) {
		groups = new Cache(BrokerFactory.getConfigurationBroker().getIntValue("cache.maxobjects", 1200), 
				BrokerFactory.getConfigurationBroker().getIntValue("cache.maxseconds", 36000), 
				Cache.METHOD_FIFO);
		parents = new Hashtable();
		members = new Hashtable();
		
		this.realBroker = realBroker;
	}
	
	public Cache getCache() {
		return groups;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroups(int, int,
	 *      net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroups(int pageSize, int pageNum, Group[] groups) {
		String[] uuids = new String[groups.length];
		int size = getGroupUuids(pageSize, pageNum, uuids);

		if (this.groups.size() < 10) {
			Group[] newGroups = new Group[size];
			realBroker.getGroups(size, 0, newGroups);

			this.groups.removeAllElements();

			for (int i = 0; i < size; i++) {
				groups[i] = newGroups[i];
				this.groups.addElement(groups[i]);
			}
		} else {
			for (int i = 0; i < size; i++) {
				groups[i] = getGroupByUuid(uuids[i]);
			}
		}
		
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupsLike(int, int,
	 *      java.lang.String, net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroupsLike(int pageSize, int pageNum, String substring,
			Group[] groups) {
		String[] uuids = new String[groups.length];
		int size = getGroupsUuidsLike(pageSize, pageNum, substring, uuids);
		
		for (int i = 0; i < size; i++) {
			groups[i] = getGroupByUuid(uuids[i]);
		}
		
		return size;
	}
	
	public int getGroupsUuidsLike(int pageSize, int pageNum, String substring, 
			String[] uuids) {
		return realBroker.getGroupsUuidsLike(pageSize, pageNum, substring, uuids);
	}
	
	public int getGroupUuids(int pageSize, int pageNum, String[] uuids) {
		return realBroker.getGroupUuids(pageSize, pageNum, uuids);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupByUuid(java.lang.String)
	 */
	public Group getGroupByUuid(String uuid) {
		Group group = (Group) groups.getByUuid(uuid);
		if (group != null) return group;

		group = realBroker.getGroupByUuid(uuid);
		if (group != null) {
			groups.addElement(group);
		}
		return group;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupByName(java.lang.String)
	 */
	public Group getGroupByName(String name) {
		for (int i = 0; i < groups.size(); i++) {
			Group group = (Group) groups.elementAt(i);
			if (group.getGroupName().equals(name)) {
				return group;
			}
		}

		Group group = realBroker.getGroupByName(name);
		if (group != null) {
			groups.addElement(group);
		}
		return group;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupMembers(net.reliableresponse.notification.usermgmt.Group)
	 */
	public Member[] getGroupMembers(Group group) {
		group.loadMembers();
		Vector groupMembers = (Vector) members.get(group);
		if (groupMembers == null) {
			groupMembers = new Vector();
		}
		return (Member[]) groupMembers.toArray(new Member[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupsOfMember(net.reliableresponse.notification.usermgmt.Member)
	 */
	public Group[] getGroupsOfMember(Member member) {
		return realBroker.getGroupsOfMember(member);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addGroup(net.reliableresponse.notification.usermgmt.Group)
	 */
	public String addGroup(Group group) throws NotSupportedException {
		synchronized (groups) {
			groups.addElement(group);
			return realBroker.addGroup(group);
		}
	}
	
	

	public Group getGroupByEmail(String emailAddress) {
		// TODO Auto-generated method stub
		return realBroker.getGroupByEmail(emailAddress);
	}

	public void deleteGroup(Group group) throws NotSupportedException {
		synchronized (groups) {
			groups.removeElement(group);
			realBroker.deleteGroup(group);
		}
	}
	
	public int purgeGroupsBefore(Date before) {
		String[] deletedUuids = realBroker.getDeletedUuidsBefore(before);
		int numDeleted = realBroker.purgeGroupsBefore(before);
		if (numDeleted>0) {
			for (int i = 0; i < deletedUuids.length;i++) {
				Group group = (Group)groups.getByUuid(deletedUuids[i]);
				if (group != null) {
					groups.remove(group);
				}
			}
		}
		return numDeleted;
	}

	public String[] getDeletedUuidsBefore (Date before) {
		return realBroker.getDeletedUuidsBefore(before);
	}

	public void undeleteGroup (Group group) {
			realBroker.undeleteGroup(group);
	}
	
	/**
	 * Finds a deleted group, based on the name of the group
	 * @param groupName
	 * @return
	 */
	public Group getDeletedGroup (String groupName) {
		return realBroker.getDeletedGroup(groupName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addEscalationGroup(net.reliableresponse.notification.usermgmt.EscalationGroup)
	 */
	public String addEscalationGroup(EscalationGroup group)
			throws NotSupportedException {
		return addGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addNotificationGroup(net.reliableresponse.notification.usermgmt.NotificationGroup)
	 */
	public String addNotificationGroup(BroadcastGroup group)
			throws NotSupportedException {
		return addGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addRolloverGroup(net.reliableresponse.notification.usermgmt.RolloverGroup)
	 */
	public String addRolloverGroup(OnCallGroup group)
			throws NotSupportedException {
		return addGroup(group);
	}

	/**
	 * 
	 * @param member
	 *            The member to add to the group
	 * @param group
	 *            The group to add the member to
	 */
	public void addMemberToGroup(Member member, Group group) {
		group.loadMembers();
		Vector groupMembers = (Vector) members.get(group);
		if (groupMembers == null) {
			groupMembers = (Vector) members.get(group);
		}

		if (groupMembers == null) {
			groupMembers = new Vector();
		}

		groupMembers.addElement(member);
		members.put(group, groupMembers);

		Vector parentMembers = (Vector) parents.get(member);
		if (parentMembers == null) {
			parentMembers = new Vector();
		}

		parentMembers.addElement(group);
		parents.put(member, parentMembers);
		
		realBroker.addMemberToGroup(member, group);
	}
	
	public void removeMemberFromGroup (int memberNum, Group group) {
		group.loadMembers();

		Vector groupMembers = (Vector) members.get(group);
		Member member = null;
		if (groupMembers == null) {
			groupMembers = (Vector) members.get(group);
		}
		if (groupMembers != null) {
			BrokerFactory.getLoggingBroker().logDebug("We have "+groupMembers.size()+" member of "+group);
			for (int i = 0; i < groupMembers.size(); i++) {
				BrokerFactory.getLoggingBroker().logDebug(groupMembers.elementAt(i).toString());
			}
			member = (Member)groupMembers.elementAt(memberNum);
			BrokerFactory.getLoggingBroker().logDebug("removing member "+member);
			groupMembers.removeElementAt(memberNum);
			members.put(group, groupMembers);
		}
			
		if (member != null) {
			Vector parentMembers = (Vector) parents.get(member);
			if (parentMembers != null) {
				parentMembers.removeElement(group);
				parents.put(member, parentMembers);
			}
		}
		BrokerFactory.getLoggingBroker().logDebug("Removing membernum "+memberNum);
		realBroker.removeMemberFromGroup(memberNum, group);	
	}

	/**
	 * Use this to find out how many Groups there are in the system, total
	 * 
	 * @return The total number of Groups
	 */
	public int getNumGroups() {
		return realBroker.getNumGroups();
	}

	/**
	 * Use this to find out how many Groups there are in the system that match
	 * the substring
	 * 
	 * @return The total number of Groups that match the substring
	 */
	public int getNumGroupsLike(String substring) {
		return realBroker.getNumGroupsLike(substring);
	}
	
	public void moveMemberDown(Group group, int memberNum) {
		if (memberNum < (group.getMembers().length)-1) {
			Vector members = (Vector)this.members.get(group);
			Member member = (Member)members.elementAt(memberNum);
			realBroker.moveMemberDown(group, memberNum);
			members.removeElementAt(memberNum);
			members.insertElementAt(member, memberNum+1);
			group.reloadMembers();
		}
	}
	
	public void moveMemberUp(Group group, int memberNum) {
		if (memberNum > 0) {
			Vector members = (Vector) this.members.get(group);
			Member member = (Member) members.elementAt(memberNum);
			realBroker.moveMemberUp(group, memberNum);
			members.removeElementAt(memberNum);
			members.insertElementAt(member, memberNum - 1);
			group.reloadMembers();
		}
	}
	
	
	public void setEscalationTime(EscalationGroup group, int memberNum, int time) {
		realBroker.setEscalationTime(group, memberNum, time);
	}
	
	public void updateGroup(Group group) {
		groups.removeElement(group);
		groups.addElement(group);
		
		realBroker.updateGroup(group);
	}
	
	
	public OnCallSchedule getOnCallSchedule(OnCallGroup group, int memberNum) {
		return realBroker.getOnCallSchedule(group, memberNum);
	}
	
	/**
	 * Sets an oncallschedule for a member of an oncallgroup
	 * @param schedule the schedule to use
	 * @param group The group to add the schedule to
	 * @param memberNum the member set the schedule for
	 */
	public void setOnCallSchedule(OnCallSchedule schedule, OnCallGroup group, int memberNum) {
		realBroker.setOnCallSchedule(schedule, group, memberNum);
	}

	
	public String[] getUuidsInPermanentCache() {
		return realBroker.getUuidsInPermanentCache();
	}

	/**
	 * Adds an owner to the group.  Owners can only be users
	 * @param user
	 */
	public void addOwner (Group group, Member member){ 
		realBroker.addOwner(group, member);
	}
	
	/**
	 * Removes an owner from the group.  Owners can only be users
	 * @param user
	 */
	public void removeOwner (Group group, Member member) {
		realBroker.removeOwner(group, member);
	}
	
	public void loadMembers(Group group) {
		realBroker.loadMembers(group);
		//group.loadMembers();
		Member[] members = group.getMembers();
		Vector membersVec = new Vector();
		for (int i = 0; i < members.length; i++) {
			membersVec.addElement(members[i]);
		}
		this.members.put (group, membersVec);
	}
}
