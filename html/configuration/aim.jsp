<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean aimEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("aim",false);
	String aimAccount = BrokerFactory.getConfigurationBroker().getStringValue("aim.account", "");
%>
<tr><td>Enable AIM Notification</td>
<td><input type="checkbox" name="aim" <%= aimEnabled?"CHECKED":""%>></td></tr>
<tr><td>AIM Username</td>
<td><input type="text" name="aim.account" value="<%= aimAccount %>"></td></tr>
<tr><td>AIM Password</td>
<td><input type="password" name="aim.password"></td></tr>
