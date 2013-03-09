
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<%
String userUuid = (String) session.getAttribute("user");
User user = null;

if (userUuid != null) {
	user = BrokerFactory.getUserMgmtBroker()
	.getUserByUuid(userUuid);
}

if (user == null) {
	response.sendRedirect("/notification/login.jsp");
}

String memberUuid = request.getParameter("uuid");
if (StringUtils.isEmpty(memberUuid)) {
	// TODO: give a good error
	return;
}

User member = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
if (member == null) {
	if (StringUtils.isEmpty(memberUuid)) {
		// TODO: give a good error
		return;
	}
}

String existingLogin = BrokerFactory.getAuthenticationBroker().getIdentifierByUser(member);
if (existingLogin != null) {
	%>This member already has a login, <b><%= existingLogin %></b><%
	return;
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Reliable Response Notification - Allow Login for <%= member %></title>
<link href="rr.css" rel="stylesheet" type="text/css">
</head>
<body> <div align="center">
<table  border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="3" align="center" valign="top"><img src="images/splash_top.gif" width="677" height="5"></td>
    </tr>
  <tr>
    <td width="12" valign="top" background="images/splash_bgleft.gif"><img src="images/splash_topleft.gif" width="12" height="8"></td>
    <td width="653" align="center" valign="top" class="splash"><table  border="0" cellpadding="10" cellspacing="0">
        <tr>
          <td width="338"><p><img src="images/spacer.gif" width="338" height="30"><br>
            <img src="images/splash_logo.gif" width="338" height="75"></p>
              <form name="allowLoginForm" method="post" action="process/allowLogin.jsp">
              <input type="hidden" name="uuid" value="<%= memberUuid %>" />
                <table width="100%" border="0">
<%
	String[] messages = (String[])session.getAttribute("allowlogin_messages");
	session.setAttribute("allowlogin_messages", null);
	if (messages != null) {
		for (int m = 0; m < messages.length; m++) {
		%>
				<tr>
					<td colspan="25"><font color="#cc0000"><%= messages[m] %></font></td>
				</tr>
		<%}
	}
%>
                  <tr>
                    <td class="cellspace">
                    <p>
                      Login Name<br>
                      <input type="text" name="addLogin">
                      <br>
                      <br>
                      Password<br>
                      <input type="Password" name="addPassword">
                      <br>
                      Confirm Password<br>
                      <input type="Password" name="addConfirmPassword">
                      <br>
                      <br>
                      <span class="button" style="background-color: #FFFFFF;" onclick="document.allowLoginForm.submit()">&nbsp;Submit&nbsp;</span>
                      </td>
                  </tr>
                </table>
              </form>              <p class="titles">&nbsp;</p>            </td>
        </tr>
      </table>
        <p>&nbsp;</p>      </td>
    <td width="12" valign="top" background="images/splash_bgright.gif"><img src="images/splash_topright.gif" width="12" height="8"></td>
    </tr>
  
  <tr>
    <td colspan="3" valign="top"><img src="images/splash_bottom.gif" width="677" height="15"></td>
    </tr>
</table>
</div>
</body>
</html>
