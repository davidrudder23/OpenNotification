<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<%
	String groupUuid = (String)request.getAttribute ("group_uuid");
	OnCallGroup group = (OnCallGroup)BrokerFactory.getGroupMgmtBroker().getGroupByUuid(groupUuid);
	Member[] members = group.getMembers();
	OnCallSchedule[] schedules = group.getOnCallSchedules();
	
%>

<td><table width="100%"  border="0" cellpadding="0" cellspacing="0">
<tr><td height="3"></td></tr>
<%
	for (int memberNum = 0; memberNum < members.length; memberNum++) {
		Member member = members[memberNum];
		String scheduleID = groupUuid + "_" + memberNum;
		OnCallSchedule schedule = schedules[memberNum];
		Date from = schedule.getFromDate();
		Date to = schedule.getToDate();
		BrokerFactory.getLoggingBroker().logDebug("to date = "+to);
%>
<tr><td><img src="images/spacer.gif" width="10" height="1"></td>
<td class="cellrule" width="100%" colspan="4"><center><span class="subexpand" style="color: #000000">&nbsp;&nbsp;&nbsp;<%= member.toString()%>&nbsp;&nbsp;&nbsp;</span></center></td></tr>
<tr><td colspan="2"></td>
<td>Date</td>
<td width="60%"><table>
<tr><td>From</td><td><SELECT name="fromMonth_<%= scheduleID %>">
<OPTION value="01" <%= (from.getMonth()== 0)?"SELECTED":"" %>>January
<OPTION value="02" <%= (from.getMonth()== 1)?"SELECTED":"" %>>February
<OPTION value="03" <%= (from.getMonth()== 2)?"SELECTED":"" %>>March
<OPTION value="04" <%= (from.getMonth()== 3)?"SELECTED":"" %>>April
<OPTION value="05" <%= (from.getMonth()== 4)?"SELECTED":"" %>>May
<OPTION value="06" <%= (from.getMonth()== 5)?"SELECTED":"" %>>June
<OPTION value="07" <%= (from.getMonth()== 6)?"SELECTED":"" %>>July
<OPTION value="08" <%= (from.getMonth()== 7)?"SELECTED":"" %>>August
<OPTION value="09" <%= (from.getMonth()== 8)?"SELECTED":"" %>>September
<OPTION value="10" <%= (from.getMonth()== 9)?"SELECTED":"" %>>October
<OPTION value="11" <%= (from.getMonth()== 10)?"SELECTED":"" %>>November
<OPTION value="12" <%= (from.getMonth()== 11)?"SELECTED":"" %>>December
</SELECT>
<SELECT name="fromDate_<%= scheduleID %>">
<%
	for (int fromDate = 1; fromDate <= 31; fromDate++) {
		String value = null;
		if (fromDate < 10) {
			value = "0"+fromDate;
		} else {
			value = ""+fromDate;
		}
		%><OPTION value="<%= value %>" <%= (from.getDate()== fromDate)?"SELECTED":"" %>><%= fromDate %>
		<%
	}
%>
</SELECT>
<SELECT name="fromYear_<%= scheduleID %>">
<%
        for (int fromYear = 2000; fromYear <= 2100; fromYear++) {
                %><OPTION value="<%= fromYear %>"  <%= ((from.getYear()+1900)==fromYear)?"SELECTED":"" %>><%= fromYear %>
                <%
        }
%>
</SELECT>
</td></tr>
	<tr>
<td>To
</td><td><SELECT name="toMonth_<%= scheduleID %>">
<OPTION value="01" <%= (to.getMonth()== 0)?"SELECTED":"" %>>January
<OPTION value="02" <%= (to.getMonth()== 1)?"SELECTED":"" %>>February
<OPTION value="03" <%= (to.getMonth()== 2)?"SELECTED":"" %>>March
<OPTION value="04" <%= (to.getMonth()== 3)?"SELECTED":"" %>>April
<OPTION value="05" <%= (to.getMonth()== 4)?"SELECTED":"" %>>May
<OPTION value="06" <%= (to.getMonth()== 5)?"SELECTED":"" %>>June
<OPTION value="07" <%= (to.getMonth()== 6)?"SELECTED":"" %>>July
<OPTION value="08" <%= (to.getMonth()== 7)?"SELECTED":"" %>>August
<OPTION value="09" <%= (to.getMonth()== 8)?"SELECTED":"" %>>September
<OPTION value="10" <%= (to.getMonth()== 9)?"SELECTED":"" %>>October
<OPTION value="11" <%= (to.getMonth()== 10)?"SELECTED":"" %>>November
<OPTION value="12" <%= (to.getMonth()== 11)?"SELECTED":"" %>>December
</SELECT>
<SELECT name="toDate_<%= scheduleID %>">
<%
	for (int toDate = 1; toDate <= 31; toDate++) {
		String value = null;
		if (toDate < 10) {
			value = "0"+toDate;
		} else {
			value = ""+toDate;
		}
		%><OPTION value="<%= value %>" <%= (to.getDate()== toDate)?"SELECTED":"" %>><%= toDate %>
		<%
	}
%>
</SELECT>
<SELECT name="toYear_<%= scheduleID %>">
<%
        for (int toYear = 2000; toYear <= 2100; toYear++) {
                %><OPTION value="<%= toYear %>"  <%= ((to.getYear()+1900)==toYear)?"SELECTED":"" %>><%= toYear %>
                <%
        }
%>
</SELECT>
</td></tr>
</table></td>
<td rowspan="3" width="20%">
<table width="100%">
<tr><td>Every <input type="text" size="2" name="repcount_<%= scheduleID %>" value="<%= schedule.getRepetitionCount() %>"><br/>
&nbsp;&nbsp;<input type="radio" name="repeat_<%= scheduleID %>" value="<%= ""+OnCallSchedule.REPEAT_DAILY %>" <%= (schedule.getRepetition() == OnCallSchedule.REPEAT_DAILY)?"CHECKED":"" %>>Days<br/>
&nbsp;&nbsp;<input type="radio" name="repeat_<%= scheduleID %>" value="<%= ""+OnCallSchedule.REPEAT_WEEKLY %>" <%= (schedule.getRepetition() == OnCallSchedule.REPEAT_WEEKLY)?"CHECKED":"" %>>Weeks<br/>
&nbsp;&nbsp;<input type="radio" name="repeat_<%= scheduleID %>" value="<%= ""+OnCallSchedule.REPEAT_MONTHLY_DATE %>" <%= (schedule.getRepetition() == OnCallSchedule.REPEAT_MONTHLY_DATE)?"CHECKED":"" %>>Months</td></tr>
<tr><td><br/><input type="image" src="images/btn_save.gif" name="action_save_schedule_<%= scheduleID %>"></td></tr>
</table>
</td>
<td><img src="images/spacer.gif" width="20"></td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif" width="10" height="20"></td></tr>
<%
	String fromAMPM = "AM";
	int fromHours = from.getHours();
	if (fromHours>11) {
		fromAMPM = "PM";
		fromHours -= 12;
	}
%>
<tr><td></td><td></td><td>Time</td>
<td colspan="25">
	<table width="60%">
<tr><td>From</td><td>
<SELECT name="fromHours_<%= scheduleID %>" id="fromHours_<%= scheduleID %>">
<OPTION value="12" <%= (fromHours == 0)?"SELECTED":"" %>>12
<OPTION value="01" <%= (fromHours == 1)?"SELECTED":"" %>>1
<OPTION value="02" <%= (fromHours == 2)?"SELECTED":"" %>>2
<OPTION value="03" <%= (fromHours == 3)?"SELECTED":"" %>>3
<OPTION value="04" <%= (fromHours == 4)?"SELECTED":"" %>>4
<OPTION value="05" <%= (fromHours == 5)?"SELECTED":"" %>>5
<OPTION value="06" <%= (fromHours == 6)?"SELECTED":"" %>>6
<OPTION value="07" <%= (fromHours == 7)?"SELECTED":"" %>>7
<OPTION value="08" <%= (fromHours == 8)?"SELECTED":"" %>>8
<OPTION value="09" <%= (fromHours == 9)?"SELECTED":"" %>>9
<OPTION value="10" <%= (fromHours == 10)?"SELECTED":"" %>>10
<OPTION value="11" <%= (fromHours == 11)?"SELECTED":"" %>>11
</SELECT>:<SELECT name="fromMinutes_<%= scheduleID %>" id="fromMinutes_<%= scheduleID %>">
<OPTION VALUE="00" <%= (from.getMinutes() == 0)?"SELECTED":"" %>>00
<OPTION VALUE="15" <%= (from.getMinutes() == 15)?"SELECTED":"" %>>15
<OPTION VALUE="30" <%= (from.getMinutes() == 30)?"SELECTED":"" %>>30
<OPTION VALUE="45" <%= (from.getMinutes() == 45)?"SELECTED":"" %>>45
</SELECT> <SELECT name="fromAMPM_<%= scheduleID %>" id="fromAMPM_<%= scheduleID %>">
<OPTION value="AM" <%= (fromAMPM.equals("AM"))?"SELECTED":"" %>>AM
<OPTION value="PM" <%= (fromAMPM.equals("PM"))?"SELECTED":"" %>>PM
</SELECT>
</td>
<%
	String toAMPM = "AM";
	int toHours = to.getHours();
	if (toHours>11) {
		toAMPM = "PM";
		toHours -= 12;
	}
%>
<tr><td>To</td><td>
<SELECT name="toHours_<%= scheduleID %>" id="toHours_<%= scheduleID %>">
<OPTION value="12" <%= (toHours == 0)?"SELECTED":"" %>>12
<OPTION value="01" <%= (toHours == 1)?"SELECTED":"" %>>1
<OPTION value="02" <%= (toHours == 2)?"SELECTED":"" %>>2
<OPTION value="03" <%= (toHours == 3)?"SELECTED":"" %>>3
<OPTION value="04" <%= (toHours == 4)?"SELECTED":"" %>>4
<OPTION value="05" <%= (toHours == 5)?"SELECTED":"" %>>5
<OPTION value="06" <%= (toHours == 6)?"SELECTED":"" %>>6
<OPTION value="07" <%= (toHours == 7)?"SELECTED":"" %>>7
<OPTION value="08" <%= (toHours == 8)?"SELECTED":"" %>>8
<OPTION value="09" <%= (toHours == 9)?"SELECTED":"" %>>9
<OPTION value="10" <%= (toHours == 10)?"SELECTED":"" %>>10
<OPTION value="11" <%= (toHours == 11)?"SELECTED":"" %>>11
</SELECT>:<SELECT name="toMinutes_<%= scheduleID %>" id="toMinutes_<%= scheduleID %>">
<OPTION VALUE="00" <%= (to.getMinutes() == 0)?"SELECTED":"" %>>00
<OPTION VALUE="15" <%= (to.getMinutes() == 15)?"SELECTED":"" %>>15
<OPTION VALUE="30" <%= (to.getMinutes() == 30)?"SELECTED":"" %>>30
<OPTION VALUE="45" <%= (to.getMinutes() == 45)?"SELECTED":"" %>>45
</SELECT> <SELECT name="toAMPM_<%= scheduleID %>" id="toAMPM_<%= scheduleID %>">
<OPTION value="AM" <%= (toAMPM.equals("AM"))?"SELECTED":"" %>>AM
<OPTION value="PM" <%= (toAMPM.equals("PM"))?"SELECTED":"" %>>PM
</SELECT>
<input type="checkbox" name="allday_<%= scheduleID %>" <%= schedule.isAllDay()?"CHECKED":"" %>> All Day
</td></tr></table></td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif" width="10" height="20"></td></tr>

<%}%>
</table>