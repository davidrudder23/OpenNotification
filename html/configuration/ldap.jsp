<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.broker.impl.MultiRealmAuthenticationBroker" %>
<%@ page import="net.reliableresponse.notification.broker.AuthenticationBroker" %>
<%@ page import="net.reliableresponse.notification.broker.impl.LDAPAuthenticationBroker" %>
<%
	boolean ldapEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("ldap.import", false);
	AuthenticationBroker authnBroker = BrokerFactory.getAuthenticationBroker();
	boolean ldapAuthnEnabled = false;
	
	if (authnBroker instanceof MultiRealmAuthenticationBroker) {
		AuthenticationBroker[] authnBrokers = ((MultiRealmAuthenticationBroker)authnBroker).getAuthenticationBrokers();
		for (int i = 0; i < authnBrokers.length; i++) {
			if (authnBrokers[i] instanceof LDAPAuthenticationBroker) {
				ldapAuthnEnabled = true;
			}
		}		
	}
	String ldapHost = BrokerFactory.getConfigurationBroker().getStringValue("ldap.host", "");
	String ldapUsername = BrokerFactory.getConfigurationBroker().getStringValue("ldap.username", "");
	String ldapBase = BrokerFactory.getConfigurationBroker().getStringValue("ldap.base", "");
	String ldapSearchString = BrokerFactory.getConfigurationBroker().getStringValue("ldap.searchString", "");
	String ldapCompare = BrokerFactory.getConfigurationBroker().getStringValue("ldap.authn.compare", "");
	String ldapField = BrokerFactory.getConfigurationBroker().getStringValue("ldap.authn.field", "sAMAccountName");
	boolean ldapUseSSL = BrokerFactory.getConfigurationBroker().getBooleanValue("ldap.useSSL", false);
%>
<td><table><tr>
<tr><td>LDAP Import Enabled</td>
<td><input type="checkbox" name="ldap.import" <%= ldapEnabled?"CHECKED":"" %>></td></tr>
<tr><td>LDAP Authentication Enabled</td>
<td><input type="checkbox" name="ldap.authentication" <%= ldapAuthnEnabled?"CHECKED":"" %>></td></tr>
<tr><td>LDAP Hostname</td>
<td><input type="text" name="ldap.host" value="<%= ldapHost %>"></td></tr>
<tr><td>LDAP Username</td>
<td><input type="text" name="ldap.username" value="<%= ldapUsername %>"></td></tr>
<tr><td>LDAP Password</td>
<td><input type="password" name="ldap.password"></td></tr>
<tr><td>LDAP Base</td>
<td><input type="text" name="ldap.base" value="<%= ldapBase %>"></td></tr>
<tr><td>LDAP Search String</td>
<td><input type="text" name="ldap.searchString" value="<%= ldapSearchString %>"></td></tr>
<tr><td>String to use when logging in to LDAP.<br>Will replace %n with login name.</td>
<td><input type="text" name="ldap.authn.compare" value="<%= ldapCompare %>"></td></tr>
<tr><td>Which field stores this value</td>
<td><input type="text" name="ldap.authn.field" value="<%= ldapField %>"></td></tr>
</table></td>