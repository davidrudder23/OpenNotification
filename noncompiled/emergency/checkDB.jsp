
<jsp:directive.page import="net.reliableresponse.notification.broker.DatabaseBroker"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Roles"/><%

User responder = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
if (!BrokerFactory.getAuthorizationBroker().isUserInRole(responder, Roles.ADMINISTRATOR)) {
	%><h1>You must be an admin to use this page</h1><%
	return;
}

DatabaseBroker broker = BrokerFactory.getDatabaseBroker();
%>
<html><head><title>Check Database</title></head>
<body>
<h1>Checking the Database</h1>
<table>
<tr><td>Num Open</td><td><%= broker.getNumOpenConnections() %></td></tr>
<tr><td>Num Idle</td><td><%= broker.getNumIdleConnections() %></td></tr>
</table>
</body>
