<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.broker.AuthenticationBroker"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%

String oldPassword = request.getParameter("oldPass");
String newPassword1 = request.getParameter("newPass1");
String newPassword2 = request.getParameter("newPass2");

if (StringUtils.isEmpty(oldPassword)) {
	session.setAttribute("error", "Please enter your existing password");
	response.sendRedirect("../change_password.jsp");
	return;
}

if (StringUtils.isEmpty(newPassword1)) {
	session.setAttribute("error", "Please enter a new password");
	response.sendRedirect("../change_password.jsp");
	return;
}

if (StringUtils.isEmpty(newPassword2)) {
	session.setAttribute("error", "Please confirm your new password");
	response.sendRedirect("../change_password.jsp");
	return;
}

if (!newPassword1.equals(newPassword2)) {
	session.setAttribute("error", "Your password did not match the confirmation");
	response.sendRedirect("../change_password.jsp");
	return;	
}
AuthenticationBroker authnBroker = BrokerFactory.getAuthenticationBroker();
User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
String loginName = authnBroker.getIdentifierByUser(user);
if (authnBroker.authenticate(loginName, oldPassword)==null) {
	session.setAttribute("error", "Please make sure your old password is correct ");
	response.sendRedirect("../change_password.jsp");
	return;
}

authnBroker.changePassword(loginName, newPassword1);
session.setAttribute("error", "Your password has been updated");
response.sendRedirect("../change_password.jsp");
%>