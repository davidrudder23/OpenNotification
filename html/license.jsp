<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.license.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%
// We have to handle the processing in the JSP because license checking 
// won't allow it to be handled normally

String licenseString = (String)request.getParameter("license");
BrokerFactory.getLoggingBroker().logDebug("license="+licenseString);
if (!StringUtils.isEmpty(licenseString)) {
	LicenseFile licenseFile = LicenseFile.getInstance();
	try {
		licenseFile.save(licenseString);
		response.sendRedirect("index.jsp");
		return;
	} catch (Exception anyExc) {
		BrokerFactory.getLoggingBroker().logWarn(anyExc);
	}
}
%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Reliable Response Notification</title>
<link href="stylesheet.css" rel="stylesheet" type="text/css">
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="mainform" action="license.jsp" method="POST">

<input type="image" src="images/hidden.gif" name="action_mispress">
<input type="hidden" name="action" value="unset">
<script type="text/javascript" language="JavaScript">
function setAction(action) {
   document.mainform.action.value = action;
}
</script>


<table width="780" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="5" bgcolor="#999999">
    <a name="top"></a><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td><img src="images/spacer.gif" width="3" height="3"></td>
    <td valign="center" align="left"><img src="images/spacer.gif" width="13" height="69"></td>
    
    <td class="identity"><p><strong>Invalid License</strong><br>
    </p>
    </td>
    <td valign="center" align="right"><img src="images/RRlogo.gif" width="339" height="69"></td>
    <td bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td rowspan="2" bgcolor="#999999">&nbsp;</td>
    <td colspan="3" background="images/bg_tabbar.gif" bgcolor="#1AA2E7"><%
    	String pageName = "/license.jsp";
	%></td>
    <td align="right" background="images/bg_tabbar.gif" bgcolor="#1AA2E7"><img src="images/tab_endcap.gif" width="2" height="32"></td>
  </tr>
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
%>

<!-- <input type="image" src="images/refresh.gif" name="action_refresh">-->
<tr><td colspan="4" class="mainarea">
<div align="right"><a name="current"></a><a href="#sendNotification" class="anchorlinks">&nbsp</a></div>
<table width="100%" cellspacing="0" border="0" cellpadding="0">

<!-- start content -->
<tr width="100%"><td width="100%">
<%
	String errorMessage = (String)session.getAttribute("errorMessage");
	session.setAttribute("errorMessage", null);
	
	if (StringUtils.isEmpty(errorMessage)) {
		errorMessage = "I'm sorry, but your license has run out.  Please contact "+ 
			"Reliable Response to talk to a sales representative.";
	}
%>
<p>
<%= errorMessage %>
</p>
<p>
If you have a license, please copy the contents of the file into the text box below and hit "save'. 
</p>
<p>
<input type="hidden" name="page" value="/license.jsp" />
<textarea rows="10" cols="40" name="license"></textarea>
<input type="submit" value="save">
</p>
</td></tr>
<jsp:include page="footer.jsp" />
