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
	String smtpServer = getParameterValue(request, "smtp.server", "localhost");
%>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
<tr><td colspan="25">
Reliable Response Notification needs an SMTP server it can use to send out emails.  Usually, 
you should get this value, if you don't already know it, from your ISP or your network
administrator.  
</td></tr>
<tr><td class="abovecell" colspan="25"><table border="0" cellspacing="3" width="100%">
</td></tr>
<tr><td>SMTP Server For Delivering Mail</td><td><input name="smtp.server" value="<%= smtpServer %>"></td></tr>
</table></td></tr>
</table></td></tr>