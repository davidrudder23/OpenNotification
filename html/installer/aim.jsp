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
	boolean aimEnabled = getParameterValue(request, "aim", "false").toLowerCase().startsWith("t");
	String checked = aimEnabled?" checked":"";
	String notChecked = aimEnabled?"":" checked";
	String aimUser = getParameterValue(request, "aim.account", "");
	String aimPassword = getParameterValue(request, "aim.password", "");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td>Enable AIM</td><td>
Yes<input type="radio" name="aim" value="true" <%= checked %>>
No<input type="radio" name="aim" value="false" <%= notChecked %>><tr><td>AIM Username</td><td><input name="aim.username" value="<%= aimUser %>"></td></tr>
<tr><td>AIM Password</td><td><input type="password" name="aim.password" value="<%= aimPassword %>"></td></tr>
</table></td></tr>