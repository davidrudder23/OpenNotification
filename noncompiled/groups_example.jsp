<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%
	Group[] groups = new Group[1000];

	int numGroups = BrokerFactory.getGroupMgmtBroker().getGroups(1000,
			0, groups);
%>

<%@page import="net.reliableresponse.notification.usermgmt.Member"%>
<%@page import="net.reliableresponse.notification.usermgmt.EscalationGroup"%>
<%@page import="net.reliableresponse.notification.usermgmt.OnCallGroup"%>
<%@page import="java.util.Date"%>
<%@page import="net.reliableresponse.notification.usermgmt.OnCallSchedule"%><table>
	<%
		for (int groupNum = 0; groupNum < numGroups; groupNum++) {
			Group group = groups[groupNum];

			String type = "";
			switch (group.getType()) {
			case Group.BROADCAST:
				type = "Broadcast";
				break;
			case Group.ESCALATION:
				type = "Escalation";
				break;
			case Group.ONCALL:
				type = "OnCall";
				break;

			}
			
			Member[] members = group.getMembers();

		/***
		 * print the basic group info
		 ***/
	%>
	<tr><td colspan="2"><b><i><%=group.getGroupName()%></i></b></td></tr>
	<tr><td>Contact:</td><td><a href="mailto: <%= group.getEmailAddress() %>"><%= group.getEmailAddress() %></a></td></tr>
	<tr><td>Type:</td><td><%= type%> Group</td></tr>

<%
		/***
		 * print the group info that's specific to this group type
		 ***/
			if (group.getType() == Group.ESCALATION) {
				EscalationGroup escalationGroup = (EscalationGroup)group;
				String loopCheck = (escalationGroup.getLoopCount()>0)?"CHECKED":"";
%>
				<tr><td>Loop:</td><td><input type=checkbox <%= loopCheck %> disabled="disabled"/></td></tr>
<%	
			} else if (group.getType() == Group.ONCALL) {
				OnCallGroup oncallGroup = (OnCallGroup)group;
				Member[] oncallMembers = oncallGroup.getOnCallMembers(new Date());
				
%>
				<tr><td colspan="2">Members currently on call:</td></tr>
<%
				for (int memberNum = 0; memberNum < oncallMembers.length; memberNum++) {
					Member member = oncallMembers[memberNum];
%>
				<tr><td></td><td><%= member.toString() %></td></tr>
<%
				}

				OnCallSchedule[] schedules = oncallGroup.getOnCallSchedules();
				%>
				<tr><td colspan="2">On Call Schedules:</td></tr>
<%
				for (int schedNum = 0; schedNum < schedules.length; schedNum++) {
					OnCallSchedule schedule = schedules[schedNum];
					
					String repetition;
					
					switch (schedule.getRepetition()) {
					case OnCallSchedule.REPEAT_DAILY: repetition="days";
					break;
					case OnCallSchedule.REPEAT_WEEKLY: repetition="weeks";
					break;
					case OnCallSchedule.REPEAT_MONTHLY_DATE: repetition="months  (on the same date each month)";
					break;
					case OnCallSchedule.REPEAT_MONTHLY_DAY: repetition="months (on that day of the month)";
					break;
					}
%>
				<tr><td></td><td><%= members[schedNum].toString() %> is on call from <%= schedule.getFromDate() %> to <%= schedule.getToDate() %> every <%= schedule.getRepetitionCount() %> <%= schedule.getRepetition() %></td></tr>
<%
				}
			}
%>	

	<tr><td colspan="2">Members</td></tr>
<% 

			/***
			 * print the member list
			 ***/
			for (int memberNum = 0; memberNum < members.length; memberNum++) {
				Member member = members[memberNum];
%>
	<tr><td></td><td><%= member.toString() %></td></tr>
<%
			}
%>
	<%
		}
	%>
</table>
<br/>