<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%
	String uuid = (String)request.getParameter("user_uuid");
	if (uuid == null) {
		uuid = (String)request.getAttribute("user_uuid");
	}
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
%>

<table>
<tr><td>First Name</td><td><input type="text" name="usermgmt_firstname_<%= user.getUuid() %>" value="<%= user.getFirstName() %>" onchange="setAction('action_update_user_<%= user.getUuid() %>')"></td></tr>
<tr><td>Last Name</td><td><input type="text" name="usermgmt_lastname_<%= user.getUuid() %>" value="<%= user.getLastName() %>" onchange="setAction('action_update_user_<%= user.getUuid() %>')"></td></tr>
<tr><td>Email Address</td><td><input type="text" name="usermgmt_email_<%= user.getUuid() %>" value="<%= user.getEmailAddress() %>" onchange="setAction('action_update_user_<%= user.getUuid() %>')"></td></tr>
<tr><td colspan="2"><input type="image" src="images/edit.gif" name="action_update_user_<%= user.getUuid() %>"></td></tr>
</table>