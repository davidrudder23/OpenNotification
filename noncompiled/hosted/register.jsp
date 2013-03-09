<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Reliable Response Notification System</title>
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
              <form name="registerForm" method="post" action="process/register.jsp">
                <table width="100%" border="0">
<%
	String[] messages = (String[])session.getAttribute("register_messages");
	session.setAttribute("register_messages", null);
	if (messages != null) {
		for (int m = 0; m < messages.length; m++) {
		%>
				<tr>
					<td colspan="25"><font color="#cc0000"><%= messages[m] %></font></td>
				</tr>
		<%}
	}
%>
<tr><td width="733">
<p>Notify &amp; Acknowledge is a hosted notification server for small businesses.<br/>
<a href="http://www.reliableresponse.net/docs/index.php?id=60">
For more information, visit the product documentation.</a></p>
<p>
<b>Each Account: $40/month</b>
<table><tr><td><ul>
<li>4 Users</li>
<li>Email</li>
<li>Cell Phone TXT</li>
<li>Alpha-Numeric Pager</li>
<li>Jabber IM</li>
</ul>
</td><td valign="top">
<ul>
<li>Unlimited Notifications</li>
<li>Nagios Integration</li>
<li>Realtime Interface</li>
<li>Reports</li>
</ul>
</td></tr></table>
<b>Telephone Text-To-Speech: $10/month per user</b><br/>
(available after registration, in your account settings)
</p>
<tr>
                    <td class="cellspace">
                    <p>Company name<br>  
                        <input type="text" name="addDepartment">
                        <br>
                        <br>
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
                      Coupon<br>
                      <input type="text" name="addCoupon" value="">
                      <br>
                      <br>
                      <span class="button" style="background-color: #FFFFFF;" onclick="document.registerForm.submit()">&nbsp;Sign Up&nbsp;</span>
                      <br>
                      <br>
                       Payment is through PayPal.<br>
                      You do not have to be a PayPal
                        member to purchase.<br>
                        <img src="images/paypal.gif" width="90" height="29"></p>
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
