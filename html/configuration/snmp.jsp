<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	int snmpPort = BrokerFactory.getConfigurationBroker().getIntValue("snmp.port", 2161);
%>
<td><table><tr>
<tr><td>SNMP Port</td>
<td><input type="text" name="snmp.port" value="<%= snmpPort %>"></td></tr>
</table></td>
