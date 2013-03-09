<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean voiceshotEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("voiceshot",false);
	String voiceshotCampaign = BrokerFactory.getConfigurationBroker().getStringValue("voiceshot.campaign", "");
%>
<tr><td>Enable Telephone Notification via VoiceShot</td>
<td><input type="checkbox" name="voiceshot" <%= voiceshotEnabled?"CHECKED":""%>></td></tr>
<tr><td>Campaign or Menu ID</td>
<td><input type="text" name="voiceshot.campaign" value="<%= voiceshotCampaign %>"></td></tr>
