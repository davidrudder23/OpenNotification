<%@ page import="java.util.*" %><%@ page import="net.reliableresponse.notification.sender.*" %><%@ page import="net.reliableresponse.notification.actions.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="java.math.BigInteger" %><%@ page import="java.util.Vector" %><%@ page import="net.reliableresponse.notification.util.SortedVector" %>
<jsp:directive.page import="java.util.Date"/><%
response.setContentType("text/xml");
%><?xml version="1.0" encoding="iso-8859-1"?>
<%
	BrokerFactory.getLoggingBroker().logDebug("uuid in rss jsp = "+(String)session.getAttribute("user"));
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));	
	BrokerFactory.getLoggingBroker().logDebug("user in rss jsp ="+user);
	SimpleDateFormat dateFormatter = new SimpleDateFormat ("HH:mm:ss z");

	boolean isAdmin = BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.ADMINISTRATOR) ||
	BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.OBSERVER);

	int numHours = 2;
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	BigInteger bigint = new BigInteger(""+numHours);
	bigint = bigint.multiply(new BigInteger("3600"));
	bigint = bigint.multiply(new BigInteger("1000"));
	
	List<Notification> recentNotifications = broker.getNotificationsSince(bigint.longValue());
	Vector sorted = new SortedVector();
	for (Notification recentNotification: recentNotifications) {
		if (recentNotification.getParentUuid() == null) {
			if (isAdmin) {
					sorted.addElement(recentNotification);
			} else {
				Member recipient = recentNotification.getRecipient();
				if (recipient.getType() == Member.USER) {
					if (recipient.equals(user)) {
						sorted.addElement(recentNotification);
					}
				} else {
					Group group= (Group)recipient;
					if (group.isMember(user)) {
						sorted.addElement(recentNotification);
					}
				}
			}
		}
	}
	
	%>
	
<!DOCTYPE rss PUBLIC 
  "-//Netscape Communications//DTD RSS 0.91//EN" 
  "http://my.netscape.com/publish/formats/rss-0.91.dtd"
>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
<channel>
    <title><%= user.getFirstName() %> <%= user.getLastName() %>'s Latest Notifications</title>
    <link><%= BrokerFactory.getConfigurationBroker().getStringValue("base.url") %></link>
    <description>The list of your most recent notifications.</description>
    <language>en-us</language>
    <%
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	for (int i = 0; i < sorted.size(); i++) {
		Notification notification = (Notification)sorted.elementAt(i);
		String status = "Active";
		switch (notification.getStatus()) {
		case Notification.CONFIRMED:
			status="Confirmed";
			break;
		case Notification.EXPIRED:
			status = "Expired";
			break;
		}
	%>    	
    <item>
      <title><%= notification.getSubject() %> (<%= status %>)</title>
      <link><%= BrokerFactory.getConfigurationBroker().getStringValue("base.url") %></link>
      <description><%
      	NotificationMessage[] messages = notification.getMessages();
      	for (int m = 0; m < messages.length; m++) {
      	%><%= messages[m].getMessage() %>
      	<%}%></description>
      <dc:creator><%= notification.getSender().toString() %></dc:creator>
      <dc:date><%= dateFormat.format(notification.getTime()) %></dc:date>    
    </item>
    <%}
    
    if (sorted.size() == 0) {
    %><item>
    <title>No new notifications</title>
    <link><%= BrokerFactory.getConfigurationBroker().getStringValue("base.url") %></link>
    <description>You have no new notifications</description>
    <dc:creator>Reliable Response</dc:creator>
      <dc:date><%= dateFormat.format(new Date()) %></dc:date>    
    <%}
    %>
  </channel>
</rss>