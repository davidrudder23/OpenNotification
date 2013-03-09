<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.broker.ConfigurationBroker" %>
<% 
	ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
	String emailMethod = broker.getStringValue("email.method", "SMTP");
	String popSelected = emailMethod.equalsIgnoreCase("POP")?"CHECKED":"";
	String smtpSelected = emailMethod.equalsIgnoreCase("POP")?"":"CHECKED";
	String popDisplay = emailMethod.equalsIgnoreCase("POP")?"block":"none";
	String smtpDisplay = emailMethod.equalsIgnoreCase("SMTP")?"block":"none";

	String smtpServerHostname = broker.getStringValue("smtp.server.hostname", "");
	int smtpPort = broker.getIntValue("smtp.port", 2525);
	
	String popAddress = broker.getStringValue("email.pop.address", "");
	String popHostname = broker.getStringValue("email.pop.hostname", "");
	String popUsername = broker.getStringValue("email.pop.username", "");
	boolean popCheckAll = broker.getBooleanValue("email.pop.catchall", true);
	boolean popUseSSL = broker.getBooleanValue("email.pop.usessl", false);
%>
<script>

function showMethod(method) {
	var popdiv = document.getElementById("POP");
	var smtpdiv = document.getElementById("SMTP");
	
	if (method == "POP") {
		popdiv.style.display="block";
		smtpdiv.style.display="none";
	} else if (method == "SMTP") {
		popdiv.style.display="none";
		smtpdiv.style.display="block";
	}
}
</script>

<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td colspan="25">
<p>Reliable Response Notification requires an email address it can use to receive new 
notification requests and responses to sent notifications.  You can choose to use
a POP email box, or to have Notification run its own SMTP server.
</p>
<p>
If you use a POP email account, it must be dedicated to Notification.  <b><em>Do not 
use a POP account that you are using for your email!  Notification will delete all
the email in thos POP box!</em></b>
</p>  
</td></tr>
<tr><td><input type="radio" name="email.method" value="POP" <%= popSelected %> onclick="showMethod('POP');">POP</td>
<td><input type="radio" name="email.method" value="SMTP" <%= smtpSelected %>  onclick="showMethod('SMTP');">SMTP</td>
</tr>

<tr><td colspan="2">
<div id="POP" style="display: <%= popDisplay %>;">
<table>
<tr><td>POP Email Address: </td><td><input type="text" name="email.pop.address" value="<%= popAddress %>"></td></tr>
<tr><td>POP Server Hostname: </td><td><input type="text" name="email.pop.hostname" value="<%= popHostname %>"></td></tr>
<tr><td>POP Username: </td><td><input type="text" name="email.pop.username" value="<%= popUsername %>"></td></tr>
<tr><td>POP Password: </td><td><input type="password" name="email.pop.password"></td></tr>
<tr><td>Use SSL: </td><td><input type="checkbox" name="email.pop.usessl" <%= popUseSSL?"CHECKED":"" %>></td></tr>
<input type="hidden" name="email.pop.catchall" value="false">
</table>
</div>
</td></tr>
<tr><td colspan="2">
<div id="SMTP" style="display: <%= smtpDisplay %>">
<table>
<tr><td>SMTP Hostname: </td><td><input type="text" name="smtp.server.hostname" value="<%= smtpServerHostname %>"></td></tr>
<tr><td>Port to Listen On: </td><td><input type="text" name="smtp.port" value="<%= smtpPort %>"></td></tr>
</table>
</div>
</td></tr>
</table></td></tr>