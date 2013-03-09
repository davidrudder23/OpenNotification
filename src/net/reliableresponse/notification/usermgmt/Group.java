/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.EmailUtil;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Group implements Member {

	int type;

	String uuid;

	String groupName;

	Vector members;
	
	Vector owners;

	String category;

	String description;

	boolean membersLoaded = false;

	boolean deleted = false;
	
	boolean inPermanentCache;
	
	String email = null;

	/**
	 * This is used to tell the user whether to autocommit itself or not. This
	 * is most useful for the Brokers, so we can avoid infinite loops
	 *  
	 */
	boolean autocommit;

	public Group() {
		type = ESCALATION;
		members = new Vector();
		owners = new Vector();
	}

	public boolean isEscalation() {
		return getType() == ESCALATION;
	}

	public boolean isRollover() {
		return getType() == ONCALL;
	}

	public boolean isNotification() {
		return getType() == BROADCAST;
	}

	/**
	 * @return
	 */
	public String getGroupName() {
		if (groupName == null) {
			groupName = "";
		}
		return groupName;
	}

	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getUuid() {

		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		return uuid;
	}

	/**
	 * @param string
	 */
	public void setGroupName(String string) {
		BrokerFactory.getLoggingBroker().logDebug ("Setting group name to "+string+", with existing email="+email);
		setEmailAddress("");
		groupName = string;
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().updateGroup(this);
		}
	}

	/**
	 * @param i
	 */
	public void setType(int i) {
		type = i;
	}

	/**
	 * @param string
	 */
	public void setUuid(String string) {
		uuid = string;
	}

	/**
	 * Returns all the members of this group. May include other groups
	 * 
	 * @return
	 */
	public Member[] getMembers() {
		if (!membersLoaded) {
			loadMembers();
		}
		return (Member[]) members.toArray(new Member[0]);
	}

	public void setEmailAddress (String email) {
		this.email = email;
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().updateGroup(this);
		}
	}
	
	public String getEmailAddress() {
		BrokerFactory.getLoggingBroker().logDebug ("Getting email for group w/ uuid "+getUuid()+".  It's currently set to "+email);
		if (StringUtils.isEmpty(email)) {
			email = EmailUtil.makeEmailAddress(this); 
			if (autocommit) {
				BrokerFactory.getGroupMgmtBroker().updateGroup(this);
			}
		}

		return email;
	}

	/**
	 * 
	 * @param member
	 *            The member to add
	 * @param order
	 *            The order in the list. Use a negative number to append to the
	 *            end
	 */
	public void addMember(Member member, int order)
			throws InvalidGroupException {
		if (!membersLoaded) {
			loadMembers();
		}

		if ((order < 0) || (order > members.size())) {
			members.addElement(member);
		} else {
			members.insertElementAt(member, order);
		}

		if (autocommit)
			BrokerFactory.getGroupMgmtBroker().addMemberToGroup(member, this);
	}

	public void addMembers(Member[] members) throws InvalidGroupException {
		if (!membersLoaded) {
			loadMembers();
		}
		for (int i = 0; i < members.length; i++) {
			addMember(members[i], -1);
		}
	}

	public void removeMemberFromGroup(int memberNum) {
		if (!membersLoaded) {
			loadMembers();
		}
		if (autocommit)
			BrokerFactory.getGroupMgmtBroker().removeMemberFromGroup(memberNum,
					this);
		members.removeElementAt(memberNum);
	}

	/**
	 * Returns the index of this member. This is useful for order-sensitive
	 * groups, like the EscalationGroup.
	 * 
	 * @param member
	 *            The member to look for
	 * @return The number of this group in natural order, from 0
	 */
	public int getMemberNumber(Member member) {
		if (!membersLoaded) {
			loadMembers();
		}
		Member[] members = getMembers();
		for (int i = 0; i < members.length; i++) {
			if (members[i].equals(member)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * This will tell you if the given member is a member of this group, or a
	 * descendant of this group.
	 * 
	 * @param member
	 *            The member to check
	 * @return Whether this member is a member of this group or a descendant of
	 *         it
	 */
	public boolean isMember(Member member) {
		if (!membersLoaded) {
			loadMembers();
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"Checking is " + member + " is a member of " + this);
		Member[] members = getMembers();
		for (int i = 0; i < members.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug(
					members[i] + " is a direct member of " + this);
			if (member.equals(members[i]))
				return true;
			if (members[i] instanceof Group) {
				if (!members[i].equals(this)) {
					if (((Group) members[i]).isMember(member))
						return true;
				}
			}
		}
		return false;
	}

	public void moveMemberUp(Member member) {

	}

	public void moveMemberUp(int memberNum) {
		if (!membersLoaded) {
			loadMembers();
		}
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().moveMemberUp(this, memberNum);
		} else {

		if (memberNum < members.size()) {
			Member member = (Member) members.elementAt(memberNum);
			members.remove(member);
			members.add(memberNum - 1, member);
		}
		}
	}

	public void moveMemberDown(int memberNum) {
		if (!membersLoaded) {
			loadMembers();
		}
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().moveMemberDown(this, memberNum);
		} else {

		if (memberNum < (members.size()-1)) {
			Member member = (Member) members.elementAt(memberNum);
			members.remove(member);
			members.add(memberNum + 1, member);
		}
		}
	}

	public String getDescription() {
		if (description == null)
			description = "";
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setInPermanentCache(boolean inPermanentCache) {
		this.inPermanentCache = inPermanentCache;
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().updateGroup(this);
		}
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


	public boolean isInPermanentCache() {
		return inPermanentCache;
	}
	
	public boolean isOwner(Member member, boolean showAll) {
		if (member instanceof User) {
			if (BrokerFactory.getAuthorizationBroker().isUserInRole((User)member, Roles.ADMINISTRATOR)) {
				return true;
			}
		}
		Member[] members = getMembers();
		if (owners.contains(member)) {
			BrokerFactory.getLoggingBroker().logDebug(member+" is an owner because it is in the owners list directly");
			return true;
		}
		
		if (showAll) {
			for (int i = 0; i < members.length; i++) {
				if ((owners.contains(members[i])) && 
					(members[i] instanceof Group) && 
					(((Group)members[i]).isMember(member))) {
					BrokerFactory.getLoggingBroker().logDebug(member+" is an owner because it is a member of "+members[i]+" which is in the owners list");
					return true;
				}
			}
		}
		BrokerFactory.getLoggingBroker().logDebug(member+" is not an owner of "+this);
		return false;
	}
	
	public void setOwner (int memberNum) {
		if (memberNum >= members.size()) {
			return;
		}
		Member member = (Member)members.elementAt(memberNum);
		setOwner (member);
	}

	public void setOwner (Member member) {
		BrokerFactory.getLoggingBroker().logDebug("Making "+member+" an owner of "+this);
		if (!owners.contains(member)) {
			owners.addElement(member);
			if (autocommit) {
				BrokerFactory.getGroupMgmtBroker().addOwner(this, member);
			}
		}		
	}
	
	public void unsetOwner (int memberNum) {
		Member member = (Member)members.elementAt(memberNum);
		unsetOwner (member);
	}

	public void unsetOwner (Member member) {
		BrokerFactory.getLoggingBroker().logDebug("Making "+member+" not an owner of "+this);
		if (owners.contains(member)) {
			owners.removeElement(member);
			if (autocommit) {
				BrokerFactory.getGroupMgmtBroker().removeOwner(this, member);
			}
		}
		
	}
	

	public boolean getAutocommit() {
		return autocommit;
	}

	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	public String toString() {
		String name = getGroupName();
		if (name == null) {
			BrokerFactory.getLoggingBroker().logWarn("Group name is null");
			name = "";
		}
		return name;
	}

	public boolean equals(Object other) {
		if ((other instanceof Group)
				&& (((Group) other).getUuid().equals(getUuid()))) {
			return true;
		} else {
			return false;
		}
	}

	public void reloadMembers() {
		membersLoaded = false;
		members = new Vector();
		loadMembers();
	}
	public void loadMembers() {
		if (!membersLoaded) {
			membersLoaded = true;
			boolean storedAutoCommit= autocommit;
			setAutocommit(false);
			BrokerFactory.getGroupMgmtBroker().loadMembers(this);
			setAutocommit(storedAutoCommit);
		}
		for (int i = 0; i <members.size(); i++) {
			BrokerFactory.getLoggingBroker().logDebug("Member ["+i+"="+members.elementAt(i));
		}
	}
	
	public String getAsXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append("<group>\n");

		xml.append("<url>");
		xml.append("/notification/rest/groups/"+getUuid());
		xml.append("</url>\n");

		xml.append("<groupname>");
		xml.append(getGroupName());
		xml.append("</groupname>\n");
		
		xml.append("<description>");
		xml.append(getDescription());
		xml.append("</description>\n");

		xml.append("<members>");
		Member[] members = getMembers();
		for (int memberNum = 0; memberNum < members.length; memberNum++) {
			Member member = members[memberNum];
			String value = "";
			if (member instanceof Group) {
				value = "/notification/rest/groups/"+member.getUuid();
			} else {
				value = "/notification/rest/users/"+member.getUuid();
			}
			xml.append ("<member>"+value+"</member>\n");
		}
		xml.append("</members>\n");
		xml.append("</group>\n");
		
		return xml.toString();
	}
}
