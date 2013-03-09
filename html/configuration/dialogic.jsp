<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%
	boolean dialogicIncoming = BrokerFactory.getConfigurationBroker().getBooleanValue("dialogic.incoming", false);
	String dialogicIncomingBoard = BrokerFactory.getConfigurationBroker().getStringValue("dialogic.incoming.board", "dxxxB1C1");
	boolean dialogicOutgoing = BrokerFactory.getConfigurationBroker().getBooleanValue("dialogic.outgoing", false);
	String dialogicOutgoingBoard = BrokerFactory.getConfigurationBroker().getStringValue("dialogic.outgoing.board", "dxxxB1C1");
%>
<tr><td>Enable Inbound Confirmation of Notifications?</td>
<td><input type="checkbox" name="dialogic.incoming" <%= dialogicIncoming?"CHECKED":"" %>></td></tr>
<tr><td>Incoming Dialogic Boardname</td>
<td><input type="text" name="dialogic.incoming.boardname" value="<%= dialogicIncomingBoard %>"></td></tr>
<tr><td>Enable Sending Notifications via Standard Pager and Text-To-Speech?</td>
<td><input type="checkbox" name="dialogic.outgoing" <%= dialogicOutgoing?"CHECKED":"" %>></td></tr>
<tr><td>Outgoing Dialogic Boardname</td>
<td><input type="text" name="dialogic.outgoing.boardname" value="<%= dialogicOutgoingBoard %>"></td></tr>
