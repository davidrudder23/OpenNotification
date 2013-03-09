<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	String addUserTitle = request.getParameter("addUserTitle");
	String editUserTitle = request.getParameter("editUserTitle");
	String editRoleTitle = request.getParameter("editRoleTitle");
	if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.MANAGED)) {
		response.sendRedirect("hosted/index.jsp");
	} else if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.TXTIT)) {
		response.sendRedirect("txtit/index.jsp");
	}
	if (addUserTitle == null) {
		response.sendRedirect("ActionServlet?page=/administration.jsp");
		return;
	} 

%><jsp:include page="header.jsp" />
<tr><td colspan="4" class="mainarea">
<input type="hidden" name="page" value="/administration.jsp" />

<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>

<table width="100%" cellspacing="0" border="0" cellpadding="0">
<reliable:collapseable tag="add User" title="<%= addUserTitle %>"
contentURL="/usermgmt/addnew.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25">
<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
</td>

<table width="100%" cellspacing="0" border="0" cellpadding="0">
<reliable:collapseable tag="edit User" title="<%= editUserTitle %>"
contentURL="/usermgmt/edituser.jsp" opened="false">
</reliable:collapseable>

<tr><td colspan="25">
<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
</td>

<reliable:collapseable tag="jobs" title="Manage Jobs</td>"
contentURL="/jobs.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25">
<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
</td>

<%
	String configurator = request.getParameter("configurator");
	if (configurator == null) {
		configurator = "backup_configuration";
	}
	
	String[] configurators = {
		"Logging", 
		"Database", 
		"LDAP", 
		"Event Storm", 
		"Freebusy Calendaring",
		"Blackberry", 
		"Jabber", 
		"AIM", 
		"Yahoo IM",
		"MSN Messenger",
		"SameTime IM",
		"Twitter",
		"SNMP", 
		"Syslog",
		"VoiceShot",
		"Email Receiving",
		"Email Sending",
		"Clustering"
	};
		
	String configurationTitle = "Configuration</td><td align=\"right\" class=\"headercell\">"+
	"<SELECT NAME=\"configurator\" onchange=\"submit()\">\n";
	
	for (int i = 0; i < configurators.length; i++) {
		String pageName = configurators[i].toLowerCase().replaceAll(" ", "_");
		String selected = configurator.equals (pageName)?"SELECTED":"";
		configurationTitle += "<OPTION value=\""+pageName+"\" "+selected+">"+configurators[i]+"\n";
	}
	configurationTitle += "</SELECT></td>";
%>

<reliable:collapseable tag="configuration" title="<%= configurationTitle %>"
contentURL="/configuration/index.jsp" opened="false">
</reliable:collapseable>

<jsp:include page="footer.jsp" />