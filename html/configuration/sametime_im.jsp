<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean sametimeEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("sametime",false);
	String sametimeAccount = BrokerFactory.getConfigurationBroker().getStringValue("sametime.account", "");
	String sametimeServer = BrokerFactory.getConfigurationBroker().getStringValue("sametime.server", "");
%>
<tr><td>Enable SameTime Notification</td>
<td><input type="checkbox" name="sametime" <%= sametimeEnabled?"CHECKED":""%>></td></tr>
<tr><td>SameTime Server Hostname</td>
<td><input type="text" name="sametime.server" value="<%= sametimeServer %>"></td></tr>
<tr><td>SameTime Username</td>
<td><input type="text" name="sametime.account" value="<%= sametimeAccount %>"></td></tr>
<tr><td>SameTime Password</td>
<td><input type="password" name="sametime.password"></td></tr>
