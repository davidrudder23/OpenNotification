/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BroadcastGroup extends Group {
	public BroadcastGroup() {
	}
	
	public void addMember(Member member, int order) throws InvalidGroupException {
		if (member == null) return;
		if (member.equals(this)) {
			BrokerFactory.getLoggingBroker().logDebug("Can't add a broadcast group to itself");
			throw new InvalidGroupException("I'm sorry, but you can't add a Broadcast group to itself");
			
		}
		BrokerFactory.getLoggingBroker().logDebug("type="+member.getType());
		BrokerFactory.getLoggingBroker().logDebug("broadcast="+BROADCAST);
		BrokerFactory.getLoggingBroker().logDebug("this.isMember(that)="+isMember(member));
		if (member.getType() == BROADCAST)
			BrokerFactory.getLoggingBroker().logDebug("that.isMember(this)="+((Group)member).isMember(this));
		if ((member.getType() == BROADCAST) && (isMember(member))) {
			BrokerFactory.getLoggingBroker().logDebug("Can't add a broadcast group to itself");
			throw new InvalidGroupException("I'm sorry, but you can't add a Broadcast group to itself");
		}
		
		if ((member.getType() == BROADCAST) && (((Group)member).isMember(this))) {
			BrokerFactory.getLoggingBroker().logDebug("Can't add a broadcast group to itself");
			throw new InvalidGroupException("I'm sorry, but you can't add a Broadcast group to itself");
		}
		BrokerFactory.getLoggingBroker().logDebug("Adding "+member+" to "+this);
		super.addMember(member, order);
	}
	public int getType() {
		return BROADCAST;
	}
}
