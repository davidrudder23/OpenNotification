<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	String smtpServer = BrokerFactory.getConfigurationBroker().getStringValue("smtp.server", "");
	boolean showAttachments  = BrokerFactory.getConfigurationBroker().getBooleanValue("show.attachments.email", true);
	boolean includeAttachments  = BrokerFactory.getConfigurationBroker().getBooleanValue("email.attachments.attach", true);
%>
<td><table><tr>
<tr><td>Outgoing SMTP Server Hostname</td>
<td><input type="text" name="smtp.server" value="<%= smtpServer %>"></td></tr>
<tr><td>Show Link to Attachments</td>
<td><input type="checkbox" name="showattachments" <%= showAttachments?"CHECKED":""%>></td></tr>
<tr><td>Include Attachments in Email</td>
<td><input type="checkbox" name="includeattachments" <%= includeAttachments?"CHECKED":""%>></td></tr>
</table></td>
