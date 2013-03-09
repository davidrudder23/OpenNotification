
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
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Account"/>
<jsp:directive.page import="java.text.NumberFormat"/>
<jsp:directive.page import="net.reliableresponse.notification.license.Coupon"/>
<jsp:directive.page import="net.reliableresponse.notification.util.PaypalUtil"/><%
String userUuid = (String) session.getAttribute("user");
User user = null;

if (userUuid != null) {
	user = BrokerFactory.getUserMgmtBroker()
	.getUserByUuid(userUuid);
}

if (user == null) {
	response.sendRedirect("/notification/login.jsp");
}

// Load the group
Group group = null;
Group[] allGroups = BrokerFactory.getGroupMgmtBroker()
		.getGroupsOfMember(user);
BrokerFactory.getLoggingBroker()
		.logDebug("all groups=" + allGroups);
if (allGroups != null) {
	BrokerFactory.getLoggingBroker().logDebug(
	"all groups length=" + allGroups.length);
	for (int i = 0; i < allGroups.length; i++) {
		if (allGroups[i].isOwner(user, false)) {
	group = allGroups[i];
		}
	}
}

Account account = BrokerFactory.getAccountBroker().getUsersAccount(user);

NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Notify &amp; Acknowledge Account Settings</title>
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
	document.account.submit();
}

function cancelSubscription() {
window.location=("<%= PaypalUtil.getCancelURL(account) %>");
}
</script>

<form id="account" name="account" method="post" action="process/account.jsp">
<div align="center">
<table width="410" border="0" cellpadding="0" cellspacing="0" class="box">
  <tr>
    <td bgcolor="#EDEDED" class="header_box">Your Account</td>
    <tr>
      <td bgcolor="#FFFFFF"><br />
        <table width="400" border="0" align="center" cellpadding="5" cellspacing="0">
<%
	String[] messages = (String[])session.getAttribute("account_messages");
	session.setAttribute("account_messages", null);
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
          <td><img src="images/spacer.gif" width="1" /></td>
          <td align="right" colspan="2" nowrap>Company Name : &nbsp;
          <input type="text" name="companyName" id="companyId" value="<%= group.getGroupName() %>"></td>
        </tr>
        <tr>
          <td align="right">&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="center" colspan="3" nowrap width="100%" class="shadedbg"><center><b>Telephone Support</b></center></td>
        </tr>
        <tr><td class="notice" colspan="3">
        Changing your phone settings will bring you to<br/>
        PayPal to approve your new subscription charges</td></tr>
<%
	Member[] members = group.getMembers();
	for (int memberNum = 0; memberNum < members.length; memberNum++) {
		Member member = members[memberNum];
		boolean hasTelephone = BrokerFactory.getAuthorizationBroker().isUserInRole(member, "Telephone User");
		%><tr><td><img src="images/spacer.gif" width="1" /></td>
		<td align="right" colspan="2" nowrap><%= member.toString() %>&nbsp;
		<input type="checkbox" name="telephone_<%=member.getUuid() %>" <%= hasTelephone?"CHECKED":"" %>></input></td>
		</tr>
	<%
	}
%>
        <tr>
          <td align="center" colspan="3" nowrap width="100%" class="shadedbg"><center><b>Monthly Billing</b></center></td>
        </tr>
        <tr><tr><td><img src="images/spacer.gif" width="1" /></td>
        <td colspan="25"><table width="100%">
        <tr><td>Monthly Rate : </td><td><%= currencyFormat.format(account.getBaseRate()) %></td></tr>
        <tr>
        <td>Telephone Rate : </td><td><%= currencyFormat.format(account.getPhoneRate()) %> per user
        </td></tr>
        <%
        Coupon[] coupons = BrokerFactory.getCouponBroker().getAccountsCoupons(account);
        for (int i = 0; i < coupons.length; i++) {
        	%>
	        <tr>
	        <td>Coupon : </td><td><%= coupons[i].toShortString() %>
	        <% if (coupons[i].isExpired(account)) { %> (<font color="#cc0000">expired</font>)<%} %>
	        </td></tr>
        	<%
        }
        %>
        <tr>
        <td>Total Charges : </td><td><%= currencyFormat.format(account.getTotalMonthlyBill(true)) %> per month
        </td></tr>
        </table></td></tr>
        <tr><td>&nbsp</td></tr>
        <tr>
        <td colspan="25" align="center" style="text-align: center"><label>
          	<span class="button" onclick="doSubmit()">&nbsp;Save&nbsp;</span>
          </label>&nbsp;
          	<span class="button" onclick="cancelSubscription()" style="color: #cc0000;">&nbsp;Cancel Account&nbsp;</span>
		  </td>
        </tr>
        <tr><td>&nbsp</td></tr>
      </table></td>    
</table></div>
</form>
</body>
</html>
