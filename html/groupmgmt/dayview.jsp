<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<%
	Date now = (Date)session.getAttribute ("date");
	Group group = (Group)session.getAttribute ("group");
	String dateString = new SimpleDateFormat("MMMM dd, yyyy").format(now);

	Vector[][][] thisMonth = ((OnCallGroup)group).getMonth(now.getMonth(), now.getYear());
	Vector[][] today = new Vector[4][24];
	for (int h = 0; h < 24; h++) {
		today[0][h] = new Vector();
        today[1][h] = new Vector();
        today[2][h] = new Vector();
        today[3][h] = new Vector();

		int date = now.getDate()-1;
		for (int m = 0; m < 60; m++) {
			Vector members = thisMonth[m][h][date];
			BrokerFactory.getLoggingBroker().logDebug("Testing "+h+":"+m+" - "+members.size()+" members");
			for(int mem = 0; mem < members.size(); mem++) {
				Member member = (Member)members.elementAt (mem);
				BrokerFactory.getLoggingBroker().logDebug(""+h+":"+m+" - "+member);
				if (!today[m/15][h].contains(member)) {
					BrokerFactory.getLoggingBroker().logDebug(h+":"+m+" has member "+member);
					today[m/15][h].addElement(member);
				}
			}
		}
	}
%>
<span class="dayview">
<table  bgcolor="#2AB2F7">
<tr><td colspan="25" align="center"><b><em><%= dateString %></em></b></TD></TR>
<tr><TD> </TD><TD>00</TD><TD>15</TD><TD>30</TD><TD>45</TD></tr>
<%
for (int h = 0; h < 24; h++) {
String hour = ""+h;
String ampm = "am";

if ( h == 0) {
        hour = "12";
} else if (h < 10) {
        hour = "0"+hour;
} else if (h > 12) {
	hour = ""+(h-12);
}

if (h>=12) {
	ampm = "PM";
}
%><TR><TD><%= hour %> <%= ampm %></TD><%
for (int m = 0; m < 4; m++) {
	Vector members = today[m][h];
	if (members == null) {
		members = new Vector();
	}
	String color = "#b22222";
	if (members.size() > 0) {
		color = "#228b22";
	}
        %><TD bgcolor="<%= color %>" ><%
	for (int mem = 0; mem < members.size(); mem++) {
		Member member = (Member)members.elementAt(mem);
		%><%= member.toString() %><br/><%
        }
}
%></TR>
<%
}
%>
</span>

