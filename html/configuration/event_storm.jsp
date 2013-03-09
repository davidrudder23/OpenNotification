<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	int maxNotifs = BrokerFactory.getConfigurationBroker().getIntValue("transmit.limit.num", 50);
	int maxTime = BrokerFactory.getConfigurationBroker().getIntValue("transmit.limit.seconds", 600);
%>
<tr><td>Maximum notifications </td>
<td colspan="2"><input type="text" name="transmit.limit.num" value="<%= maxNotifs %>" size="2"></td>
<td>per</td>
<td colspan="2"><input type="text" name="transmit.limit.seconds" value="<%= maxTime %>" size=10> seconds</td></tr>

