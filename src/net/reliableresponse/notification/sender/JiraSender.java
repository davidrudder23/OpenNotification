/*
 * Created on Dec 22, 2008
 *
 *Copyright Reliable Response, 2008
 */
package net.reliableresponse.notification.sender;

import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

public class JiraSender extends AbstractNotificationSender {

	public void addVariable(int index, String value) {
		// TODO Auto-generated method stub

	}

	public String[] getVariables() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	public boolean getVariablesFromNotification(Notification notification) {
		String message = notification.getMessages()[0].getMessage();
		message = message.replaceAll("\n", "");
		message = message.replaceAll("\r", "");
		String regexp = BrokerFactory.getConfigurationBroker().getStringValue("jira.priority.regex", ".*Priority:.*?\\/P(\\d+).*");
		Pattern pattern = Pattern.compile(regexp);
		BrokerFactory.getLoggingBroker().logDebug("Matching "+regexp+" against "+message);
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			BrokerFactory.getLoggingBroker().logDebug("Matched with "+matcher.groupCount()+" matches!");
			String priorityString = matcher.group(1);
			BrokerFactory.getLoggingBroker().logDebug("JIRA Matched string is "+priorityString); 
			BrokerFactory.getLoggingBroker().logDebug(
					"JIRA sender got Priority " + priorityString);
			try {
				int priorityInt = Integer.parseInt(priorityString);
				notification.setPriority(priorityInt);
				BrokerFactory.getLoggingBroker().logDebug("Matched! - "+priorityInt);
				return (priorityInt < 3);
			} catch (NumberFormatException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}  else {
			BrokerFactory.getLoggingBroker().logDebug("Didn't Match!");
		}
			

		return true;
	}

	public static void main(String[] args) throws Exception {

		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001");
		Notification notification = new Notification(null, user, new JiraSender(), args[0], args[1]);
		
		new JiraSender().getVariablesFromNotification(notification);
	}
}
