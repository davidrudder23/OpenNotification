import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

/*
 * Created on Oct 12, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddUsersAndGroups {

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		for (int i = 0; i < 3000; i++) {
			User user = new User();
			user.setFirstName("User");
			user.setLastName(i+"");
			user.setDepartment("Test");
			BrokerFactory.getUserMgmtBroker().addUser(user);
		}
		for (int i = 0; i < 300; i++) {
			EscalationGroup group = new EscalationGroup();
			group.setGroupName("Escalation Group "+i);
			group.setDescription("Test Group");
			BrokerFactory.getGroupMgmtBroker().addGroup(group);
			User[] users = new User[3];
			BrokerFactory.getUserMgmtBroker().getUsers(3, i, users);
			group.addMembers(users);
		}
		for (int i = 0; i < 300; i++) {
			BroadcastGroup group = new BroadcastGroup();
			group.setGroupName("Broadcast Group "+i);
			group.setDescription("Test Group");
			BrokerFactory.getGroupMgmtBroker().addGroup(group);
			User[] users = new User[3];
			BrokerFactory.getUserMgmtBroker().getUsers(3, i, users);
			group.addMembers(users);
		}
	}
}
