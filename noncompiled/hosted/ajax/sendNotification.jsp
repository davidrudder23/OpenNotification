<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.sender.EmailSender"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.actions.SendNotification"/>
<jsp:directive.page import="net.reliableresponse.notification.sender.UserSender"/><%
String responseString = "";
try {
	String subject = request.getParameter("subject");
	String message = request.getParameter("message");
	BrokerFactory.getLoggingBroker().logDebug("Sending notification");
	BrokerFactory.getLoggingBroker().logDebug("subject="+subject);
	BrokerFactory.getLoggingBroker().logDebug("message="+message);
	
	String userUuid = (String) session.getAttribute("user");
	User user = null;

	if (userUuid != null) {
		user = BrokerFactory.getUserMgmtBroker()
		.getUserByUuid(userUuid);
	}

	if (user == null) {
		response.sendRedirect("/notification/login.jsp");
	}
	String name = "";
	if (user != null)
		name = user.getFirstName() + " " + user.getLastName();

	// Load the group
	Group group = null;
	Group[] allGroups = BrokerFactory.getGroupMgmtBroker()
			.getGroupsOfMember(user);
	BrokerFactory.getLoggingBroker()
			.logDebug("all groups=" + allGroups);
	if (allGroups != null) {
		BrokerFactory.getLoggingBroker().logDebug(
		"all groups length=" + allGroups.length);
		for (int i = 0; i < allGroups.length; i++) {
			if (allGroups[i].isOwner(user, false)) {
		group = allGroups[i];
			}
		}
	}

	if (group == null) {
		group = new EscalationGroup();
		group.setAutocommit(false);
		group.setGroupName(name + "'s Group");
		group.addMember(user, -1);
		group.setOwner(0);
		group.setAutocommit(true);
		BrokerFactory.getGroupMgmtBroker().addEscalationGroup(
		(EscalationGroup) group);
	}

	Notification notification = new Notification(null, group, new UserSender(user), subject, message);
	SendNotification.getInstance().doSend(notification);
	responseString = "Sent Message";
} catch (Exception e) {
	responseString = "Error: "+e.getMessage();
}
%><%= responseString %>