<%@page import="net.reliableresponse.notification.usermgmt.User"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="java.util.Enumeration"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%

String[] dtmfNames =  {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

BrokerFactory.getLoggingBroker().logDebug("Got response");

Enumeration paramNames = request.getParameterNames();
while (paramNames.hasMoreElements()) {
	String name = (String)paramNames.nextElement();
	String value = request.getParameter(name);
	BrokerFactory.getLoggingBroker().logDebug("VOXEO: "+name+":"+value);
	
}

String notifResponse = request.getParameter("response");
String responderUuid = request.getParameter("responder");
String notificationUuid = request.getParameter("notification");

if (StringUtils.isEmpty(notificationUuid) || StringUtils.isEmpty(responderUuid) || StringUtils.isEmpty(notifResponse)) {
	BrokerFactory.getLoggingBroker().logInfo("Voxeo response didn't have required fields");
	return;
}

Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
if (notification == null) {
	BrokerFactory.getLoggingBroker().logInfo("Voxeo response had a bad notif uuid "+notificationUuid);
	return;
}

User responder = BrokerFactory.getUserMgmtBroker().getUserByUuid(responderUuid);
if (responder == null) {
	BrokerFactory.getLoggingBroker().logInfo("Voxeo response had a bad responder uuid "+responderUuid);
	return;
}

String[] responses = notification.getSender().getAvailableResponses(notification);
int responseNum = -1;
try {
	responseNum = Integer.parseInt(notifResponse);
} catch (NumberFormatException nfExc) {
}

if (responseNum < 0) {
	for (int i = 0; i < dtmfNames.length; i++) {
		if (notifResponse.equalsIgnoreCase(dtmfNames[i])) {
			responseNum = i;
		}
	}
}

if ((responseNum>=0) && (responseNum < responses.length)) {
	notifResponse = responses[responseNum];
}

notification.getSender().handleResponse(notification, responder, notifResponse, null);
%>
 <form>
    <block>
      <prompt>
        Thank you for replying with <%= notifResponse %>
      </prompt>
    </block>
  </form>
