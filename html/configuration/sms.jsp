<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean smsEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("sms",false);
	String smsPort = BrokerFactory.getConfigurationBroker().getStringValue("sms.port", "/dev/ttyS0");
%>
<tr><td>Enable Cell Notification via SMS Modem</td>
<td><input type="checkbox" name="sms" <%= smsEnabled?"CHECKED":""%>></td></tr>
<tr><td>Serial Port</td>
<td><input type="text" name="sms.port" value="<%= smsPort %>"></td></tr>
