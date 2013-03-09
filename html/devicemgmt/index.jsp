<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.device.*" %>
<%@ page import="net.reliableresponse.notification.priority.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<tr><td class="abovecell" colspan="25">&nbsp;</td></tr>
<tr>
<td colspan="23" width="100%">
<table width="100%">
<%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)request.getAttribute("user"));
	if (user == null) {
		user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	}

	Device[] devices = user.getDevices();
	for (int d = 0; d < devices.length; d++) {
		String tagName = "deviceDetails_"+devices[d].getUuid();
		
		String openedString = request.getParameter("opened."+tagName);
		boolean opened =  ((openedString != null) && (openedString.toLowerCase().equals ("true")));
		if (request.getParameter("action_toggle_collapseable."+tagName+".x") != null) {
			opened = !opened;
		}
		
		String deviceDetailsTitle = "<strong>";
		if (!opened) {
			deviceDetailsTitle += devices[d].toString();
		} else {
			deviceDetailsTitle += devices[d].getName();
		}
		deviceDetailsTitle += "</strong></td>\n";

		if (!opened) {
			deviceDetailsTitle += "<td>";
			for (int num = 1; num <= 3; num++) {
				Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, devices[d], num);
				String initials = "Free";
				if (priority != null) {
					initials = priority.getInitials();
				}
				deviceDetailsTitle += "<strong>"+num+":</strong>"+initials+" ";
			}
			deviceDetailsTitle += "</td>";
		}
		
		request.setAttribute ("devicedetails", devices[d].getUuid());
		%><reliable:collapseable tag="<%= tagName %>" title="<%= deviceDetailsTitle %>"
contentURL="/devicemgmt/devicedetails.jsp" tagClass="individualgroup">
</reliable:collapseable><%
	}
%>
<tr><td colspan="52">	
&nbsp;
</td></tr>

<%
	String[] classNames = BrokerFactory.getDeviceBroker().getDeviceClassNames();
	for (int i = 0; i < classNames.length; i++) {
		BrokerFactory.getLoggingBroker().logDebug("classname="+classNames[i]);
		Device device = null;
		try {
			device = (Device)Class.forName(classNames[i]).newInstance();
			BrokerFactory.getLoggingBroker().logDebug("device="+device);
			String tagName = "addNew"+device.getName();
			String title = "<span class=\"subexpand\">&nbsp;+ add a new "+device.getName()+" device&nbsp;</span>";
			request.setAttribute("device_classname", classNames[i]);
%>
<tr><td colspan="52">	
<reliable:collapseable tag="<%= tagName%>" title="<%= title %>"
contentURL="/devicemgmt/newdevice.jsp" tagClass="cellrule">
</reliable:collapseable>
</td></tr>
<%
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
		}
	}
%>
<tr><td height="5"></td></tr>
</table></td>
<td><img src="images/spacer.gif" width="11"></td>
</tr>
