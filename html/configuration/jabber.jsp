<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean jabberEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("jabber",false);
	String jabberServer = BrokerFactory.getConfigurationBroker().getStringValue("jabber.server", "");
	String jabberAccount = BrokerFactory.getConfigurationBroker().getStringValue("jabber.account", "");
%>
<td><table><tr>
<tr><td>Enable Jabber Notification</td>
<td><input type="checkbox" name="jabber" <%= jabberEnabled?"CHECKED":""%>></td></tr>
<tr><td>Jabber Server</td>
<td><input type="text" name="jabber.server" value="<%= jabberServer %>"></td></tr>
<tr><td>Jabber Username</td>
<td><input type="text" name="jabber.account" value="<%= jabberAccount %>"></td></tr>
<tr><td>Jabber Password</td>
<td><input type="password" name="jabber.password"></td></tr></table></td>
