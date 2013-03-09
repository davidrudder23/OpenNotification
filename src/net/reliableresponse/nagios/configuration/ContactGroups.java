/*
 * Created on Apr 15, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.nagios.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ContactGroups {

	private static final int PAGE_SIZE=10;

	public ContactGroups() {
		
	}
	
	public void writeToFile (String filename) throws IOException {
		writeToFile( new File (filename));
	}

	public void writeToFile (File file) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(file));
	
		// Since Nagios can't contact user's  directly, we make a contact group for each user
		User[] users = new User[PAGE_SIZE];
		int numUsers = BrokerFactory.getUserMgmtBroker().getNumUsers();
		int exportedUsers = 0;
		int pageNum = 0;
		while (exportedUsers < numUsers) {
			int numUsersRetrieved = BrokerFactory.getUserMgmtBroker().getUsers(PAGE_SIZE, pageNum, users);
			
			for (int i = 0; i < numUsersRetrieved; i++) {
				out.println ("# Define Reliable Response Notification contact group for "+users[i].toString());
				out.println ("define contactgroup {");
				out.println ("\tcontactgroup_name\t\t\t"+users[i].getEmailAddress());
				out.println ("\talias\t\t\t"+users[i].toString());
				out.println ("\tmembers\t\t\t" +users[i].getEmailAddress());
				out.println ("}");
				out.println ("");
			}
			pageNum++;
			exportedUsers += numUsersRetrieved;
		}
		
		Group[] groups = new Group[PAGE_SIZE];
		int numGroups = BrokerFactory.getGroupMgmtBroker().getNumGroups();
		int exportedGroups = 0;
		pageNum = 0;
		while (exportedGroups < numGroups) {
			int numGroupsRetrieved = BrokerFactory.getGroupMgmtBroker().getGroups(PAGE_SIZE, pageNum, groups);
			
			for (int i = 0; i < numGroupsRetrieved; i++) {
				out.println ("# Define Reliable Response Notification contact group for "+groups[i].toString());
				out.println ("define contactgroup {");
				out.println ("\tcontactgroup_name\t\t\t"+formatGroupName(groups[i].getGroupName()));
				out.println ("\talias\t\t\t"+groups[i].toString());
				out.println ("\tmembers\t\t\t"+formatGroupName(groups[i].getGroupName()));
				out.println ("}");
				out.println ("");
			}
			pageNum++;
			exportedGroups += numGroupsRetrieved;
		}
		
		out.close();
	}
	
	private String formatGroupName (String groupName) {
		//String formatted = groupName.toLowerCase().replace(" ", "_");
		return groupName;
	}
}
