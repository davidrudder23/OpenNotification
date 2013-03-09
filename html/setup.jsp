<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.util.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	String pageName = request.getParameter("page");
	if ((pageName == null) || (pageName.equals(""))) {
		response.sendRedirect("ActionServlet?page=/installer.jsp");
		return;
	} 
%><jsp:include page="header.jsp" />

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


	public String getEnabled (HttpServletRequest request, String name) {
		String enabled = getParameterValue (request, name, "false");
		if ((enabled != null) && (enabled.toLowerCase().startsWith("t"))) {
			return "<font color=\"#229922\">(enabled)</font>";
		} else {
			return "<font color=\"#992222\">(disabled)</font>";
		}
	}
%>

<input type="hidden" name="page" value="/installer.jsp" />

<tr><td colspan="4" class="mainarea">

<div align="right"><a name="current"></a><a href="#sendNotification" class="anchorlinks">&nbsp</a></div>
<table width="100%" cellspacing="0" border="0" cellpadding="0">

<reliable:collapseable tag="tomcatConfig" title="Tomcat Configuration"
contentURL="/installer/tomcat.jsp" opened="true">
</reliable:collapseable>
<tr><td colspan="25">&nbsp;</td></tr>
<reliable:collapseable tag="smtpConfig" title="SMTP Configuration"
contentURL="/installer/smtp.jsp" opened="true">
</reliable:collapseable>
<tr><td colspan="25">&nbsp;</td></tr>
<%
	String ldapTitle = "LDAP Configuration " + getEnabled(request, "ldap.import");
%>
	<reliable:collapseable tag="ldapConfig" title="<%= ldapTitle %>"
contentURL="/installer/ldap.jsp" opened="true">
</reliable:collapseable>
<tr><td colspan="25">&nbsp;</td></tr>
<%
	String databaseTitle = "Database </td>";
	
	String databaseName = getParameterValue(request, "broker.impl", "oracle");
	String dbPageName = "installer/oracle.jsp";

	databaseTitle += "<td class=\"headercell\"><font color=\"#666666\">Oracle<input type=\"radio\" name=\"broker.impl\" value=\"oracle\" onclick=\"this.mainform.submit();\"";
	if (databaseName.equals ("oracle")) {
		dbPageName = "/installer/oracle.jsp";
		databaseTitle += " checked";
	}
	databaseTitle +="></font></td>";
		
	databaseTitle += "<td class=\"headercell\"><font color=\"#666666\">PostgreSQL<input type=\"radio\" name=\"broker.impl\" value=\"postgresql\" onclick=\"this.mainform.submit();\"";
	if (databaseName.equals ("postgresql")) {
		dbPageName = "/installer/postgresql.jsp";
		databaseTitle += " checked";
	}
	databaseTitle +="></font></td>";

	databaseTitle += "<td class=\"headercell\"><font color=\"#666666\">MS SQL Server<input type=\"radio\" name=\"broker.impl\" value=\"mssql\" onclick=\"this.mainform.submit();\"";
	if (databaseName.equals ("mssql")) {
		dbPageName = "/installer/mssql.jsp";
		databaseTitle += " checked";
	}
	databaseTitle +="></font></td>";

	databaseTitle += "<td class=\"headercell\"><font color=\"#666666\">MySQL<input type=\"radio\" name=\"broker.impl\" value=\"mysql\" onclick=\"this.mainform.submit();\"";
	if (databaseName.equals ("mysql")) {
		dbPageName = "/installer/mysql.jsp";
		databaseTitle += " checked";
	}
	databaseTitle +="></font>";

%>
<tr><td colspan="25">&nbsp;</td></tr>
<reliable:collapseable tag="databaseConfig" title="<%= databaseTitle %>"
contentURL="<%= dbPageName %>" opened="true">
</reliable:collapseable>

	
<tr><td colspan="25">&nbsp;</td></tr>
<%
	String aimTitle = "AIM Configuration " + getEnabled(request, "aim");
%>
<reliable:collapseable tag="aimConfig" title="<%= aimTitle %>"
contentURL="/installer/aim.jsp" opened="true">
</reliable:collapseable>
<tr><td colspan="25" align="left">&nbsp;</td></tr>
<tr><td colspan="25" align="left">
<input type="image" src="images/btn_save.gif" name="action_save_config">
<img src="images/spacer.gif">
<input type="image" src="images/btn_cancel.gif" name="action_cancel_config"></td></tr>
<jsp:include page="footer.jsp" />