<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean bbEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("blackberry",false);
	String bbHost = BrokerFactory.getConfigurationBroker().getStringValue("blackberry.mdshost", "");
	String bbPort = BrokerFactory.getConfigurationBroker().getStringValue("blackberry.mdsport", "8080");
%>
<tr><td>Enable Blackberry Notification</td>
<td><input type="checkbox" name="blackberry" <%= bbEnabled?"CHECKED":""%>></td></tr>
<tr><td>Blackberry MDS Server Hostname</td>
<td><input type="text" name="blackberry.mdshost" value="<%= bbHost %>"></td></tr>
<tr><td>Blackberry MDS Port</td>
<td><input type="text" name="blackberry.mdsport" value="<%= bbPort %>"></td></tr>
