/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import java.util.Date;

import net.reliableresponse.notification.NotSupportedException;
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
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface GroupMgmtBroker {

	/**
	 * Gets all groups, but restricts the search space to a limited size. Since
	 * this is an enterprise app, and can have hundreds of thousands of groups,
	 * we do *not* support getting all groups in a single call.
	 * 
	 * @param pageSize
	 *            The number of groups to get in a single gulp
	 * @param pageNum
	 *            The notification you're looking for (starting from 0)
	 * @param groups
	 *            An array in which to place the groups
	 * @return The number of groups returned (may be less than the available
	 *         slots)
	 */
	public int getGroups(int pageSize, int pageNum, Group[] groups);
	
	/**
	 * Gets all groups' uuids, but restricts the search space to a limited size. Since
	 * this is an enterprise app, and can have hundreds of thousands of groups,
	 * we do *not* support getting all groups in a single call.
	 * 
	 * @param pageSize
	 *            The number of groups to get in a single gulp
	 * @param pageNum
	 *            The notification you're looking for (starting from 0)
	 * @param uuids
	 *            An array in which to place the groups' uuids
	 * @return The number of groups returned (may be less than the available
	 *         slots)
	 */
	public int getGroupUuids(int pageSize, int pageNum, String[] uuids);

	/**
	 * Does a basic substring match on the list of groups
	 * 
	 * @param pageSize
	 *            The number of groups to get in a single gulp
	 * @param pageNum
	 *            The notification you're looking for (starting from 0)
	 * @param substring
	 *            The substring to look for
	 * @param groups
	 *            An array in which to place the groups
	 * @return The number of groups returned (may be less than the available
	 *         slots)
	 */
	public int getGroupsLike(int pageSize, int pageNum, String substring,
			Group[] groups);

	/**
	 * Does a basic substring match on the list of groups, returning their uuids
	 * 
	 * @param pageSize
	 *            The number of groups to get in a single gulp
	 * @param pageNum
	 *            The notification you're looking for (starting from 0)
	 * @param substring
	 *            The substring to look for
	 * @param groups
	 *            An array in which to place the groups
	 * @return The number of groups returned (may be less than the available
	 *         slots)
	 */
	public int getGroupsUuidsLike(int pageSize, int pageNum, String substring,
			String[] uuids);
	
	/**
	 * Gets a group based on it's uuid.
	 * 
	 * @param uuid
	 *            The uuid of the group to look for
	 * @return The group
	 */
	public Group getGroupByUuid(String uuid);

	/**
	 * Since group names have to be unique, this is a deterministic retrieval
	 * method
	 * 
	 * @param name
	 *            The name of the group
	 * @return The group
	 */
	public Group getGroupByName(String name);
	
	/**
	 * Gets the members of the given group
	 * @param group The group to get the members of
	 * @return The members of that group
	 */
	public Member[] getGroupMembers(Group group);

	/**
	 * Gets the groups a member is a member of.
	 * 
	 * @param member
	 *            The member to inspect
	 * @return What groups this member is a member of
	 */
	public Group[] getGroupsOfMember(Member member);
	
	/**
	 * Gets the group that has the corresponding email address
	 * @param emailAddress
	 * @return The group that uses the supplied email address
	 */
	public Group getGroupByEmail (String emailAddress);

	/**
	 * Adds a group.
	 * 
	 * @param group
	 * @return The uuid of the group
	 * 
	 * @throws NotSupportedException
	 *             If the underlying data store does not support adding a group
	 *             (like read-only ldap).
	 */
	public String addGroup(Group group) throws NotSupportedException;
	
	/**
	 * Finds a deleted group, based on the name of the group
	 * @param groupName
	 * @return
	 */
	public Group getDeletedGroup (String groupName);

	/**
	 * 
	 * Undeletes a group.  All membership sshould remain, unless
	 * explicitly changed in upstream undelete function
	 * 
	 */
	public void undeleteGroup(Group group);
	
	/**
	 * Deletes a group
	 * 
	 * @param group
	 *            The group to delete
	 * @throws NotSupportedException
	 */
	public void deleteGroup(Group group) throws NotSupportedException;

	/**
	 * Purges groups from the database.  This is different from a delete, because
	 * the data is actually gone from the db.
	 * 
	 * @param before
	 * @return
	 */
	public int purgeGroupsBefore (Date before);

	/**
	 * Gets all the uuids of the groups who were deleted before the
	 * specified date
	 * 
	 * @param before
	 * @return
	 */
	public String[] getDeletedUuidsBefore(Date before);

	/**
	 * Updates only the group information, nothing about the membership
	 * Right now, that means only the group name
	 * @param group The group to update
	 */
	public void updateGroup(Group group);

	/**
	 * Use this to find out how many Groups there are in the system, total
	 * 
	 * @return The total number of Groups
	 */
	public int getNumGroups();

	/**
	 * Use this to find out how many Groups there are in the system that match
	 * the substring
	 * 
	 * @param substring The string to search on
	 * @return The total number of Groups that match the substring
	 */
	public int getNumGroupsLike(String substring);

	/**
	 * 
	 * @param member
	 *            The member to add to the group
	 * @param group
	 *            The group to add the member to
	 */
	public void addMemberToGroup(Member member, Group group);

	/**
	 * Removes a member from a group
	 * 
	 * @param member
	 *            The member to remove
	 * @param group
	 *            The group to remove the member from
	 */
	public void removeMemberFromGroup(int memberNum, Group group);

	/**
	 * Adds a group.
	 * 
	 * @param group
	 * @return The uuid of the group
	 * 
	 * @throws NotSupportedException
	 *             If the underlying data store does not support adding a group
	 *             (like read-only ldap).
	 */
	public String addEscalationGroup(EscalationGroup group)
			throws NotSupportedException;

	/**
	 * Adds a group.
	 * 
	 * @param group
	 * @return The uuid of the group
	 * 
	 * @throws NotSupportedException
	 *             If the underlying data store does not support adding a group
	 *             (like read-only ldap).
	 */
	public String addNotificationGroup(BroadcastGroup group)
			throws NotSupportedException;

	/**
	 * Adds a group.
	 * 
	 * @param group
	 * @return The uuid of the group
	 * 
	 * @throws NotSupportedException
	 *             If the underlying data store does not support adding a group
	 *             (like read-only ldap).
	 */
	public String addRolloverGroup(OnCallGroup group)
			throws NotSupportedException;

	/**
	 * Use this to move a particular member up in the order of the group.  
	 * This is most useful for the escalation groups, where the order 
	 * determines the order of escalation. 
	 * 
	 * @param member The member to move
	 * @param group The group who's order to set
	 */
	public void moveMemberUp(Group group, int memberNum);

	/**
	 * Use this to move a particular member down in the order of the group.  
	 * This is most useful for the escalation groups, where the order 
	 * determines the order of escalation. 
	 * 
	 * @param member The member to move
	 * @param group The group who's order to set
	 */
	public void moveMemberDown(Group group, int memberNum);
	
	/**
	 * Sets the escalation time for a member of an escalation group
	 * 
	 * @param group The escalation group
	 * @param member The member of the group
	 * @param time The amount of time between escalations
	 */
	public void setEscalationTime (EscalationGroup group, int memberNumber, int time);
	
	/**
	 * Loads the oncall schedules for the specified oncall group
	 * @param group The group to load the schedules for
	 * @param memberNum The member of the group.  We use a num because a member can be in twice
	 * @return The schedules
	 */
	public OnCallSchedule getOnCallSchedule(OnCallGroup group, int memberNum);
	
	/**
	 * Sets an oncallschedule for a member of an oncallgroup
	 * @param schedule the schedule to use
	 * @param group The group to add the schedule to
	 * @param memberNum the member set the schedule for
	 */
	public void setOnCallSchedule(OnCallSchedule schedule, OnCallGroup group, int memberNum);
	
	/**
	 * Adds an owner to the group.  Owners can only be users
	 * @param user
	 */
	public void addOwner (Group group, Member member);
	
	/**
	 * Removes an owner from the group.  Owners can only be users
	 * @param user
	 */
	public void removeOwner (Group group, Member member);

	/**
	 * This method is used to identify all the groups that are in the permanent
	 * cache.  The purpose of the permanent cache is so that notifications
	 * can be sent even if the database is down.
	 * @return
	 */
	public String[] getUuidsInPermanentCache();
		
	/**
	 * Loads the members of the group.  The group won't load these until some method
	 * is called that needs the members, to speed up searches and initial loads
	 * @param group
	 */
	public void loadMembers(Group group);
}