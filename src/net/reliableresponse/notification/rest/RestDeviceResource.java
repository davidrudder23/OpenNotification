package net.reliableresponse.notification.rest;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

public class RestDeviceResource extends AbstractRestResource {
	Device device = null;
	String htmlXslURL = "xsl/notificationHtml.xsl";
	String textXslURL = "xsl/notificationText.xsl";

	public RestDeviceResource(String uuid) {
		device = BrokerFactory.getDeviceBroker().getDeviceByUuid(uuid);

	}

	public boolean isValidResource() {
		return (device != null);
	}

	public String getRepresentation(String contentType, String method,
			HttpServletRequest req) throws NotificationException {
		if (device == null) {
			throw new NotificationException(NotificationException.UNAVAILABLE,
					"Can not find specified device");
		}
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("device", Device.class);
				String xml = xstream.toXML(device);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(new JettisonMappedXmlDriver());
				xstream.alias("device", Device.class);
				String xml = xstream.toXML(device);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/javascript")) {
				XStream xstream = new XStream(new JettisonMappedXmlDriver());
				xstream.alias("device", Device.class);
				String xml = xstream.toXML(device);
				return "addDevice(" + xml + ");";
			} else if (contentType.equalsIgnoreCase("text/html")) {
				String xml = device.getAsXML();
				return transform(xml, htmlXslURL);
			} else if (contentType.equalsIgnoreCase("text/plain")) {
				return device.toString();
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(400, "Representation failed: "
					+ anyExc.getMessage());
		}

		throw new NotificationException(400, "Representation " + contentType
				+ " is not available");
	}

	private Device getDevice(String contentType, String method,
			ServletRequest request) throws NotificationException {
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("device", Device.class);
				Device device = (Device) xstream.fromXML(request.getReader());
				return device;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(new JettisonMappedXmlDriver());
				xstream.registerConverter(new HashtableConverter());
				xstream.alias("device", Device.class);
				BrokerFactory.getLoggingBroker().logDebug(
						"{\"device\":" + request.getParameter("json") + "}");
				Device device = (Device) xstream.fromXML("{\"device\":"
						+ request.getParameter("json") + "}");
				return device;
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(500, anyExc.getMessage());
		}

		return null;
	}

	public void doUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		Device device = getDevice(contentType, method, request);
		if (device == null) {
			throw new NotificationException(404, "Can't load device");
		}
		Hashtable settings = device.getSettings();
		Enumeration keys = settings.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			BrokerFactory.getDeviceBroker().updateSetting(device, key,
					(String) settings.get(key));
		}
		return;
	}

	public void doAdd(String contentType, String method, ServletRequest request)
			throws NotificationException {
		throw new NotificationException(404, "Can't add device");
	}

	public void doDelete(String contentType, String method,
			ServletRequest request) throws NotificationException {
		throw new NotificationException(404, "Can't delete device");
	}

	public void delete() throws NotificationException {
		BrokerFactory.getDeviceBroker().removeDevice(device.getUuid());
		device = null;
	}
}
