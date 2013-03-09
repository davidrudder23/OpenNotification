<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean msnEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("msn",false);
	String msnAccount = BrokerFactory.getConfigurationBroker().getStringValue("msn.account", "");
%>
<tr><td>Enable MSN Notification</td>
<td><input type="checkbox" name="msn" <%= msnEnabled?"CHECKED":""%>></td></tr>
<tr><td>MSN Username</td>
<td><input type="text" name="msn.account" value="<%= msnAccount %>"></td></tr>
<tr><td>MSN Password</td>
<td><input type="password" name="msn.password"></td></tr>
