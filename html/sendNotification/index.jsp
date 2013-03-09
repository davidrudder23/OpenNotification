<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="java.util.*" %>

<%@ taglib uri="/reliable.tld" prefix="reliable" %>


<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	String subject = request.getParameter ("send_subject");
	if (subject == null) subject = "";
	
	String message = request.getParameter ("send_message");
	if (message == null) message = "";
	
	String date = JSPHelper.getDateFormatter().format(new Date());
%>
	<tr><td class="abovecell" colspan="25">
<table width="100%"  border="0" cellspacing="3" cellpadding="0">
	<tr>
		<td valign="top" width="11"><img src="images/led_green.gif" alt="" width="11" height="11"></td>
		<td>Subject:</td><td>Message:</td><td>Recipients:</td></tr>
		<tr><td><img src="images/spacer.gif"></td>
		<td valign="top">
			<input name="send_subject" type="text" taborder="1" value="<%= subject %>" size="25" onchange="setAction('action_send_notification');">
			<br>
			<strong><%= user.toString() %><br>
			<%= date %></strong>
			<p><strong>
				<br>
				<input name="action_send_notification" type="image"  src="images/btn_send.gif" alt="send">
				<img src="images/spacer.gif" width="5" height="20">
				<input name="action_send_cancel" type="image" value="cancel" src="images/btn_cancel.gif" alt="cancel">
			</strong></p>
		</td>
<td valign="top"><textarea name="send_message" cols="35" rows="4" taborder="2" id="newmessage"  onchange="setAction('action_send_notification');"><%=message%></textarea></td>
<td valign="top">
<%
	// Add the selected members
	String[] userUuids = JSPHelper.getParameterEndings(request, "selected_user_");
	String[] groupUuids = JSPHelper.getParameterEndings(request, "selected_group_");
	
	for (int i = 0; i < userUuids.length; i++) {
		String name="selected_user_"+userUuids[i];
		String value=request.getParameter(name);
		%><input type="hidden" name="<%= name%>" value="<%= value %>" \><%		

		// Write out the devices
		name = "selected_userdevice_"+userUuids[i];
		String[] deviceUuids = request.getParameterValues(name);
		if (deviceUuids != null) {
			for (int d = 0; d < deviceUuids.length; d++) {
				%><input type="hidden" name="<%= name%>" value="<%= deviceUuids[d] %>" \><%
			}
		}
	}
	
	
	for (int i = 0; i < groupUuids.length; i++) {
		String name="selected_group_"+groupUuids[i];
		String value=request.getParameter(name);
		%><input type="hidden" name="<%= name%>" value="<%= value %>" \><%		
	}
	
%>
<select name="recipient_list" size="4" id="recipientlist" multiple>
<%
	for (int i = 0; i < userUuids.length; i++) {
		String uuid = userUuids[i];
		String paramName = "selected_user_"+userUuids[i];
		String text = request.getParameter(paramName);
		%><option value="<%= uuid %>"><%= text %></option><%
	}

	for (int i = 0; i < groupUuids.length; i++) {
		String uuid = groupUuids[i];
		String paramName = "selected_group_"+groupUuids[i];
		String text = request.getParameter(paramName);
		%><option value="<%= uuid %>"><%= text %></option><%
	}
	
	if ((groupUuids.length == 0) && (userUuids.length == 0)) {
		%><option value="none">Add Recipients Below</option><%
	}
%>
</select>
<br>
<br>
<center><input type="image" src="images/btn_remove.gif" width="70" height="24" name="action_remove_recipients"><br>
</center>
</td>
</tr>
<tr><td colspan="25" width="100%">
<%
	String searchTitle="<span class=\"subexpand\">&nbsp;search for recipient(s)&nbsp;</span>";
	String addTitle="<span class=\"subexpand\">&nbsp;add recipient(s) from list&nbsp;</span>";
%>
<reliable:collapseable tag="searchRecipients" title="<%= searchTitle %>"
contentURL="/sendNotification/search.jsp" tagClass="cellrule" opened="true">
</reliable:collapseable>

<reliable:collapseable tag="addFromList" title="<%= addTitle %>"
contentURL="/sendNotification/addlist.jsp" tagClass="cellrule" opened="true">
</reliable:collapseable>
</td></tr></table>
</td></tr>
	