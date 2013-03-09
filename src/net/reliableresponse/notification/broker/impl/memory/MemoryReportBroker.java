/*
 * Created on Sep 26, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker.impl.memory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ReportBroker;
import net.reliableresponse.notification.usermgmt.Group;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MemoryReportBroker implements ReportBroker {
	String[] names;

	String[] descriptions;

	public MemoryReportBroker() {
		names = new String[5];
		names[0] = "groupsummary";
		names[1] = "allnotifications";
		names[2] = "individual";
		names[3] = "allmissed";
		names[4] = "missedbygroup";
		
		descriptions = new String[5];
		descriptions[0] = "Summary of Activity of the Members of a Group";
		descriptions[1] = "List of All Notifications";
		descriptions[2] = "An Individual's History";
		descriptions[3] = "List of All Missed Notifications";
		descriptions[4] = "Notifications Missed by Members of a Group";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.ReportBroker#getReportNames()
	 */
	public String[] getReportNames() {
		return names;
	}

	public String getReportDescription(String name) {
		for (int i = 0; i < names.length; i++) {
			if (name.toLowerCase().equals(names[i].toLowerCase())) {
				return descriptions[i];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.ReportBroker#getReport(java.lang.String)
	 */
	public InputStream getReportStream(String name) {
		return (this.getClass().getClassLoader().getResourceAsStream("reports/"
				+ name + ".jasper"));
	}

	public JasperReport getReport(String name) {
		try {
			InputStream in = getReportStream(name);
			if (in == null) {
				BrokerFactory.getLoggingBroker().logWarn(
						"Unable to find report " + name);
				return null;
			}
			return JasperManager.loadReport(in);
		} catch (JRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
		}
	}

	public Map getParameterTypes(String name) {
		HashMap map = new HashMap();
		if (name.toLowerCase().equals("missedbygroup")) {
			int numGroups = BrokerFactory.getGroupMgmtBroker().getNumGroups();
			Group[] groups = new Group[numGroups];
			String[] groupNames = new String[numGroups];
			for (int i = 0; i < groups.length; i++) {
				groupNames[i] = groups[i].getGroupName();
			}

			BrokerFactory.getGroupMgmtBroker().getGroups(numGroups, 1, groups);
			map.put("Group Name", groupNames);
		}
		return map;
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		String[] reports = BrokerFactory.getReportBroker().getReportNames();

		for (int i = 0; i < reports.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Running report " + reports[i]);
			JasperReport report = BrokerFactory.getReportBroker().getReport(
					reports[i]);
			if (report != null) {
				JRParameter[] reportParams = report.getParameters();
				HashMap filledParams = new HashMap();
				for (int p = 0; p < reportParams.length; p++) {
					if ((reportParams[p].isForPrompting())
							&& (!reportParams[p].isSystemDefined())) {
						String question = reportParams[p].getDescription();
						BrokerFactory.getLoggingBroker().logDebug(
								"question=" + question);
						if (question == null)
							question = reportParams[p].getName();
						System.out.print(question);
						String value = new BufferedReader(
								new InputStreamReader(System.in)).readLine();
						filledParams.put(reportParams[p].getName(), value);
					}
				}
				JasperPrint print = JasperFillManager.fillReport(report,
						filledParams, BrokerFactory.getDatabaseBroker()
								.getConnection());
				JasperPrintManager.printReportToPdfFile(print, "/tmp/"
						+ reports[i] + ".pdf");
			}
		}
	}
}