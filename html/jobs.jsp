<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.web.util.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<% try {%>
<tr><td colspan="25" class="abovecell">&nbsp;<td></tr>
<tr><td><img src="images/spacer.gif" width="11"></td>
<td><table>

<script type="text/javascript" language="JavaScript">
function runNow(action) {
   document.mainform.action.value = action;
   document.mainform.submit();
}

function stopJob(action) {
   document.mainform.action.value = action;
   document.mainform.submit();
}
</script>
<%
	JobsBroker broker = BrokerFactory.getJobsBroker();


%>
<%
	// List all the configured jobs
	String[] jobNames = broker.getJobNames();
	if (jobNames == null) jobNames = new String[0];
	Vector requiredAttributes = new Vector();
	requiredAttributes.addElement ("jobname");
	request.setAttribute ("requiredAttributes", requiredAttributes);
	
	for (int jobNum = 0; jobNum < jobNames.length; jobNum++) {
		request.setAttribute ("jobname", jobNames[jobNum]);
		String title = "<td>";
		title += jobNames[jobNum];
		title += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td align=\"right\"><a href=\"javascript:runNow('action_trigger_"+jobNames[jobNum]+"');\">--&gt;Run Now</a></td>";
		String tag = "job_"+jobNames[jobNum];
	%>
		<reliable:collapseable title="<%= title %>" tag="<%= tag %>"
			contentURL="/jobs/index.jsp" tagClass="individualgroup">
		</reliable:collapseable>
		
	<%
	}
%>

</tr>
<tr><td colspan="25">&nbsp;</td></tr>
<tr><td colspan="25"><strong>Currently Running Jobs</strong></td></tr>
<%
	// List all the currently executing jobs
	jobNames = broker.getNamesOfCurrentlyRunningJobs();
	if (jobNames == null) jobNames = new String[0];
	for (int jobNum = 0; jobNum < jobNames.length; jobNum++) {
		request.setAttribute ("jobname", jobNames[jobNum]);
		String title = jobNames[jobNum]+" is currently running";
		String name = "action_stop_"+jobNames[jobNum];
		%><tr><td colspan="25"><%= title %>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td align="right"><a href="javascript:stopJob('<%= name %>');">Stop Job</a></td></tr><%
	}
%>
</table></td></tr>
<tr><td colspan="25">
<%} catch (Exception anyExc) { BrokerFactory.getLoggingBroker().logError(anyExc); } %>