<%@ page import="net.reliableresponse.notification.broker.NotificationBroker" %>
<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.Notification" %>
<%@ page import="net.reliableresponse.notification.usermgmt.User" %>
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
		%><h1>You must be an admin to use this page</h1><%
		return;
	}
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	Notification[] unconfirmed = broker.getAllUnconfirmedNotifications();
	for (int i = 0; i < unconfirmed.length; i++) {
		unconfirmed[i].getSender().handleResponse(unconfirmed[i], user, "Confirm", "Confirming all messages");
		%>Notification <%= unconfirmed[i].getUuid() %> confirmed<br/><%
	}
%>