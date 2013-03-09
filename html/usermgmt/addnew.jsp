<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.*" %>

<%
	String uuid = (String)session.getAttribute("user");
	
	String addFirstName = request.getParameter("addFirstName");
	if (addFirstName == null) addFirstName = "";

	String addLastName = request.getParameter("addLastName");
	if (addLastName == null) addLastName = "";
	
	String addLogin = request.getParameter("addLogin");
	if (addLogin == null) addLogin = "";

	String addDepartment = request.getParameter("addDepartment");
	if (addDepartment == null) addDepartment = "";

	String addStartHour = request.getParameter("addStartHour");
	if (addStartHour == null) addStartHour = "9";

	String addStartMinutes = request.getParameter("addStartMinutes");
	if (addStartMinutes == null) addStartMinutes = "";

	String addStartAMPM = request.getParameter("addStartAMPM");
	boolean startAM = true;
	if ((addStartAMPM != null) && (addStartAMPM.equals ("PM"))) startAM=false;

	String addEndHour = request.getParameter("addEndHour");
	if (addEndHour == null) addEndHour = "5";

	String addEndMinutes = request.getParameter("addEndMinutes");
	if (addEndMinutes == null) addEndMinutes = "";

	String addEndAMPM = request.getParameter("addEndAMPM");
	boolean endAM = false;
	if ((addEndAMPM != null) && (addEndAMPM.equals ("AM"))) endAM=true;


%>
<tr><td colspan="25" class="abovecell" width="100%">&nbsp;</td></tr>
<td></tr>
<tr><td align="left" colspan="25">
<table>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">First Name</td>
<td align="left"><input tabindex="1" type="text" name="addFirstName" size="30" value="<%= addFirstName %>" onchange="setAction('action_addnew_save');"></td>
<td><img src="images/spacer.gif"></td>
<td><strong>Work Hours</strong></td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Last Name</td>
<td align="left"><input tabindex="2" type="text" name="addLastName" size="30" value="<%= addLastName %>" onchange="setAction('action_addnew_save');"></td>
<td><img src="images/spacer.gif"></td>
<td>Start
<select name="addStartHour" tabindex="6">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
		
		if ((hour+"").equals(addStartHour)) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="addStartMinutes" tabindex="7">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if ((minutes+"").equals(addStartMinutes)) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="addStartAMPM" tabindex="8">
<%if (startAM) {
	%><option name="am" SELECTED>AM
<%} else {
	%><option name="am">AM
<%}%>

<%if (startAM) {
	%><option name="pm">PM
<%} else {
	%><option name="pm" SELECTED>PM
<%}%>
</select>
</td>
<td><input tabindex="12" type="image" src="images/btn_save.gif" name="action_addnew_save">
</td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Department</td>
<td align="left"><input tabindex="3" type="text" name="addDepartment" size="30" value="<%= addDepartment %>" onchange="setAction('action_addnew_save');">
<td><img src="images/spacer.gif"></td>
<td>End&nbsp;
<select name="addEndHour" tabindex="9">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
	if ((hour+"").equals(addEndHour)) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="addEndMinutes" tabindex="10">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if ((minutes+"").equals(addEndMinutes)) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="addEndAMPM" tabindex="11">
<%if (endAM) {
	%><option name="am" SELECTED>AM
<%} else {
	%><option name="am">AM
<%}%>

<%if (endAM) {
	%><option name="pm">PM
<%} else {
	%><option name="pm" SELECTED>PM
<%}%>

</select>
</td>
<td><input type="image" src="images/btn_cancel.gif" name="action_personal_cancel" tabindex="13">
</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Login</td>
<td align="left"><input tabindex="4" type="text" name="addLogin" size="30" value="<%= addLogin %>" onchange="setAction('action_addnew_save');"></td>
<td colspan="2"><img src="images/spacer.gif"></td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Password</td><td><input tabindex="4" type="password" size="30" name="addPassword" onchange="setAction('action_addnew_save');"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="25">Confirm Password <input tabindex="5" type="password" size="30" name="addConfirmPassword" onchange="setAction('action_addnew_save');"></td>
</tr>
</table>