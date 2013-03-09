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
	boolean ldapImport = getParameterValue(request, "ldap.import", "false").toLowerCase().startsWith("t");
	String checked = ldapImport?" checked":"";
	String notChecked = ldapImport?"":" checked";
	String ldapHostname = getParameterValue(request, "ldap.host", "localhost");
	String ldapUser = getParameterValue(request, "ldap.username", "Administrator");
	String ldapPassword = getParameterValue(request, "ldap.password", "");
	String searchBase = getParameterValue(request, "ldap.searchString", "ou=Users");
	String loginParams = getParameterValue(request, "ldap.login.param", "cn");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td>Enable LDAP Import</td><td>
Yes<input type="radio" name="ldap.import" value="true" <%= checked %>>
No<input type="radio" name="ldap.import" value="false" <%= notChecked %>>
</td></tr>
<tr><td>LDAP Server Hostname</td><td><input name="ldap.host" value="<%= ldapHostname %>"></td></tr>
<tr><td>LDAP Username</td><td><input name="ldap.username" value="<%= ldapUser %>"></td></tr>
<tr><td>LDAP Password</td><td><input type="password" name="ldap.password" value="<%= ldapPassword %>"></td></tr>
<tr><td>LDAP Search String</td><td><input name="ldap.searchString" value="<%= searchBase %>"></td></tr>
</table></td></tr>