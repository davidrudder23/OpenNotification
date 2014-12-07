<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="java.util.*"/><%
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
	List<Notification> notifs = BrokerFactory.getNotificationBroker()
			.getUpdatedNotificationsTo(user, lastUpdate);
	SortedVector sortedNotifs = new SortedVector();
	for (Notification notif: notifs) {
		notif.sortByTime();
		sortedNotifs.addElement(notif);
	}
	notifs = new ArrayList(sortedNotifs);
	
	for (Notification notif: notifs) {
%>
	<notification uuid="<%= notif.getUuid() %>" 
	status="<%= notif.getStatusAsString() %>"
	date="<%= user.getFormattedDate(notif.getTime(), "dd/MM/yyyy HH:mm z") %>"
	>
		<subject><%= StringUtils.escapeForXML(StringUtils.htmlEscape(notif.getSubject())) %></subject>
		<message><%= StringUtils.escapeForXML(StringUtils.htmlEscape(notif.getDisplayText())) %></message>
	</notification>

<% } %>
</notifications>