<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.Notification" %>
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	String confirmUuid = request.getParameter("id");
	if (confirmUuid == null) {
		%>Please enter an ID of a notification to respond to<br>
		<%
		return;
	}
	Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(confirmUuid);
	if (notification == null) {
		%>Please enter a valid notification ID<br>
		<%
		return;
	}
	String responseType = request.getParameter("response");
	if (responseType == null) {
		%>Please enter a response to that notification<br>
		<%
		return;
	}
	String text = request.getParameter("text");
	notification.getSender().handleResponse(notification, user, responseType, text);
	%>Notification <%= confirmUuid %> responded to with <%= responseType%> <%
%>