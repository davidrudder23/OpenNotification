/*
 * Created on Dec 16, 2008
 *
 *Copyright Reliable Response, 2008
 */
package net.reliableresponse.notification.sender;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;

public class ProactiveNetSender extends AbstractNotificationSender {

	public static final int SEVERITY = 1;
	public static final int DEVICE = 2;
	public static final int MO_TYPE = 3;
	public static final int INSTANCE_NAME = 4;
	public static final int ATTR_NAME = 5;
	public static final int PNET_SERVER = 6;
	public static final int STATE = 7;

	String severity;
	String device;
	String moType;
	String instanceName;
	String attributeName;
	String pnetServer;
	String state;

	public void addVariable(int index, String value) {
		switch (index) {
		case SEVERITY:
			severity = value;
			break;
		case DEVICE:
			device = value;
			break;
		case MO_TYPE:
			moType = value;
			break;
		case INSTANCE_NAME:
			instanceName = value;
			break;
		case ATTR_NAME:
			attributeName = value;
			break;
		case PNET_SERVER:
			pnetServer = value;
			break;
		case STATE:
			state = value;
			break;
		default:
			BrokerFactory.getLoggingBroker().logWarn(
					"ProactiveNetSender Got bad index for variable: " + index);
		}
	}

	public String[] getVariables() {
		String[] variables = { severity, device, moType, instanceName,
				attributeName, pnetServer, state };
		return variables;
	}

	public String[] getAvailableResponses(Notification notification) {
		String[] escalationOptions = { "Ack", "AckAll", "Close", "CloseAll",
				"Pass" };
		String[] options = { "Ack", "AckAll", "Close", "CloseAll" };
		if (notification.getUltimateParent().getRecipient() instanceof EscalationGroup) {
			return escalationOptions;
		} else {
			return options;
		}
	}

	public void handleResponse(Notification notification, Member responder,
			String response, String text) {
		String logMessage = "Sending " + response
				+ " to ProactiveNet with uuid=" + notification.getUuid()
				+ " and args:\n";
		String[] variables = getVariables();

		String message = text;
		if (message == null)
			message = "";

		for (int varNum = 0; varNum < variables.length; varNum++) {
			logMessage += "    " + (varNum + 1) + ": " + variables[varNum];
		}
		BrokerFactory.getLoggingBroker().logDebug(logMessage);

		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory
					.getLoggingBroker()
					.logInfo(
							responder
									+ " tried to confirm an expired notification with uuid "
									+ notification.getUuid());
			return;
		}

		if ((response.equalsIgnoreCase("ack"))
				|| (response.equalsIgnoreCase("confirm"))) {
			super.handleResponse(notification, responder, response, text);
			String cmdLine = BrokerFactory.getConfigurationBroker()
					.getStringValue("proactivenet.cmdline",
							"./reliable_script.pl");
			try {
                               new ProcessThread(new String[]{
							cmdLine,
                                                        "-h "+pnetServer+"",
							"--monitor-type="+moType+"", 
                                                        "--monitor-instance=="+instanceName+"",
                                                        "--attribute="+attributeName+"",
                                                        "--severity="+severity+"",
                                                        "--ack-msg="+text.replaceAll("\"", "\"").trim()+"",
                                                        "--hostname="+pnetServer+"",
                                                        "--subject="+notification.getSubject().replaceAll("\"", "\"").trim()+"",
                                                        "--pnet-server=" + pnetServer + ""}).start();

			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		} else if (response.equalsIgnoreCase("ackall")) {
			super.handleResponse(notification, responder, "confirmall", text);
		} else if (response.equalsIgnoreCase("close")) {
			String cmdLine = BrokerFactory.getConfigurationBroker()
					.getStringValue("proactivenet.cmdline",
							"./reliable_script.pl");
			try {
				new ProcessThread(new String[]{cmdLine,
							"--monitor-type="+moType+"", 
							"--monitor-instance="+instanceName+"",
							"--attribute="+attributeName+"",
							"--severity="+severity+"",
							"--ack-msg="+text.replaceAll("\"", "\"").trim()+"",
							"--hostname="+pnetServer+"",
							"--subject="+notification.getSubject().replaceAll("\"", "\"").trim()+"",
							"--pnet-server=" + pnetServer + ""}).start();
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			notification.addMessage(response, responder);
			super.handleResponse(notification, responder, "confirm", text);
		} else if (response.equalsIgnoreCase("closeall")) {
			Member recipient = responder;
			if ((recipient == null) || (recipient instanceof UnknownUser)) {
				recipient = notification.getUltimateParent().getRecipient();
			}
			List<Notification> unconfirmedNotifs = BrokerFactory.getNotificationBroker().getMembersUnconfirmedNotifications(recipient);
			BrokerFactory.getLoggingBroker().logDebug(
					"CloseAll found " + unconfirmedNotifs.size()
							+ " unconfirmed notifs");
			for (Notification unconfirmedNotif:unconfirmedNotifs) {
				if (unconfirmedNotif.getSender().getClass().equals(
						getClass())) {
					BrokerFactory.getLoggingBroker().logDebug(
							"CloseAll closing " + unconfirmedNotif);
					unconfirmedNotif.getSender().handleResponse(
							unconfirmedNotif, responder, "Close", text);
				}
			}
			if ((notification.getStatus() == Notification.PENDING)
					|| (notification.getStatus() == Notification.NORMAL)) {
				notification.getSender().handleResponse(notification,
						responder, "Confirm", text);
			}
		} else {
			super.handleResponse(notification, responder, response, text);
		}
	}

	public String getConfirmEquivalent(Notification notification) {
		return "Confirm";
	}

	public String getPassEquivalent(Notification notification) {
		return "Pass";
	}

	public String toString() {
		return "ProactiveNet network monitor";
	}

	public boolean getVariablesFromNotificationOld(Notification notification) {
		String subject = notification.getSubject();
		String regexp = ":";
		Pattern pattern = Pattern.compile(regexp);
		String[] parts = pattern.split(notification.getSubject());
		BrokerFactory.getLoggingBroker().logDebug(
				"ProactiveNet sender got " + parts.length
						+ " variables and was expecting 5");
		for (int partNum = 0; partNum < parts.length; partNum++) {
			BrokerFactory.getLoggingBroker().logDebug(
					"ProactiveNet sender adding variable #" + (partNum + 1)
							+ " " + parts[partNum]);
			addVariable(partNum + 1, parts[partNum]);
		}

		return true;
	}

	public boolean getVariablesFromNotification(Notification notification) {
		String message = notification.getDisplayText();

		BufferedReader msgIn = new BufferedReader(new StringReader(message));
		
		if (notification.getSubject().indexOf(":")>0) {
			BrokerFactory.getLoggingBroker().logDebug("Looking for attr in "+notification.getSubject());
			String attr = notification.getSubject().substring(notification.getSubject().lastIndexOf(":")+1);
			BrokerFactory.getLoggingBroker().logDebug("attr="+attr);
			addVariable(ATTR_NAME, attr);
		}

		try {
			String line = "";
			while ((line = msgIn.readLine()) != null) {
				if (line.indexOf(":") > 0) {
					String key = line.substring(0, line.indexOf(":"));
					String value = line.substring(line.indexOf(":") + 1, line
							.length());
					BrokerFactory.getLoggingBroker().logDebug("key=" + key);
					BrokerFactory.getLoggingBroker().logDebug("value=" + value);

					if (key.equalsIgnoreCase("Severity")) {
						addVariable(SEVERITY, value);
					} else if (key.equalsIgnoreCase("Device")) {
						addVariable(DEVICE, value);
					} else if (key.equalsIgnoreCase("Monitor Type")) {
						addVariable(MO_TYPE, value);
					} else if (key.equalsIgnoreCase("Instance")) {
						addVariable(INSTANCE_NAME, value);
					} else if (key.equalsIgnoreCase("source_pnet_server")) {
						addVariable(PNET_SERVER, value);
					} else if (key.equalsIgnoreCase("Reporting Agent")) {
						//addVariable(ATTR_NAME, value);
					}
				}
			}
		} catch (Exception anyExc) {
			anyExc.printStackTrace();
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		ProactiveNetSender sender = new ProactiveNetSender();

		String message = "Device:sjc1-eos-pr-db009\n"
				+ "Monitor Type:repli_threads_st\n"
				+ "Instance: 10.1.14.217 repli_threads\n"
				+ "Severity:Critical\n"
				+ "Description: repli_threads_st state above 0.100#\n"
				+ "Reporting Agent: 10.1.14.217\n"
				+ "Threshold reached at: Oct 2, 01:00:35 - state=1.0 #\n"
				+ "Acknowledgement status=false\n"
				+ "Acknowledgement message=None\n"
				+ "source_pnet_server:ussj-pr-pan001.sc9.ciscoeos.com\n"
				+ "n"
				+ "The following is a link to the web page for the Event\n"
				+ "http://ussj-pr-pan001/servlets/com.proactivenet.servlet.ProntoPageServlet?display=%2Fservlets%2Fcom.proactivenet.servlet.AlarmDetailServlet%3Falarmid%3D152413\n"
				+ "\n"
				+ "\n"
				+ "Note: This email was triggered by alarm rule \"email-All-PRODUCTION\".";
		
		String foomessage = "Device:sjc1-eos-pr-db009\n"+
		"Monitor Type:TESTING\n"+
		"Instance: TESTING db009 - ignore\n"+
		"Severity:Critical\n"+
		"Description: TESTING Data Availability above 0%  for 1 min.\n"+
		"Reporting Agent: 10.1.14.217\n"+
		"Threshold reached at: Jan 7, 14:12:33 - Data Availability=100.0 %\n"+
		"Acknowledgement status=false\n"+
		"Acknowledgement message=None\n"+
		"source_pnet_server:ussj-pr-pan001.sc9.ciscoeos.com\n"+
		"\n"+
		"The following is a link to the web page for the Event\n"+
		"\n"+
		"http://ussj-pr-pan001/servlets/com.proactivenet.servlet.ProntoPageServlet?display=%2Fservlets%2Fcom.proactivenet.servlet.AlarmDetailServlet%3Falarmid%3D154994\n"+
		"\n"+
		"\n"+
		"Note: This email was triggered by alarm rule \"email-All-PRODUCTION\".";
		
		message = "Device:sjc1-eos-pr-db009\n"+
"Monitor Type:TESTING\n"+
"Instance: TESTING db009 - ignore\n"+
"Severity:Critical\n"+
"Description: TESTING state above 0#  for 1 min.\n"+
"Reporting Agent: 10.1.14.217\n"+
"Threshold reached at: Jan 26, 16:13:19 - state=1.0 #\n"+
"Acknowledgement status=false\n"+
"Acknowledgement message=None\n"+
"source_pnet_server:ussj-pr-pan001.sc9.ciscoeos.com\n"+
"\n"+
"The following is a link to the web page for the Event\n"+
"http://ussj-pr-pan001/servlets/com.proactivenet.servlet.ProntoPageServlet?disp\n"+
"lay=%2Fservlets%2Fcom.proactivenet.servlet.AlarmDetailServlet%3Falarmid%3D1559\n"+
"62\n"+
"\n"+
"\n"+
"Note: This email was triggered by alarm rule \"DB-Prod-SQL-STATS-CRIT-ONLY\".\n"+
"\n"+
"\n"+
"You may reply to this message by entering any of these responses in the\n"+ 
"subject\n"+
"\n"+
"    Ack\n"+
"    AckAll\n"+
"    Close\n"+
"    CloseAll";

		Notification notification = new Notification(
				null,
				new User(),
				sender,
				"Critical:sjc1-eos-pr-db009:TESTING:TESTING db009 - ignore:state",
				message);

		sender.getVariablesFromNotification(notification);
		
		BrokerFactory.getLoggingBroker().logDebug("Mo Type="+sender.moType);
		BrokerFactory.getLoggingBroker().logDebug("Instance name="+ sender.instanceName);
		BrokerFactory.getLoggingBroker().logDebug("Attrib name="+ sender.state);
		BrokerFactory.getLoggingBroker().logDebug("severity="+sender.severity);
		BrokerFactory.getLoggingBroker().logDebug("pnet server="+ sender.pnetServer);
		BrokerFactory.getLoggingBroker().logDebug("device="+ sender.device);
		
		sender.handleResponse(notification, new User(),
				"Confirm", "be right back");
	}
}

class ProcessThread extends Thread {

	String[] args;

	public ProcessThread(String[] args) {
		this.args = args;
	}

	public void run() {
		try {
			BrokerFactory.getLoggingBroker().logDebug(
					"running proactivenet connection with args "+Arrays.toString(args));
			//Process process = Runtime.getRuntime().exec(command, args);
			Process process = Runtime.getRuntime().exec(args);
			process.waitFor();
			int exitValue = process.exitValue();
			BrokerFactory.getLoggingBroker().logInfo(
					"ProactiveNet returned with " + exitValue);
			
			String line = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while ((line = in.readLine())!= null) {
				BrokerFactory.getLoggingBroker().logDebug("Read from process: "+line);
			}
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
}
