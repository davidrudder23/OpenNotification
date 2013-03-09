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
	String tomcatLocation = getParameterValue(request, "tomcat.location", "/opt/tomcat");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td>Location of Tomcat</td><td><input name="tomcat.location" value=<%= tomcatLocation %>></td></tr>
</table></td></tr>