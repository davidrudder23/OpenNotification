/*
 * Created on Jan 17, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.caching;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.AuthorizationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * This class handles caching for the AuthorizationBroker, which is called
 * frequently and can represent undue database utilization.
 * 
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class CachingAuthorizationBroker implements AuthorizationBroker {
	protected Hashtable roles, notroles;
	AuthorizationBroker realBroker;

	public CachingAuthorizationBroker(AuthorizationBroker realBroker) {
		roles = new Hashtable();
		notroles = new Hashtable();
		this.realBroker = realBroker;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#isUserInRole(net.reliableresponse.notification.usermgmt.Member, java.lang.String)
	 */
	public boolean isUserInRole(Member member, String role) {
		Vector members = (Vector)roles.get (role);
		if (members == null) {
			members = new Vector();
		}
		if (members.contains(member)) {
				return true;
		}

		Vector notmembers = (Vector)notroles.get (role);
		if (notmembers == null) {
			notmembers = new Vector();
		}
		if (notmembers.contains(member)) {
				return false;
		}

		boolean isInRole = realBroker.isUserInRole(member, role);
		if (isInRole) {
			members.addElement(member);
			roles.put (role,members);
		} else {
			notmembers.addElement(member);
			notroles.put (role,notmembers);
		}
		
		return isInRole;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#addUserToRole(net.reliableresponse.notification.usermgmt.Member, java.lang.String)
	 */
	public void addUserToRole(Member member, String role) {
		BrokerFactory.getLoggingBroker().logDebug("Adding "+member+" to role "+role);
		Vector members = (Vector)roles.get(role);
		if (members == null) {
			members = new Vector();
		}
		if (!members.contains(member)) members.addElement(member);
		
		Vector notmembers = (Vector)notroles.get (role);
		if (notmembers == null) {
			notmembers = new Vector();
		}
		if (notmembers.contains(member)) {
			notmembers.remove(member);
			notroles.put (roles, notmembers);
		}
				
		roles.put (role, members);
		realBroker.addUserToRole(member, role);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#removeUserFromRole(net.reliableresponse.notification.usermgmt.Member, java.lang.String)
	 */
	public void removeMemberFromRole(Member member, String role) {
		Vector members = (Vector)roles.get(role);
		if (members == null) {
			return;
		}
		if (members.contains(member)) members.removeElement(member);

		Vector notmembers = (Vector)notroles.get (role);
		if (notmembers == null) {
			notmembers = new Vector();
		}
		if (!notmembers.contains(member)) {
			notmembers.addElement(member);
			notroles.put (roles, notmembers);
		}
		
		roles.put (role, members);
		realBroker.removeMemberFromRole(member, role);
	}
	
	public String[] getRoles() {
		return realBroker.getRoles();
	}
	
	

	public Member[] getMembersInRole(String role) {
		return realBroker.getMembersInRole(role);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#isResourceAllowed(java.lang.Object, net.reliableresponse.notification.usermgmt.Member)
	 */
	public boolean isResourceAllowed(Object resource, Member member) {
		// TODO Auto-generated method stub
		return realBroker.isResourceAllowed(resource, member);
	}

}
