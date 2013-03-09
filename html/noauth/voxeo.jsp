<?xml version="1.0" encoding="UTF-8"?>
<%@page import="net.reliableresponse.notification.NotificationException"%>
<vxml version = "2.1" >
<meta name="maintainer" content="support@reliableresponse.net"/>
<%@page import="net.reliableresponse.notification.Notification"%>
<%@page import="net.reliableresponse.notification.util.StringUtils"%>
<%@page import="net.reliableresponse.notification.broker.BrokerFactory"%>
<%@page import="net.reliableresponse.notification.device.Device"%>
<%@page import="net.reliableresponse.notification.device.VoxeoDevice"%>
<%
String[] dtmfNames =  {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

response.setContentType("text/xml");

Notification notification = null;

String notifUuid = request.getParameter("notification");
if (!StringUtils.isEmpty(notifUuid)) {
	notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(notifUuid);
}

Device device = null;
String deviceUuid = request.getParameter("device");
if (!StringUtils.isEmpty(deviceUuid)) {
	device = BrokerFactory.getDeviceBroker().getDeviceByUuid(deviceUuid);
}


BrokerFactory.getLoggingBroker().logDebug("Voxeo notification="+notifUuid+", device="+deviceUuid);

if ((notification == null) || (device == null) || (!(device instanceof VoxeoDevice))) {
	BrokerFactory.getLoggingBroker().logDebug("Voxeo notification or device not found");
	// If we can't find the notification, device or the device is the
	// wrong type, then just return the boilerplate error
%>
  <form>
    <block>
    <prompt>
        I'm sorry, but we could not find the notification
    </prompt>
    </block>
  </form>
<%
} else {
	
	String[] responses = notification.getSender().getAvailableResponses(notification);
	String message ="";
	if ((!StringUtils.isEmpty(notification.getSender().getNotificationType()) && (!notification.getSender().getNotificationType().equalsIgnoreCase("blank")))) {
			message += "You have a new "+notification.getSender().getNotificationType()+" from "+notification.getSender()+".  ";

	}
	if (!StringUtils.isEmpty(notification.getSubject())) {
		if  (!notification.getSender().getNotificationType().equalsIgnoreCase("blank")) {
			message += "The subject is ";
		}
		message += notification.getSubject()+".  ";
	}
	message +=notification.getMessages()[0].getMessage()+".  ";
	
	String wordGrammer = "";
	String responseGrammer = "";
	String digitGrammer = "";
	if (notification.isPersistent()) {
		for (int i = 0; i < responses.length; i++) {
			message += "Please press " + (i+1) + " to respond with " + responses[i]
					+ ".  ";
			wordGrammer += dtmfNames[i] + " ";
			responseGrammer += "("+responses[i] + ") ";
			digitGrammer+= "dtmf-"+(i+1)+" ";			                         
		}
	}
	
	BrokerFactory.getLoggingBroker().logDebug("word grammer="+wordGrammer);
	BrokerFactory.getLoggingBroker().logDebug("response grammer="+responseGrammer);
	BrokerFactory.getLoggingBroker().logDebug("digit grammer="+digitGrammer);

	
	%>
 
  <form id="MainMessage">
    <field name="response" >

    <grammar type="text/gsl">
        [<%= wordGrammer %>]
    </grammar>

    <grammar type="text/gsl">
        [<%= digitGrammer %> ]
    </grammar>
    
    <prompt>
        <%= message %>
    </prompt>

    <noinput>
      <prompt>
        I did not hear you. Please try again. <reprompt/>
      </prompt>
    </noinput>

    <nomatch>
      <prompt>
        Is that a number? Please try again.
      </prompt>
      <reprompt/>
    </nomatch>

	</field>
	<filled>
		<prompt>
		Thank you for replying with <value expr="response"/>
		</prompt>
		<submit next="voxeo_response.jsp?notification=<%= notifUuid %>&amp;responder=<%= notification.getRecipient().getUuid() %>" namelist="response"  method = "get"/>
	</filled>
  </form>
<%
}
%>
</vxml>
