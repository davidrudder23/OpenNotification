<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.device.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.scheduling.*" %><%@ page import="net.reliableresponse.notification.priority.*" %><%@ page import="java.util.*" %><%	
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getAttribute("user"));
	if (user == null) {
		user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	}
	String className = (String)request.getAttribute("device_classname");
	Device device = null;
	try {
		device = (Device)Class.forName(className).newInstance();
	} catch (Exception anyExc) {
		BrokerFactory.getLoggingBroker().logError(anyExc);
	}
	BrokerFactory.getLoggingBroker().logDebug("device="+device);
	String saveName = "action_device_add_"+className;
	String cancelName = "action_device_cancel";
%><table border="0" cellspacing="0" cellpadding="0"><tr valign="top">
<td><img src="images/spacer.gif" width="10"></td><td>
<input type="hidden" name="device_classname" value="<%=className%>">

<table>
<%
	DeviceSetting[] settings = device.getAvailableSettings();
	for (int i = 0; i < settings.length; i++) {
		String name = settings[i].getName();
		BrokerFactory.getLoggingBroker().logDebug("name="+name);
	%><tr><td align="right"><%=name%></td>
<%
	// TODO: This area needs to handle other data types, like dates
	// right now, though, we only have strings and selecteds
	// Also, input validation might be nice
		String value = "";
		String key = settings[i].getName()+"_devicesetting";
		Vector options = settings[i].getOptions();
		BrokerFactory.getLoggingBroker().logDebug("options="+options);
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
	%><td><input name="<%= key %>" type="text" id="<%= key %>" value="<%= value %>" test="test"></td>
	<td>&nbsp</td>
	</tr>
	<%
		}		
	}
%>
</table>
                	<input type="image" src="images/btn_save.gif" width="52" height="24" name="<%= saveName %>">
                	<img src="images/spacer.gif" width="10" height="1">
                	<input type="image" src="images/btn_cancel.gif" width="64" height="24" name="<%= cancelName %>">

              </td>
              <%
              	for (int num = 1; num <= 3; num++) {
              		String ohChecked = "";
              		String onChecked = "";
              		String infChecked = "";
              		String vaChecked = "";
              		String outChecked = "";
              		String mChecked = "";
              		String neverChecked = "";
              		
              		Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, device, num);
              		if (priority == null) priority = new Priority(user);
              		
              		Schedule[] schedules = priority.getSchedules();
              		for (int s = 0; s < schedules.length; s++) {
              			String initials = schedules[s].getInitials();
              			if (initials.equals("OH")) ohChecked = " CHECKED";
              			if (initials.equals("ONH")) onChecked = " CHECKED";
              			if (initials.equals("INF")) infChecked = " CHECKED";
              			if (initials.equals("VA")) vaChecked = " CHECKED";
              			if (initials.equals("OOO")) outChecked = " CHECKED";
              			if (initials.equals("M")) mChecked = " CHECKED";
              			if (initials.equals("Never")) neverChecked = " CHECKED";
              		}
              		
              		String name = "prioritylist_"+num;
              %>
              <td><strong>Priority <%= num %>:<br>
              Don't Use When
              </strong>
              <br>
                <input type="hidden" name="user" value="<%= user.getUuid() %>">
             	<input type="checkbox" name="<%= name %>_inf" <%= infChecked %>>Message is Informational<br>
             	<input type="checkbox" name="<%= name %>_oh" <%= ohChecked %>>Off Hours<br>
             	<input type="checkbox" name="<%= name %>_onh" <%= ohChecked %>>On Hours<br>
              	<input type="checkbox" name="<%= name %>_va" <%= vaChecked %>>On Vacation<br>
                <% if (BrokerFactory.getCalendarBroker().isCalendaringEnabled()) { %>
	              	<input type="checkbox" name="<%= name %>_ooo" <%= outChecked %>>Out of Office<br>
	              	<input type="checkbox" name="<%= name %>_m" <%= mChecked %>>In a Meeting<br>
                <% } %>
              	<input type="checkbox" name="<%= name %>_never" <%= neverChecked %>>Never Use<br>   
	           </td>
              <%}%>
            </tr><tr><td>