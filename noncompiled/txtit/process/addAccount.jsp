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

String name = request.getParameter("contactName");
String email = request.getParameter("contactEmail");
String phone = request.getParameter("contactPhone");
String message = request.getParameter("contactMessage");

boolean addAccount = !StringUtils.isEmpty(request.getParameter("account"));
boolean sendInfo = !StringUtils.isEmpty(request.getParameter("info"));

Group notifyGroup = BrokerFactory.getGroupMgmtBroker().getGroupByName("New Account Signup");
User admin = BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001");
if (notifyGroup == null) {
	notifyGroup = new BroadcastGroup();
	notifyGroup.setGroupName("New Account Signup");
	BrokerFactory.getGroupMgmtBroker().addGroup(notifyGroup);
	if (admin != null) {
		notifyGroup.addMember(admin, -1);
	}
}
Notification newUserNotif = new Notification(null, notifyGroup, new UserSender(admin), name+" has requested a new account",
		"Name: "+name+"\n"+
		"Email: "+email+"\n"+
		"Phone: "+phone+"\n"+
		"Message: "+message);
SendNotification.getInstance().doSend(newUserNotif);

if (addAccount) {
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
	BrokerFactory.getAuthorizationBroker().addUserToRole(newUser, Roles.TXTIT);
	String password = PasswordGenerator.generate();
	System.out.println ("password="+password);
	BrokerFactory.getAuthenticationBroker().addUser(email, password, newUser);
	if (!StringUtils.isEmpty(email)) {
		EmailDevice emailDev = new EmailDevice();
		Hashtable params = new Hashtable();
		params.put("Address", email);
		emailDev.initialize(params);
		newUser.addDevice(emailDev);
	}
	
	Notification newUserSignup = new Notification(null, newUser,
			new EmailSender("info@txtitllc.com"), "Welcome to TxtIt!", 
			"Your new password is "+password);
	newUserSignup.setPersistent(false); // make sure this isn't treated as a notification that needs responses
	SendNotification.getInstance().doSend(newUserSignup);
}
session.setAttribute("error", "Thank you, you will be receiving your sign-in information soon");
response.sendRedirect("../login.jsp");
%>