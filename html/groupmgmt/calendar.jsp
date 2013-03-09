<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<table border="0"><tr>
<%
        String groupUuid = (String)request.getAttribute("group_uuid");
%>
<input type="hidden" name="dayview_<%= groupUuid%>" id="dayview_<%= groupUuid%>" value="<%= request.getParameter("dayview_"+groupUuid) %>">
<%
	Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(groupUuid);
	if (group == null) {
		return;
	}

	for (int monthNum = 0; monthNum < 3; monthNum++) {
	%><td><%
	Date now = new Date();
	now.setMonth (now.getMonth()+monthNum);
	now.setDate(1);
	Vector[][][] thisMonth = ((OnCallGroup)group).getMonth(now.getMonth(), now.getYear());
	
	// This represents the days in the month grid (7 days by 6 weeks)
	String[] colors = new String[42];
	for (int i = 0; i < 42; i++) {
		colors[i] = "#EBF8FF";
	}
	String[] numbers = new String[42];
	for (int i = 0; i < 42; i++) {
		numbers[i] = "&nbsp;";
	}

	int date = now.getDay();
	for (int i = 0; i < thisMonth[0][0].length; i++) {
		boolean someCovered = false;
		boolean someEmpty = false;
		numbers[date+i]=(i+1)+"";
		for (int m = 0; m < 60; m++) {
			for (int h = 0; h < 24; h++) {
				if ((thisMonth[m][h][i] != null) && (thisMonth[m][h][i].size()>0)){
					someCovered = true;
				} else {
					someEmpty = true;
				}
			}
		}
		if (someCovered && someEmpty) { // partial
			colors[date+i]="#daa520";
		} else if (someCovered) {
			colors[date+i]="#228b22"; // all covered
		} else {
			colors[date+i]="#b22222"; // none covered
		}			
	}
	
	String monthYear = new SimpleDateFormat("MMMM, yyyy").format(now);
	String monthYearNumbers = (now.getMonth()+1)+""+(now.getYear()+1900);
	if (monthYearNumbers.length() < 6) {
		monthYearNumbers = "0"+monthYearNumbers;
	}
	%>
<!-- monthYearNuimbers=<%= monthYearNumbers %> -->
<table border="0" cellspacing="1" cellpadding="0">
<tr><td align="center" bgcolor="#2AB2F7"><%= monthYear %></td></tr>
<tr><td bgcolor="#2AB2F7">
<table border="0">
<tr><th width="20">S</th><th width="20">M</th><th width="20">T</th><th width="20">W</th><th width="20">T</th><th width="20">F</th><th width="20">S</th></tr>
<%
	for (int i = 0 ; i < 6; i++) {
%><tr><%
		for (int subDay = 0; subDay < 7; subDay++) {
			String num = numbers[(i*7)+subDay];
			if (num.length() < 2) {
				num = "0"+num;
			}
			String dateParamString = num+monthYearNumbers;
%>
<td bgcolor="<%= colors[(i*7)+subDay] %>" align="center"><div onclick="showDayView ('dayview_<%= group.getUuid() %>', '<%= dateParamString %>');"><%= numbers[(i*7)+subDay] %></div></td>
<%		}%>
</tr>
<%
	}
%>
</table>
</td></tr></table></td>
<%
	}%>
</tr></table>
<td>
<img src="images/led_forest_green.gif"> - Completely Covered<br>
<img src="images/led_goldenrod_yellow.gif"> - Partially Covered<br>
<img src="images/led_firebrick_red.gif"> - Not Covered<br>
</td></tr></table>
<table><tr><td><img src="images/spacer.gif" width="30"></td><td width="90%">
<%
	session.setAttribute ("group", group);
	String paramDateString = request.getParameter("dayview_"+group.getUuid());
	Date paramDate = null;
	if ((StringUtils.isEmpty(paramDateString)) ||(paramDateString.equals("null"))) {
		paramDate = new Date();
	} else {
		SimpleDateFormat paramDateFormat = new SimpleDateFormat ("ddMMyyyy");
		paramDate = paramDateFormat.parse(paramDateString);
	}
	session.setAttribute ("date", paramDate);
        paramDateString = new SimpleDateFormat("MMMM dd, yyyy").format(paramDate);
	String title = "<span class=\"subexpand\">&nbsp;Day View for "+paramDateString+"&nbsp;</span>";
%>
<table width="100%"><reliable:collapseable tag="dayView" title="<%= title %>" contentURL="/groupmgmt/dayview.jsp" tagClass="cellrule" /></table>
</td></tr></table>
