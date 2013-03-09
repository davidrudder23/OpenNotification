<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ taglib uri="/reliable.tld" prefix="reliable" %>
<%
	JobsBroker broker = BrokerFactory.getJobsBroker();
	String jobName = (String)request.getParameter("jobname");
	if (jobName == null) {
		jobName = (String)request.getAttribute("jobname");
	}

	BrokerFactory.getLoggingBroker().logDebug("jobname="+jobName);
	String[] history = broker.getHistory (jobName);
	BrokerFactory.getLoggingBroker().logDebug("Got "+history.length+" history elements");
	
	for (int i = 0; i < (Math.min(history.length, 30)); i++) {
		%><%= history[(history.length-1)-i] %><br><%
	}
%>
