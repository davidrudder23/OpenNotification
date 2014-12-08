<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.*" %>
<%@ page import="net.reliableresponse.notification.aggregation.*" %>
<%@ page import="net.reliableresponse.notification.actions.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.math.BigInteger" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<script type="text/javascript" language="JavaScript">
	function toggleCheck(theElement) {
		for (i=0; i < document.mainform.notification_list.length; i++) {
			document.mainform.notification_list[i].checked = theElement.checked;
		}
	}
	
	function display_message(message) {
		confirm(message);
	}
	
	function run_checked (action) {
		setAction (action);
		document.mainform.submit();
	}
	
</script>

<%
	User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user")));
	
	SimpleDateFormat dateFormatter = new SimpleDateFormat ("HH:mm:ss z");

	boolean isAdmin = BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.ADMINISTRATOR) ||
	BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.OBSERVER);

	// Get the stored "display past" value
	String numHoursString = (String) session.getAttribute("notification_hours");
	if ((numHoursString == null) || (numHoursString.length() == 0)) {
		numHoursString = "2";
	}
	int numHours = 2;
	try {
		numHours = Integer.parseInt(numHoursString);
	} catch (NumberFormatException e) {
		BrokerFactory.getLoggingBroker().logError(e);
	}
		
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
	
	%>
	<input type="hidden" name="view_active" value="<%= viewActive %>">
	<input type="hidden" name="view_confirmed" value="<%= viewConfirmed %>">
	<input type="hidden" name="view_expired" value="<%= viewExpired %>">
	<input type="hidden" name="view_onhold" value="<%= viewOnhold %>">
	<%
		
	NotificationBroker broker = BrokerFactory.getNotificationBroker();
	BigInteger bigint = new BigInteger(""+numHours);
	bigint = bigint.multiply(new BigInteger("3600"));
	bigint = bigint.multiply(new BigInteger("1000"));
	
	List<Notification> recentNotifications = broker.getNotificationsSince(bigint.longValue());
	SortedVector sorted = new SortedVector();
	for (Notification recentNotification: recentNotifications) {
		try {
			if (recentNotification.getParentUuid() == null) {
				if (isAdmin) {
						sorted.addElement(recentNotification);
				} else {
					Member recipient = recentNotification.getRecipient();
					if (recipient.getType() == Member.USER) {
						if (recipient.equals(user)) {
							sorted.addElement(recentNotification);
						}
					} else {
						Group group= (Group)recipient;
						if (group.isMember(user)) {
							sorted.addElement(recentNotification);
						}
					}
				}
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
		}
	}
	
			// Add all the pending notifications
		recentNotifications = broker.getAllPendingNotifications();
		for (Notification recentNotification: recentNotifications) {
			if (recentNotification.getParentUuid() == null) {
				if (!sorted.contains(recentNotification)) {
					if (isAdmin) {
						sorted.addElement(recentNotification);
					} else {
						Member recipient = recentNotification.getRecipient();
						if (recipient.getType() == Member.USER) {
							if (recipient.equals(user)) {
								sorted.addElement(recentNotification);
							}
						} else {
							Group group= (Group)recipient;
							if (group.isMember(user)) {
								sorted.addElement(recentNotification);
							}
						}
					}
				}
			}
		}
		
	SortedVector removal = new SortedVector();

	// sort out turned-off types
	for (int i = 0; i < sorted.size(); i++) {
		try {
			Notification notification = (Notification)sorted.elementAt(i);
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
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
		}
	}		
	
	sorted = removal;
	sorted.sort();
	Notification notification = null;
	for (int i = 0; i < sorted.size(); i++) {
		try {
		notification = (Notification)sorted.elementAt(i);
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
				if (escThread != null) {
					escNumber = escThread.getRecipientNumber();
				}
			}
		}
		NotificationMessage[] messages = notification.getMessages();
	
		String imageName = "images/led_green.gif";
		if (notification.getStatus() == Notification.CONFIRMED) {
			imageName = "images/led_yellow.gif";
		} else if ((notification.getStatus() == Notification.PENDING) || (notification.getStatus() == Notification.NORMAL)) {
			imageName = "images/led_green.gif";
			if (Squelcher.isSquelched(notification.getChildSentToThisUser(user))) {
				imageName = "images/led_squelched.gif";
			}
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
	<td><% if (i>0) { %><font color="#FFFFFF"><% } %>&nbsp;Recipients</td>
	<td><% if (i>0) { %><font color="#FFFFFF"><% } %>Confirm</td></tr>
	
	<tr>
      <td width="11" valign="top"><img src="<%= imageName %>" alt="" width="11" height="11">
      </td>
      
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
<% //This is the messages area %>
<div id="messages" style="position: relative; display: block; overflow: auto; width: 25em; height: 9em" onDblclick="confirm('<%= message %>');">
<%
	for (int messageNum = 0; messageNum < messages.length; messageNum++) {
		String addedby = messages[messageNum].getAddedby();
		if ((addedby != null) && (addedby.length() > 0)) {
			User addedByUser = BrokerFactory.getUserMgmtBroker()
					.getUserByUuid(addedby);
			if (addedByUser != null)
				addedby = addedByUser.toString();
		} else {
			addedby = notification.getSender().toString();
		}
		String contentType = messages[messageNum].getContentType();
		%>From <%= addedby %> on <%= messages[messageNum].getAddedon() %>:<%

		if ((contentType.toLowerCase().indexOf("text/plain")>=0) || (contentType.equalsIgnoreCase(NotificationMessage.NOTIFICATION_CONTENT_TYPE))){
			%><pre style="line-height: .75em">    <%= messages[messageNum].getMessage()%></pre><%
		} else if (BrokerFactory.getConfigurationBroker().getBooleanValue("show.attachments", true)) {
			%>
			<br/>&nbsp;&nbsp;&nbsp;&nbsp;<a href="AttachmentServlet?uuid=<%= notification.getUuid() %>&messageID=<%= messageNum %>">
			<%= messages[messageNum].getFilename() %></a><br/>
			<%
		}
	}
%>

</div>
<!-- End messages area -->
</td><td align=left>
<textarea name="recipientlist" rows="4" cols="13" readonly="readonly" wrap="off" disabled="disabled" id="recipientlist_<%= notification.getUuid()%>">
<% 
for (int r = 0; r < recipients.length; r++) { 
	if (r == escNumber) {
%>*<%= recipients[r].toString() %>*
<% } else {
%><%= recipients[r].toString() %>
<%}}%></textarea></td><td>
<% if ((notification.getStatus() == Notification.ONHOLD) || (notification.getStatus() == Notification.PENDING) || (notification.getStatus() == Notification.NORMAL)) {%>
<input type="checkbox" name="notification_list" value="<%= notification.getUuid() %>">
<%}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
		}
%>
</td>
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
<%
	tagName = "respond_"+notification.getUuid();
	title="<span class=\"subexpand\">&nbsp;respond&nbsp;</span>";
	request.setAttribute ("notification_uuid", notification.getUuid());
%>
<tr><td colspan="25" width="100%">
<reliable:collapseable tag="<%= tagName %>" title="<%= title %>"
contentURL="/pendingNotifications/respond.jsp" tagClass="cellrule">
</reliable:collapseable>
</td></tr>
</table>
</td></tr>
<%
}
%>
<tr><td colspan="25">&nbsp;</td></tr>
<tr><td colspan="25" align="right">
<input type="checkbox" name="checkall" onclick="toggleCheck(this);"/>Select/Deselect All
</td></tr>
<tr><td align="right">
<table border="0" cellspacing="0" cellpadding="0">
<%
	String[] options = {"confirm", "pass", "release"};
	for (int optionNum = 0; optionNum < options.length; optionNum++) {
		String option = options[optionNum];
		String command = "action_"+option+"_marked";
%>
<!-- top -->
<tr><td><img src="images/rsp_topleft.gif"></td>
<td style=" background-repeat: repeat-x; background-image: url('images/rsp_top.gif');"></td>
<td><img src="images/rsp_topright.gif"></td></tr>

<!-- middle -->
<tr><td style=" background-repeat: repeat-y; background-image: url('images/rsp_left.gif');"><img src="spacer.gif" height="5" width="1"></td>
<td class="subexpand" align="center"><div style="line-height: 10pt; cursor: pointer;" onclick="run_checked('<%= command %>');"><%= option %></div></td>
<td style=" background-repeat: repeat-y; background-image: url('images/rsp_right.gif');"><img src="spacer.gif" height="5" width="1"></td></tr>

<!-- bottom -->
<tr><td><img src="images/rsp_botleft.gif"></td><td style=" background-repeat: repeat-x; background-image: url('images/rsp_bottom.gif');" valign="bottom"><img src="spacer.gif" height="6"></td>
<td><img src="images/rsp_botright.gif"></td></tr>
<tr><td colspan="25"><img src="images/spacer.gif" width="1" height="6"></td></tr>
<!-- End button building code -->
<%
	}
%>
</table></td><td>&nbsp;</td>
</td></tr>
<tr><td colspan="25">&nbsp;</td></tr>
