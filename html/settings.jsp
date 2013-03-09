<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	String groupSettingsTitle = request.getParameter("groupSettingsTitle");
	if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.MANAGED)) {
		response.sendRedirect("hosted/index.jsp");
	} else if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.TXTIT)) {
		response.sendRedirect("txtit/index.jsp");
	}

	if (groupSettingsTitle == null) {
		response.sendRedirect("ActionServlet?page=/settings.jsp");
		return;
	} 
%><jsp:include page="header.jsp" />

<tr><td colspan="4" class="mainarea">
<input type="hidden" name="page" value="/settings.jsp" />

<%
	String openedString = request.getParameter("opened.groupSettings");
	boolean opened =  ((openedString != null) && (openedString.toLowerCase().equals ("true")));
	if (request.getParameter("action_toggle_collapseable.groupSettings.x") != null) {
		opened = !opened;
	}
	
	if ((opened) && (1==0)){
%>
<div align="right"><a name="groups" id="groups"></a><a href="#devices" class="anchorlinks">Manage Devices &amp; Rules</a></div>
<%} else {%>
<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
<%}%>

<%
	String personalSettingsTitle = "Personal Settings";
%>
<table width="100%" cellspacing="0" border="0" cellpadding="0">
<reliable:collapseable tag="personal Settings" title="<%= personalSettingsTitle %>"
contentURL="/usermgmt/personal.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25">
<div align="right"><a href="#groups" class="anchorlinks"> &nbsp;</a></div>
<reliable:collapseable tag="group Settings" title="<%= groupSettingsTitle %>"
contentURL="/groupmgmt/index.jsp" >
</reliable:collapseable>

<tr><td colspan="25">

<%	if ((opened) && (1==0)) {
%>
<div align="right"><a name="devices" id="devices"></a><a href="#groups" class="anchorlinks">Manage
        Groups</a></div>
<%} else {%>
<div align="right"><a href="#groups" class="anchorlinks"> &nbsp;</a></div>
<%}%>      
</td>
<%
	String deviceSettingsTitle = "Device Settings</td><td align=\"right\" class=\"headercell\"></td>";
	
%>

<reliable:collapseable tag="device Settings" title="<%= deviceSettingsTitle %>"
contentURL="/devicemgmt/index.jsp" >
</reliable:collapseable>
 
<jsp:include page="footer.jsp" />