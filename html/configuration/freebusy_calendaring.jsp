<%@ page import="net.reliableresponse.notification.broker.BrokerFactory" %>
<%@ page import="net.reliableresponse.notification.broker.CalendarBroker" %>
<%@ page import="net.reliableresponse.notification.broker.impl.ExchangeCalendarBroker" %>
<%@ page import="net.reliableresponse.notification.broker.impl.ICalCalendarBroker" %>
<%
	CalendarBroker calendarBroker = BrokerFactory.getCalendarBroker();
	BrokerFactory.getLoggingBroker().logDebug("calendar broker = "+calendarBroker);
	boolean exchangeSelected = false;
	boolean icalSelected = false;
	if (calendarBroker instanceof ExchangeCalendarBroker) {
		exchangeSelected = true;
	} else if (calendarBroker instanceof ICalCalendarBroker) {
		icalSelected = true;
	}
%>
<td><input type="radio" name="calendar.broker" value="exchange" <%= exchangeSelected?"CHECKED":"" %> />Exchange Calendaring</td></tr>
<tr><td><input type="radio" name="calendar.broker" value="ical" <%= icalSelected?"CHECKED":"" %> />iCal Calendaring</td></tr>
<%
	if (exchangeSelected) {
	boolean exchangeEnabled = BrokerFactory.getConfigurationBroker().getBooleanValue("calendar.exchange",false);
	String hostname = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.hostname", "");
	int port = BrokerFactory.getConfigurationBroker().getIntValue("calendar.exchange.port", 80);
	String user = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.username", "");
	String password = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.password", "");
	String domain = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.domain", "");
%>
<tr><td>Enable Exchange Calendaring</td>
<td><input type="checkbox" name="calendar.exchange" <%= exchangeEnabled?"CHECKED":""%>></td></tr>
<tr><td>Exchange Hostname</td>
<td><input type="text" name="calendar.exchange.hostname" value="<%= hostname %>"></td></tr>
<tr><td>Exchange Port</td>
<td><input type="text" name="calendar.exchange.port" value="<%= port %>"></td></tr>
<tr><td>Exchange Username</td>
<td><input type="text" name="calendar.exchange.username" value="<%= user %>"></td></tr>
<tr><td>Exchange Password</td>
<td><input type="password" name="calendar.exchange.password"></td></tr>
<tr><td>Exchange Domain</td>
<td><input type="text" name="calendar.exchange.domain" value="<%= domain %>"></td></tr>
<%
	} else if (icalSelected) {
%><tr><td>Please tell the users to configure their ICal FreeBusy URL
in their Personal Settings area</td></tr><%
	}
%>