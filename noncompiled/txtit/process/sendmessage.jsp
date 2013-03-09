<%@page import="net.reliableresponse.notification.sender.TXTItSender"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.sender.UserSender"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.actions.SendNotification"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%
User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));

Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
Group group = null;
for (int groupNum = 0; groupNum < groups.length; groupNum++) {
	if (groups[groupNum] instanceof BroadcastGroup) {
		group = groups[groupNum];
	}
}
if (group == null) {
	group = new BroadcastGroup();
	group.setGroupName(user.getFirstName()+" "+user.getLastName()+"'s Group");
	BrokerFactory.getGroupMgmtBroker().addGroup(group);
	group.setAutocommit(true);
	group.addMember(user, -1);
}

String message = request.getParameter("smsMessage");
if (StringUtils.isEmpty(message)) {
	session.setAttribute("error", "Please enter a message to send");
	response.sendRedirect("../send_message.jsp");
	return;
}
Notification notif = new Notification(null, group, new TXTItSender(user), "", message); 
SendNotification.getInstance().doSend(notif);

session.setAttribute("error", "Message sent");
response.sendRedirect("../send_message.jsp");
%>