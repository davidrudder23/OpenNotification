<%
	String error = (String)session.getAttribute("error");
	session.setAttribute("error", null);
	if (error == null) error = "";
	
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	
	Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
	BrokerFactory.getLoggingBroker().logDebug("We found "+groups.length+" groups");
	Group group = null;
	for (int groupNum = 0; groupNum < groups.length; groupNum++) {
		if (groups[groupNum] instanceof BroadcastGroup) {
			group = groups[groupNum];
		}
	}
	if (group == null) {
		group = new BroadcastGroup();
		group.setGroupName(user.getFirstName()+" "+user.getLastName()+"'s Group");
		BrokerFactory.getGroupMgmtBroker().addGroup(group);
		group.setAutocommit(true);
		group.addMember(user, -1);
	}

	Notification[] notifs = BrokerFactory.getNotificationBroker().getNotificationsSentTo(group);
%>

<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<html>
<head>
	<title>TxtIt :: Message History</title>
	<style type="text/css">{}
		body{font: 14px Helvetica; color: #000;}
		div#button{position:absolute;WIDTH:153px; HEIGHT:22; text-align:center; color:#FFF; font: 12px Helvetica;text-decoration:none; font-weight:bold;}
		a#button{text-decoration:none;color:#333; font: 12px Helvetica;font-weight:normal;}
		textarea{border:0px;background:#E9EFF5;color:#333;overflow:hidden;font: 14px Helvetica;}
		input{border:0px;background:#E9EFF5;color: #333;font: 14px Helvetica;}
	</style>
	<!-- javascript functions -->
	<script type="text/javascript">
	function blue2red(image){
		image.src="images/blank_active.jpg"
	}
	function red2blue(image){
		image.src="images/blank.jpg"
	}
	</script>
</head>

<body>
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
		<img name="mh_img" style="position:absolute; top:57px; left:151px" border="0" src="images/blank_active.jpg">
		<div id="button" name="mHistory" style="TOP:71px; LEFT:151px;">MESSAGE HISTORY</div>
	<!-- Manage Group Button -->
	<a id="button" href="manage_group.jsp" onMouseOver="blue2red(document.mg_img)" onMouseOut="red2blue(document.mg_img)">
		<img name="mg_img" style="position:absolute; top:57px; left:303px" border="0" src="images/blank.jpg">
		<div id="button" name="changePW" style="TOP:71px; left:303px;">MANAGE GROUP</div>
	</a>
	<!-- Change Password Button -->
	<a id="button" href="change_password.jsp" onMouseOver="blue2red(document.cp_img)" onMouseOut="red2blue(document.cp_img)">
		<img name="cp_img" style="position:absolute; top:57px; right:151px" border="0" src="images/blank.jpg">
		<div id="button" name="changePW" style="TOP:71px; right:151px;">CHANGE PASSWORD</div>
	</a>
	<!-- Logout -->
	<a id="button" href="process/logout.jsp" onMouseOver="blue2red(document.lo_img)" onMouseOut="red2blue(document.lo_img)">
		<img name="lo_img" style="position:absolute; top:57px; right:0px" border="0" src="images/blank.jpg">
		<div id="button" name="logout" style="TOP:71px; right:0px;">LOGOUT</div>
	</a>
	<div style="position:absolute;top:89px; left:0px; width:100%; height:2px; background:#000" >&nbsp;</div>
	<div style="position:absolute;top:91px; left:0px; width:100%; padding:3px 0px 0px 0px; background:#E9EFF5;text-align:center">
<!-- End Header -->
	<center>
<!-- This should fill in with the history of messages sent, newest first 
	The date should be in the form <DAY>, <MONTH> <DAY OF MONTH>, <YEAR> <HOUR>:<MIN>:<SEC> and in the legend tag.
	The message should be in the textarea tag
-->
		<div style="width:900px">
<%
	int length = 1500;
	if (notifs.length<length) length = notifs.length;
	
	for (int notifNum = (length-1); notifNum >=0 ; notifNum--) {
		Notification notif = notifs[notifNum];
		if (notif.getParentUuid() == null) {
%>
		<fieldset><legend style="font:bold 14px Helvetica;text-decoration:underline;color:#333;"><%= notif.getTime() %></legend>
		<textarea readonly name="mess_0" cols=120 rows=1><%= notif.getDisplayText() %></textarea>
		</fieldset><br>
<% 
		}
	}
%>
		Page Number 1 of 1
		<div id="button" style="position:absolute;right:5px;"><a id="button" href="#"> Next > </a> <a id="button" href="#"> Last >> </a></div>
		<div id="button" style="position:absolute;left:5px;"><a id="button" href="#"> << First </a> <a id="button" href="#"> < Previous </a></div>
		</div>
 	</center>
<br>
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
</body>
</html>