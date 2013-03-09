
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="java.util.Vector"/><%
boolean passed = true;
Vector messages = new Vector();
String userUuid = (String) session.getAttribute("user");
User user = null;

if (userUuid != null) {
	user = BrokerFactory.getUserMgmtBroker()
	.getUserByUuid(userUuid);
}

if (user == null) {
	response.sendRedirect("/notification/login.jsp");
}

String memberUuid = request.getParameter("uuid");
if (StringUtils.isEmpty(memberUuid)) {
	messages.addElement("There was an error processing.  Please try again.");
	passed = false;			
}

User member = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
if (member == null) {
	if (StringUtils.isEmpty(memberUuid)) {
		messages.addElement("There was an error processing.  Please try again.");
		passed = false;			
	}
}

String existingLogin = BrokerFactory.getAuthenticationBroker().getIdentifierByUser(member);
if (existingLogin != null) {
	messages.addElement("This member already has a login, "+existingLogin+"</b>");
	passed = false;			
}

String login = request.getParameter("addLogin");
if (StringUtils.isEmpty(login)) {
	messages.addElement("Please supply a login name");
	passed = false;			
}

String password = request.getParameter("addPassword");
if (StringUtils.isEmpty(password)) {
	messages.addElement("Please supply a password");
	passed = false;			
}

String confirmPassword = request.getParameter("addConfirmPassword");
if (StringUtils.isEmpty(confirmPassword)) {
	messages.addElement("Please confirm your password");
	passed = false;			
}

if ((passed) && (!confirmPassword.equals(password))) {
	messages.addElement("Your password and confirmation don't match");
	passed = false;			
}

if (BrokerFactory.getAuthenticationBroker().getUserByIdentifier(login) != null) {
	messages.addElement("That login is already taken");
	passed = false;			
}

if (!passed) {
	session.setAttribute("allowlogin_messages", messages.toArray(new String[0]));
	response.sendRedirect("../setAuthentication.jsp?uuid="+memberUuid);
	return;
}

BrokerFactory.getAuthenticationBroker().addUser(login, password, member);
BrokerFactory.getAuthenticationBroker().setPaymentAuthorized(member, true);
%>
<body onload="window.close();">
</body>