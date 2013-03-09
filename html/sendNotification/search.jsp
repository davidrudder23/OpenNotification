<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<script type="text/javascript" language="JavaScript">
function setCheckUsersDevices(user, theElement) {
	
	var pattern= new RegExp("add_device_notification_0*"+user);
	for (i=0; i < document.mainform.elements.length; i++) {
		if (document.mainform.elements[i].name.match(pattern) != null) {
			document.mainform.elements [i].checked = theElement.checked;
		}
	}
}
</script>
<td><table width="100%"  border="0" cellpadding="0" cellspacing="0">
<tr><td height="3"></td></tr>
<tr>

<td  colspan="3" valign="middle">
<table width="100%"  border="0" cellpadding="5" cellspacing="0" bgcolor="#EDEDED" class="maintable">
<tr>
<td width="29%">substring:<br>
<input name="recipient_search_substring" type="text" id="keywords" onchange="setAction('action_search_recipients');">
<br></td>
<td width="21%">&nbsp;type:<br>
<%
	String searchFilter = (String)request.getAttribute("search_filter");
	if (searchFilter == null) searchFilter = "none";
	
	if (searchFilter.equals("groups")) {
%>
<input type="hidden" name="recipient_search_type" value="any group">
		search for all groups
<%
	} else {
%>
<select name="recipient_search_type">
<option value="any" selected>search all</option>
<option value="any group">all groups</option>
<option value="escalation group">escalation group</option>
<option value="broadcast group">broadcast group</option>
<option value="any individual">individual recipient</option>
</select>
<%
	}
%>
</td>
<%
	String addAction = (String)request.getAttribute ("search_response_action_name");
	BrokerFactory.getLoggingBroker().logDebug ("addAction="+addAction);
	if (addAction == null) addAction = "add_selected";
	String groupUuidForAdd = (String)request.getAttribute ("groupUuidForAdd");
	if (groupUuidForAdd != null) {
		addAction = "add_selected_"+groupUuidForAdd;
	}
%>
<td width="11%"><input name="action_search_recipients" type="image" value="search" src="images/btn_search.gif" alt="search"></td>
<td width="39%" align="right"><input name="<%= addAction %>" type="image" value="<%= addAction %>" src="images/btn_addselected.gif" alt="add selected"></td>
</tr>
<%
	String[] messages = request.getParameterValues("search_system_message");
	if (messages != null) {
		for (int m = 0; m < messages.length; m++) {
	%>
<tr><td colspan="25" class="mainarea"><span class="systemalert"><%= messages[m] %></span></td></tr>
	<%
		}
	} 

%>
</table>
<table width="100%" bgcolor="#FFFFFF" cellspacing="0" cellpadding="0">
<%	
	String[] titles = JSPHelper.getParameterEndings(request, "recipient_search_found_");
	BrokerFactory.getLoggingBroker().logDebug("Found "+titles.length+" titles");
	if (titles != null) {
		boolean darken = true;
		
		SortedVector members = new SortedVector();
		for (int t = 0; t < titles.length; t++) {
			Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(titles[t]);
			if (member == null) member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(titles[t]);
			if (member != null)
				members.addElement(member);
		}
		
		for (int i = 0; i < members.size(); i++) {
			Member member = (Member)members.elementAt(i);
			String paramName = "recipient_search_found_"+member.getUuid();

			String title = request.getParameter (paramName);
			String tag="collapsetag_"+paramName;
			if (title != null) {
				String color="#FFFFFF";
				if (darken) color= "#ECF8FF";
				darken = !darken;
				request.setAttribute ("color", color);
				request.setAttribute ("member", member.getUuid());
				%>
				<input type="hidden" name="found_recipient" value="<%= member.getUuid() %>">
				<reliable:collapseable tag="<%= tag %>" title="<%= title %>"
				contentURL="/sendNotification/foundMember.jsp" tagClass="plain"
				color="<%= color %>">
				</reliable:collapseable>
				<%
			}
		}
	}
	%>
</td></tr></table>
</td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
</table>