<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.broker.NotificationLoggingBroker" %>
<%@ page import="net.reliableresponse.notification.broker.impl.SyslogNotificationLoggingBroker" %>
<%
	boolean syslogEnabled = BrokerFactory.getNotificationLoggingBroker() instanceof SyslogNotificationLoggingBroker;
	String syslogHost = BrokerFactory.getConfigurationBroker().getStringValue("syslog.host", "");
	int syslogPort = BrokerFactory.getConfigurationBroker().getIntValue("syslog.port", 514);
%>
<tr><td>Output Notification Logs to Syslog?</td>
<td><input type="checkbox" name="syslog" <%= syslogEnabled?"CHECKED":"" %>></td></tr>
<tr><td>Syslog Collector's Hostname</td>
<td><input type="text" name="syslog.host" value="<%= syslogHost %>"></td></tr>
<tr><td>Syslog Collector's Port</td>
<td><input type="text" name="syslog.port" value="<%= syslogPort %>"></td></tr>
