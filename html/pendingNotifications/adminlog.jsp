<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ page import="net.reliableresponse.notification.providers.*" %><%@ page import="java.util.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	User adminuser = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(adminuser, "Administrators")) {
		response.sendRedirect("ActionServlet?page=/index.jsp");
	}
%>
		<tr><td colspan="25" class="abovecell" width="100%" >&nbsp;</td></tr>
		<tr><td><img src="images/spacer.gif" width="12"></td>
		<td><table>
<%
		NotificationBroker notifBroker = BrokerFactory.getNotificationBroker();
		List<Notification> notifications = notifBroker.getNotificationsSince(new Date((long)0));
		BrokerFactory.getLoggingBroker().logDebug("Got "+notifications.size()+" notifs");
		
		StringBuffer output = new StringBuffer();
		for (Notification notification: notifications) {
			%><tr><td>From: <%= notification.getSender()%></td>
			<td><img src="images/spacer.gif" width="12"></td>
			<td>To: <%=notification.getRecipient()%></td></tr>
			<tr><td>Subject: <%=notification.getSubject()%></td>
			<td><img src="images/spacer.gif" width="12"></td>
			<td>Sent On: <%=notification.getTime()%></td></tr>
			<%
			List<NotificationProvider> providers = notification.getNotificationProviders();
			for (NotificationProvider provider: providers) {
				String statusOfSend = provider.getStatusOfSend(notification);
				if (statusOfSend == null) statusOfSend = "unknown";
				String color = "#FF0000";
				if (statusOfSend.toLowerCase().startsWith("succeed")) {
					color = "#02CD34";
				}
				%><tr><td>Device <%=provider.getName()%> <font color="<%= color %>"><%= statusOfSend %></font></td></tr>
				<%
			}
			
			NotificationMessage[] messages = notification.getMessages();
			for (int m = 0; m < messages.length; m++) {
				String addedBy = messages[m].getAddedby();
				if (addedBy == null) addedBy = "unknown";
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(addedBy);
				if (user != null) addedBy = user.toString();
				%><tr><td colspan="3">Message added by <%= addedBy %> on <%= messages[m].getAddedon() %></td></tr>
				<tr><td colspan="3"><%= messages[m].getMessage()%></td></tr>
				<%
			}
			%><tr><td colspan="3">&nbsp;</td></tr><%
		}
%>
</td></tr></table></td></tr>