
<jsp:directive.page import="net.reliableresponse.notification.util.StringUtils"/>
<jsp:directive.page import="net.reliableresponse.notification.broker.BrokerFactory"/>
<jsp:directive.page import="net.reliableresponse.notification.Notification"/>
<jsp:directive.page import="net.reliableresponse.notification.NotificationMessage"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Member"/>
<jsp:directive.page import="net.reliableresponse.notification.util.SortedVector"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.EscalationGroup"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Group"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.User"/><%
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

String uuid = (String)request.getParameter("uuid");
if (StringUtils.isEmpty(uuid)) {
	// TODO: give a decent error for no uuid
	return;
}
Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid);
if (notification == null) {	
	// TODO: give a decent error for no uuid
	return;
}

String browseType = request.getParameter("browse");
if (StringUtils.isEmpty(browseType)) {
	browseType = "all";
}

String status = "active";
switch (notification.getStatus()) {
case Notification.PENDING:
	status = "active";
	break;
case Notification.NORMAL:
	status = "active";
	break;
case Notification.CONFIRMED:
	status = "confirmed";
	break;
case Notification.EXPIRED:
	status = "expired";
	break;
case Notification.ONHOLD:
	status = "onhold";
	break;
}

String sentDate = user.getFormattedDate(notification.getTime(), "MM/dd/yyyy - HH:mm:ss z");

// Figure out the previous and next notifs

// First, grab all applicable notifs and sort
Notification[] notifs = BrokerFactory.getNotificationBroker().getNotificationsSentTo(user);
SortedVector prevNextList = new SortedVector();
for (int n = 0; n < notifs.length; n++) {
	if (browseType.equals("active")) {
		if ((notifs[n].getStatus() == Notification.PENDING) ||
			(notifs[n].getStatus() == Notification.NORMAL)) {
			prevNextList.addElement(notifs[n], false);
		}
	} else {
		prevNextList.addElement(notifs[n], false);
	}
}
prevNextList.sort();

// Next, find which notif we're looking at
int notifIndex = 0;
for (int n = 0; n < prevNextList.size(); n++) {
	Notification thisNotif = (Notification)prevNextList.elementAt(n);
	if (thisNotif.equals(notification)) {
		notifIndex = n;
	}
}

String prev = notification.getUuid();
String next = notification.getUuid();

if (prevNextList.size()>0) {
if (notifIndex <= 0) {
	prev = ((Notification)prevNextList.elementAt(prevNextList.size()-1)).getUuid();
} else {
	prev = ((Notification)prevNextList.elementAt(notifIndex-1)).getUuid();	
}

if (notifIndex == prevNextList.size()-1) {
	next = ((Notification)prevNextList.elementAt(0)).getUuid();
} else {
	next = ((Notification)prevNextList.elementAt(notifIndex+1)).getUuid();	
}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Reliable Response Notification</title>
<link href="rr.css" rel="stylesheet" type="text/css" />
<style type="text/css">
<!--
body {
	background-color: #19A1E6;
}
-->
</style></head>

<body>
<script>
function doRespond(responseValue) {
document.confirmForm.response.value=responseValue;
document.confirmForm.submit();
}

</script>

<!--  this is the form for confirming -->
<form name="confirmForm" action="process/confirm.jsp">
<input type="hidden" name="uuid" value="<%= uuid %>" />
<input type="hidden" name="browse" value="<%= browseType %>" />
<input type="hidden" name="response" value="confirm" />
</form>

<div align="center">
<table width="310" border="0" cellpadding="0" cellspacing="0" class="box">
  <tr>
    <td bgcolor="#FFFFFF" class="<%= status %>"><%= status.toUpperCase() %></td>
  <tr>
    <td bgcolor="#FFFFFF" class="cellspace"><div align="center" style="border: thin solid #000000; background-color: #FFFFFF;">
    <a href="notification.jsp?browse=<%= browseType %>&uuid=<%=prev %>"><img src="images/btn_previous.gif" width="10" height="10" border="0" /></a> browse
        <%= browseType %> 
    <a href="notification.jsp?browse=<%= browseType %>&uuid=<%=next %>"><img src="images/btn_next.gif" width="10" height="10" border="0" /></a></div></td>
  <tr>
      <td width="380" bgcolor="#FFFFFF" class="cellspace"><strong>From: <%= notification.getSender() %><br />
        Subject: <%= StringUtils.htmlEscape(notification.getSubject()) %><br/>
        <%= sentDate %><br />
		</strong>
        <br /><br />
		<%= StringUtils.htmlEscape(notification.getDisplayText()) %><br />
        <br />
        <% if (status.equals("active")) {
        	String[] responses = notification.getSender().getAvailableResponses(notification);
        	for (int responseNum = 0; responseNum < responses.length; responseNum++) {
        %>
		<span class="button" style="color: #cc0000;" onclick="doRespond('<%= responses[responseNum] %>');">&nbsp;<%= responses[responseNum] %>&nbsp;</span>
		<%} 
		}%>
		<br/><br/>
        <p><strong><span class="header_plain">Comments</span></strong><br /></p>
        <%
        NotificationMessage[] messages = notification.getMessages();
        for (int m = 1; m < messages.length; m++) {
        	NotificationMessage message = messages[m];
        	String commentDate = user.getFormattedDate(message.getAddedon(), "MM/dd/yyyy - HH:mm:ss z");
        	String addedBy = message.getAddedby();
        	if (StringUtils.isEmpty(addedBy)) {
        		addedBy = "";
        	} else {
        		Member addedMember = BrokerFactory.getUserMgmtBroker().getUserByUuid(addedBy);
        		if (addedMember != null) {
        			addedBy = addedMember.toString();
        		}
        		addedBy = addedBy+" - ";
        	}
        %>
          <strong><%= commentDate %></strong><br />
        	<%= addedBy %><%= StringUtils.htmlEscape(message.getMessage()) %> 
	         <br/><br/>
         <% } %>
        <p>
        <form id="commentForm" name="commentForm" method="post" action="process/comment.jsp">
		<input type="hidden" name="uuid" value="<%= uuid %>" />
		<input type="hidden" name="browse" value="<%= browseType %>" />
        <span class="header_plain">Add a Comment</span>
          <textarea name="comment" cols="40"></textarea>
          <br /><br />
          <!-- Comment and close buttons -->
        <table width="100%"><tr><td align="left" width="50%">
		<br/>
		<span class="button" onclick="document.commentForm.submit();">&nbsp;Comment&nbsp;</span>
		<br/>
		<br/>
		</td><td align="right" width="50%">
		<br/>
		<span class="button" onclick="window.close();">&nbsp;Close&nbsp;</span>
		<br/>
		<br/>
		</td></tr></table><br/><br/>
		</form>
     </td>    
     </tr>
    </table>
</div>
</form>
</body>
</html>
