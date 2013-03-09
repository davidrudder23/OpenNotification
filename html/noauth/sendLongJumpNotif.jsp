
<%@page import="net.reliableresponse.notification.device.Device"%>
<%@page import="java.util.Hashtable"%>
<%@page import="net.reliableresponse.notification.device.VoiceShotDevice"%>
<%@page import="net.reliableresponse.notification.device.TelephoneDevice"%>
<%@page import="net.reliableresponse.notification.actions.SendNotification"%>
<%@page import="net.reliableresponse.notification.sender.UserSender"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.Notification"%><%

String objectID = (String)request.getParameter("object_id");
String recordID = (String)request.getParameter("record_id");
String first = (String)request.getParameter("first");
String last = (String)request.getParameter("last");
String phone = (String)request.getParameter("phone");
String subject = (String)request.getParameter("subject");
String message = (String)request.getParameter("message");

User user = BrokerFactory.getUserMgmtBroker().getUserByInformation("LongJump UUID", objectID+":"+recordID);

if (user == null) {
	user = new User();
	user.setAutocommit(false);
	user.setInformation("LongJump UUID", objectID+":"+recordID);
	user.setFirstName(first);
	user.setLastName(last);
	
	VoiceShotDevice phoneDevice = new VoiceShotDevice();
	Hashtable params = new Hashtable();
	params.put("Phone Number", phone);
	phoneDevice.initialize(params);
	phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),1);
	phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),2);
	phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),3);
	
	user.addDevice(phoneDevice);

	BrokerFactory.getUserMgmtBroker().addUser(user);
	user.setAutocommit(true);
} else {
	user.setAutocommit(true);
	TelephoneDevice phoneDevice = null;
	Device[] devices = user.getDevices();
	
	for (int deviceNum = 0; deviceNum < devices.length; deviceNum++) {
		if (devices[deviceNum] instanceof TelephoneDevice) {
			phoneDevice = (TelephoneDevice)devices[deviceNum];

			Hashtable params = new Hashtable();
			params.put("Phone Number", phone);
			phoneDevice.initialize(params);
			
			phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),1);
			phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),2);
			phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),3);

			BrokerFactory.getDeviceBroker().updateSetting(phoneDevice, "Phone Number", phone);
		}
	}
	
	if (phoneDevice == null) {
		phoneDevice = new VoiceShotDevice();
		Hashtable params = new Hashtable();
		params.put("Phone Number", phone);
		phoneDevice.initialize(params);
		phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),1);
		phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),2);
		phoneDevice.addSchedule(user,BrokerFactory.getScheduleBroker().getSchedule("net.reliableresponse.notification.scheduling.InformationalSchedule"),3);
		
		user.addDevice(phoneDevice);
	}

}



user.setFirstName(first);
user.setLastName(last);

Notification notification = new Notification(null, user, new UserSender(user), subject, message);
SendNotification.getInstance().doSend(notification);
%>