<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.device.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>

<%
	String color = (String)request.getAttribute ("color");
	if ((color == null)  || (color.length() == 0)) color = "#FFFFFF";
	
	String uuid = (String)request.getAttribute ("member");
	if (uuid == null) return;
	Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
	if (member == null) member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);
%>

<tr bgcolor="<%=color%>">
<%
	if (member instanceof Group) { 
		Group group = (Group)member;
%>
<td></td>
<td colspan="25">
<% if (BrokerFactory.getConfigurationBroker().getStringValue("email.method").equalsIgnoreCase("smtp")) { %>
<img src="images/rr_icon.gif">: <%= group.getEmailAddress() %>
<% } else if (BrokerFactory.getConfigurationBroker().getBooleanValue("email.pop.checkAll", true)) { %>
<img src="images/rr_icon.gif">: <%= group.getEmailAddress() %>
<% } else {%>&nbsp;<%}%>
</td></tr>
<tr><td></td>
<td colspan="3" bgcolor="<%= color %>"><textarea name="description" cols="35" rows="4" readonly="readonly" id="description"><%=group.getDescription() %></textarea>
<textarea name="recipientlist" rows="4" disabled="disabled" id="recipientlist" cols="14" wrap="off"><%  Member[] members = group.getMembers();
	for (int i = 0; i < members.length; i++) {
		String name = members[i].toString();
	%><%= name %>
<%}%>
</textarea></td>
<%} else {
	User user = (User)member;
	List<Device> devices = user.getDevices();
	if (devices == null) devices = new ArrayList<Device>();
%>

	<td width="11">&nbsp;</td>
	<td colspan="4"><table  border="0" cellspacing="0" cellpadding="2">
	<tr>
		<td align="right">
		<font color="#666666">
		<table cellspacing="0" cellborder="0">
		
		<% if (BrokerFactory.getConfigurationBroker().getStringValue("email.method").equalsIgnoreCase("smtp")) {
			%><tr><td align="right"><img src="images/rr_icon.gif"></td><td valign="center">: <%= user.getEmailAddress() %></td></tr>
		<% } else if (BrokerFactory.getConfigurationBroker().getBooleanValue("email.pop.checkAll", true)) {
			%><tr><td align="right"><img src="images/rr_icon.gif"></td><td valign="center">: <%= user.getEmailAddress() %></td></tr>
		<%}%>
		<tr><td align="right"><img src="images/rr_icon.gif"></td><td valign="center" align="left">: <%= user.getUuid() %></td></tr>
		<tr><td align="right"><img src="images/rr_icon.gif"></td><td valign="center" align="left">: <%= BrokerFactory.getAuthenticationBroker().getIdentifierByUser(user) %></td></tr>
		<tr><td align="right"><font color="#666666">Department</font></td><td valign="center" align="left">: <%= user.getDepartment() %></td></tr>
        </font>
        <%
    	for (Device device: devices) {
        	String deviceUserCheckName = "add_device_notification_"+user.getUuid()+"_"+device.getUuid();
        %>
        <tr><td align="right"><font color="#666666"><%= device.getName() %></font></td><td align="left">: <%= device.getShortIdentifier() %></td>
        <td><input type="checkbox" name="<%= deviceUserCheckName %>"></td></tr>
        <%}%>
		</table></td><td>
    </tr></table></td>
    <%}%>
</tr>
