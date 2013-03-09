<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="javax.servlet.jsp.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>

<%
	String report_name = (String)request.getAttribute("report_name");
	String file_name = report_name+".jsp";
	BrokerFactory.getLoggingBroker().logDebug("report name = "+file_name);
%>
<input name="report_name" type="hidden" value="<%= report_name %>">
<tr><td colspan="25">&nbsp;</td></tr>
<tr>
<td width="11" valign="top"><img src="images/spacer.gif" width="1" height="1"></td>
<td valign="middle">
<jsp:include page="<%= file_name %>" />
</td>
</tr>
<tr>
<td width="11" valign="top"><img src="images/spacer.gif" width="1" height="1"></td>
<td colspan="10" align="center">
<input type="image" src="images/btn_pdf.gif" alt="Run report as PDF" name="action_report_pdf_<%= report_name %>" target="report">
<input type="image" src="images/btn_excel.gif"  alt="Run report as Excel" name="action_report_excel_<%= report_name %>" target="report">
<input type="image" src="images/btn_html.gif"  alt="Run report as HTML" name="action_report_html_<%= report_name %>" target="report">
</td>
</tr>
<tr><td height="5"></td></tr>