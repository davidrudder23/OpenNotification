
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/><%
	String numString = request.getParameter("num");
	int num = 0;
	try {
		num = Integer.parseInt(numString);
	} catch (NumberFormatException nfExc) {
		%>Please try again
		<%
		return;
	}
	
	String groupUuid = request.getParameter("group");
	if (StringUtils.isEmpty(groupUuid)) {
		%>Please try again
		<%
		return;
	}
	
	String timeString = request.getParameter("time");
	int time = 0;
	try {
		time = Integer.parseInt(timeString);
	} catch (NumberFormatException nfExc) {
		%>Please make sure the escalation time is a number
		<%
		return;
	}

	EscalationGroup group = (EscalationGroup)BrokerFactory.getGroupMgmtBroker().getGroupByUuid(groupUuid);
	group.setAutocommit(true);
	group.setEscalationTime(num, time);
	BrokerFactory.getLoggingBroker().logDebug("Group with uiud="+groupUuid+" is "+group.getGroupName()+" and has "+group.getMembers().length+" members");
	Member member = group.getMembers()[num];
%>
Set escalation for <%= member.toString() %> to <%= time %>