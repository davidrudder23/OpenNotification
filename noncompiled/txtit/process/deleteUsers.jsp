<%@page import="java.util.Enumeration"%>
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

StringBuffer errors = new StringBuffer();
Enumeration paramNames = request.getParameterNames();
while ((paramNames != null) && (paramNames.hasMoreElements())) {

	String paramName = (String)paramNames.nextElement();
	
	if (paramName.startsWith  ("delete_")) {
		String uuid = paramName.substring("delete_".length(), paramName.length());

		if (!StringUtils.isEmpty(uuid)) {
			User deletedUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
			if (deletedUser != null) {
				if (!group.isMember(deletedUser)) {
					errors.append("<font color=\"red\">You do not have the rights to delete "+deletedUser+"</font><br>");
				} else {
					if (deletedUser.equals(user)) {
						errors.append("<font color=\"red\">You can not delete yourself</font><br>");
					} else {
						BrokerFactory.getUserMgmtBroker().deleteUser(deletedUser);
						errors.append(deletedUser+" deleted<br>");
					}
				}
			}
		}
	}
}
session.setAttribute("error", errors.toString());
response.sendRedirect("../manage_group.jsp");
%>