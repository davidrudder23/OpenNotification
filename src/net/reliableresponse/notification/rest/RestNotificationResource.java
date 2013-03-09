package net.reliableresponse.notification.rest;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.rest.converters.NotificationMessageConverter;
import net.reliableresponse.notification.usermgmt.User;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class RestNotificationResource extends AbstractRestResource {
	Notification notification = null;
	String htmlXslURL = "xsl/notificationHtml.xsl";
	String textXslURL = "xsl/notificationText.xsl";

	public RestNotificationResource(String uuid) {
		notification = BrokerFactory.getNotificationBroker()
				.getNotificationByUuid(uuid);

	}

	public boolean isValidResource() {
		return (notification != null);
	}

	public String getRepresentation(String contentType, String method,
			HttpServletRequest req) throws NotificationException {
		if (notification == null) {
			throw new NotificationException(NotificationException.UNAVAILABLE,
					"Can not find specified notification");
		}
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("notification", Notification.class);
				xstream.registerConverter(new NotificationMessageConverter());
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(notification);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(new JettisonMappedXmlDriver());
				xstream.alias("notification", Notification.class);
				xstream.registerConverter(new NotificationMessageConverter());
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(notification);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/javascript")) {
				XStream xstream = new XStream(new JettisonMappedXmlDriver());
				xstream.alias("notification", Notification.class);
				xstream.registerConverter(new NotificationMessageConverter());
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(notification);
				return "addNotification(" + xml + ");";
			} else if (contentType.equalsIgnoreCase("text/html")) {
				String xml = notification.getAsXML();
				return transform(xml, htmlXslURL);
			} else if (contentType.equalsIgnoreCase("text/plain")) {
				return notification.getDisplayText();
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(400, "Representation failed: "
					+ anyExc.getMessage());
		}

		throw new NotificationException(400, "Representation " + contentType
				+ " is not available");
	}

	private Notification getNotification(String contentType, String method,
			ServletRequest request) throws NotificationException {
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("notification", Notification.class);
				Notification notification = (Notification) xstream.fromXML(request.getReader());
				return notification;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.registerConverter(new HashtableConverter());
				xstream.alias("notification", Notification.class);
				BrokerFactory.getLoggingBroker().logDebug("{\"notification\":"+request.getParameter("json")+"}");
				Notification notification = (Notification) xstream
						.fromXML("{\"notification\":"+request.getParameter("json")+"}");
				return notification;
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(500, anyExc.getMessage());
		}
		
		return null;
	}

	public void doUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		Notification notification = getNotification(contentType, method, request);
		if (notification == null) {
			throw new NotificationException(404, "Can't load notification");
		}
		BrokerFactory.getNotificationBroker().setNotificationStatus(notification, notification.getStatusAsString());
	}
	
	public void doAdd (String contentType, String method, ServletRequest request) throws NotificationException {
		Notification notification = getNotification(contentType, method, request);
		SendNotification.getInstance().doSend(notification);
	}
	
	public void doDelete (String contentType, String method, ServletRequest request) throws NotificationException {
		throw new NotificationException(NotificationException.UNAVAILABLE,
		"Can not delete notifications");
	}


	public static void main(String[] args) throws Exception {

		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		RestNotificationResource res = new RestNotificationResource("0000001");
		System.out.println(res
				.getRepresentation("text/javascript", "GET", null));
	}
}
