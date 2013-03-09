<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	String brokerImpl = BrokerFactory.getConfigurationBroker().getStringValue("broker.impl");
	String postgresql = brokerImpl.equalsIgnoreCase("postgresql")?"SELECTED":"";
	String oracle = brokerImpl.equalsIgnoreCase("oracle")?"SELECTED":"";
	String mysql = brokerImpl.equalsIgnoreCase("mysql")?"SELECTED":"";
	String mssql = brokerImpl.equalsIgnoreCase("mssql")?"SELECTED":"";
	
	String databaseHostname = BrokerFactory.getConfigurationBroker().getStringValue("database."+brokerImpl+".hostname", "");
	String databaseDatabase = BrokerFactory.getConfigurationBroker().getStringValue("database."+brokerImpl+".database", "");
	String databaseUsername = BrokerFactory.getConfigurationBroker().getStringValue("database."+brokerImpl+".username", "");
	String databasePassword = BrokerFactory.getConfigurationBroker().getStringValue("database."+brokerImpl+".password", "");
%>
<td>Database Type: </td><td><SELECT name="broker.impl">
<OPTION value="oracle" <%= oracle %>>Oracle
<OPTION value="postgresql" <%= postgresql %>>PostgreSQL
<OPTION value="mysql" <%= mysql %>>MySQL
<OPTION value="mssql" <%= mssql %>>Microsoft SQL Server
</SELECT></td></tr>
<tr><td>Database Hostname</td>
<td><input type="text" name="database.hostname" value="<%= databaseHostname %>"></td></tr>
<tr><td>Database Instance</td>
<td><input type="text" name="database.database" value="<%= databaseDatabase %>"></td></tr>
<tr><td>Database Username</td>
<td><input type="text" name="database.username" value="<%= databaseUsername %>"></td></tr>
<tr><td>Database Password</td>
<td><input type="text" name="database.password" value="<%= databasePassword %>"></td></tr>
<tr><td>Reinitialize Database?</td>
<td><input type="checkbox" name="database.reinitialize"></td></tr>
