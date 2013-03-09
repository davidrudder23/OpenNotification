<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="java.util.Vector"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="java.util.Date"/><%
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
	Date lastUpdate = (Date)session.getAttribute("lastupdate");
	
	if (lastUpdate == null) {
		lastUpdate = new Date(0);
	} else {
		// backup for 2 seconds to make sure we get everything
		lastUpdate.setSeconds(lastUpdate.getSeconds()-180);
	}
	
	session.setAttribute("lastupdate", new Date());
	if (request.getParameter("clearSession")!= null) session.setAttribute("lastupdate", null);
	response.setContentType("text/xml");
%>
<notifications>
<lastUpdate><%= lastUpdate.toString() %></lastUpdate>
<%
	Notification[] notifs = BrokerFactory.getNotificationBroker()
			.getUpdatedNotificationsTo(user, lastUpdate);
	SortedVector sortedNotifs = new SortedVector();
	for (int i = 0; i < notifs.length; i++) {
		notifs[i].sortByTime();
		sortedNotifs.addElement(notifs[i]);
	}
	notifs = (Notification[])sortedNotifs.toArray(new Notification[0]);
	
	for (int i = 0; i < notifs.length; i++) {
%>
	<notification uuid="<%= notifs[i].getUuid() %>" 
	status="<%= notifs[i].getStatusAsString() %>"
	date="<%= user.getFormattedDate(notifs[i].getTime(), "dd/MM/yyyy HH:mm z") %>"
	>
		<subject><%= StringUtils.escapeForXML(StringUtils.htmlEscape(notifs[i].getSubject())) %></subject>
		<message><%= StringUtils.escapeForXML(StringUtils.htmlEscape(notifs[i].getDisplayText())) %></message>
	</notification>

<% } %>
</notifications>