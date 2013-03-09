<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean yahooEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("yahoo",false);
	String yahooAccount = BrokerFactory.getConfigurationBroker().getStringValue("yahoo.account", "");
%>
<tr><td>Enable Yahoo IM Notification</td>
<td><input type="checkbox" name="yahoo" <%= yahooEnabled?"CHECKED":""%>></td></tr>
<tr><td>Yahoo Username</td>
<td><input type="text" name="yahoo.account" value="<%= yahooAccount %>"></td></tr>
<tr><td>Yahoo Password</td>
<td><input type="password" name="yahoo.password"></td></tr>
