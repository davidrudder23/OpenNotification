<%@ page import="net.reliableresponse.notification.broker.NotificationBroker" %>
<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.Notification" %>
<%@ page import="net.reliableresponse.notification.usermgmt.User" %>
<table border="1">
<tr><td>Subject</td><td>UUID</td><td>Sender</td><td>Recipient</td><td>Date</td></tr>
	<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
		%><h1>You must be an admin to use this page</h1><%
		return;
	}
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	Notification[] unconfirmed = broker.getAllUnconfirmedNotifications();
	for (int i = 0; i < unconfirmed.length; i++) {
		Notification notif = unconfirmed[i];
		%><tr><td><%=notif.getSubject()%></td><td><%=notif.getUuid()%></td><td><%= notif.getSender() %></td>
		<td><%=notif.getRecipient()%></td><td><%= notif.getTime()%></td></tr><%
	}
%>
</table>
<%= unconfirmed.length %> unconfirmed notifs found