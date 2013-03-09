
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="java.security.MessageDigest"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="java.net.URLEncoder"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/>
<jsp:directive.page import="net.reliableresponse.notification.util.PaypalUtil"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Account"/><%
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
	Account account = BrokerFactory.getAccountBroker().getUsersAccount(user);
	if (!account.isAuthorized()) {
		session.setAttribute("user", null);
		
	%>
	Your payment has not been received yet <br>
	<a href="login.jsp">Login as another user</a><br>
	<meta http-equiv="refresh" content="15;url=login.jsp">
	<%
		return;
	}

	// Load the group
	Group group = null;
	Group[] allGroups = BrokerFactory.getGroupMgmtBroker()
			.getGroupsOfMember(user);
	BrokerFactory.getLoggingBroker()
			.logDebug("all groups=" + allGroups);
	if (allGroups != null) {
		BrokerFactory.getLoggingBroker().logDebug(
		"all groups length=" + allGroups.length);
		for (int i = 0; i < allGroups.length; i++) {
			if (allGroups[i].isOwner(user, false)) {
		group = allGroups[i];
			}
		}
	}

	if (group == null) {
		group = new EscalationGroup();
		group.setAutocommit(false);
		group.setGroupName(name + "'s Group");
		group.addMember(user, -1);
		group.setOwner(0);
		group.setAutocommit(true);
		BrokerFactory.getGroupMgmtBroker().addEscalationGroup(
		(EscalationGroup) group);
	}

	Notification[] notifs = BrokerFactory.getNotificationBroker()
			.getNotificationsSentTo(group);
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
	
	Member[] deviceMembers = group.getMembers();
	boolean hasDevice = false;
	for (int memNum = 0; memNum < deviceMembers.length; memNum++) {
		if (deviceMembers[memNum] instanceof User) {
			Device[] devices = ((User)deviceMembers[memNum]).getDevices();
			if ((devices != null) && (devices.length>0)) {
				hasDevice = true;
			}
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
	<%
		if (!hasDevice) {
			%>showError ("To receive notifications, please\n"+
						"configure an email address by\n"+
						"clicking on a user's name in the \n"+
						"Group Members Settings below.");
			clearTimeout(errorTimeout);
			errorTimeout = setTimeout ("hideError()", 1200000);
			<%
		}
	%>
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
<%@ include file="include/ajax.jsp" %>
<%@ include file="include/updateNotifs.jsp" %>
<div align="center">
  <table width="780" border="0" align="center" cellpadding="0" cellspacing="0" style="width: 780px; border: thin solid #000000;">
  
      
  
      <tr>
        <td colspan="2" bgcolor="#1AA2E7"><img src="images/spacer.gif" width="20" height="10"></td>
      </tr>
      <tr>
        <td class="identity">
          <%= user.getFirstName() %> <%= user.getLastName() %> (<%= user.getGMTOffset() %> )<br/>
          <a href="mailto:<%= group.getEmailAddress() %>"><%= group.getEmailAddress() %></a><br>
          <a href="../LogoutServlet">LOG OFF </a></td>
        <td class="identity"><a href="http://www.reliableresponse.net"><img src="images/RRlogo.gif" width="339" height="69" align="right" border="0"></a></td>
      </tr>
      <tr>
        <td colspan="2" background="images/bg_tabbar.gif"><img src="images/spacer.gif" width="20" height="21"></td>
      </tr>
  
  <tr>
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
	  <%@ include file="include/sendnewnotif.jsp" %>
	  <%@ include file="include/members.jsp" %>
</td>
    <td valign="top" rowspan="2"><table width="100%"  border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td valign="top" class="header_notif"> Active Notifications </td>
      </tr>
      <tr>
        <td align="left">
			<div id="activeNotifs">
				<strong>Loading Active Notifications...</strong>
			</div>
		</td>
      </tr>
    </table>
      <br>
      <table width="100%"  border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td valign="top" class="header_notif">All Recent Notifications </td>
        </tr>
      <tr>
        <td align="left">
			<div id="recentNotifs">
				<strong>Loading Recent Notifications...</strong>
			</div>
        </td>
      </tr>
    </table>    </td>
  </tr>
    <tr>
<td bgcolor="#19A1E6">
<table bgcolor="#19A1E6" cellspacing="2" cellpadding="0" width="100%" border="0">
<tr>
<td colspan="25" align="right" bgcolor="#19A1E6" class="copyright" style="text-align: right;">&copy; Copyright Reliable Response
      2007</td></tr>
<tr height="12">
    <td>&nbsp;</td>
    <td bgcolor="#19A1E6" class="button"><a href="http://www.reliableresponse.net/docs/index.php?id=60" target="manual">&nbsp;Manual&nbsp;</a></td>
    <td bgcolor="#19A1E6" class="button"><a href="/jforum" target="forums">&nbsp;Forums&nbsp;</a></td>
    <td bgcolor="#19A1E6" class="button"><a href="mailto:support@reliableresponse.net">&nbsp;Contact&nbsp;</a></td>
    <td bgcolor="#19A1E6" class="button"><a href="#" onClick="MM_openBrWindow('account.jsp','pop','scrollbars=yes,width=450,height=500')">&nbsp;Account&nbsp;</a></td>
    <td bgcolor="#19A1E6" class="button"><div onclick="MM_openBrWindow('credits.jsp','pop','scrollbars=yes,resizable=yes,width=400,height=600')">&nbsp;Credits&nbsp;</div></td>
    <td bgcolor="#19A1E6">&nbsp;</td>
</tr></table></td></tr></table>
</div>
<div style="display: none" id="debug">
</div>
</body>
</html>
