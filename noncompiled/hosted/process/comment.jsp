<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="java.text.SimpleDateFormat"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.sender.NotificationSender"/><%
// Load the user object
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

String uuid = (String)request.getParameter("uuid");
if (StringUtils.isEmpty(uuid)) {
	// TODO: give a decent error for no uuid
	return;
}
Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
if (notification == null) {	
	// TODO: give a decent error for no uuid
	return;
}

String browseType = request.getParameter("browse");
if (StringUtils.isEmpty(browseType)) {
	browseType = "all";
}

String comment = request.getParameter("comment");

NotificationSender sender = notification.getSender();
sender.handleResponse(notification, user, "comment", comment);

response.sendRedirect("../notification.jsp?browse="+browseType+"&uuid="+uuid);
%>