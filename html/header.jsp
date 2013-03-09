<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Reliable Response Notification</title>
<link href="stylesheet.css" rel="stylesheet" type="text/css">
</head>

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
    
    <%
    	String userUuid = (String)session.getAttribute("user");
    	User user = null;
    	
    	if (userUuid != null) {
	    	user = BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid);
	    }
    	String name = "";
	if (user != null) name = user.getFirstName()+" "+user.getLastName();
    %>
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
  <tr>
    <td rowspan="2" bgcolor="#999999">&nbsp;</td>
    <td colspan="3" background="images/bg_tabbar.gif" bgcolor="#1AA2E7"><%
    	String pageName = request.getParameter("page");
    	if (pageName == null) pageName = "/index.jsp";
    	if (pageName.equals("/index.jsp")) { 
		    %><img src="images/tab_notificationsA.gif" width="244" height="32" alt="My Notifications"><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/settings.jsp"><img src="images/tab_settingsB.gif" alt="Settings" width="158" height="32" border="0"></a><%
		    if ((user != null) && (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR))) {
			    %><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/administration.jsp"><img src="images/tab_administrationB.gif" alt="Administration" width="186" height="32" border="0"></a><%
		    }
		} else if (pageName.equals("/settings.jsp")) {
			%><img src="images/tab_settingsA.gif" alt="My Settings" width="244" height="32"><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/index.jsp"><img src="images/tab_notificationsB.gif" alt="My Notifications" width="202" height="32" border="0"></a><%
		    if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
				%><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/administration.jsp"><img src="images/tab_administrationB.gif" alt="Administration" width="186" height="32" border="0"></a><%
			}
	    } else if (pageName.equals("/administration.jsp")) {
		    %><img src="images/tab_administrationA.gif" width="244" height="32"><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/index.jsp"><img src="images/tab_notificationsB.gif" alt="My Notifications" width="202" height="32" border="0"></a><img src="images/spacer.gif" width="20" height="32"><a href="ActionServlet?page=/settings.jsp"><img src="images/tab_settingsB.gif"  alt="Settings" width="158" height="32" border="0"></a><%
	    }
	%></td>
    <td align="right" background="images/bg_tabbar.gif" bgcolor="#1AA2E7"><img src="images/tab_endcap.gif" width="2" height="32"></td>
  </tr>
