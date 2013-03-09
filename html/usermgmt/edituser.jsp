<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<script type="text/javascript" language="JavaScript">
function runNow(action) {
   document.mainform.action.value = action;
   document.mainform.submit();
}
</script>
<%
	String uuid = request.getParameter ("user_to_edit");
	request.setAttribute("user", uuid);
	String editFirstName = "";
	String editLastName = "";
	String editDepartment = "";
	String editEmail = "";
	String adminChecked = "";
	String observerChecked = "";
	String cacheChecked = "";

	int editStartHour = 9;
	int editStartMinutes = 0;
	boolean editStartAM = true;
	int editEndHour = 5;
	int editEndMinutes = 00;
	boolean editEndAM = false;

	if (uuid == null) {
		User[] firstUser = new User[1];
		BrokerFactory.getUserMgmtBroker().getUsers(1, 0, firstUser);
		uuid = firstUser[0].getUuid();
	}
	if (uuid != null) {
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		if (user == null) {
			User[] users = new User[1];
			BrokerFactory.getUserMgmtBroker().getUsers(1, 0, users);
			user = users[0];
		}
	
		editFirstName = user.getFirstName();
		editLastName = user.getLastName();
		editDepartment = user.getDepartment();
		editEmail = user.getEmailAddress();
	
		Date startTime = user.getStartTime();
		editStartHour = startTime.getHours();
		editStartMinutes = startTime.getMinutes();
		editStartAM = true;
		if (editStartHour > 12) {
			editStartAM = false;
			editStartHour -= 12;
		}
		
		Date endTime = user.getEndTime();
		editEndHour = endTime.getHours();
		editEndMinutes = endTime.getMinutes();
		editEndAM = true;
		if (editEndHour > 12) {
			editEndAM = false;
			editEndHour -= 12;
		}

		if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
			adminChecked = " CHECKED";
		}
		
		if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Observers")) {
			observerChecked = " CHECKED";
		}
	
		if (user.isInPermanentCache()) {
			cacheChecked = " CHECKED";
		}

	} else {
		uuid = "";
	}
%>
<input type="hidden" name="edit_user" value="<%=uuid%>">
<tr><td colspan="25" class="abovecell" width="100%">&nbsp;</td></tr>
<tr><td align="left" colspan="25">
<table>
<tr><td>
<td><img src="images/spacer.gif"></td>
<td>Select User</td>
<td><SELECT name="user_to_edit" onchange="document.mainform.submit();">
<%
	User[] users = new User[BrokerFactory.getUserMgmtBroker().getNumUsers()];
	int numUsers = BrokerFactory.getUserMgmtBroker().getUsers(users.length, 0, users);
	
	for (int i = 0; i < numUsers; i++) {
		String checked = "";
		if (users[i].getUuid().equals(uuid)) checked = "SELECTED";
%>
	<option value="<%= users[i].getUuid() %>" <%= checked %>><%= users[i].toString()%>
<%}
%>
</SELECT>&nbsp;<a href="javascript:runNow('action_loginas_<%= uuid %>');">Login as User</a></td></tr>
<tr><td>&nbsp;<tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">First Name</td>
<td align="left"><input tabindex="21" type="text" name="editFirstName" size="30" value="<%= editFirstName %>" onchange="setAction('action_edituser_save');"></td>
<td><img src="images/spacer.gif"></td>
<td><strong>Work Hours</strong></td>
<td><input tabindex="32" type="image" src="images/btn_save.gif" name="action_edituser_save">
</td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Last Name</td>
<td align="left"><input tabindex="22" type="text" name="editLastName" size="30" value="<%= editLastName %>" onchange="setAction('action_edituser_save');"></td>
<td><img src="images/spacer.gif"></td>
<td>Start
<select name="editStartHour" tabindex="26">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
		
		if (hour == editStartHour) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="editStartMinutes" tabindex="27">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if (minutes == editStartMinutes) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="editStartAMPM" tabindex="28">
<%if (editStartAM) {
	%><option name="am" SELECTED>AM
<%} else {
	%><option name="am">AM
<%}%>

<%if (editStartAM) {
	%><option name="pm">PM
<%} else {
	%><option name="pm" SELECTED>PM
<%}%>
</select>
</td>
<td><input tabindex="32" type="image" src="images/btn_remove.gif" name="action_edituser_remove">
</td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Department</td>
<td align="left"><input tabindex="23" type="text" name="editDepartment" size="30" value="<%= editDepartment %>" onchange="setAction('action_edituser_save');">
<td><img src="images/spacer.gif"></td>
<td>End&nbsp;
<select name="editEndHour" tabindex="29">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
	if (hour == editEndHour) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="editEndMinutes" tabindex="30">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if (minutes == editEndMinutes) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="editEndAMPM" tabindex="31">
<%if (editEndAM) {
	%><option name="am" SELECTED>AM
<%} else {
	%><option name="am">AM
<%}%>

<%if (editEndAM) {
	%><option name="pm">PM
<%} else {
	%><option name="pm" SELECTED>PM
<%}%>

</select>
</td>
<td><input tabindex="33" type="image" src="images/btn_cancel.gif" name="action_personal_cancel">
</td></tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Email Address</td><td><input tabindex="24" type="text" name="editEmail" onchange="setAction('action_edituser_save');" value="<%= editEmail %>"></td>
<td align="left" colspan="3">Administrator&nbsp;<input tabindex="24" type="checkbox" name="editAdministrator" onchange="setAction('action_edituser_save');"<%= adminChecked %>>
Observer&nbsp;&nbsp;<input tabindex="24" type="checkbox" name="editObserver" onchange="setAction('action_edituser_save');"<%= observerChecked %>>
<br>Keep in Cache<input tabindex="25" type="checkbox" name="editCached" onchange="setAction('action_edituser_save');"<%= cacheChecked %>></td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Password</td><td><input tabindex="34" type="password" size="30" name="editPassword" onchange="setAction('action_edituser_save');"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="25">Confirm Password <input tabindex="35" type="password" size="30" name="editConfirmPassword" onchange="setAction('action_edituser_save');"></td>
</tr>
<tr><td>&nbsp;</td></tr>
<%
	String deviceSettingsTitle = "Device Settings</td><td align=\"right\" class=\"headercell\"></td>";
	
%>
<tr><td colspan="25">
<reliable:collapseable tag="device Settings" title="<%= deviceSettingsTitle %>"
contentURL="/devicemgmt/index.jsp" >
</reliable:collapseable>
</td></tr></table>