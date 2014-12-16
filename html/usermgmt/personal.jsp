<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.broker.impl.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.*" %>
<%@ page import="net.reliableresponse.notification.util.StringUtils" %>

<%
	String uuid = (String)session.getAttribute("user");
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		
	String firstName = user.getFirstName();
	String lastName = user.getLastName();
	String department = user.getDepartment();
	
	Date startTime = user.getStartTime();
	int startHour = startTime.getHours();
	int startMinutes = startTime.getMinutes();
	boolean startAM = true;
	if (startHour > 12) {
		startAM = false;
		startHour -= 12;
	}
	
	Date endTime = user.getEndTime();
	int endHour = endTime.getHours();
	int endMinutes = endTime.getMinutes();
	boolean endAM = true;
	if (endHour > 12) {
		endAM = false;
		endHour -= 12;
	}
	
	String freebusyURL = user.getInformation("freebusyURL");
	if (freebusyURL == null) freebusyURL="";
	
	int priority = 3;
	String priorityString = user.getInformation("priority");
	if (!StringUtils.isEmpty(priorityString)) {
		try {
			priority = Integer.parseInt(priorityString);
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
%>
<tr><td colspan="25" class="abovecell" width="100%">&nbsp;</td></tr>
<tr><td align="left" colspan="25">
<table>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">First Name</td>
<td align="left"><input tabindex="1" type="text" name="firstName" size="30" value="<%= firstName %>" onchange="setAction('action_personal_save');"></td>
<td><img src="images/spacer.gif"></td>
<td><strong>Work Hours</strong></td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Last Name</td>
<td align="left"><input tabindex="1" type="text" name="lastName" size="30" value="<%= lastName %>" onchange="setAction('action_personal_save');"></td>
<td><img src="images/spacer.gif"></td>
<td>Start
<select name="starthour" tabindex="6">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
		
		if (hour == startHour) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="startminutes" tabindex="7">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if (minutes == startMinutes) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="startampm" tabindex="8">
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
<td><input tabindex="12" type="image" src="images/btn_save.gif" name="action_personal_save">
</td>
</tr><tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right">Department</td>
<td align="left"><input tabindex="3" type="text" name="department" size="30" value="<%= department %>" onchange="setAction('action_personal_save');">
<td><img src="images/spacer.gif"></td>
<td>End&nbsp;
<select name="endhour" tabindex="9">
<%
	for (int i = 0; i < 12; i++) {
		int hour = i;
		if (i == 0) hour = 12;
	if (hour == endHour) {
		%><option name="<%= hour %>" SELECTED><%= hour %>
		<%} else {
		%><option name="<%= hour %>"><%= hour %>
		<%}
	}
%>
</select>
<select name="endminutes" tabindex="10">
<%
	for (int i = 0; i < 4; i++) {
		int minutes = i*15;
		if (minutes == endMinutes) {
		%><option name="<%= minutes %>" SELECTED><%= minutes %>
		<%} else {
		%><option name="<%= minutes %>"><%= minutes %>
		<%}
	}
%>
</select>
<select name="endampm" tabindex="11">
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
<td><input tabindex="13" type="image" src="images/btn_cancel.gif" name="action_personal_cancel">
</td></tr>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<%
	String vacation = user.isOnVacation()?"CHECKED":"";
%>
<td align="right">On Vacation </td><td><input type="checkbox" name="vacation" <%= vacation %>></td>
<td>&nbsp;</td>
</tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td align="right"valign="center">Password</td><td><input tabindex="4" type="password" size="30" name="password" onchange="setAction('action_personal_save');"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="25" valign="center">Confirm Password <input tabindex="5" type="password" size="30" name="confirm_password" onchange="setAction('action_personal_save');"></td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td colspan="2">Priority of Notifications Sent Directly to Me</td>
<td><img src="images/spacer.gif"></td>
<td colspan="2">1: <input type="radio" name="priority" value="1" <%= (priority==1)?"CHECKED":"" %>/>
2: <input type="radio" name="priority" value="2" <%= (priority==2)?"CHECKED":"" %>/>
3: <input type="radio" name="priority" value="3" <%= (priority==3)?"CHECKED":"" %>/>
</td>
</tr>
<%
	CalendarBroker calendarBroker = BrokerFactory.getCalendarBroker();
	if (calendarBroker instanceof ICalCalendarBroker) {
	%>
<tr><td colspan="25"><img src="images/spacer.gif">&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td>Free/Busy URL</td>
<td colspan="4"><input type="text" size="50" name="freebusyURL" value="<%= freebusyURL %>"></td>
</tr>
<% } %>
<tr><td>&nbsp;</td></tr>
<tr>
<td><img src="images/spacer.gif"></td>
<td><img src="images/spacer.gif"></td>
<td>Device Escalation</td>
<td colspan="4"><select name="deviceEscalationPolicy">
<option value="Simultaneous" <%= User.DEVICE_ESCALATION_SIMULTANEOUS.equals(user.getDeviceEscalationPolicy())?"SELECTED":"" %>>Alert all devices simultaneously</option>
<option value="Static" <%= User.DEVICE_ESCALATION_STATIC_TIMING.equals(user.getDeviceEscalationPolicy())?"SELECTED":"" %>>1 at a time, specify the interval</option>
<option value="Proportional"  <%= User.DEVICE_ESCALATION_PROPORTIONAL_TIMING.equals(user.getDeviceEscalationPolicy())?"SELECTED":"" %>>1 at a time, automatic interval</option>
<span id="deviceEscalationTimeArea"><label>Minutes</label><input type="text" name="deviceEscalationTime" style="max-width: 2em" value="<%= user.getDeviceEscalationTime() %>"></span>
</td>
</tr>

<tr><td>&nbsp;</td></tr>
</table>
