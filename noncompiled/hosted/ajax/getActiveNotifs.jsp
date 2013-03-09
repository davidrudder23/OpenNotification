
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="java.util.Vector"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="java.text.SimpleDateFormat"/><%
	// Load the user object
	String userUuid = (String) session.getAttribute("user");
	User user = null;

	if (userUuid != null) {
		user = BrokerFactory.getUserMgmtBroker()
		.getUserByUuid(userUuid);
	}

	if (user == null) {
		response.sendRedirect("/notification/login.jsp");
	}
	String name = "";
	if (user != null)
		name = user.getFirstName() + " " + user.getLastName();

	// Load the group
	Group group = null;
	Group[] allGroups = BrokerFactory.getGroupMgmtBroker()
			.getGroupsOfMember(user);
	BrokerFactory.getLoggingBroker()
			.logDebug("all groups=" + allGroups);
	if (allGroups != null) {
		BrokerFactory.getLoggingBroker().logDebug(
		"all groups length=" + allGroups.length);
		for (int i = 0; i < allGroups.length; i++) {
			if (allGroups[i].isOwner(user, false)) {
				group = allGroups[i];
			}
		}
	}

	if (group == null) {
		group = new EscalationGroup();
		group.setAutocommit(false);
		group.setGroupName(name + "'s Group");
		group.addMember(user, -1);
		group.setOwner(0);
		group.setAutocommit(true);
		BrokerFactory.getGroupMgmtBroker().addEscalationGroup(
		(EscalationGroup) group);
	}

	Notification[] notifs = BrokerFactory.getNotificationBroker()
			.getMembersUnconfirmedNotifications(group);
	SortedVector sortedNotifs = new SortedVector();
	for (int i = 0; i < notifs.length; i++) {
		Notification notif = notifs[i];
		sortedNotifs.addElement(notif, false);
	}
	sortedNotifs.sort();
	notifs = (Notification[])sortedNotifs.toArray(new Notification[0]);
%>
<!--  active notifs -->
        
<div class="list" >
	<div align="right">
		<a href="#" onClick="MM_openBrWindow('notification_list.jsp?list=active','pop','scrollbars=yes,resizable=yes,width=400,height=600')">view/print all</a>
	</div>
</div>

<%
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm z");
	for (int i = 0; i < notifs.length; i++) {
		Notification notif = notifs[i];
		String date = formatter.format(notif.getTime());
		String style = "shadedbg list";
		if ((i&1)==1) {
			style = "list";
		}

		String url = "notification.jsp?browse=active&uuid="+notif.getUuid();
%>
<a href="#" class="<%= style %>" onClick="MM_openBrWindow('<%= url %>','pop','scrollbars=yes,width=400,height=600')">
<strong><%= date %> - <%= StringUtils.htmlEscape(notif.getSubject()) %></strong><br>
<%=  StringUtils.htmlEscape(notif.getDisplayText()) %></a>
<%
	}
%>  
<!--  end active notifs -->
