<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<script lanugage="javascript">

function showDayView (field, date) {
   document.mainform.action.value = "refresh";
   document.getElementById(field).value = date;
   document.mainform.submit();
}

</script>
<tr><td colspan="25" class="abovecell">&nbsp;</td></tr>
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	
	Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
	SortedVector sorted = new SortedVector(groups);
	groups = (Group[])sorted.toArray(new Group[0]);
	
	for (int g = 0; g < groups.length; g++) {
		Group group = groups[g];
		if ((g==0) || (!(groups[g-1].equals(group)))) {
	
		String tagName = "individualGroupSettings_"+group.getUuid()+"_"+g;
		String openedString = request.getParameter("opened."+tagName);
		boolean opened =  ((openedString != null) && (openedString.toLowerCase().equals ("true")));
		if (request.getParameter("action_toggle_collapseable."+tagName+".x") != null) {
			opened = !opened;
		}
	
			
		String individualGroupSettingsTitle = "";
		if (opened) {
			individualGroupSettingsTitle = "<input name=\"groupname_"+groups[g].getUuid()+"\" type=\"text\" id=\"groupname\" value=\"";
			individualGroupSettingsTitle += StringUtils.htmlEscape(group.getGroupName());
			individualGroupSettingsTitle += "\" size=\"25\" onchange=\"setAction('action_group_save_"+group.getUuid()+"');\"></td>";
		} else {
			individualGroupSettingsTitle = "<strong>"+StringUtils.htmlEscape(group.toString())+"</strong></td>";
		}
		individualGroupSettingsTitle += "<td>";
		individualGroupSettingsTitle += group.getMembers().length;
		individualGroupSettingsTitle += "</td><td>";
		
		if (group.getType() == Member.ESCALATION) {
			individualGroupSettingsTitle+="Escalation Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else if (group.getType() == Member.BROADCAST) {
			individualGroupSettingsTitle+="Broadcast Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else if (group.getType() == Member.ONCALL) {
			individualGroupSettingsTitle+="On Call Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else {
			individualGroupSettingsTitle+="Group";
		}
		
		int groupPriority = BrokerFactory.getUserMgmtBroker().getPriorityOfGroup (user, group);
		if (groupPriority == 0) groupPriority = 3;
		individualGroupSettingsTitle += "</td><td>Priority: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
		
		for (int p = 1;  p <= 3; p++) {
		individualGroupSettingsTitle += "<td>"+p+"&nbsp;</td>";
		individualGroupSettingsTitle += "<td><input name=\"priority_"+g+"_"+group.getUuid()+"\" type=\"radio\" value=\""+p+"\"";
			if (groupPriority == p) {
				individualGroupSettingsTitle += " checked";
			}
		if (opened) {
			individualGroupSettingsTitle += " onclick=\"document.mainform.action_save_"+group.getUuid()+".click()\"";
		} else {
			individualGroupSettingsTitle += " onclick=\"document.mainform.submit()\"";
		}
		individualGroupSettingsTitle += ">&nbsp;</td>";
		}		
				
		request.setAttribute ("individualGroupUuid", group.getUuid());
		String contentURL = "/groupmgmt/groupdetails.jsp";
		if (group.getType() == Group.ONCALL) {
			contentURL = "/groupmgmt/oncalldetails.jsp";
		}
	%>

<tr><td colspan="9"><table width="100%">
<reliable:collapseable tag="<%= tagName %>" title="<%= individualGroupSettingsTitle %>"
contentURL="<%= contentURL %>" tagClass="individualgroup">
</reliable:collapseable>
</table></td></tr>
		
<%		}
	}

	
	groups = new Group[BrokerFactory.getGroupMgmtBroker().getNumGroups()];
	BrokerFactory.getGroupMgmtBroker().getGroups(groups.length, 0, groups);
	sorted = new SortedVector(groups);
	groups = (Group[])sorted.toArray(new Group[0]);
	
	for (int g = 0; g < groups.length; g++) {
		Group group = groups[g];
		if ( !group.isMember(user) && 
				( (g==0) || 
				  (!(groups[g-1].equals(group)))
				 )
		    ) {
	
		String tagName = "individualGroupSettings_"+group.getUuid()+"_"+g;
		String openedString = request.getParameter("opened."+tagName);
		boolean opened =  ((openedString != null) && (openedString.toLowerCase().equals ("true")));
		if (request.getParameter("action_toggle_collapseable."+tagName+".x") != null) {
			opened = !opened;
		}
	
			
		String individualGroupSettingsTitle = "";
		if (opened) {
			individualGroupSettingsTitle = "<input name=\"groupname_"+groups[g].getUuid()+"\" type=\"text\" id=\"groupname\" value=\"";
			individualGroupSettingsTitle += StringUtils.htmlEscape(group.getGroupName());
			individualGroupSettingsTitle += "\" size=\"25\" onchange=\"setAction('action_group_save_"+group.getUuid()+"');\"></td>";
		} else {
			individualGroupSettingsTitle = "<strong>"+StringUtils.htmlEscape(group.toString())+"</strong></td>";
		}
		individualGroupSettingsTitle += "<td>";
		individualGroupSettingsTitle += group.getMembers().length;
		individualGroupSettingsTitle += "</td><td>";
		
		if (group.getType() == Member.ESCALATION) {
			individualGroupSettingsTitle+="Escalation Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else if (group.getType() == Member.BROADCAST) {
			individualGroupSettingsTitle+="Broadcast Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else if (group.getType() == Member.ONCALL) {
			individualGroupSettingsTitle+="On Call Group&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		} else {
			individualGroupSettingsTitle+="Group";
		}
		
		int groupPriority = BrokerFactory.getUserMgmtBroker().getPriorityOfGroup (user, group);
		if (groupPriority == 0) groupPriority = 3;
		individualGroupSettingsTitle += "</td><td>Priority: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
		
		for (int p = 1;  p <= 3; p++) {
		individualGroupSettingsTitle += "<td>"+p+"&nbsp;</td>";
		individualGroupSettingsTitle += "<td><input name=\"priority_"+g+"_"+group.getUuid()+"\" type=\"radio\" value=\""+p+"\"";
			if (groupPriority == p) {
				individualGroupSettingsTitle += " checked";
			}
		if (opened) {
			individualGroupSettingsTitle += " onclick=\"document.mainform.action_save_"+group.getUuid()+".click()\"";
		} else {
			individualGroupSettingsTitle += " onclick=\"document.mainform.submit()\"";
		}
		individualGroupSettingsTitle += ">&nbsp;</td>";
		}		
				
		request.setAttribute ("individualGroupUuid", group.getUuid());
		String contentURL = "/groupmgmt/groupdetails.jsp";
		if (group.getType() == Group.ONCALL) {
			contentURL = "/groupmgmt/oncalldetails.jsp";
		}
	%>

<tr><td colspan="9"><table width="100%">
<reliable:collapseable tag="<%= tagName %>" title="<%= individualGroupSettingsTitle %>"
contentURL="<%= contentURL %>" tagClass="individualgroup">
</reliable:collapseable>
</table></td></tr>
		
<%		}
	}
	request.setAttribute("search_response_action_name", "add_group_action");
	request.setAttribute("search_filter", "groups");
	String addTitle="<span class=\"subexpand\">&nbsp;+ join groups(s) from list&nbsp;</span>";
%>
<tr><td>&nbsp;</td></tr>
<tr><td colspan="9"><table width="100%">
<tr><td>
<reliable:collapseable tag="searchForGroup" title="<span class=\"subexpand\">&nbsp;+ join a group&nbsp;</span>"
contentURL="/sendNotification/search.jsp" tagClass="cellrule">
</reliable:collapseable></td></tr> 
</tr></td></table>
<tr><td height="5"></td></tr>
<tr><td colspan="9"><table width="100%">
<reliable:collapseable tag="addFromList" title="<%= addTitle %>"
contentURL="/sendNotification/addlist.jsp" tagClass="cellrule" >
</reliable:collapseable></td></tr>

</tr></td></table>
<tr><td height="5"></td></tr>
<tr><td colspan="9"><table width="100%">
<tr><td>
<reliable:collapseable tag="addGroup" title="<span class=\"subexpand\">&nbsp;+ create a new group&nbsp;</span>"
contentURL="/groupmgmt/newgroup.jsp" tagClass="cellrule">
</reliable:collapseable></td></tr> 
</tr></td></table>
<tr><td height="5"></td></tr>
