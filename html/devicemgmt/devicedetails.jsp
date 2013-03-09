<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.device.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.scheduling.*" %>
<%@ page import="net.reliableresponse.notification.priority.*" %>
<%@ page import="java.util.*" %><%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getAttribute("user"));
	if (user == null) {
		user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	}
	String uuid = (String)request.getAttribute ("devicedetails");
	Device device = user.getDeviceWithUuid(uuid);	
	
	String saveName = "action_device_edit_"+device.getUuid();
	String removeName = "action_device_remove_"+device.getUuid();
	String cancelName = "action_device_cancel_"+device.getUuid();
%><table><tr valign="top">
<td><img src="images/spacer.gif" width="12"></td>
<td>
<input type="hidden" name="deviceuuid" value="<%= uuid %>">
<table><%
	DeviceSetting[] settings = device.getAvailableSettings();
	for (int i = 0; i < settings.length; i++) {
		String name = settings[i].getName();
	%><tr><td><%=name%></td>
<%
	// TODO: This area needs to handle other data types, like dates
	// right now, though, we only have strings and selecteds
	// Also, input validation might be nice
		String value = device.getSettings().get(name).toString();
		String key = settings[i].getName()+"_devicesetting_"+uuid;
		Vector options = settings[i].getOptions();
		if (options != null) {
			%><td><select name="<%= key %>">
			<%
			for (int o = 0; o < options.size(); o++) {
				String option = (String)options.elementAt(o);
				String selected = "";
				if (option.toLowerCase().equals (value.toLowerCase())) selected = " SELECTED ";
		%><option value="<%=  option %>"<%= selected %>><%= option %>
		<%
			}
			%></select></td></tr><%
		} else if (settings[i].getType() == Boolean.class) {
			%><td><input name="<%= key %>" type="checkbox"  id="<%= key %>" ></td></tr>
			<%
		} else {
	%><td><input name="<%= key %>" type="text" size="30" id="<%= key %>" value="<%= value %>"></td></tr>
	<%
		}		
	}
%>
</table>
                	<input type="image" src="images/btn_save.gif" width="52" height="24" name="<%= saveName %>">
                	<img src="images/spacer.gif" width="10">
                	<input type="image" src="images/btn_remove.gif" width="64" height="24" name="<%= removeName %>">
                	<img src="images/spacer.gif" width="10" height="1">
                	<input type="image" src="images/btn_cancel.gif" width="64" height="24" name="<%= cancelName %>">

              </td>
              <%
              	for (int num = 1; num <= 3; num++) {
              		String ohChecked = "";
              		String vaChecked = "";
              		String infChecked = "";
              		String outChecked = "";
              		String onChecked = "";
              		String mChecked = "";
              		String neverChecked = "";
              		
              		Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, device, num);
              		if (priority == null) priority = new Priority(user);
              		
              		Schedule[] schedules = priority.getSchedules();
              		for (int s = 0; s < schedules.length; s++) {
              			String initials = schedules[s].getInitials();
              			if (initials.equals("OH")) ohChecked = " CHECKED";
              			if (initials.equals("VA")) vaChecked = " CHECKED";
              			if (initials.equals("INF")) infChecked = " CHECKED";
              			if (initials.equals("OOO")) outChecked = " CHECKED";
              			if (initials.equals("ONH")) onChecked = " CHECKED";
              			if (initials.equals("M")) mChecked = " CHECKED";
              			if (initials.equals("Never")) neverChecked = " CHECKED";
              		}
              		
              		String name = "prioritylist_"+num+"_"+device.getUuid();
              %><td><strong>&nbsp;Priority <%= num %>:<br>&nbsp;Don't Use When
              </strong>
              	<br>
                <input type="hidden" name="user" value="<%= user.getUuid() %>">
              	<input type="checkbox" name="<%= name %>_inf" <%= infChecked %>>Message is Informational<br>
              	<input type="checkbox" name="<%= name %>_oh" <%= ohChecked %>>Off Hours<br>
              	<input type="checkbox" name="<%= name %>_onh" <%= onChecked %>>On Hours<br>
              	<input type="checkbox" name="<%= name %>_va" <%= vaChecked %>>On Vacation<br>
                <% if (BrokerFactory.getCalendarBroker().isCalendaringEnabled()) { %>
	              	<input type="checkbox" name="<%= name %>_ooo" <%= outChecked %>>Out of Office<br>
	              	<input type="checkbox" name="<%= name %>_m" <%= mChecked %>>In a Meeting<br>
                <% } %>
              	<input type="checkbox" name="<%= name %>_never" <%= neverChecked %>>Never Use<br>
              </td>
              <%}%>
                  <br>
              </strong>
              <br></td>
            </tr>
<tr><td>&nbsp;</td></tr>
</table>
