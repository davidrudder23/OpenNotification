<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.usermgmt.Group" %>
<%@ page import="java.util.Date" %>
<%
	Date earliestNotifDate = BrokerFactory.getNotificationBroker().getEarliestNotificationDate();
	Date today = new Date(System.currentTimeMillis());
	int fromyear = earliestNotifDate.getYear();
	int frommonth = earliestNotifDate.getMonth();
	int fromdom = earliestNotifDate.getDate();
	int toyear = today.getYear();
	int tomonth = today.getMonth();
	int todom = today.getDate();
	BrokerFactory.getLoggingBroker().logDebug("todom="+todom);
%>	
<table><tr><td>Group Name? <SELECT name="groupname">
<%
	int numGroups = BrokerFactory.getGroupMgmtBroker().getNumGroups();
	Group[] groups = new Group[numGroups];
	numGroups = BrokerFactory.getGroupMgmtBroker().getGroups(groups.length, 0, groups);
	for (int i = 0; i < numGroups; i++) {
%>
	<OPTION value="<%= groups[i].getGroupName() %>"><%= groups[i].getGroupName() %>
<%
	}
%>
</SELECT></td>
<td>From</td><td>
<SELECT name="from_month">
<option value="00"<%= (frommonth==0)?"SELECTED":""%>>January
<option value="01"<%= (frommonth==1)?"SELECTED":""%>>February
<option value="02"<%= (frommonth==2)?"SELECTED":""%>>March
<option value="03"<%= (frommonth==3)?"SELECTED":""%>>April
<option value="04"<%= (frommonth==4)?"SELECTED":""%>>May
<option value="05"<%= (frommonth==5)?"SELECTED":""%>>June
<option value="06"<%= (frommonth==6)?"SELECTED":""%>>July
<option value="07"<%= (frommonth==7)?"SELECTED":""%>>August
<option value="08"<%= (frommonth==8)?"SELECTED":""%>>September
<option value="09"<%= (frommonth==9)?"SELECTED":""%>>October
<option value="10"<%= (frommonth==10)?"SELECTED":""%>>November
<option value="11"<%= (frommonth==11)?"SELECTED":""%>>December
</SELECT>
<SELECT name="from_day">
<%
	for (int dom = 1; dom <=31; dom++) {
		String selected = (fromdom == dom)?"SELECTED":"";
		%><option value="<%= dom %>"<%= selected %>><%= dom %>
		<%
	}
%>
</SELECT>
<SELECT name="from_year">
<%
	int thisYear = new Date().getYear();
	for (int i = fromyear; i <= thisYear; i++) {
		int displayYear = i+1900;
		String selected = (fromyear == i)?"SELECTED":"";
		%><OPTION value="<%= displayYear %>"<%= selected%>><%= displayYear %><%
	}
%>
</SELECT>
</td></tr>
<tr><td>&nbsp;</td><td>To</td><td><SELECT name="to_month">
<option value="00"<%= (tomonth==0)?"SELECTED":""%>>January
<option value="01"<%= (tomonth==1)?"SELECTED":""%>>February
<option value="02"<%= (tomonth==2)?"SELECTED":""%>>March
<option value="03"<%= (tomonth==3)?"SELECTED":""%>>April
<option value="04"<%= (tomonth==4)?"SELECTED":""%>>May
<option value="05"<%= (tomonth==5)?"SELECTED":""%>>June
<option value="06"<%= (tomonth==6)?"SELECTED":""%>>July
<option value="07"<%= (tomonth==7)?"SELECTED":""%>>August
<option value="08"<%= (tomonth==8)?"SELECTED":""%>>September
<option value="09"<%= (tomonth==9)?"SELECTED":""%>>October
<option value="10"<%= (tomonth==10)?"SELECTED":""%>>November
<option value="11"<%= (tomonth==11)?"SELECTED":""%>>December
</SELECT>
<SELECT name="to_day">
<%
	for (int dom = 1; dom <=31; dom++) {
		String selected = (todom == dom)?"SELECTED":"";
		%><option value="<%= dom %>"<%= selected %>><%= dom %>
		<%
	}
%>
</SELECT>
<SELECT name="to_year">
<%
	thisYear = today.getYear();
	for (int i = toyear; i <= thisYear; i++) {
		int displayYear = i+1900;
		String selected = (thisYear == i)?"SELECTED":"";
		%><OPTION value="<%= displayYear %>"<%= selected%>><%= displayYear %><%
	}
%>
</SELECT>
</td></tr></table>