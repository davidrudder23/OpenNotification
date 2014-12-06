<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="java.util.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ page import="net.reliableresponse.notification.providers.*" %><%@ page import="java.util.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
%><jsp:include page="header.jsp" />

<tr><td colspan="4" class="mainarea">
<input type="hidden" name="page" value="/notification_log.jsp" />

<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
<table width="100%" cellspacing="0" border="0" cellpadding="0">

<%
		NotificationBroker notifBroker = BrokerFactory.getNotificationBroker();
		List<Notification> notifications = notifBroker.getNotificationsSince(new Date((long)0));
		
		StringBuffer output = new StringBuffer();
		for (Notification notification: notifications) {
			%><tr><td>From: <%= notification.getSender()%></td>
			<td>To: <%=notification.getRecipient()%></td></tr>
			<tr><td>Subject: <%=notification.getSubject()%></td>
			<td>Sent On: <%=notification.getTime()%></td></tr>
			<%
			NotificationProvider[] providers = notification.getNotificationProviders();
			for (int p = 0; p < providers.length; p++) {
				String statusOfSend = providers[p].getStatusOfSend(notification);
				if (statusOfSend == null) statusOfSend = "unknown";
				String color = "#FF0000";
				if (statusOfSend.toLowerCase().startsWith("succeed")) {
					color = "#000000";
				}
				%><tr><td>Device <%=providers[p].getName()%> <font color="<%= color %>"><%= statusOfSend %></font></td></tr>
				<%
			}
			
			NotificationMessage[] messages = notification.getMessages();
			for (int m = 0; m < messages.length; m++) {
				String addedBy = messages[m].getAddedby();
				if (addedBy == null) addedBy = "unknown";
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(addedBy);
				if (user != null) addedBy = user.toString();
				%><tr><td colspan="2">Message add by <%= addedBy %> on <%= messages[m].getAddedon() %></td></tr>
				<tr><td colspan="2"><%= messages[m].getMessage()%></td></tr>
				<%
			}
			%><tr><td colspan="2">&nbsp;</td></tr><%
		}
%>
<jsp:include page="footer.jsp" />