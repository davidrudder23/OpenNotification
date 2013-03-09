<%@page import="net.reliableresponse.notification.scheduling.InformationalSchedule"%>
<%@page import="net.reliableresponse.notification.priority.Priority"%>
<%@page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"%>
<%@page import="net.reliableresponse.notification.sender.EmailSender"%>
<%@page import="net.reliableresponse.notification.util.PasswordGenerator"%>
<%@page import="net.reliableresponse.notification.usermgmt.Roles"%>
<%@page import="java.util.Hashtable"%>
<%@page import="net.reliableresponse.notification.device.EmailDevice"%>
<%@page import="net.reliableresponse.notification.actions.SendNotification"%>
<%@page import="net.reliableresponse.notification.sender.UserSender"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.NotificationException"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%
User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));

String name = request.getParameter("contactName");
String phone = request.getParameter("contactPhone");
String carrier = request.getParameter("contactCarrier");
String email = request.getParameter("contactEmail");

if (StringUtils.isEmpty(name)) {
	session.setAttribute("error", "Please specify a user's name");
	response.sendRedirect("../manage_group.jsp");
	return;	
}

User newUser = new User();
String firstName = name;
String lastName = "";
int spaceIdx = name.indexOf(" "); 
if (spaceIdx>0) {
	firstName = name.substring(0, spaceIdx);
	lastName = name.substring(spaceIdx+1, name.length());
}
newUser.setFirstName(firstName);
newUser.setLastName(lastName);
BrokerFactory.getUserMgmtBroker().addUser(newUser);
newUser.setAutocommit(true);
BrokerFactory.getAuthorizationBroker().addUserToRole(newUser, Roles.TXTIT);

Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
Group group = null;
for (int groupNum = 0; groupNum < groups.length; groupNum++) {
	if (groups[groupNum] instanceof BroadcastGroup) {
		group = groups[groupNum];
	}
}
if (group == null) {
	group = new BroadcastGroup();
	group.setGroupName(user.getFirstName()+" "+user.getLastName()+"'s Group");
	BrokerFactory.getGroupMgmtBroker().addGroup(group);
	group.setAutocommit(true);
	group.addMember(user, -1);
}
group.addMember(newUser, -1);

if (!StringUtils.isEmpty(email)) {
	EmailDevice emailDev = new EmailDevice();
	Hashtable params = new Hashtable();
	params.put("Address", email);
	emailDev.initialize(params);
	newUser.addDevice(emailDev);
	emailDev.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 1);
	emailDev.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 2);
	emailDev.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 3);
}
	
if (!StringUtils.isEmpty(phone)) {
	CellPhoneEmailDevice phoneDevice = new CellPhoneEmailDevice();
	Hashtable params = new Hashtable();
	params.put("Phone Number", phone);
	params.put("Provider", carrier);
	phoneDevice.initialize(params);
	newUser.addDevice(phoneDevice);
	phoneDevice.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 1);
	phoneDevice.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 2);
	phoneDevice.addSchedule(newUser, BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"), 3);
}

session.setAttribute("error", name+" added");
response.sendRedirect("../manage_group.jsp");
%>