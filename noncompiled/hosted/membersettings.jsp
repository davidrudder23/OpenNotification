
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/>
<jsp:directive.page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.EmailDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.JabberDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.PagerDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.DeviceSetting"/>
<jsp:directive.page import="java.util.Vector"/>
<jsp:directive.page import="net.reliableresponse.notification.device.TwoWayPagerDevice"/>
<jsp:directive.page import="java.util.TimeZone"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="net.reliableresponse.notification.device.TelephoneDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.VoiceShotDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Roles"/><%
String userUuid = (String) session.getAttribute("user");
User user = null;

if (userUuid != null) {
	user = BrokerFactory.getUserMgmtBroker()
	.getUserByUuid(userUuid);
}

if (user == null) {
	response.sendRedirect("/notification/login.jsp");
}

String groupUuid = request.getParameter("group");
if (StringUtils.isEmpty(groupUuid)) {
	// TODO: give a good error
	return;
}
Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(groupUuid);
if (group == null) {
	// TODO: give a good error
	return;
}

if (!group.isMember(user)) {
	// TODO: give a good error
	return;
}

String memberNumString = request.getParameter("member");
if (StringUtils.isEmpty(memberNumString)) {
	// TODO: give a good error
	return;
}

int memberNum = 0;
try {
	memberNum = Integer.parseInt (memberNumString);
} catch (NumberFormatException nfExc) {
	// TODO: give a good error
	return;	
}
User member = (User)group.getMembers()[memberNum];

String cellValue, emailValue, pagerValue, jabberValue, phoneNumber;
String cellProvider, pagerProvider, jabberServer;
cellValue = emailValue = pagerValue = jabberValue = cellProvider = pagerProvider = jabberServer = phoneNumber = "";

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
		jabberValue = (String)device.getSettings().get("Account Name");
		jabberServer = (String)device.getSettings().get("Server Name");
	} else if (device instanceof VoiceShotDevice) {
		phoneNumber = (String)device.getSettings().get("Phone Number");
	}
}

String loginName = BrokerFactory.getAuthenticationBroker().getIdentifierByUser(member);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Notify &amp; Acknowledge Member Settings</title>
<link href="rr.css" rel="stylesheet" type="text/css" />
<style type="text/css">
<!--
body {
	background-color: #19A1E6;
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style></head>

<body>
<script>
function doSubmit() {
	window.opener.location.reload();
	document.membersettings.submit();
}
</script>

<form id="membersettings" name="membersettings" method="post" action="process/memberSettings.jsp">
<input type="hidden" name="membernum" value="<%= memberNumString %>" />
<div align="center">
<table width="410" border="0" cellpadding="0" cellspacing="0" class="box">
  <tr>
    <td bgcolor="#EDEDED" class="header_box">Member Settings Edit</td>
    <tr>
      <td bgcolor="#FFFFFF"><br />
        <table width="400" border="0" align="center" cellpadding="5" cellspacing="0">
<%
	String[] messages = (String[])session.getAttribute("membersettings_messages");
	session.setAttribute("membersettings_messages", null);
	if (messages != null) {
		for (int m = 0; m < messages.length; m++) {
		%>
				<tr>
					<td colspan="25"><font color="#cc0000"><%= messages[m] %></font></td>
				</tr>
		<%}
	}
%>
        
        <tr>
          <td align="right">Login Name : </td>
          <td align="left">
          <%
	        if (StringUtils.isEmpty(loginName)) {
        		%><span class="button" onclick="window.open('setAuthentication.jsp?uuid=<%= member.getUuid() %>');">&nbsp;Allow Login&nbsp;</span>
        	<%} else { %>
        		<%= loginName %>
        	<%} %>
          </td>
        </tr>
        <%
        if ((member.equals(user)) || 
        		BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.TEAM_LEADER)){
        %>
        <tr>
          <td align="right" nowrap>Password : </td>
          <td align="left" nowrap><input name="password" type="password" id="password" /></td>
        </tr>
        <tr>
          <td align="right" nowrap>Confirm Password : </td>
          <td align="left" nowrap><input name="confirmPassword" type="password" id="confirmPassword" /></td>
        </tr>
        <%} %>
        <tr>
          <td align="right" nowrap>First Name : </td>
          <td align="left" nowrap><input name="firstname" type="text" id="firstname" value="<%= member.getFirstName() %>"/></td>
        </tr>
        <tr>
          <td align="right" nowrap>Last Name : </td>
          <td align="left" nowrap><input name="lastname" type="text" id="lastname" value="<%= member.getLastName() %>"/></td>
        </tr>
        <tr>
          <td align="right" nowrap>Time Zone : </td>
          <td align="left" nowrap>
          <SELECT name="timezone">
          <%
	  		String[] tzs = TimeZone.getAvailableIDs();
          	SortedVector sortedVector = new SortedVector();
	  		for (int i = 0; i < tzs.length; i++) {
	  			if ((tzs[i].indexOf("/")>0) &&
	  				(tzs[i].indexOf("Etc")<0)) {
	  				sortedVector.addElement(tzs[i], false);
	  			}
	  		}
	  		sortedVector.sort();
	  		tzs = (String[])sortedVector.toArray(new String[0]);
	  		String myTimeZone = member.getInformation("Timezone");
	  		if (StringUtils.isEmpty(myTimeZone)) {
	  			myTimeZone = "America/Denver";
	  		}
	  		for (int i = 0; i < tzs.length; i++) {
	  			String timeZoneName = tzs[i];
	  			String selected = (timeZoneName.equals(myTimeZone))?" SELECTED":"";
	  			TimeZone tz = TimeZone.getTimeZone(timeZoneName);
	  			int hourOffset = tz.getOffset(System.currentTimeMillis())/3600000;
	  			String sign = (hourOffset>=0)?"+":"";
	  			%><OPTION value="<%= timeZoneName%>" <%= selected %>><%= timeZoneName %> - GMT<%= sign %><%= hourOffset %></OPTION><%
	  		}

          %>
          </SELECT>
          </td>
        </tr>
        <tr>
          <td align="right" nowrap>Escalation Time : </td>
          <td align="left" nowrap><label>
            <input name="esctime" type="text" size="3" value="<%= ((EscalationGroup)group).getEscalationTime(memberNum) %>"/>
            </label>
            minutes</td>
        </tr>
        <tr>
          <td align="right" nowrap>Email Address : </td>
          <td align="left" nowrap><label>
            <input name="email_address" type="text" size="35" id="email_address" value="<%= emailValue %>"/>
          </label></td>
        </tr>
        <tr>
          <td align="right" nowrap>Cell Phone : </td>
          <td align="left" nowrap><label>
            <input name="cell_phone" type="text" id="cell_phone" size="14"  value="<%= cellValue %>"/>
            </label><label>
            <select name="cell_provider" id="cell_provider">
              <%
              	Vector providers = new CellPhoneEmailDevice().getAvailableSettings()[1].getOptions();
              	for (int p = 0; p < providers.size(); p++) {
    	            String selected = "";
              		String providerName = (String)providers.elementAt(p);
              		if (!StringUtils.isEmpty(providerName)) {
              			if (providerName.equals(cellProvider)) {
              				selected = " SELECTED";
              			}
              		}
              		%><OPTION value="<%= providerName %>" <%= selected %>><%= providerName %></OPTION><%
              	}
              	
              %>
            </select>
          </label></td>
        </tr>
        
        <tr>
          <td align="right" nowrap>Pager : </td>
          <td align="left" nowrap><input name="pager" type="text" id="pager" size="14"  value="<%= pagerValue %>"/>
            <select name="pager_provider" id="pager_provider">
              <%
              	providers = new TwoWayPagerDevice().getAvailableSettings()[1].getOptions();
              	for (int p = 0; p < providers.size(); p++) {
    	            String selected = "";
              		String providerName = (String)providers.elementAt(p);
              		if (!StringUtils.isEmpty(providerName)) {
              			if (providerName.equals(pagerProvider)) {
              				selected = " SELECTED";
              			}
              		}
              		%><OPTION value="<%= providerName %>" <%= selected %>><%= providerName %></OPTION><%
              	}
              	
              %>
            </select></td>
        </tr>
        
        <tr>
          <td align="right" nowrap>Jabber IM : </td>
          <td align="left" nowrap><label>
            <input name="jabber_account" type="text" id="jabber_account" size="10"  value="<%= jabberValue %>"/>
          </label>@<label>
          <%
          if (StringUtils.isEmpty(jabberServer)) {
        	  jabberServer = "gmail.com";
          }
          %>
          <input name="jabber_server" type="text" id="jabber_server" size="20" value="<%= jabberServer %>"/>
          </label></td>
        </tr>
        <% if (BrokerFactory.getAuthorizationBroker().isUserInRole(member, "Telephone User")) { %>
        <tr>
          <td align="right" nowrap>Telephone : </td>
          <td align="left" nowrap>
          <label>
            <input name="phone_number" type="text" id="phone_number" size="10"  value="<%= phoneNumber %>"/>
		  </label>
		  </td>
        </tr>
        <%} %>
        <tr>
          <td align="right">&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="center" colspan="2" nowrap><label>
          	<span class="button" onclick="doSubmit()">&nbsp;Save&nbsp;</span>
          </label></td>
        </tr>
        <tr><td>&nbsp</td></tr>
      </table></td>    
</table></div>
</form>
</body>
</html>
