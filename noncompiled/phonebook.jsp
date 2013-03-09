<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/>
<jsp:directive.page import="java.util.Date"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Reliable Response Notification</title>
<link href="stylesheet.css" rel="stylesheet" type="text/css">
</head>
    <%
    	String userUuid = (String)session.getAttribute("user");
    	User user = null;
    	
    	if (userUuid != null) {
	    	user = BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid);
	    }
    	String name = "";
	if (user != null) name = user.getFirstName()+" "+user.getLastName();
    %>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="mainform" action="ActionServlet" method="POST">

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
    <td class="identity"><p><strong>Welcome</strong><br>
      <%= name %><br>
      <a href="LogoutServlet">LOG OFF</a>
      <br>
    </p>
    </td>
    <td valign="center" align="right"><img src="images/RRlogo.gif" width="339" height="69"></td>
    <td bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
<%
	User[] allUsers = new User[BrokerFactory.getUserMgmtBroker().getNumUsers()];
	int numUsers = BrokerFactory.getUserMgmtBroker().getUsers(allUsers.length, 0, allUsers);
	boolean doColor = false;
	for (int i = 0; i < numUsers; i++) {
		boolean printedName = false;
		doColor = !doColor;
		User thisUser = allUsers[i];
		Device[] devices = thisUser.getDevices();
		for (int d = 0; d < devices.length; d++) {
%>
	<tr bgcolor="<%= doColor?"#CCCCCC":"#FFFFFF" %>"><td><img src="images/spacer.gif" width="13" height="3"></td>
	<td><%= printedName?"&nbsp;":thisUser.toString()%></td>
	<td><%= devices[d].toString() %></td>
	<td>&nbsp;</td>
	</tr>
<%
			printedName = true;
		}
	}
%>
<%
	int year = new Date().getYear()+1900;
%>
 <tr bgcolor="#999999">
    <td colspan="5"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td><img src="images/spacer.gif" width="3" height="3"></td>
    <td>
    <td align="right" class="copyright" colspan="3"><a href="copyright.html">&copy; Copyright <%= year %> </a>Reliable Response, LLC. All rights reserved.  
    <img src="images/spacer.gif" width="3" height="3"><img src="images/spacer.gif" width="5" height="10"></td>
    <td align="right" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  </table>
</form>
</body>
</html>
