<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<%
	boolean filterGroups = false;
	String filter = (String)request.getAttribute("search_filter");
	if ((filter != null) && (filter.equals ("groups"))) {
		filterGroups = true;
	}
	%>

<td><table width="100%"  border="0" cellpadding="0" cellspacing="0">
<tr><td height="3"></td></tr>
<tr>

<td  colspan="3" valign="middle">
<table width="100%"  border="0" cellpadding="5" cellspacing="0" bgcolor="#EDEDED" class="maintable">
<tr><td><% if (!filterGroups) {%>Users<%}%></td><td>Groups</td><td></td></tr>
<tr><td>
<%
	long start = System.currentTimeMillis();
	if (!filterGroups) {
%>
<SELECT name="add_users_from_list" size="4" id="userlist" multiple>
<%
	int numUsers = BrokerFactory.getUserMgmtBroker().getNumUsers();
	if (numUsers > 500) { 
		numUsers = 500;
	}
	User[] users = new User[numUsers];
	
	BrokerFactory.getUserMgmtBroker().getUsers(users.length, 0, users);
	
	SortedVector sortedUsers = new SortedVector();
	for (int i = 0; i < users.length; i++) {
		sortedUsers.addElement(users[i], false);
	}
	sortedUsers.sort();
	
	for (int i = 0; i < sortedUsers.size(); i++) {
		User user = (User)sortedUsers.elementAt(i);
		String uuid = user.getUuid();
%><option value="<%= uuid %>"><%= StringUtils.htmlEscape(user.toString()) %>
<%
	}
	long end = System.currentTimeMillis();
	BrokerFactory.getLoggingBroker().logDebug("Building user list took "+(end-start)+" millis");
%>
</SELECT>
<%}%>
</td>
<td>
<SELECT name="add_groups_from_list" size="4" id="grouplist" multiple>
<%
	start = System.currentTimeMillis();
	SortedVector sortedGroups = new SortedVector();
	int numGroups = BrokerFactory.getGroupMgmtBroker().getNumGroups();
	if (numGroups > 500) numGroups = 500;
	
	Group[] groups = new Group[numGroups];
	numGroups = BrokerFactory.getGroupMgmtBroker().getGroups(numGroups, 0, groups);
	for (int i = 0; i < numGroups; i++) {
		sortedGroups.addElement(groups[i], false);
	}
	sortedGroups.sort();
	BrokerFactory.getLoggingBroker().logDebug("SortedGroups has "+sortedGroups.size()+" groups");
	for (int i = 0; i < sortedGroups.size(); i++) {
		Group group = (Group)sortedGroups.elementAt(i);
		if (group != null) {
			String uuid = group.getUuid();
			if (group.getMembers().length <1) {
%><option class="nomembers" value="<%= uuid %>"><%= StringUtils.htmlEscape(group.toString()) %> (0 members)<%
			} else {
%><option value="<%= uuid %>"><%= StringUtils.htmlEscape(group.toString()) %><%
			}
		}
	}
	long end = System.currentTimeMillis();
	BrokerFactory.getLoggingBroker().logDebug("Building group list took "+(end-start)+" millis");
%></SELECT></td>
<td>
<input type="image" name="action_add_from_list" src="images/btn_addselected.gif"><br>
</td>
</tr>
<tr><td colspan="25"><img src="images/spacer.gif" height="5" width="10"></td></tr></table>
</td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
</table>
