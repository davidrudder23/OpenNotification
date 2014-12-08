<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.util.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ page import="net.reliableresponse.notification.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="net.reliableresponse.notification.actions.*" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.jsp.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<script language="JavaScript">
function respond(response) {
        document.mainform.response_placeholder.name=response;
        document.mainform.response_placeholder.value=1;
        document.mainform.submit();
}
</script>
<tr>
<td width="11"><img src="images/spacer.gif" width="1" height="1"></td>
<td valign="middle">
<%
	User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user")));
	String uuid = (String)request.getAttribute ("notification_uuid");
	Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid).getChildSentToThisUser(user);
	String respondTextName = "action_respond_text_"+uuid;
	String respondText = request.getParameter(respondTextName);
	
	if (respondText == null) respondText = "";

	String forwardName = "action_forward_"+uuid;
	String commentName = "action_comment_"+uuid;
	
%>
	<textarea name="<%= respondTextName %>" cols="55" rows="4" id="respond"><%= respondText %></textarea> 
</td>
<td valign="top">
<!-- This is the button building code --> 
<table border="0" cellspacing="0" cellpadding="0">
<tr><td>&nbsp;</td></tr>
		<%
			String[] responses = notification.getSender().getAvailableResponses(notification);
			// This needs to move to a utility class
			String[] tmp = new String[responses.length+2];
			System.arraycopy (responses, 0, tmp, 0, responses.length);
			tmp[responses.length]="Comment";
			tmp[responses.length+1]="Forward";
			responses = tmp;
			for (int r = 0; r < responses.length; r++) {
				String responseString = responses[r];
				String responseName = "action_"+responseString.toLowerCase()+"_"+uuid+".x";
				%>
<!-- top -->
<tr><td><img src="images/rsp_topleft.gif"></td>
<td style=" background-repeat: repeat-x; background-image: url('images/rsp_top.gif');"></td>
<td><img src="images/rsp_topright.gif"></td></tr>

<!-- middle -->
<tr><td style=" background-repeat: repeat-y; background-image: url('images/rsp_left.gif');"><img src="spacer.gif" height="5" width="1"></td>
<td class="subexpand" align="center"><div style="line-height: 10pt; cursor: pointer;" onclick="respond('<%= responseName %>');"><%= responseString %></div></td>
<td style=" background-repeat: repeat-y; background-image: url('images/rsp_right.gif');"><img src="spacer.gif" height="5" width="1"></td></tr>

<!-- bottom -->
<tr><td><img src="images/rsp_botleft.gif"></td><td style=" background-repeat: repeat-x; background-image: url('images/rsp_bottom.gif');" valign="bottom"><img src="spacer.gif" height="6"></td>
<td><img src="images/rsp_botright.gif"></td></tr>
<tr><td colspan="25"><img src="images/spacer.gif" width="1" height="6"></td></tr>
<!-- End button building code -->
<%
			}
		%>
</table>
</td>
</tr>
<tr><td height="5"></td></tr>
