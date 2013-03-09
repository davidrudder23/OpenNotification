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
<link href="../stylesheet.css" rel="stylesheet" type="text/css">
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

<input type="image" src="../images/hidden.gif" name="action_mispress">
<input type="hidden" name="action" value="unset">
<script type="text/javascript" language="JavaScript">
function setAction(action) {
   document.mainform.action.value = action;
}
</script>


<table width="780" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="5" bgcolor="#999999">
    <a name="top"></a><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td><img src="../images/spacer.gif" width="3" height="3"></td>
    <td valign="center" align="left"><img src="../images/spacer.gif" width="13" height="69"></td>
    <td class="identity"><p><strong>Welcome</strong><br>
      <%= name %><br>
      <a href="LogoutServlet">LOG OFF</a>
      <br>
    </p>
    </td>
    <td valign="center" align="right"><img src="../images/RRlogo.gif" width="339" height="69"></td>
    <td bgcolor="#999999"><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="4" bgcolor="#999999"><b>On Call Groups</b></td>
  </tr>
<%
	Group[] groups = new Group[BrokerFactory.getGroupMgmtBroker().getNumGroups()];
	BrokerFactory.getGroupMgmtBroker().getGroups(groups.length, 0, groups);
	boolean doColor = false;
	for (int i = 0; i < groups.length; i++) {
		boolean printedName = false;
		doColor = !doColor;
		Group thisGroup = groups[i];
		if (thisGroup instanceof OnCallGroup) {
			OnCallGroup onCallGroup = (OnCallGroup)thisGroup;
			Member[] members = onCallGroup.getOnCallMembers(new Date());
			
			for (int m = 0; m < members.length; m++) {
				Member member = members[m];
%>
	<tr bgcolor="<%= doColor?"#CCCCCC":"#FFFFFF" %>"><td><img src="../images/spacer.gif" width="13" height="3"></td>
	<td><b><%= printedName?"&nbsp;":onCallGroup.toString()%></b></td>
	<td><%= member.toString()%></td>
	<td>&nbsp;</td>
	</tr>
<%
				if (members[m] instanceof Group) {
					Group group = (Group)member;
					Member[] children = group.getMembers();
					for (int c = 0; c < children.length; c++) { %>
						<tr bgcolor="<%= doColor?"#CCCCCC":"#FFFFFF" %>"><td><img src="../images/spacer.gif" width="13" height="3"></td>
						<td>&nbsp;</td>
						<td><i>&nbsp;&nbsp;&nbsp;<%= children[c].toString()%></i></td>
						<td>&nbsp;</td>
						</tr>
					<%}
				}
				printedName = true;
			}
		}
	}


	// Escalation Groups
	%>  
  <tr>
    <td colspan="5" bgcolor="#FFFFFF">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="4" bgcolor="#999999"><b>Escalation Groups</b></td>
  </tr>
  <% 
	doColor = false;
	for (int i = 0; i < groups.length; i++) {
		boolean printedName = false;
		doColor = !doColor;
		Group thisGroup = groups[i];
		if (thisGroup instanceof EscalationGroup) {
			EscalationGroup escGroup = (EscalationGroup)thisGroup;
			Member[] members = escGroup.getMembers();
			
			for (int m = 0; m < members.length; m++) {
				Member member = members[m];
%>
	<tr bgcolor="<%= doColor?"#CCCCCC":"#FFFFFF" %>"><td><img src="../images/spacer.gif" width="13" height="3"></td>
	<td><b><%= printedName?"&nbsp;":escGroup.toString()%></b></td>
	<td><%= member.toString()%></td>
	<td>&nbsp;</td>
	</tr>
<%
				if (members[m] instanceof Group) {
					Group group = (Group)member;
					Member[] children = group.getMembers();
					for (int c = 0; c < children.length; c++) { %>
						<tr bgcolor="<%= doColor?"#CCCCCC":"#FFFFFF" %>"><td><img src="../images/spacer.gif" width="13" height="3"></td>
						<td>&nbsp;</td>
						<td><i>&nbsp;&nbsp;&nbsp;<%= children[c].toString()%></i></td>
						<td>&nbsp;</td>
						</tr>
					<%}
				}
				printedName = true;
			}
		}
	}
%>
<%
	int year = new Date().getYear()+1900;
%>
 <tr bgcolor="#999999">
    <td colspan="5"><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td><img src="../images/spacer.gif" width="3" height="3"></td>
    <td align="right" class="copyright" colspan="3"><a href="copyright.html">&copy; Copyright <%= year %> </a>Reliable Response, LLC. All rights reserved.  
    <img src="../images/spacer.gif" width="3" height="3"><img src="../images/spacer.gif" width="5" height="10"></td>
    <td align="right" bgcolor="#999999"><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="../images/spacer.gif" width="3" height="3"></td>
  </tr>
  </table>
</form>
</body>
</html>
