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
<jsp:directive.page import="net.reliableresponse.notification.scheduling.InformationalSchedule"/><%
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

if (group == null) {
	group = new EscalationGroup();
	group.setAutocommit(false);
	group.setGroupName(name + "'s Group");
	group.addMember(user, -1);
	group.setOwner(0);
	group.setAutocommit(true);
	BrokerFactory.getGroupMgmtBroker().addEscalationGroup(
	(EscalationGroup) group);
}

String memberNumString = (String)request.getParameter("membernum");
if (StringUtils.isEmpty(memberNumString)) {
	BrokerFactory.getLoggingBroker().logError("membernum is empty");
	// TODO: give a decent error for no uuid
	%>No membernum<%
	return;
}

int memberNum = 0;
try {
	memberNum = Integer.parseInt(memberNumString);
} catch (NumberFormatException nfExc) {
	BrokerFactory.getLoggingBroker().logError(nfExc);
	// TODO: give a decent error for no uuid
	return;
}

User member = (User)group.getMembers()[memberNum];
if (!group.isMember(member)) {
	BrokerFactory.getLoggingBroker().logError("member "+member+" is not a member of "+group);
	// TODO: give a decent error for no uuid
	return;	
}

String firstName = (String)request.getParameter("firstname");
String lastName = (String)request.getParameter("lastname");
String timezone = (String)request.getParameter("timezone");
String escTimeString = (String)request.getParameter("esctime");
String email = (String)request.getParameter("email_address");
String cell = (String)request.getParameter("cell_phone");
String cellProvider = (String)request.getParameter("cell_provider");
String pager = (String)request.getParameter("pager");
String pagerProvider = (String)request.getParameter("pager_provider");
String jabber = (String)request.getParameter("jabber_account");
String jabberServer = (String)request.getParameter("jabber_server");
String phoneNumber = (String)request.getParameter("phone_number");

member.setFirstName(firstName);
member.setLastName(lastName);
member.setInformation("Timezone", timezone);
if (!StringUtils.isEmpty(escTimeString)) {
	int escTime = 0;	
	try {
		escTime = Integer.parseInt(escTimeString);
	} catch (NumberFormatException nfExc) {
		// TODO: give a decent error for no uuid
		return;
	}
	((EscalationGroup)group).setEscalationTime(memberNum, escTime);
}

// Okay, now deal with the devices
Device[] devices = member.getDevices();
boolean foundEmail = false;
boolean foundCell = false;
boolean foundPager = false;
boolean foundJabber = false;
boolean foundPhone = false;
for (int d = 0; d < devices.length; d++) {
	Device device = devices[d];
	BrokerFactory.getLoggingBroker().logDebug("Inspecting device "+device);
	if (device instanceof CellPhoneEmailDevice) {
		foundCell = true;
		if ((StringUtils.isEmpty(cell)) || (StringUtils.isEmpty(cellProvider))){
			member.removeDevice(device);
		} else {
			device.changeSetting("Phone Number", cell);
			device.changeSetting("Provider", cellProvider);
		}		
	} else if (device instanceof EmailDevice) {
		foundEmail = true;
		if (StringUtils.isEmpty(email)) {
			member.removeDevice(device);
		} else {
			device.changeSetting("Address", email);
		}		
	} else if (device instanceof PagerDevice) {
		foundPager = true;
		if ((StringUtils.isEmpty(pager)) || (StringUtils.isEmpty(pagerProvider))){
			member.removeDevice(device);
		} else {
			device.changeSetting("Pager Number", pager);
			device.changeSetting("Provider", pagerProvider);
		}		
	} else if (device instanceof JabberDevice) {
		foundJabber = true;
		if ((StringUtils.isEmpty(jabber)) || (StringUtils.isEmpty(jabberServer))){
			member.removeDevice(device);
		} else {
			device.changeSetting("Account Name", jabber);
			device.changeSetting("Server Name", jabberServer);
		}		
	} else if (device instanceof VoiceShotDevice) {
		foundPhone = true;
		if ((StringUtils.isEmpty(phoneNumber)) || (StringUtils.isEmpty(phoneNumber))){
			member.removeDevice(device);
		} else {
			device.changeSetting("Phone Number", phoneNumber);
		}		
	}
}
	
BrokerFactory.getLoggingBroker().logDebug("email= "+email);
BrokerFactory.getLoggingBroker().logDebug("found email= "+foundEmail);
if ((!StringUtils.isEmpty(email)) && (!foundEmail)) {
	BrokerFactory.getLoggingBroker().logDebug("Adding email "+email);
	EmailDevice emailDevice = new EmailDevice();
	Hashtable params = new Hashtable();
	params.put ("Address", email);
	emailDevice.initialize(params);
	member.addDevice(emailDevice);
}
if ((!StringUtils.isEmpty(cell)) 
		&& (!StringUtils.isEmpty(cellProvider)) 
		&& (!foundCell)) {
	CellPhoneEmailDevice cellDevice = new CellPhoneEmailDevice();
	Hashtable params = new Hashtable();
	params.put ("Phone Number", cell);
	params.put ("Provider", cellProvider);
	cellDevice.initialize(params);
	member.addDevice(cellDevice);
}
if ((!StringUtils.isEmpty(pager)) 
		&& (!StringUtils.isEmpty(pagerProvider)) 
		&& (!foundPager)) {
	TwoWayPagerDevice pagerDevice = new TwoWayPagerDevice();
	Hashtable params = new Hashtable();
	params.put ("Pager Number", pager);
	params.put ("Provider", pagerProvider);
	pagerDevice.initialize(params);
	member.addDevice(pagerDevice);
}
if ((!StringUtils.isEmpty(jabber)) 
		&& (!StringUtils.isEmpty(jabberServer)) 
		&& (!foundJabber)) {
	JabberDevice jabberDevice = new JabberDevice();
	Hashtable params = new Hashtable();
	params.put ("Account Name", jabber);
	params.put ("Server Name", jabberServer);
	jabberDevice.initialize(params);
	member.addDevice(jabberDevice);
}
if ((!StringUtils.isEmpty(phoneNumber)) 
		&& (!foundPhone)) {
	VoiceShotDevice phoneDevice = new VoiceShotDevice();
	phoneDevice.addSchedule(member, new InformationalSchedule(), 1);
	phoneDevice.addSchedule(member, new InformationalSchedule(), 2);
	phoneDevice.addSchedule(member, new InformationalSchedule(), 3);
	Hashtable params = new Hashtable();
	params.put ("Phone Number", phoneNumber);
	phoneDevice.initialize(params);
	member.addDevice(phoneDevice);
}

String password = request.getParameter("password");
String confirmPassword = request.getParameter("confirmPassword");

if ((!StringUtils.isEmpty(password)) &&
		(!StringUtils.isEmpty(confirmPassword))) {
	if (password.equals(confirmPassword)) {
		BrokerFactory.getAuthenticationBroker().changePassword(member, password);
	} else {
		String[] messages = new String[1];
		messages[0] = "The password and confirmation don't match";
		session.setAttribute("membersettings_messages", messages);
	}
}
%>
<html>
<body onload="window.close();">
</body>
</html>