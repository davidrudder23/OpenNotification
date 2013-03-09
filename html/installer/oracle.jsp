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
	String oraUser = getParameterValue(request, "database.oracle.username", "reliable");
	String oraPassword = getParameterValue(request, "database.oracle.password", "reliable");
	String oraHostname = getParameterValue(request, "database.oracle.hostname", "localhost");
	String oraSid = getParameterValue(request, "database.oracle.sid", "reliable");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td>Oracle Username</td><td><input type="text" name="database.oracle.username" value="<%= oraUser %>"></td></tr>
<tr><td>Oracle Password</td><td><input type="password" name="database.oracle.password" value="<%= oraPassword %>"></td></tr>
<tr><td>Oracle Hostname</td><td><input type="text" name="database.oracle.hostname" value="<%= oraHostname %>"></td></tr>
<tr><td>Oracle SID</td><td><input type="text" name="database.oracle.sid" value="<%= oraSid %>"></td></tr>
</table></td></tr>