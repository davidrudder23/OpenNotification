<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.util.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	/*String pageName = request.getParameter("page");
	if ((pageName == null) || (pageName.equals(""))) {
		response.sendRedirect("ActionServlet?page=/installer.jsp");
		return;
	} */
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
<%

	String incomingTitle = "Incoming Mail Configuration";
	String incomingMessage = request.getParameter("email_receiving_system_error");
	if (!StringUtils.isEmpty(incomingMessage)) {
		incomingTitle += "<br><span class=\"systemalert\">"+incomingMessage+"</span>";
	}

%>

<input type="hidden" name="page" value="/installer.jsp" />

<tr><td colspan="4" class="mainarea">

<div align="right"><a name="current"></a><a href="#sendNotification" class="anchorlinks">&nbsp</a></div>
<table width="100%" cellspacing="0" border="0" cellpadding="0">

<tr><td colspan="25">&nbsp;</td></tr>
<reliable:collapseable tag="sendingingConfig" title="Outgoing SMTP Configuration"
contentURL="/installer/email_sending.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25"> &nbsp;</td></tr>

<reliable:collapseable tag="receivingConfig" title="<%= incomingTitle %>"
contentURL="/installer/email_receiving.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25" align="left">&nbsp;</td></tr>
<tr><td colspan="25" align="left">
<input type="image" src="images/btn_save.gif" name="action_save_config">
<img src="images/spacer.gif">
<input type="image" src="images/btn_cancel.gif" name="action_cancel_config"></td></tr>
<jsp:include page="footer.jsp" />