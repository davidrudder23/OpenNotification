<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<%
	Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid((String)request.getAttribute ("individualGroupUuid"));
	String groupUuid = group.getUuid();
	String saveName = "action_group_save_"+group.getUuid();
	String cancelName = "action_group_cancel_"+group.getUuid();
	String descriptionName = "description_"+group.getUuid();
	String actionRemoveGroupName = "action_remove_group_"+group.getUuid();
	String actionAddSearch = "action_add_recipients_"+group.getUuid();
	String loopName = "loop_"+group.getUuid();
	String ownerName = "owner_"+group.getUuid();
	Member[] members = group.getMembers();
	
%>
              <tr valign="bottom">
                <td nowrap="on"><p><strong><img src="images/spacer.gif" width="10" height="1">
                	<input type="image" src="images/btn_save.gif" width="52" height="24" name="<%= saveName %>">
                	<img src="images/spacer.gif" width="10" height="1">
                	<input type="image" src="images/btn_remove.gif" width="70" height="24" name="<%= actionRemoveGroupName %>">
                	<img src="images/spacer.gif" width="10" height="1">
                	<input type="image" src="images/btn_cancel.gif" width="64" height="24" name="<%= cancelName %>">
                </strong></p></td>
                <td valign="top" colspan="2">
                  <textarea name="<%= descriptionName %>" cols="23" rows="4" id="description"><%= group.getDescription() %></textarea></td>
                <td valign="top" nowrap colspan="7">
                   <table width="100%"  border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
<table frame="border">
<tr bgcolor="#ededed"><td><b>Member</b></td><td>Owner<td></td></tr>
<%	for (int i = 0; i < members.length; i++) {
	String color="#FFFFFF";
	if ((i&1)==0) color= "#ECF8FF";
	String ownerNameParam = ownerName+"_"+i;
	String ownerSelected = group.isOwner(members[i], false)?"CHECKED":"";
	%>
<tr bgcolor=<%= color %>>
<td><%= members[i].toString()%></td>
<td><input type="checkbox" name="<%= ownerNameParam %>" <%=ownerSelected%>></td>
<%
		String removeName = "action_remove_selected_"+group.getUuid()+"_"+i;
%>
<td><input type="image" src="images/btn_delete.gif" name="<%= removeName %>"></td></tr>
<%}%>
</table>                        
</td></tr>
                      <tr>
                        <td colspan="2"><img src="images/spacer.gif" width="1" height="2"></td>
                      </tr>
                    </table>
                </td>
              </tr>
              <tr>
                <td colspan="25" valign="top">
                <table width="100%"  border="0" cellspacing="0" cellpadding="0"><tr><td>
                <img src="images/spacer.gif" width="12">
                </td><td>
                <table width="100%"  border="0" cellspacing="3" cellpadding="0">
<%
	String title="<span class=\"subexpand\">&nbsp;add new recipient(s)&nbsp;</span>";
	String schedulesTitle="<span class=\"subexpand\">&nbsp;schedule(s)&nbsp;</span>";
	String tag="add_new_recipients_"+group.getUuid();
	String scheduleTag = "schedules_"+group.getUuid();
	String calendarTitle = "<span class=\"subexpand\">&nbsp;calendar&nbsp;</span>";
	String calendarTag = "calendar_"+group.getUuid();
	request.setAttribute("search_response_action_name", "add_selected_"+group.getUuid());
	request.setAttribute("group_uuid", group.getUuid());
%>
<reliable:collapseable tag="<%= calendarTag %>" title="<%= calendarTitle %>"
contentURL="/groupmgmt/calendar.jsp" tagClass="cellrule">
</reliable:collapseable>

<reliable:collapseable tag="<%= scheduleTag %>" title="<%= schedulesTitle %>"
contentURL="/groupmgmt/schedules.jsp" tagClass="cellrule">
</reliable:collapseable>

<reliable:collapseable tag="<%= tag %>" title="<%= title %>"
contentURL="/sendNotification/search.jsp" tagClass="cellrule">
</reliable:collapseable>
               </table>
               </td></tr>
                  </table></td></tr>
