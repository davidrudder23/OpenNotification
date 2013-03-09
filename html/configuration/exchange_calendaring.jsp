<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean exchangeEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("calendar.exchange",false);
	String hostname = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.hostname", "");
	int port = BrokerFactory.getConfigurationBroker().getIntValue("calendar.exchange.port", 80);
	String user = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.username", "");
	String password = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.password", "");
	String domain = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.domain", "");
%>
<tr><td>Enable Exchange Calendaring</td>
<td><input type="checkbox" name="calendar.exchange" <%= exchangeEnabled?"CHECKED":""%>></td></tr>
<tr><td>Exchange Hostname</td>
<td><input type="text" name="calendar.exchange.hostname" value="<%= hostname %>"></td></tr>
<tr><td>Exchange Port</td>
<td><input type="text" name="calendar.exchange.port" value="<%= port %>"></td></tr>
<tr><td>Exchange Username</td>
<td><input type="text" name="calendar.exchange.username" value="<%= user %>"></td></tr>
<tr><td>Exchange Password</td>
<td><input type="password" name="calendar.exchange.password"></td></tr>
<tr><td>Exchange Domain</td>
<td><input type="text" name="calendar.exchange.domain" value="<%= domain %>"></td></tr>