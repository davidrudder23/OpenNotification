/*
 * Created on Nov 4, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.command.Command;
import net.reliableresponse.notification.usermgmt.Member;

public interface CommandBroker {
	
	public Command[] getAllCommands();
	
	public Command[] getCommandsForMember(Member member);

}
