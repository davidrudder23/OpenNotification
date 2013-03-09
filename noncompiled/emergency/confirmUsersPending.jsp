<%@ page import="net.reliableresponse.notification.broker.NotificationBroker" %>
<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.Notification" %>
<%@ page import="net.reliableresponse.notification.usermgmt.User" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="net.reliableresponse.notification.usermgmt.Member" %>
<%@ page import="net.reliableresponse.notification.util.SortedVector" %>
<%@ page import="java.util.Enumeration" %>
<%
	User responder = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(responder, "Administrators")) {
		%><h1>You must be an admin to use this page</h1><%
		return;
	}

	// This section handles a confirmAll request
	String recipientUuid = request.getParameter("recipient");
	if (recipientUuid != null) {
		Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(recipientUuid);
		if (member == null) {
			member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(recipientUuid);
		}
		
		if (member == null) {
			%><%= recipientUuid %> Not Found
			<%
		} else {
			Notification[] unconfirmed = BrokerFactory.getNotificationBroker().getMembersUnconfirmedNotifications(member);
			for (int i = 0; i < unconfirmed.length; i++) {
				unconfirmed[i].getSender().handleResponse(unconfirmed[i], responder, "Confirm", "Administrator confirmed");
				%>Confirmed notification <%= unconfirmed[i].getUuid() %>
				<%
			}
		}
	}
%>
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
		%><h1>You must be an admin to use this page</h1><%
		return;
	}
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	Notification[] unconfirmed = broker.getAllUnconfirmedNotifications();
	Hashtable users = new Hashtable();
	SortedVector sortedUsers = new SortedVector();
	for (int i = 0; i < unconfirmed.length; i++) {
		Member recipient = unconfirmed[i].getRecipient();
		Integer count = (Integer)users.get(recipient.getUuid());
		if (count == null) {
			count = new Integer(1);
			sortedUsers.addElement(recipient);
		} else {
			count = new Integer (count.intValue()+1);
		}
		users.put (recipient.getUuid(), count);
	}
		
	%><form action="confirmUsersPending.jsp" method="GET" name="mainform" id="mainform">
	<SELECT name="recipient" onchange="mainform.submit();">
	<OPTION value="" SELECTED>--
	<%
	for (int i = 0; i < sortedUsers.size(); i++) {
		Member member = (Member)sortedUsers.elementAt(i);
		%><OPTION value="<%= member.getUuid() %>"><%= member.toString() %> has <%= users.get(member.getUuid())%> notifs
		<%
	}
	%></SELECT>
	</form>
	<%
%>