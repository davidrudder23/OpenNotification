/*
 * Created on Apr 15, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.nagios.configuration;

import java.io.File;
import java.io.FileInputStream;
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
public class Contacts {
	
	private static final int PAGE_SIZE=10;
	
	public Contacts() {
		
	}
	
	public void writeToFile (String filename) throws IOException {
		writeToFile( new File (filename));
	}

	public void writeToFile (File file) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		User[] users = new User[PAGE_SIZE];
		int numUsers = BrokerFactory.getUserMgmtBroker().getNumUsers();
		int exportedUsers = 0;
		int pageNum = 0;
		while (exportedUsers < numUsers) {
			int numUsersRetrieved = BrokerFactory.getUserMgmtBroker().getUsers(PAGE_SIZE, pageNum, users);
			
			for (int i = 0; i < numUsersRetrieved; i++) {
				out.println ("# Define Reliable Response Notification contact for "+users[i].toString());
				out.println ("define contact {");
				out.println ("\tcontact_name\t\t\t"+users[i].getEmailAddress());
				out.println ("\talias\t\t\t"+users[i].toString());
				out.println ("\tservice_notification_period\t\t\t24x7");
				out.println ("\thost_notification_period\t\t\t24x7");
				out.println ("\tservice_notification_options\t\t\tw,u,c");
				out.println ("\thost_notification_options\t\t\td,u");
				out.println ("\tservice_notification_commands\t\t\tnotify-by-reliable");
				out.println ("\thost_notification_commands\t\t\thost-notify-by-reliable");
				out.println ("\tpager\t\t\t"+users[i].getEmailAddress());
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
				out.println ("define contact {");
				out.println ("\tcontact_name\t\t\t"+formatGroupName(groups[i].getGroupName()));
				out.println ("\talias\t\t\t"+groups[i].toString());
				out.println ("\tservice_notification_period\t\t\t24x7");
				out.println ("\thost_notification_period\t\t\t24x7");
				out.println ("\tservice_notification_options\t\t\tw,u,c");
				out.println ("\thost_notification_options\t\t\td,u");
				out.println ("\tservice_notification_commands\t\t\tnotify-by-reliable");
				out.println ("\thost_notification_commands\t\t\thost-notify-by-reliable");
				out.println ("\tpager\t\t\t"+groups[i].getUuid());
				out.println ("}");
				out.println ("");
			}
			pageNum++;
			exportedGroups += numGroupsRetrieved;
		}

		out.close();
	}
	
	private String formatGroupName (String groupName) {
		return groupName;
	}

	
	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		Contacts contacts = new Contacts();
		contacts.writeToFile("/etc/nagios/reliable_contacts.cfg");
		
		ContactGroups contactGroups = new ContactGroups();
		contactGroups.writeToFile("/etc/nagios/reliable_contactgroups.cfg");
	}
}
