
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
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
			.getNotificationsSentTo(group);
	SortedVector sortedNotifs = new SortedVector();
	for (int i = 0; i < notifs.length; i++) {
		Notification notif = notifs[i];
		notif.sortByTime();
		sortedNotifs.addElement(notif, false);
	}
	sortedNotifs.sort();
	notifs = (Notification[])sortedNotifs.toArray(new Notification[0]);
	int numActive = 0;
	int numConfirmed = 0;
	int numExpired = 0;
	int numOnhold = 0;
	
	for (int i = 0; i < notifs.length; i++) {
		switch (notifs[i].getStatus()) {
		case Notification.PENDING:
		case Notification.NORMAL:
			numActive++;
			break;
		case Notification.CONFIRMED:
			numConfirmed++;
			break;
		case Notification.EXPIRED:
			numExpired++;
			break;
		case Notification.ONHOLD:
			numOnhold++;
			break;
		}
	}
%>
<!--  active notifs -->
<div class="list">
          <div align="right">
<span class="greytype"><img src="images/led_green.gif" width="11" height="11"> active:<%=numActive%><img src="images/spacer.gif" width="5" height="1">
<img src="images/led_yellow.gif" width="11" height="11"> confirmed:<%= numConfirmed %><img src="images/spacer.gif" width="5" height="1">
<img src="images/led_red.gif" width="11" height="11"> expired:<%= numExpired %><img src="images/spacer.gif" width="5" height="1">
<img src="images/led_blue.gif" width="11" height="11"> on hold:<%= numOnhold %></span><br>
<a href="#" onClick="MM_openBrWindow('notification_list.jsp?list=all','pop','scrollbars=yes,resizable=yes,width=400,height=600')">view/print all</a>
</div></div>
<%
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm z");
	for (int i = 0; i < notifs.length; i++) {
		Notification notif = notifs[i];
		String date = formatter.format(notif.getTime());
		String style = "shadedbg list";
		String statusColor = "green";
		if (notif.getStatus() == Notification.CONFIRMED) {
			statusColor = "yellow";
		} else if (notif.getStatus() == Notification.EXPIRED) {
			statusColor = "red";
		} else if (notif.getStatus() == Notification.ONHOLD) {
			statusColor = "blue";
		} 
		if ((i&1)==1) {
			style = "list";
		}
		
		String url = "notification.jsp?browse=all&uuid="+notif.getUuid();

%>
<a href="#" class="<%= style %>" onClick="MM_openBrWindow('<%= url %>','pop','scrollbars=yes,width=400,height=600')">
<img src="images/led_<%= statusColor %>.gif" width="11" height="11" border="0"> <%= date %> - <%= notif.getSubject() %></a>
<%
	}
%>  
<!--  end active notifs -->
