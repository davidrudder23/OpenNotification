<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<jsp:include page="blank_header.jsp" />

<input type="hidden" name="page" value="/register.jsp" />


<!-- start content -->

<%
	String uuid = (String)session.getAttribute("user");
	
	String addFirstName = request.getParameter("addFirstName");
	if (addFirstName == null) addFirstName = "";

	String addLastName = request.getParameter("addLastName");
	if (addLastName == null) addLastName = "";

	String addDepartment = request.getParameter("addDepartment");
	if (addDepartment == null) addDepartment = "";

	String addEmail = request.getParameter("addEmail");
	if (addEmail == null) addEmail = "";

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
<tr><td align="left" colspan="25">
<table>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="25"><p>
<center><b>Welcome to Reliable Response Notification&#8482;!</b></center>
<%
	String[] messages = request.getParameterValues("add_user_system_message");
	BrokerFactory.getLoggingBroker().logDebug("messages = "+messages);
	if (messages != null) {
		BrokerFactory.getLoggingBroker().logDebug("messages length = "+messages.length);
		for (int i = 0; i < messages.length; i++) {
			%><br><center><font class="systemalert"><%= messages[i] %></font></center><%
		}
	}
%>
</p><br>
</td>
</tr>
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
<select name="addStartHour" tabindex="7">
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
<select name="addStartMinutes" tabindex="8">
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
<select name="addStartAMPM" tabindex="9">
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
<td><input tabindex="13" type="image" src="images/btn_save.gif" name="action_addnew_save">
</td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Email</td>
<td align="left"><input tabindex="3" type="text" name="addEmail" size="30" value="<%= addEmail %>" onchange="setAction('action_addnew_save');">
<td><img src="images/spacer.gif"></td>
<td>End&nbsp;
<select name="addEndHour" tabindex="10">
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
<select name="addEndMinutes" tabindex="11">
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
<select name="addEndAMPM" tabindex="12">
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
<td><input type="image" src="images/btn_cancel.gif" name="action_personal_cancel" tabindex="14">
</td></tr>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Password</td><td><input tabindex="5" type="password" size="30" name="addPassword" onchange="setAction('action_addnew_save');"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="25">Confirm Password <input tabindex="6" type="password" size="30" name="addConfirmPassword" onchange="setAction('action_addnew_save');"></td>
</tr>
</table>
<!-- end content -->


<jsp:include page="footer.jsp" />
