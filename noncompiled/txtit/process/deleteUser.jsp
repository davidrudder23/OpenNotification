<%@page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"%>
<%@page import="net.reliableresponse.notification.sender.EmailSender"%>
<%@page import="net.reliableresponse.notification.util.PasswordGenerator"%>
<%@page import="net.reliableresponse.notification.usermgmt.Roles"%>
<%@page import="java.util.Hashtable"%>
<%@page import="net.reliableresponse.notification.device.EmailDevice"%>
<%@page import="net.reliableresponse.notification.actions.SendNotification"%>
<%@page import="net.reliableresponse.notification.sender.UserSender"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.NotificationException"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
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
	session.setAttribute("error", "You do not have the rights to delete that user");
	response.sendRedirect("../manage_group.jsp");
	return;	
}

String uuid = request.getParameter("uuid");
if (StringUtils.isEmpty(uuid)) {
	session.setAttribute("error", "Unable to delete that user");
	response.sendRedirect("../manage_group.jsp");
	return;		
}
User deletedUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
if (deletedUser == null) {
	session.setAttribute("error", "Unable to delete that user");
	response.sendRedirect("../manage_group.jsp");
	return;			
}

if (!group.isMember(deletedUser)) {
	session.setAttribute("error", "You do not have the rights to delete that user");
	response.sendRedirect("../manage_group.jsp");
	return;	
}

if (deletedUser.equals(user)) {
	session.setAttribute("error", "You can not delete yourself");
	response.sendRedirect("../manage_group.jsp");
	return;	
}
BrokerFactory.getUserMgmtBroker().deleteUser(deletedUser);
session.setAttribute("error", deletedUser+" deleted");
response.sendRedirect("../manage_group.jsp");
%>