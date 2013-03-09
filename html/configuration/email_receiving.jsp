<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.broker.ConfigurationBroker" %>
<% 
	ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
	String emailMethod = broker.getStringValue("email.method", "SMTP");
	String popSelected = emailMethod.equalsIgnoreCase("POP")?"CHECKED":"";
	String smtpSelected = emailMethod.equalsIgnoreCase("POP")?"":"CHECKED";

	String smtpServerHostname = broker.getStringValue("smtp.server.hostname", "");
	int smtpPort = broker.getIntValue("smtp.port", 2525);
	
	String popAddress = broker.getStringValue("email.pop.address", "");
	String popHostname = broker.getStringValue("email.pop.hostname", "");
	String popUsername = broker.getStringValue("email.pop.username", "");
	boolean popCheckAll = broker.getBooleanValue("email.pop.catchall", true);
	boolean popUseSSL = broker.getBooleanValue("email.pop.usessl", false);
%>
<td><input type="radio" name="email.method" value="POP" <%= popSelected %> >POP</td>
<td><input type="radio" name="email.method" value="SMTP" <%= smtpSelected %>  >SMTP</td>
</tr>

<%
if (emailMethod.equalsIgnoreCase("pop")) {
%>
<tr><td>POP Email Address: </td><td><input type="text" name="email.pop.address" value="<%= popAddress %>"></td></tr>
<tr><td>POP Server Hostname: </td><td><input type="text" name="email.pop.hostname" value="<%= popHostname %>"></td></tr>
<tr><td>POP Username: </td><td><input type="text" name="email.pop.username" value="<%= popUsername %>"></td></tr>
<tr><td>POP Password: </td><td><input type="password" name="email.pop.password"></td></tr>
<tr><td>Use SSL: </td><td><input type="checkbox" name="email.pop.usessl" <%= popUseSSL?"CHECKED":"" %>></td></tr>
<tr><td>Check To and CC fields for recipients: </td><td><input type="checkbox" name="email.pop.catchall" <%= popCheckAll?"CHECKED":"" %>></td></tr>

<%
} else {
%>
<tr><td>SMTP Hostname: </td><td><input type="text" name="smtp.server.hostname" value="<%= smtpServerHostname %>"></td></tr>
<tr><td>Port to Listen On: </td><td><input type="text" name="smtp.port" value="<%= smtpPort %>"></td></tr>
<%}%>
