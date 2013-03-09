<%
String error = (String)session.getAttribute("error");
session.setAttribute("error", null);

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


Member[] members = group.getMembers();
%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.usermgmt.Member"%>
<%@page import="net.reliableresponse.notification.device.Device"%>
<%@page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.device.EmailDevice"%>
<html>
<head>
	<title>TxtIt :: Manage Group</title>
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
	

	<script>
		var http = createRequestObject();

		function createRequestObject(){
        	var request_o; //declare the variable to hold the object.
        	var browser = navigator.appName; //find the browser name
        	if(browser == "Microsoft Internet Explorer"){
                /* Create the object using MSIE's method */
                request_o = new ActiveXObject("Microsoft.XMLHTTP");
       		 }else{
                /* Create the object using other browser's method */
       	        request_o = new XMLHttpRequest();
        	}
        	return request_o; //return the object
		}

		function handleEditResponse() {
			//alert (http.readyState);
			if(http.readyState == 4) {
				var message = "";
				if (http.status == 200){ 
					message = "Updated";
				} else {
					message = "Update failed";
				}
				//alert (http.responseText);
			}
		}
	
		function submitEdit (spanName) {
			var span = document.getElementById(spanName);
			var input = document.getElementById("input_"+spanName);
			var text = input.value;
			document.getElementById("label_"+spanName).onmousedown=function onmousedown(event) {setEdit(spanName)};
			span.onchange="";
			span.innerHTML = text;
			
			runUpdate (spanName, text);
		}
		
		function runUpdate (spanName, text) {
			
			// figure out the type and the uuid
			var underbarIdx = spanName.indexOf("_", 0);
			var name  = spanName.substring(0, underbarIdx);
			var uuid = spanName.substring(underbarIdx+1, spanName.length);
			var value = text;
			
			// do the update
			http.open ('get', "process/updateUser.jsp?uuid="+uuid+"&"+name+"="+value);
			//alert ("Submitting: process/updateUser.jsp?uuid="+uuid+"&"+name+"="+value);
			http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    		http.onreadystatechange = handleEditResponse;
			http.send(null);
		}
		
		function submitCarrier (spanName) {
			var span = document.getElementById(spanName);
			var input = document.getElementById("input_"+spanName);
			var value = input.value;
			var text = input.options[input.selectedIndex].text;
			document.getElementById("label_"+spanName).onmousedown=function onmousedown(event) {editCarrier(spanName)};
			span.onchange="";
			span.innerHTML = text;
			
			runUpdate (spanName, text);
		}
		
		function handleKeyPress (e, spanName) {
			if (window.event) {
				keynum = e.keyCode;
				//alert ("code="+keynum);
				if (keynum==13) {
					submitEdit(spanName);
					
				} else {
					return true;
				} 
			}
			return true;
		}
		
		function setEdit(spanName) {
			var span = document.getElementById(spanName);
			document.getElementById("label_"+spanName).onmousedown="";
			var text = span.innerHTML;
			var innerHTML = "<input type=\"text\" onkeypress=\"return handleKeyPress(event, '"+spanName+"');\" onchange=\"return submitEdit('"+spanName+"');\" onblur=\"submitEdit('"+spanName+"');\" length=\"20\" id=\"input_"+spanName+"\" value=\""+text+"\"/>";
			//alert (innerHTML);
			span.innerHTML = innerHTML;
			span.onchange=function onchange(event) {submitEdit(spanName)};
		}
		
		function editCarrier(spanName) {
			var span = document.getElementById(spanName);
			var text = span.innerHTML;
			document.getElementById("label_"+spanName).onmousedown="";
			
			var innerHTML = "<SELECT onchange=\"submitCarrier('"+spanName+"');\" id=\"input_"+spanName+"\" name=\"contactCarrier\">";
			if (text == "Cingular/AT&T") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"Cingular\" "+checked+">Cingular/AT&T</OPTION>"
			if (text == "Sprint") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"Sprint\" "+checked+">Sprint</OPTION>";
			if (text == "CellularOne") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"CellularOne\" "+checked+">CellularOne</OPTION>";
			if (text == "Verizon") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"Verizon\" "+checked+">Verizon</OPTION>";
			if (text == "Qwest") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"Sprint\" "+checked+">Qwest</OPTION>";
			if (text == "TMobile") { checked = "SELECTED"; } else { checked = "false"}; 
			innerHTML += "<OPTION value=\"TMobile\" "+checked+">TMobile</OPTION>";
			innerHTML += "</SELECT>";
			span.innerHTML = innerHTML;
			span.onchange=function onchange(event) {submitCarrier(spanName)};
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
		<img name="mg_img" style="position:absolute; top:57px; left:303px" border="0" src="images/blank_active.jpg">
		<div id="button" name="changePW" style="TOP:71px; left:303px;">MANAGE GROUP</div>
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
	<h1 style="padding:0px;margin:0px;">
	<label id="label_group_0000000" onmousedown="setEdit('group_0000000')"><span id="group_0000000"><%= group.getGroupName() %></span></label></h1>
	<BR>
	<table width="100%" border="0" ><tr><td valign="top" style="text-align:center;">
<!-- New Member Form -->
		<form name="addUser" action="process/addUser.jsp" method=POST>
		<table border="0" cellspacing="1" cellpadding="1">
			<tr><td colspan="2">Add New Recipient</td></tr>
			<tr><td><label for="new_rec_name">Name:&nbsp;&nbsp;</label></td><td><input name="contactName" type="text" size="40" value=""></td></tr>
			<tr><td><label for="new_rec_phone">Phone:&nbsp;</label></td><td><input name="contactPhone" type="text" size="15" value="">
			<SELECT name="contactCarrier">
			<OPTION value="Cingular">Cingular/AT&T</OPTION>
			<OPTION value="Sprint">Sprint</OPTION>
			<OPTION value="CellularOne">CellularOne</OPTION>
			<OPTION value="Verizon">Verizon</OPTION>
			<OPTION value="Sprint">Qwest</OPTION>
			<OPTION value="TMobile">TMobile</OPTION>
			</SELECT></td></tr>
			<tr><td><label for="contactEmail">Email:&nbsp;</label></td><td><input name="contactEmail" type="text" size="40" value=""></td></tr>
			<tr><td colspan="2"><input type="submit" style="font:12px Helvetica; color:#048;text-decoration:none;" value="ADD"></td></tr>
		</table>
		</form>
<!-- End New Member -->
	</td>
	<td style:"font-align:left;">
<!-- Existing Members Form -->
		<form name="existing" action="process/deleteUsers.jsp">

<%
for (int memberNum = 0; memberNum < members.length; memberNum++) {
	Member member = members[memberNum];
	if (member instanceof User) {
		User recipient = (User)member;
		String phoneNumber = "";
		String carrier = "";
		String emailAddress = "";
		Device[] devices = recipient.getDevices();
		for (int devNum = 0; devNum < devices.length; devNum++) {
			Device device = devices[devNum];
			if (device instanceof CellPhoneEmailDevice) {
				CellPhoneEmailDevice cell = ((CellPhoneEmailDevice)device);
				phoneNumber = (String)cell.getSettings().get("Phone Number");
				carrier = (String)cell.getSettings().get("Provider");
			} else if (device instanceof EmailDevice) {
				EmailDevice email = ((EmailDevice)device);
				emailAddress = (String)email.getSettings().get("Address");
			}
		}
		
		if (StringUtils.isEmpty(phoneNumber)) {
			phoneNumber = " ";
		}
		if (StringUtils.isEmpty(carrier)) {
			carrier = " ";
		}
		
		String uuid = recipient.getUuid();
%>		
		<table>
		<tr><td valign="top">
		<a href="process/deleteUser.jsp?uuid=<%= uuid %>" style="font:10px Helvetica; color:#048;text-decoration:none;" >DELETE</a>
		<input type="checkbox" name="delete_<%= uuid %>">
		</td><td>
		<label id="label_username_<%= uuid %>" onmousedown="setEdit('username_<%= uuid %>')"><span id="username_<%= uuid %>"><%= recipient.getFirstName() %> <%= recipient.getLastName() %></span></label>
		<br/><label id="label_phonenum_<%= uuid %>" onmousedown="setEdit('phonenum_<%= uuid %>')">Phone: <span id="phonenum_<%= uuid %>"><%= phoneNumber %></span></label>
		<br/><label id="label_carrier_<%= uuid %>" onmousedown="editCarrier('carrier_<%= uuid %>')">Carrier: <span id="carrier_<%= uuid %>"><%= carrier %></span></label>
		<br/><label id="label_email_<%= uuid %>" onmousedown="setEdit('email_<%= uuid %>')">Email: <span id="email_<%= uuid %>"><%= emailAddress %></span></label>
		</td></tr></table>
		<br>
<%
	}
}
%>		
		<p>
		<input type="submit" style="font:10px Helvetica; color:#048;text-decoration:none;" value="DELETE CHECKED">
		</p>
		</form>
<!-- End Existing Members Form -->
		<br>
	</td></tr></table>
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