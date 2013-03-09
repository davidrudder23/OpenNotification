<%@ page import="net.reliableresponse.notification.usermgmt.User" %>
<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.usermgmt.Member" %>
<%@ page import="net.reliableresponse.notification.usermgmt.Group" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, "Administrators")) {
		response.sendRedirect("ActionServlet?page=/administration.jsp");
		return;
	}
	String[] roles = BrokerFactory.getAuthorizationBroker().getRoles();
	String role = request.getParameter("role");
	BrokerFactory.getLoggingBroker().logDebug("role from request="+role);
	if ((role == null) || (role.length() ==0)) {
		role = "Administrators";
	}
	
	String memberType = request.getParameter("member_type");
	if ((memberType == null) || (memberType.equals(""))) {
		memberType="users";
	}
	
%>

<script type="text/javascript" language="JavaScript">
</script>
<tr><td colspan="25" class="abovecell" width="100%">&nbsp;</td></tr>
<td><table width="100%"  border="0" cellpadding="0" cellspacing="0">
<tr><td>
<SELECT name="member_type" onchange="document.mainform.submit()">
<option value="users" <%= memberType.equalsIgnoreCase("users")?"SELECTED":"" %>>Users
<option value="groups" <%= memberType.equalsIgnoreCase("groups")?"SELECTED":"" %>>Groups
</select>
</td><td>&nbsp;</td><td>
<SELECT name="role" onchange="document.mainform.submit()">
<%
	for (int i = 0; i < roles.length; i++) {
		String checked = "";
		if (roles[i].equalsIgnoreCase(role)) {
			checked = " SELECTED";
		}	
	%><option value="<%= roles[i] %>" <%= checked %>><%= roles[i] %>
	<%
	}
%>
</select>
</td><tr>
<tr><td>
<%	
	Member[] members =  new Member[0];
	if (memberType.equalsIgnoreCase("users")) {
		members = new User[BrokerFactory.getUserMgmtBroker().getNumUsers()];
		BrokerFactory.getUserMgmtBroker().getUsers(members.length, 0, (User[])members);
		%><select name="users_to_add" multiple><%
	} else {
		members = new Group[BrokerFactory.getGroupMgmtBroker().getNumGroups()];
		BrokerFactory.getGroupMgmtBroker().getGroups(members.length, 0, (Group[]) members);
		%><select name="groups_to_add" multiple><%
	}
	for (int i = 0; i < members.length; i++) {
	%><option value="<%= members[i].getUuid() %>"><%= members[i] %><%
	}
	%>
	</select>
</td><td>
<table>
<tr><td>
<input type="image" src="images/btn_right.gif" name="add_members_to_role">
</td></tr><tr><td>
<input type="image" src="images/btn_left.gif" name="remove_members_from_role">
</td></tr></table>
</td><td>
<%
	members =  BrokerFactory.getAuthorizationBroker().getMembersInRole(role);
	%><select name="members_in_role" multiple><%
		for (int i = 0; i < members.length; i++) {
		%><option value="<%= members[i].getUuid() %>"><%= members[i] %><%
		}
	%>
	</select>
</td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
<tr><td>Create a new Role</td><td>&nbsp</td>
<td><input type="text" name="new_role" onchange="document.mainform.submit()"></td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
</table>