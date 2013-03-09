<%
	String error = (String)session.getAttribute("error");
	session.setAttribute("error", null);
	if (error == null) error = "";
%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<html>
<head>
	<title>TxtIt :: Change Password</title>
	<style type="text/css">{}
		body{font: 14px Helvetica; color: #000;}
		div#button{position:absolute;WIDTH:153px; HEIGHT:22; text-align:center; color:#FFF; font: 12px Helvetica;text-decoration:none; font-weight:bold;}
		a#button{text-decoration:none;color:#333; font: 12px Helvetica;font-weight:normal;}
		textarea{border:0px;background:#E9EFF5;color:#333;overflow:hidden;font: 14px Helvetica;}
		input{border:1px solid #CCCCCC;color:#383;font: 12px Helvetica;}
	</style>
	<!-- javascript functions -->
	<script type="text/javascript" src="images/jquery.js"></script>
	<script type="text/javascript" src="images/thickbox.js"></script>
	<link rel="stylesheet" href="images/thickbox.css" type="text/css" media="screen" />

	<!-- javascript functions -->
	<script type="text/javascript">
	function onLoadProc() {
		<% if (!StringUtils.isEmpty(error)) { %>
		tb_show("", "#TB_inline?TB_inline=true&inlineId=alertBox&height=180&width=300", null);
		setTimeout("tb_remove();", 5000);
		<% } %>
	}
	</script>

	<script type="text/javascript">
	function blue2red(image){
		image.src="images/blank_active.jpg"
	}
	function red2blue(image){
		image.src="images/blank.jpg"
	}
	</script>
</head>

<body onload="onLoadProc();">
<!-- Start Header -->
<center>
<div style="position:relative;width:1024;">
	<center><img src=images/logo-small.jpg></center>
	<!-- Send Message Button -->
	<a id="button" href="index.jsp" onMouseOver="blue2red(document.sm_img)" onMouseOut="red2blue(document.sm_img)">
		<img name="sm_img" style="position:absolute; top:57px; left:0px" border="0" src="images/blank.jpg">
		<div id="button" name="send" style="TOP:71px; LEFT:0px;">SEND MESSAGE</div>
	</a>
	<!-- Message History Button -->
	<a id="button" href="message_history.jsp" onMouseOver="blue2red(document.mh_img)" onMouseOut="red2blue(document.mh_img)">
		<img name="mh_img" style="position:absolute; top:57px; left:151px" border="0" src="images/blank.jpg">
		<div id="button" name="mHistory" style="TOP:71px; LEFT:151px;">MESSAGE HISTORY</div>
	</a>
	<!-- Manage Group Button -->
	<a id="button" href="manage_group.jsp" onMouseOver="blue2red(document.mg_img)" onMouseOut="red2blue(document.mg_img)">
		<img name="mg_img" style="position:absolute; top:57px; left:303px" border="0" src="images/blank.jpg">
		<div id="button" name="changePW" style="TOP:71px; left:303px;">MANAGE GROUP</div>
	</a>
	<!-- Change Password Button -->
	<!-- <a id="button" href="#" onMouseOver="blue2red(document.cp_img)" onMouseOut="red2blue(document.cp_img)"> -->
		<img name="cp_img" style="position:absolute; top:57px; right:151px" border="0" src="images/blank_active.jpg">
		<div id="button" name="changePW" style="TOP:71px; right:151px;">CHANGE PASSWORD</div>
	<!-- </a> -->
	<!-- Logout -->
	<a id="button" href="process/logout.jsp" onMouseOver="blue2red(document.lo_img)" onMouseOut="red2blue(document.lo_img)">
		<img name="lo_img" style="position:absolute; top:57px; right:0px" border="0" src="images/blank.jpg">
		<div id="button" name="logout" style="TOP:71px; right:0px;">LOGOUT</div>
	</a>
	<div style="position:absolute;top:89px; left:0px; width:100%; height:2px; background:#000" >&nbsp;</div>
	<div style="position:absolute;top:91px; left:0px; width:100%; padding:3px 0px 0px 0px; background:#E9EFF5;text-align:center">
<!-- End Header -->
	Change Password<br/>
		<center>
<!-- Begin Change Password Form -->
		<form name="changePass" action="process/changepassword.jsp" method="post">
			<table border=0 style="font: 12px Helvetica;">
			<tr><td align="right">Old Password: </td><td align="left"><input name="oldPass" type="password" size="40" value=""></td></tr>
			<tr><td align="right">New Password: </td><td align="left"><input name="newPass1" type="password" size="40" value=""></td></tr>
			<tr><td align="right">Confirm Password:</td><td align="left"><input name="newPass2" type="password" size="40" value=""></td></tr>
			</table>
			<input type="submit" style="font:12px Helvetica; color:#048;text-decoration:none;" value="Change It">
		</form>
<!-- End Form -->
		</center>
<!-- Start Footer -->
		Talk to a TxtIT representative immediately by calling 1.800.690.1388
	<div style="width:100%; height:2px; background:#000">&nbsp;</div>
	<div style="background:#054C8E; width:100%;text-align:center;padding:3px 0px 3px 0px; color:#FFF;">
		Home | Send Message | Manage Group | Message History | Change Password | Logout
	</div>
	<div style="background:#FFF; width:100%;text-align:center;padding:3px 0px 3px 0px; color:#000;font: 10px Helvetica;">
		&copy; 2007-2008 txtitllc.com
	</div>
</div>
<div id="alertBox" style="display:none">
<p><%= error %></p>
</div>

</body>
</html>