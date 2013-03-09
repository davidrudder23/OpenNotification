<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.*" %>
<%@ page import="java.net.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<%!
public String getParameterValue (HttpServletRequest request, String name, String defaultValue) {
	String value = request.getParameter(name);
	if ((value == null) ||  (value.equals(""))) {
		value = BrokerFactory.getConfigurationBroker().getStringValue(name);
	}
	
	if ((value == null) ||  (value.equals(""))) {
		value = defaultValue;
	}
	
	if (value == null) {
		value = "";
	}
	
	return value;
}
%>
<%
	// Set up the defaults or previously-supplied values
	String pgUser = getParameterValue(request, "database.postgresql.username", "reliable");
	String pgPassword = getParameterValue(request, "database.postgresql.password", "reliable");
	String pgHostname = getParameterValue(request, "database.postgresql.hostname", "localhost");
	String pgDatabase = getParameterValue(request, "database.postgresql.database", "reliable");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td>PostgreSQL Username</td><td><input type="text" name="database.postgresql.username" value="<%= pgUser %>"></td></tr>
<tr><td>PostgreSQL Password</td><td><input type="password" name="database.postgresql.password" value="<%= pgPassword %>"></td></tr>
<tr><td>PostgreSQL Hostname</td><td><input type="text" name="database.postgresql.hostname" value="<%= pgHostname %>"></td></tr>
<tr><td>PostgreSQL Database</td><td><input type="text" name="database.postgresql.database" value="<%= pgDatabase %>"></td></tr>
</table></td></tr>