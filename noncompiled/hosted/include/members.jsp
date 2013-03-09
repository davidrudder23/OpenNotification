<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/>
<jsp:directive.page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.PagerDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.JabberDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.EmailDevice"/><%


//EscalationGroup group = null;
	Member[] members = group.getMembers();
	while (members.length < 4) {
		User newUser = new User ();
		newUser.setFirstName("User "+members.length);
		newUser.setLastName(user.getDepartment());
		BrokerFactory.getUserMgmtBroker().addUser(newUser);
		group.addMember(newUser, -1);
		members = group.getMembers();
	}
	boolean doLoop = ((EscalationGroup)group).getLoopCount() <= 0;
%>

<script language="JavaScript">
function toggleLoop() {
	
    http.open('get', 'ajax/toggleLoop.jsp?'+Math.random());
    http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    http.onreadystatechange = receiveUserMessage;
	http.send(null);
	
	return false;
}
</script>
	<table width="284"  border="0" cellpadding="0" cellspacing="0" class="box">
    	<tr>
          <td bgcolor="#EDEDED" class="header_box"> Group Member Settings </td>
        </tr>
<%
	for (int i = 0; i < 4; i++) {
		String escTimeLabel = "escalation_time_"+i; 
		int escTime = ((EscalationGroup)group).getEscalationTime(i);
		String emailLabel = "email_"+group+"_"+i;
		String cellLabel = "cell_"+group+"_"+i;
		String pagerLabel = "pager_"+group+"_"+i;
		String imLabel = "im_"+group+"_"+i;
		
		User member = (User)members[i];
		String cellValue, emailValue, pagerValue, imValue;
		String cellProvider, pagerProvider, jabberServer;
		cellValue = emailValue = pagerValue = imValue = cellProvider = pagerProvider = jabberServer = "";
		Device[] devices = member.getDevices();
		for (int d = 0; d < devices.length; d++) {
			Device device = devices[d];
			if (device instanceof CellPhoneEmailDevice) {
				cellValue = (String)device.getSettings().get("Phone Number");
				cellProvider = (String)device.getSettings().get("Provider");
			} else if (device instanceof EmailDevice) {
				emailValue = (String)device.getSettings().get("Address");
			} else if (device instanceof PagerDevice) {
				pagerValue = (String)device.getSettings().get("Pager Number");
				pagerProvider = (String)device.getSettings().get("Provider");
			} else if (device instanceof JabberDevice) {
				imValue = (String)device.getSettings().get("Account Name");
				jabberServer = (String)device.getSettings().get("Server Name");
			}
		}
		
		String style = "shadedbg";
		if ((i&1)==1) {
			style = "";
		}

		%>
        <tr>
          <td colspan="2" class="abovecell"><table width="100%"  border="0" cellpadding="7" cellspacing="0">
           	<tr>
           		<td valign="top" align="left">
           			<a href="#" onClick="MM_openBrWindow('membersettings.jsp?group=<%= group.getUuid() %>&member=<%= i %>','pop','scrollbars=yes,width=450,height=500')">
                    <%=member.toString() %></a> - <%= ((EscalationGroup)group).getEscalationTime(i) %> minutes
                </td>
                <% if (i>0) { %>
                <td valign="top">
                  	<div align="center">
					<form name="moveup" method="post" action="process/moveUp.jsp">
						<input type="hidden" name="membernum" value="<%= i %>">
                    	<input name="Submit" type="image" value="Submit" src="images/btn_moveup.gif" alt="Move Member Up" align="right">
                    </form>
                  	</div>
                </td>
                  <% } else {%>
                  <td valign="top">
					<div align="center">
					<form name="moveup" method="post" action="process/moveUp.jsp">
						<img src="images/spacer.gif" width="10" height="10">
					</form>
					</div>
				  </td>
                  <% } %>
          </tr></table></td>
        </tr>
<% } %>  
    	<tr>
          <td align="right" bgcolor="#EDEDED" class="header_box" colspan="2"><label><span class="greytype">
          When escalation reaches bottom, start over?
            <input type="checkbox" name="checkbox" <%= doLoop?"CHECKED":"" %> onclick="toggleLoop();">
          </span></label></td>
        </tr>
	</table>
</form>
