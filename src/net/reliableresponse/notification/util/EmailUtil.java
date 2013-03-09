/*
 * Created on Feb 6, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.util;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

public class EmailUtil {

	public static String normalizeForEmail(String name) {
		if (name == null) {
			return "";
		}
		name = name.replace(' ', '_');
		name = name.replace('#', '.');
		name = name.replace('@', '.');
		name = name.replace('/', '.');
		name = name.replace('[', '.');
		name = name.replace(']', '.');
		name = name.replace('<', '.');
		name = name.replace('>', '.');
		name = name.replace('\'', '.');
		name = name.replace(',', '.');
		name = name.replace('"', '.');
		name = name.replace('{', '.');
		name = name.replace('}', '.');
		name = name.replace('\\', '.');
		name = name.replace('$', '.');
		name = name.replace('!', '.');
		name = name.replace('%', '.');
		name = name.replace('^', '.');
		name = name.replace('&', '.');
		name = name.replace('`', '.');
		name = name.replace('~', '.');
		name = name.replace('*', '.');
		name = name.replace('(', '.');
		name = name.replace(')', '.');
		name = name.replace('+', '.');
		name = name.replace('=', '.');

		return name;
	}

	public static String makeEmailAddress(Member member) {
		String mailMethod = BrokerFactory.getConfigurationBroker()
				.getStringValue("email.method");
		String name = "";
		String domain = "";
		String email = "";
		
		if (member instanceof User) {
			User user= (User)member;
			
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			if (StringUtils.isEmpty(firstName)) {
				firstName = "";
			}
			
			if (StringUtils.isEmpty(lastName)) {
				name = firstName;
			} else {
				name = firstName+"."+lastName;
			}
		} else {
			Group group = (Group)member;
			name = group.getGroupName();
			if (StringUtils.isEmpty(name)) {
				name = "unknown";
			}			
		}
		
		name = normalizeForEmail(name);
		
		if (mailMethod == null)
			mailMethod = "pop";
		mailMethod = mailMethod.toLowerCase();

		if (mailMethod.equals("smtp")) {
			String smtpHostname = BrokerFactory.getConfigurationBroker()
					.getStringValue("smtp.server.hostname");
			if (smtpHostname == null) {
				try {
					smtpHostname = InetAddress.getLocalHost().getHostName();
					int dotIndex = smtpHostname.lastIndexOf(".") - 1;
					if (dotIndex > 0) {
						dotIndex = smtpHostname.lastIndexOf(".", dotIndex);
						if (dotIndex > 0) {
							smtpHostname = smtpHostname.substring(dotIndex + 1,
									smtpHostname.length());
						}
					}
				} catch (UnknownHostException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}

			domain = smtpHostname;
		} else {
			domain = BrokerFactory.getConfigurationBroker().getStringValue(
					"email.pop.address");
			if (domain.indexOf("@") >= 0) {
				domain = domain.substring(domain.indexOf("@")+1, domain.length());
			}
		}

		boolean found = false;
		name = name.toLowerCase();
		boolean doCount = false;
		if (doCount) {
		String originalName = name;
		int count = 0;
		do {
			found = false;
			email = name + "@" +domain;
			User user = BrokerFactory.getUserMgmtBroker().getUserByEmailAddress(email);
			if (user != null) found = true;
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByEmail(email);
			if (group != null) found = true;
			
			if (found) {
				count++;
				name = originalName+"-"+count;
			}
		} while (found);
		} else {
			email = name + "@" +domain;
		}
		return email;
	}
	
	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		Group[] groups = new Group[1000];
		int num = BrokerFactory.getGroupMgmtBroker().getGroups(1000, 0, groups);
		for (int i = 0; i < num; i++) {
			System.out.println (groups[i]+": "+EmailUtil.makeEmailAddress(groups[i]));
		}
		User[] users = new User[1000];
		num = BrokerFactory.getUserMgmtBroker().getUsers(10000, 0, users);
		for (int i = 0; i < num; i++) {
			System.out.println (users[i]+": "+EmailUtil.makeEmailAddress(users[i]));
		}
	}
}
