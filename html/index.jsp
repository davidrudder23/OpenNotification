<%@ page import="net.reliableresponse.notification.broker.*" %><%@ page import="net.reliableresponse.notification.usermgmt.*" %><%@ page import="net.reliableresponse.notification.util.*" %><%@ page import="net.reliableresponse.notification.*" %><%@ taglib uri="/reliable.tld" prefix="reliable" %><%
	User user = BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user"));
	String notifsTitle = request.getParameter("notifsTitle");
	if (notifsTitle == null) {
		response.sendRedirect("ActionServlet?page=/index.jsp");
		return;
	} 
	String sentNotifsTitle = request.getParameter("sentNotifsTitle");
	
	if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.MANAGED)) {
		response.sendRedirect("hosted/index.jsp");
	} else if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.TXTIT)) {
		response.sendRedirect("txtit/index.jsp");
	}

%><jsp:include page="header.jsp" />
<input type="hidden" name="page" value="/index.jsp" />

<%
	String viewActiveString = request.getParameter("view_active");
	boolean viewActive = true;
	if (viewActiveString != null) viewActive = viewActiveString.toLowerCase().startsWith("t");

	String viewConfirmedString = request.getParameter("view_confirmed");
	boolean viewConfirmed = true;
	if (viewConfirmedString != null) viewConfirmed = viewConfirmedString.toLowerCase().startsWith("t"); 

	String viewExpiredString = request.getParameter("view_expired");
	boolean viewExpired = true;
	if (viewExpiredString != null) viewExpired = viewExpiredString.toLowerCase().startsWith("t"); 
	
	String viewOnholdString = request.getParameter("view_onhold");
	boolean viewOnhold = true;
	if (viewOnholdString != null) viewOnhold = viewOnholdString.toLowerCase().startsWith("t"); 
	
	String viewActiveByMeString = request.getParameter("view_byme_active");
	boolean viewActiveByMe = true;
	if (viewActiveByMeString != null) viewActiveByMe = viewActiveByMeString.toLowerCase().startsWith("t");

	String viewConfirmedByMeString = request.getParameter("view_byme_confirmed");
	boolean viewConfirmedByMe = true;
	if (viewConfirmedByMeString != null) viewConfirmedByMe = viewConfirmedByMeString.toLowerCase().startsWith("t"); 

	String viewExpiredByMeString = request.getParameter("view_byme_expired");
	boolean viewExpiredByMe = true;
	if (viewExpiredByMeString != null) viewExpiredByMe = viewExpiredByMeString.toLowerCase().startsWith("t"); 
	
	String viewOnholdByMeString = request.getParameter("view_byme_onhold");
	boolean viewOnholdByMe = true;
	if (viewOnholdByMeString != null) viewOnholdByMe = viewOnholdByMeString.toLowerCase().startsWith("t"); 

	%>
	<input type="hidden" name="view_active" value="<%= viewActive %>">
	<input type="hidden" name="view_confirmed" value="<%= viewConfirmed %>">
	<input type="hidden" name="view_expired" value="<%= viewExpired %>">
	<input type="hidden" name="view_onhold" value="<%= viewOnhold %>">
	
	<input type="hidden" name="view_byme_active" value="<%= viewActiveByMe %>">
	<input type="hidden" name="view_byme_confirmed" value="<%= viewConfirmedByMe %>">
	<input type="hidden" name="view_byme_expired" value="<%= viewExpiredByMe %>">
	<input type="hidden" name="view_byme_onhold" value="<%= viewOnholdByMe %>">
	

<!-- <input type="image" src="images/refresh.gif" name="action_refresh">-->
<tr><td colspan="4" class="mainarea">
<%
	String openedString = request.getParameter("opened.pendingNotifications");
	boolean opened =  ((openedString != null) && (openedString.toLowerCase().equals ("true")));
	if (request.getParameter("action_toggle_collapseable.pendingNotifications.x") != null) {
		opened = !opened;
	}
	
%>
<div align="right"><a name="current"></a><a href="#sendNotification" class="anchorlinks">&nbsp</a></div>
<table width="100%" cellspacing="0" border="0" cellpadding="0">

<reliable:collapseable tag="pending Notifications" title="<%= notifsTitle %>"
contentURL="/pendingNotifications/index.jsp" opened="false">
</reliable:collapseable>

<tr><td colspan="25">&nbsp;</td></tr>

<reliable:collapseable tag="sent Notifications" title="<%= sentNotifsTitle %>"
contentURL="/pendingNotifications/sentNotifications.jsp" opened="false">
</reliable:collapseable>

<tr><td colspan="25">&nbsp;</td></tr><%
	String sendTitle = request.getParameter ("sendTitle");
	if (sendTitle == null) sendTitle = "<td colspan=\"2\">Send Notification</td>";
%>
<reliable:collapseable tag="send Notification" title="<%= sendTitle %>"
contentURL="/sendNotification/index.jsp" opened="true">
</reliable:collapseable>

<tr><td colspan="25">
<div align="right"><a href="#devices" class="anchorlinks"> &nbsp;</a></div>
</td>

<reliable:collapseable tag="reports" title="Reports</td>"
contentURL="/reports/index.jsp" opened="false">
</reliable:collapseable>




<jsp:include page="footer.jsp" />