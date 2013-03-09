<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="java.text.SimpleDateFormat"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.sender.NotificationSender"/>
<jsp:directive.page import="net.reliableresponse.notification.device.Device"/>
<jsp:directive.page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.EmailDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.PagerDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.JabberDevice"/>
<jsp:directive.page import="java.util.Hashtable"/>
<jsp:directive.page import="net.reliableresponse.notification.device.TwoWayPagerDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.device.VoiceShotDevice"/>
<jsp:directive.page import="net.reliableresponse.notification.scheduling.InformationalSchedule"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Account"/>
<jsp:directive.page import="net.reliableresponse.notification.util.PaypalUtil"/>
<jsp:directive.page import="java.util.Vector"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Roles"/><%
// Load the user object
String userUuid = (String) session.getAttribute("user");
User user = null;

if (userUuid != null) {
	user = BrokerFactory.getUserMgmtBroker()
	.getUserByUuid(userUuid);
}

if (user == null) {
	response.sendRedirect("/notification/login.jsp");
}
String name = "";
if (user != null)
	name = user.getFirstName() + " " + user.getLastName();

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
double originalBill = account.getTotalMonthlyBill(false);

String groupName = request.getParameter("companyName");
if (!group.getGroupName().equals(groupName)) {
	group.setGroupName(groupName);
}

Vector<User> existingTelephoneUsers = new Vector<User>();
Vector<User> newTelephoneUsers = new Vector<User>();

Member[] members = group.getMembers();
for (int i = 0; i < members.length; i++) {
	if (members[i] instanceof User) {
		User member = (User)members[i];
		if (request.getParameter("telephone_"+member.getUuid()) != null) {
			newTelephoneUsers.addElement((User)member);
		} 
		
		if (BrokerFactory.getAuthorizationBroker().isUserInRole(member, Roles.TELEPHONE_USER)) {
			existingTelephoneUsers.addElement(member);
		}
	}
}

double newBill = account.getBaseRate()+(account.getPhoneRate()*newTelephoneUsers.size());

System.out.println ("Original Bill = "+originalBill);
System.out.println ("New Bill = "+newBill);
if (newBill != originalBill) {
	// handle PayPal modify
	response.sendRedirect(PaypalUtil.getPaypalUpdateURL(account, newTelephoneUsers.toArray(new User[0])));
} else {
%>
<html>
<body onload="window.close();">
</body>
</html>
<%}%>