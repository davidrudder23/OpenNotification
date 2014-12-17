package net.reliableresponse.notification.rest;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.rest.converters.NotificationMessageConverter;

public class RestDevicesResource extends AbstractRestResource {
	String userUuid;
	public RestDevicesResource (String userUuid) {
		this.userUuid = userUuid;
	}
	
	public String getRepresentation(String contentType, String method, HttpServletRequest req) throws NotificationException {

		
		List<Device> devices = BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid).getDevices(); 
		
		if (contentType.equalsIgnoreCase("text/xml")) {
			XStream xstream = new XStream();
			xstream.alias("notification", Notification.class);
			StringBuffer xml = new StringBuffer();
			xml.append ("<notifications>\n");
			for (Device device: devices) {
				xml.append(xstream.toXML(device));
			}
			xml.append ("</notifications>");
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/json")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			StringBuffer xml = new StringBuffer();
			for (Device device: devices) {
				xstream.alias("device", Device.class);
				xstream.registerConverter(new NotificationMessageConverter());
				xml.append(xstream.toXML(device));
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/javascript")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			StringBuffer xml = new StringBuffer();
			for (Device device: devices) {
				xstream.alias("device", Device.class);
				xstream.registerConverter(new NotificationMessageConverter());
				xml.append("addDevice("+xstream.toXML(device));
				xml.append(");\n");
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/plain")) {
			StringBuffer text = new StringBuffer();
			for (Device device: devices) {
				text.append (device.toString());
				text.append ("---------");
			}
			return text.toString();
		}
		
		throw new NotificationException(400, "Representation "+contentType+" is not available");

	}
	

	public void doAdd(String contentType, String method, ServletRequest request)
			throws NotificationException {
		throw new NotificationException(404, "Can't add notifications");
		
	}

	public void doDelete(String contentType, String method,
			ServletRequest request) throws NotificationException {
		throw new NotificationException(404, "Can't delete notifications");
		
	}

	public void doUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		throw new NotificationException(404, "Can't update notifications");
	}

	public boolean isValidResource() {
		return true;
	}
	
	public void delete() throws NotificationException {
		throw new NotificationException(NotificationException.UNAVAILABLE, "Can not delete lists of devices");
	}
}
