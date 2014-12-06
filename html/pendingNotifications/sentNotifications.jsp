<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.*" %>
<%@ page import="net.reliableresponse.notification.actions.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<%
	User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user")));
	
	SimpleDateFormat dateFormatter = new SimpleDateFormat ("HH:mm:ss z");

	boolean isAdmin = BrokerFactory.getAuthorizationBroker().isUserInRole (user, "Administrators") ||
	BrokerFactory.getAuthorizationBroker().isUserInRole (user, "Observers");

	// Get the stored "display past" value
	String numHoursString = (String) session.getAttribute("notification_byme_hours");
	if ((numHoursString == null) || (numHoursString.length() == 0)) {
		numHoursString = "2";
	}
	int numHours = 2;
	try {
		numHours = Integer.parseInt(numHoursString);
	} catch (NumberFormatException e) {
		BrokerFactory.getLoggingBroker().logError(e);
	}
		
	String viewActiveString = request.getParameter("view_byme_active");
	boolean viewActive = true;
	if (viewActiveString != null) viewActive = viewActiveString.toLowerCase().startsWith("t");

	String viewConfirmedString = request.getParameter("view_byme_confirmed");
	boolean viewConfirmed = true;
	if (viewConfirmedString != null) viewConfirmed = viewConfirmedString.toLowerCase().startsWith("t"); 

	String viewExpiredString = request.getParameter("view_byme_expired");
	boolean viewExpired = true;
	if (viewExpiredString != null) viewExpired = viewExpiredString.toLowerCase().startsWith("t"); 
	
	String viewOnholdString = request.getParameter("view_byme_onhold");
	boolean viewOnhold = true;
	if (viewOnholdString != null) viewOnhold = viewOnholdString.toLowerCase().startsWith("t"); 
	%>
	<input type="hidden" name="view_byme_active" value="<%= viewActive %>">
	<input type="hidden" name="view_byme_confirmed" value="<%= viewConfirmed %>">
	<input type="hidden" name="view_byme_expired" value="<%= viewExpired %>">
	<input type="hidden" name="view_byme_onhold" value="<%= viewOnhold %>">
	<%
		
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	List<Notification> recentNotifications = broker.getNotificationsSentBy(user);
	SortedVector removal = new SortedVector();

	// sort out turned-off types
	for (Notification notification: recentNotifications) {
		if (notification.getTime().getTime() > (System.currentTimeMillis() - (numHours*60*60*1000))) {
			int status = notification.getStatus();
			if ((status == Notification.PENDING) && (viewActive)) {
				removal.addElement(notification);
			} else if ((status == Notification.NORMAL) && (viewActive)) {
				removal.addElement(notification);
			} else if ((status == Notification.CONFIRMED) && (viewConfirmed)) {
				removal.addElement(notification);
			} else if ((status == Notification.EXPIRED) && (viewExpired)) {
				removal.addElement(notification);
			} else if ((status == Notification.ONHOLD) && (viewOnhold)) {
				removal.addElement(notification);
			}
		}
	}		
	
	SortedVector sorted = removal;
	sorted.sort();
	for (int i = 0; i < sorted.size(); i++) {
		Notification notification = (Notification)sorted.elementAt(i);
		Member recipient = notification.getRecipient();
		Member[] recipients = new Member[0];
		int escNumber = -1;
		if (recipient instanceof User) {
			recipients = new Member[1];
			recipients[0] = recipient;
		} else {
			recipients = ((Group)recipient).getMembers();
			if (recipient.getType() == Member.ESCALATION) {
				EscalationThread escThread = EscalationThreadManager.getInstance().getEscalationThread(notification.getUuid());
				BrokerFactory.getLoggingBroker().logDebug("escThread = "+escThread);
				if (escThread != null) {
					escNumber = escThread.getRecipientNumber();
					BrokerFactory.getLoggingBroker().logDebug("esc # = "+escNumber);
				}
			}
		}
		NotificationMessage[] messages = notification.getMessages();
	
		String imageName = "images/led_green.gif";
		if (notification.getStatus() == Notification.CONFIRMED) {
			imageName = "images/led_yellow.gif";
		} else if ((notification.getStatus() == Notification.PENDING) || (notification.getStatus() == Notification.NORMAL)) {
			imageName = "images/led_green.gif";
		} else if (notification.getStatus() == Notification.ONHOLD) {
			imageName = "images/led_blue.gif";
		} else {
			imageName = "images/led_red.gif";
		}
		
	if (i > 500) {
	%>	
		<tr><td colspan="25" height="5"></td></tr>
		<tr><td colspan="25" class="abovecell" width="100%" ></td></tr>
	<%
	}
	%>
	<tr><td class="abovecell" colspan="25">
	<table border="0" cellspacing="3" width="100%">	
	
	<tr><td></td>
	<td><% if (i>0) { %><font color="#FFFFFF"><% } %>&nbsp;Notification Info</td>
	<td><% if (i>0) { %><font color="#FFFFFF"><% } %>&nbsp;Messages</td>
	<td><% if (i>0) { %><font color="#FFFFFF"><% } %>&nbsp;Recipients</td></tr>
	
	<tr>
      <td width="11" valign="top"><img src="<%= imageName %>" alt="" width="11" height="11"></td>
      
      <% String subject = notification.getSubject();
      	if ((subject == null) || (subject.length() <= 0)) {
      		subject = "";
      	}
      	String sender = notification.getSender().toString();
      	if ((sender == null) || (sender.length() <= 0)) {
      		sender = "Unknown sender";
      	}
      	
      	String text  = subject+"\n"+sender+"\n"+dateFormatter.format(notification.getTime())+
      		"\nID: "+notification.getUuid();
      %>
    <td width="150" valign="top">
		<textarea readonly="readonly" rows="4" cols="14"><%= text %></textarea>
	</td>
         
      <td valign="top" nowrap>
           <%
      	String message = notification.getAllMessagesFormattedForDisplay();
        message = message.replaceAll("\n", "\\\\n");
        message = message.replaceAll("\r", "");
        message = message.replaceAll("'", "\\\\'");
      	message = message.replaceAll("\"", "&quot;");
      %>
<textarea name="messages" cols="27" rosws="4" readonly="readonly" wrap="off" id="messages" onclick="confirm('<%= message %>')"> 
<%= notification.getAllMessagesFormattedForDisplay() %>
</textarea>
</td><td>
<textarea name="recipientlist" rows="4" cols="15" readonly="readonly" wrap="off" disabled="disabled" >
<% 
for (int r = 0; r < recipients.length; r++) { 
	if (r == escNumber) {
%>*<%= recipients[r].toString() %>*
<% } else {
%><%= recipients[r].toString() %>
<%}}%></textarea></td>

</tr>
<%
	request.setAttribute ("notification_uuid", notification.getUuid());
	String tagName = "recipients_"+notification.getUuid();
	String title="<span class=\"subexpand\">&nbsp;recipient's devices&nbsp;</span>";
%>
<tr><td colspan="25" width="100%">
<reliable:collapseable tag="<%= tagName %>" title="<%= title %>"
contentURL="/pendingNotifications/recipient.jsp" tagClass="cellrule">
</reliable:collapseable>
</td></tr>
<tr><td>&nbsp;</td></tr>
</table>
</td></tr>
<%
}
%>