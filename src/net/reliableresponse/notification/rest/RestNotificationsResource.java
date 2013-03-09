package net.reliableresponse.notification.rest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.rest.converters.NotificationMessageConverter;
import net.reliableresponse.notification.usermgmt.User;

public class RestNotificationsResource extends AbstractRestResource {

	
	public String getRepresentation(String contentType, String method, HttpServletRequest req) throws NotificationException {
		
		Notification[] notifications = BrokerFactory.getNotificationBroker().getNotificationsSince(48*60*60*1000);

		if (contentType.equalsIgnoreCase("text/xml")) {
			XStream xstream = new XStream();
			xstream.alias("notification", Notification.class);
			StringBuffer xml = new StringBuffer();
			xml.append ("<notifications>\n");
			for (int notifNum = 0 ; notifNum < notifications.length; notifNum++) {
				Notification notification = notifications[notifNum];
				if (notification.getParentUuid() == null) {
					xml.append(xstream.toXML(notification));
				}
			}
			xml.append ("</notifications>");
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/json")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			StringBuffer xml = new StringBuffer();
			for (int notifNum = 0 ; notifNum < notifications.length; notifNum++) {
				Notification notification = notifications[notifNum];
				xstream.alias("notification", Notification.class);
				xstream.registerConverter(new NotificationMessageConverter());
				if (notification.getParentUuid() == null) {
					xml.append(xstream.toXML(notification));
				}
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/javascript")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			StringBuffer xml = new StringBuffer();
			for (int notifNum = 0 ; notifNum < notifications.length; notifNum++) {
				Notification notification = notifications[notifNum];
				xstream.alias("notification", Notification.class);
				xstream.registerConverter(new NotificationMessageConverter());
				if (notification.getParentUuid() == null) {
					xml.append("addNotification("+xstream.toXML(notification));
					xml.append(");\n");
				}
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/plain")) {
			StringBuffer text = new StringBuffer();
			for (int notifNum = 0 ; notifNum < notifications.length; notifNum++) {
				Notification notification = notifications[notifNum];
				if (notification.getParentUuid() == null) {
					text.append (notification.getDisplayText());
				}
				text.append ("---------");
			}
			return text.toString();
		}
		
		throw new NotificationException(400, "Representation "+contentType+" is not available");

	}

	public void doUpdate(String contentType, String method, ServletRequest request) throws NotificationException {
		throw new NotificationException(500, "Update not supported");
	}

	public void doAdd(String contentType, String method, ServletRequest request)
			throws NotificationException {
		throw new NotificationException(500, "Add not supported");		
	}

	public void doDelete(String contentType, String method,
			ServletRequest request) throws NotificationException {
		throw new NotificationException(500, "Delete not supported");
	}

	public boolean isValidResource() {
		return true;
	}
	
	public void delete() throws NotificationException {
		throw new NotificationException(NotificationException.UNAVAILABLE, "Can not delete notifications");
	}
}
