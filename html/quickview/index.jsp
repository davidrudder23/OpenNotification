
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="java.security.MessageDigest"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="java.net.URLEncoder"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/><%
	// Load the user object
	String userUuid = (String) session.getAttribute("user");
	User user = null;

	if (userUuid != null) {
		user = BrokerFactory.getUserMgmtBroker()
		.getUserByUuid(userUuid);
	}

	if (user == null) {
		response.sendRedirect("login.jsp");
	}
	
	//if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Managed")) {
	//response.sendRedirect("../index.jsp");
	//}
	

	String name = "";
	if (user != null)
		name = user.getFirstName() + " " + user.getLastName();

	Notification[] notifs = BrokerFactory.getNotificationBroker()
			.getNotificationsSentTo(user);
	int numActive = 0;
	int numConfirmed = 0;
	int numExpired = 0;
	int numOnhold = 0;

	for (int i = 0; i < notifs.length; i++) {
		int status = notifs[i].getStatus();
		switch (status) {
		case Notification.PENDING:
		case Notification.NORMAL:
			numActive++;
			break;
		case Notification.CONFIRMED:
			numConfirmed++;
		case Notification.EXPIRED:
			numExpired++;
			break;
		case Notification.ONHOLD:
			numOnhold++;
			break;
		}
	}
	
	String errorMessage = (String)session.getAttribute("system.message");
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Notify &amp; Acknowledge</title>
<link href="rr.css" rel="stylesheet" type="text/css">
<script type="text/JavaScript">
<!--
function MM_openBrWindow(theURL,winName,features) { //v2.0
     window.open(theURL,winName,features);
}
//-->

<!-- Initialization Function -->
function init() {
	//getActiveNotifs();
	initNotifs();
}

var errorTimeout;

function hideError () {
	var errorDiv = document.getElementById('errorDiv')
	errorDiv.style.display='none';
	errorDiv = null;
	
	var errorTextDiv = document.getElementById('errorText');
	errorTextDiv.innerHTML = "";
	errorTextDiv = null;
	
}

function showError(text) {

	document.getElementById('errorDiv').style.display='block';
	document.getElementById('errorText').innerHTML = 
	document.getElementById('errorText').innerHTML + text+"<br>";
	clearTimeout(errorTimeout);
	errorTimeout = setTimeout ("hideError()", 2000);
	
}
</script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="init()">
<%@ include file="ajax.jsp" %>
<%@ include file="updateNotifs.jsp" %>
<div align="center">
  <table width="300" border="0" align="center" cellpadding="0" cellspacing="0" style="width: 300px; border: thin solid #000000;">
      <tr>
        <td colspan="2" bgcolor="#1AA2E7"><img src="images/spacer.gif" width="20" height="10"></td>
      </tr>
      <tr>
        <td class="identity">
          <%= user.getFirstName() %> <%= user.getLastName() %> (<%= user.getGMTOffset() %> )<br/>
          <a href="mailto:<%= user.getEmailAddress() %>"><%= user.getEmailAddress() %></a><br>
          <a href="../LogoutServlet">LOG OFF </a></td>
        <td class="identity"><img src="images/RRlogo.gif" width="339" height="69" align="right"></td>
      </tr>
      <tr>
        <td colspan="2" background="images/bg_tabbar.gif"><img src="images/spacer.gif" width="20" height="21"></td>
      </tr>
  
<!-- <tr>
    <td width="300" align="center" valign="top" bgcolor="#19A1E6">
<%
	String visibility = "block";
	if (StringUtils.isEmpty(errorMessage)) {
		visibility = "none";
		errorMessage = "";
	}
%>
      <table width="284"  border="0" cellpadding="0" cellspacing="0" class="box" id="errorDiv" style="display: <%= visibility %>;">
        <tr>
          <td class="cellspace systemalert"><span onclick="hideError()" style="cursor: pointer;" id="errorText"><%= errorMessage %></span></td>
        </tr>
      </table>
</td></tr> -->
<tr>
  <td valign="top" class="header_notif" colspan="2"> Active Notifications </td>
      </tr>
      <tr>
        <td align="left" colspan="2">
			<div id="activeNotifs">
				<strong>Loading Active Notifications...</strong>
			</div>
		</td>
      </tr>
      <tr>
        <td valign="top" class="header_notif" colspan="2">All Recent Notifications </td>
        </tr>
      <tr>
        <td align="left" colspan="2">
			<div id="recentNotifs">
				<strong>Loading Recent Notifications...</strong>
			</div>
        </td>
      </tr>
    </table>    </td>
  </tr>
<tr width="100%" align="right">
<td colspan="25" align="right" class="copyright" style="text-align: right;">&copy; Copyright Reliable Response
      2007</td></tr>
</table>
</div>
<div style="display: none" id="debug">
</div>
</body>
</html>
