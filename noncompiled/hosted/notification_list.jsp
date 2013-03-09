
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/><%
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

String listType = request.getParameter("list");
if (StringUtils.isEmpty(listType)) {
	listType = "all";
}
Notification[] notifs = BrokerFactory.getNotificationBroker()
.getNotificationsSentTo(group);
SortedVector sortedNotifs = new SortedVector();
for (int i = 0; i < notifs.length; i++) {
	Notification notif = notifs[i];
	notif.sortByTime();
	if (listType.equals("active")) {
		if ((notif.getStatus() == Notification.PENDING) ||
		(notif.getStatus() == Notification.NORMAL)) {
			sortedNotifs.addElement(notif, false);
		}
	} else {
		sortedNotifs.addElement(notif, false);
	}
}

sortedNotifs.sort();

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Reliable Response Notification System</title>
<style type="text/css">
<!--
body {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	line-height: 15px;
}
-->
</style>
</head>

<body>
<%
	for (int i = 0; i < sortedNotifs.size(); i++) {
		Notification notif = (Notification)sortedNotifs.elementAt(i);		
%>
<table cellspacing="0" cellpadding="0">
<tr><td align="right"><strong>Subject:</strong></td><td><strong><%= StringUtils.htmlEscape(notif.getSubject()) %></strong></td></tr>
<tr><td align="right"><strong>From:</strong></td><td><strong><%= StringUtils.htmlEscape(notif.getSender().toString()) %></strong></td></tr>
<tr><td align="right"><strong>Date:</strong></td><td><strong><%= user.getFormattedDate(notif.getTime(), "MM/dd/yyyy - HH:mm:ss z") %></strong></td></tr>
<tr><td align="right"><strong>Status:</strong></td><td><strong><%= StringUtils.capitalize(notif.getStatusAsString()) %></strong></td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
<tr><td></td><td>
  <%
  NotificationMessage[] messages = notif.getMessages();
  for (int m = 0; m < messages.length; m++) {
	  NotificationMessage message = messages[m];
	  String senderString = message.getAddedby();
	  if (!StringUtils.isEmpty(senderString)) {
		  User sender = BrokerFactory.getUserMgmtBroker().getUserByUuid(senderString);
		  if (sender != null) {
			  senderString = sender.toString();
		  }
	  }
	  if (!StringUtils.isEmpty(senderString)) {
		  %><%= senderString %> - <%
	  }
	  %><%= user.getFormattedDate(message.getAddedon(), "MM/dd/yyyy - HH:mm:ss z") %><br/>
	  <%= StringUtils.htmlEscape(message.getMessage()) %><br/>
	  <br/><% 
  }
  %>
  </td></tr></table><br/>
<% } %>
</body>
</html>
