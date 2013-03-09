<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<tr><td class="abovecell" colspan="25">
<table width="100%"  border="0" cellspacing="3" cellpadding="0">

<tr><td><img src="images/spacer.gif" width="11"></td>
<td colspan="25" width="100%">
<table width="100%">

<%
User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user")));

ReportBroker reportBroker = BrokerFactory.getReportBroker();
String[] reportNames = reportBroker.getReportNames();
%>

<%
for (int r = 0; r < reportNames.length; r++) {
	String description = reportBroker.getReportDescription(reportNames[r]);
	String title="<span class=\"subexpand\">&nbsp;"+description+"&nbsp;</span>";
	request.setAttribute ("report_name", reportNames[r]);
%>
<reliable:collapseable tag="<%= reportNames[r] %>" title="<%= title %>"
contentURL="/reports/options.jsp" tagClass="cellrule">
</reliable:collapseable>
<%
}
%>
</table>
</td>
</tr>
</table></td></tr>
<tr><td colspan="25" width="100%" >&nbsp;</td></tr>
