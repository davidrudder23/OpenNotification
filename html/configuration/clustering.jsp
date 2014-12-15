<%@page import="java.util.List"%><%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	List<String> clusterServers = BrokerFactory.getConfigurationBroker().getStringValues("cluster.server");
	String clusterName = BrokerFactory.getConfigurationBroker().getStringValue("cluster.name");
%>
<tr><td>URL to use to access this server from a clustered machine?</td>
<td colspan="2"><input type="text" name="cluster.name" value="<%= clusterName %>"></td></tr>
<%
	if ((clusterServers != null) && (clusterServers.size() > 0)) {
%><tr><td colspan="2">&nbsp;</td><td>Remove?</td></tr><%
	}%>	
<%
	if (clusterServers != null) {
		for (String clusterServer: clusterServers) {
		%><tr><td>Cluster's URL</td>
		<td><input type="text" name="cluster.server" value="<%= clusterServer %>"></td>
		<td><input type="checkbox" name="remove_cluster" value="<%= clusterServer %>"></td></tr>
		<%
		}
	}
%>
<tr><td>New Cluster Server</td>
<td colspan="2"><input type="text" name="cluster.server" value=""></td></tr>

