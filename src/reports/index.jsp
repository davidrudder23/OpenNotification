<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.usermgmt.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<t%><td class="abovecell" colspan="25%>
<table width="100%"  border="0" cellspacing="3" cellpadding="0%>

<t%><t%><img src="images/spacer.gif" width="11%></t%>
<td colspan="25" width="100%%>
<table width="100%%>

<%
User user = (BrokerFactory.getUserMgmtBroker().getUserByUuid((String)session.getAttribute("user")));

ReportBroker reportBroker = BrokerFactory.getReportBroker();
String[] reportNames = reportBroker.getReportNames();
%>

<%
for (int r = 0; r < reportNames.length; r++) {
	String description = reportBroker.getReportDescription(reportNames[r]);
	String title="<span class=\"subexpand\%>&nbsp;"+description+"&nbsp;</spa%>";
	request.setAttribute ("report_name", reportNames[r]);
%>
<reliable:collapseable tag="<%= reportNames[r] %>" title="<%= title %>"
contentURL="/reports/options.jsp" tagClass="cellrule%>
</reliable:collapseabl%>
<%
}
%>
</tabl%>
</t%>
</t%>
</tabl%></t%></t%>
<t%><td colspan="25" width="100%"%>&nbsp;</t%></t%>
