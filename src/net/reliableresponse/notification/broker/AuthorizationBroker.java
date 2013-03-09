/*
 * Created on Aug 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface AuthorizationBroker {
	
	/**
	 * This is the foundation of the RBAC.  It allows you
	 * to determine whether a user is in a particular role.
	 * This is based on the J2EE spec, which has its roots in 
	 * classical RBAC design.
	 * 
	 * @param member The user or group to check
	 * @param role The role you are asking about
	 * @return Whether this user is in the role
	 */
	public boolean isUserInRole(Member member, String role);
	
	/**
	 * Get a list of all available roles
	 * @return All available roles
	 */
	public String[] getRoles();
	
	/**
	 * Returns all the members of the provided role
	 * @param role The role to inspect
	 * @return The members (users and groups) in that role
	 */
	public Member[] getMembersInRole(String role);

	/**
	 * Adds a user to a role
	 * @param member The user or group to add
	 * @param role The role to add him/her/it to
	 */
	public void addUserToRole (Member member, String role);
	
	/**
	 * Removesa user to a role
	 * @param member The user or group to add
	 * @param role The role to add him/her/it to
	 */
	public void removeMemberFromRole (Member member, String role);
	
	/**
	 * This is used to determine is a user has the rights to a certain
	 * resource, like a web notification.
	 * @param resource The resource to check access to
	 * @param member The user or group to check membership
	 * @return Whether the member has access to the resource
	 */
	public boolean isResourceAllowed (Object resource, Member member);
}
