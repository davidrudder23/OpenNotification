<%@page import="net.reliableresponse.notification.device.PagerDevice"%>
<%@page import="net.reliableresponse.notification.device.EmailDevice"%>
<%@page import="net.reliableresponse.notification.device.Device"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.broker.UserMgmtBroker"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%
response.setContentType("text/plain");

UserMgmtBroker userBroker = BrokerFactory.getUserMgmtBroker();
int numUsers = userBroker.getNumUsers();
User[] users = new User[numUsers];
userBroker.getUsers(numUsers, 0, users);

for (int userNum = 0; userNum < numUsers; userNum++) {
	User user = users[userNum];
	Device[] devices = user.getDevices();
	for (int deviceNum = 0; deviceNum < devices.length; deviceNum++) {
		Device device = devices[deviceNum];
		if (device instanceof EmailDevice) {
		%>
{<%= user.getFirstName() %><%= user.getLastName() %>Mail}
Active=True
Configuration=AssurantInternet
Subject=FONE System Message
PIN=<%= ((EmailDevice)device).getEmailAddress() %>
Schedule=FullTime
		<%
		} else if ((device instanceof PagerDevice) && (((PagerDevice)device).getEmailHost().equalsIgnoreCase("attwireless.com"))) {
%>
{<%= user.getFirstName() %><%= user.getLastName() %>Pager}
Active=True
Configuration=CingularENTPaging
PIN=<%= ((PagerDevice)device).getPagerNumber() %>
Schedule=FullTime
<%		
		}
	}
}
%>