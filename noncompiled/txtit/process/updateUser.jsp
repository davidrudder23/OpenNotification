<%@page import="java.util.Hashtable"%>
<%@page import="net.reliableresponse.notification.device.EmailDevice"%>
<%@page import="net.reliableresponse.notification.device.CellPhoneEmailDevice"%>
<%@page import="net.reliableresponse.notification.device.Device"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.usermgmt.Group"%>
<%@page import="net.reliableresponse.notification.usermgmt.BroadcastGroup"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%
String uuid = request.getParameter("uuid");
String groupname = request.getParameter("group");
String username = request.getParameter("username");
String phoneNumber = request.getParameter("phonenum");
String carrier = request.getParameter("carrier");
String email = request.getParameter("email");

User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
Group group = null;
for (int groupNum = 0; groupNum < groups.length; groupNum++) {
	if (groups[groupNum] instanceof BroadcastGroup) {
		group = groups[groupNum];
	}
}
if (group == null) {
	BrokerFactory.getLoggingBroker().logDebug("no rights");
	session.setAttribute("error", "You do not have the rights to update that user");
	BrokerFactory.getLoggingBroker().logDebug("You do not have the rights to update that user");
	response.sendRedirect("../manage_group.jsp");
	return;	
}
if (!StringUtils.isEmpty(groupname)) {
	group.setGroupName(groupname);
	response.sendRedirect("../manage_group.jsp");
	return;
}

if (StringUtils.isEmpty(uuid)) {
	BrokerFactory.getLoggingBroker().logDebug("empty uuid");
	session.setAttribute("error", "Unable to update that user");
	BrokerFactory.getLoggingBroker().logDebug("Unable to update that user");
	response.sendRedirect("../manage_group.jsp");
	return;		
}
User editUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
if (editUser == null) {
	session.setAttribute("error", "Unable to update that user");
	BrokerFactory.getLoggingBroker().logDebug("Unable to update that user");
	response.sendRedirect("../manage_group.jsp");
	return;			
}

if (!group.isMember(editUser)) {
	session.setAttribute("error", "You do not have the rights to update that user");
	BrokerFactory.getLoggingBroker().logDebug("You do not have the rights to update that user");
	response.sendRedirect("../manage_group.jsp");
	return;	
}

if (!StringUtils.isEmpty(username)) {
	String firstName = username;
	String lastName = "";
	int spaceIdx = username.indexOf(" "); 
	if (spaceIdx>0) {
		firstName = username.substring(0, spaceIdx);
		lastName = username.substring(spaceIdx+1, username.length());
	}
	editUser.setFirstName(firstName);
	editUser.setLastName(lastName);
}

Device[] devices = editUser.getDevices();
if (!StringUtils.isEmpty(phoneNumber)) {
	Device cell = null;
	for (int devNum = 0; devNum < devices.length; devNum++) {
		Device device = devices[devNum];
		if (device instanceof CellPhoneEmailDevice) {
			cell = device;
		}
	}
	if (cell == null) {
		cell = new CellPhoneEmailDevice();
		Hashtable params = new Hashtable();
		params.put ("Phone Number", phoneNumber);
		params.put ("Provider", "Verizon");
		cell.initialize(params);
		editUser.addDevice(cell);
	}
	cell.changeSetting("Phone Number", phoneNumber);
}
if (!StringUtils.isEmpty(carrier)) {
	Device cell = null;
	for (int devNum = 0; devNum < devices.length; devNum++) {
		Device device = devices[devNum];
		if (device instanceof CellPhoneEmailDevice) {
			cell = device;
		}
	}
	if (cell == null) {
		cell = new CellPhoneEmailDevice();
		Hashtable params = new Hashtable();
		params.put ("Phone Number", "");
		params.put ("Provider", carrier);
		cell.initialize(params);
		editUser.addDevice(cell);
	}
	cell.changeSetting("Provider", carrier);
}

if ((phoneNumber != null) && (phoneNumber.equals(""))) {
	Device cell = null;
	for (int devNum = 0; devNum < devices.length; devNum++) {
		Device device = devices[devNum];
		if (device instanceof CellPhoneEmailDevice) {
			cell = device;
		}
	}
	if (cell != null) {
		editUser.removeDevice(cell);
	}	
}

if (!StringUtils.isEmpty(email)) {
	Device emailDevice = null;
	for (int devNum = 0; devNum < devices.length; devNum++) {
		Device device = devices[devNum];
		if ((device instanceof EmailDevice) && (!(device instanceof CellPhoneEmailDevice))) {
			emailDevice = device;
		}
	}
	if (emailDevice == null) {
		emailDevice = new EmailDevice();
		Hashtable params = new Hashtable();
		params.put ("Address", email);
		emailDevice.initialize(params);
		editUser.addDevice(emailDevice);
	}
	emailDevice.changeSetting("Address", email);
} 

if ((email != null) && (email.equals(""))) {
	Device emailDevice = null;
	for (int devNum = 0; devNum < devices.length; devNum++) {
		Device device = devices[devNum];
		if ((device instanceof EmailDevice) && (!(device instanceof CellPhoneEmailDevice))) {
			emailDevice = device;
		}
	}
	if (emailDevice != null) {
		editUser.removeDevice(emailDevice);
	}
}

BrokerFactory.getUserMgmtBroker().updateUser(editUser);
BrokerFactory.getLoggingBroker().logDebug(editUser+" updated");
response.sendRedirect("../manage_group.jsp");
%>