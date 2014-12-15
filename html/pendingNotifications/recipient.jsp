<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="net.reliableresponse.notification.actions.*" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.jsp.*" %>
<%@ page import="net.reliableresponse.notification.device.Device" %>
<%@ page import="net.reliableresponse.notification.providers.NotificationProvider" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<tr>
<td width="11" valign="top"><img src="images/spacer.gif" width="1" height="1"></td>
<td valign="middle">
<%
	String uuid = (String)request.getAttribute ("notification_uuid");
	Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
	
	Notification[] notifs = notification.getAllChildrenSentToUsers();
	for (Notification notif: notifs) {
			List<NotificationProvider> providers = notif.getNotificationProviders();
			User user = (User)notif.getRecipient();
			BrokerFactory.getLoggingBroker().logDebug("We got "+providers.size()+" providers for user "+user);
		%>
		<strong><%= user %> (sent <%= notif.getTime() %>)</strong><br>
		<%
			for (NotificationProvider provider: providers) {
				String status = "Pending";
				String icon = "images/led_yellow.gif";
				switch (provider.getStatus(notif)) {
				case Notification.CONFIRMED:
					status = "Received";
					icon = "images/led_green.gif";
					break;
				case Notification.DELIVERED:
					status = "Received";
					icon = "images/led_green.gif";
					break;
				case Notification.PENDING:
					status = "Pending";
					icon = "images/led_yellow.gif";
					break;
				case Notification.FAILED:
					status = "Failed";
					icon = "images/led_red.gif";
					break;
				default:
					status = "Unknown";
					icon = "images/led_disabled.gif";
					break;
				}
				%><img src="<%= icon %>">&nbsp;&nbsp;&nbsp;&nbsp;<%= provider.getName() %>: <%= status %><br><%
			}
		%><br><%
	}
	
%>


</td>
</tr>
<tr><td height="5"></td></tr>
