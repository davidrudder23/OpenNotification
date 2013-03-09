
<%@page import="net.reliableresponse.notification.NotificationMessage"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%><%

String objectID = (String)request.getParameter("objectid");
System.out.println ("objectid="+objectID);
if (StringUtils.isEmpty(objectID)) return;

String recordID = (String)request.getParameter("recordid");
System.out.println ("recordid="+recordID);
if (StringUtils.isEmpty(recordID)) return;

User user = BrokerFactory.getUserMgmtBroker().getUserByInformation("LongJump UUID", objectID+":"+recordID);
System.out.println ("user="+user);
if (user == null) return;

Notification[] notifications = BrokerFactory.getNotificationBroker().getNotificationsSentTo(user);
System.out.println ("We found "+notifications.length+" notifs sent to "+user);
boolean needsComma = false;

for (int notifNum = 0; notifNum < notifications.length; notifNum++) {
	Notification notification = notifications[notifNum];
	NotificationMessage[] messages = notification.getMessages();
	System.out.println ("We found "+messages.length+" messages sent to "+user+" in notif "+notification.getUuid());

	for (int msgNum = 0; msgNum < messages.length; msgNum++) {
		NotificationMessage message = messages[msgNum];
		System.out.println ("content type="+message.getContentType());
		if (message.getContentType().equalsIgnoreCase("x-application/longjumpresponse")) {
			if (needsComma) out.write(",");
			needsComma = true;
			out.write(new String(message.getContent()));
		}
	}
}
%>