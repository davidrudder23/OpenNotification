package net.reliableresponse.notification.rest;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

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
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

public class RestGroupResource extends AbstractRestResource {
	Group group = null;
	String htmlXslURL = "xsl/notificationHtml.xsl";
	String textXslURL = "xsl/notificationText.xsl";

	public RestGroupResource(String uuid) {
		group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);

	}

	public boolean isValidResource() {
		return (group != null);
	}

	public String getRepresentation(String contentType, String method, HttpServletRequest req)
			throws NotificationException {
		if (group == null) {
			throw new NotificationException(NotificationException.UNAVAILABLE,
					"Can not find specified group");
		}
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("group", Group.class);
				String xml = xstream.toXML(group);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.alias("group", Group.class);
				String xml = xstream.toXML(group);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/javascript")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.alias("group", Group.class);
				String xml = xstream.toXML(group);
				return "addGroup("+xml+");";
			} else if (contentType.equalsIgnoreCase("text/html")) {
				String xml = group.getAsXML();
				return transform(xml, htmlXslURL);
			} else if (contentType.equalsIgnoreCase("text/plain")) {
				return group.toString();
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(400, "Representation failed: "
					+ anyExc.getMessage());
		}

		throw new NotificationException(400, "Representation " + contentType
				+ " is not available");
	}

	private Group getGroup(String contentType, String method,
			ServletRequest request) throws NotificationException {
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("group", Group.class);
				Group group = (Group) xstream.fromXML(request.getReader());
				return group;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.registerConverter(new HashtableConverter());
				xstream.alias("group", Group.class);
				BrokerFactory.getLoggingBroker().logDebug("{\"group\":"+request.getParameter("json")+"}");
				Group group = (Group) xstream
						.fromXML("{\"group\":"+request.getParameter("json")+"}");
				return group;
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(500, anyExc.getMessage());
		}
		
		return null;
	}

	public void doUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		Group group = getGroup(contentType, method, request);
		if (group == null) {
			throw new NotificationException(404, "Can't load group");
		}
		BrokerFactory.getGroupMgmtBroker().updateGroup(group);
	}
	
	public void doAdd (String contentType, String method, ServletRequest request) throws NotificationException {
		Group group = getGroup(contentType, method, request);
		try {
			if (group == null) {
				throw new NotificationException(404, "Can't add group");
			}
			BrokerFactory.getGroupMgmtBroker().addGroup(group);
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
			throw new NotificationException(404, "Can't add group");
		}
	}
	
	public void doDelete (String contentType, String method, ServletRequest request) throws NotificationException {
		Group group = getGroup(contentType, method, request);
		try {
			if (group == null) {
				throw new NotificationException(404, "Can't delete group");
			}
			BrokerFactory.getGroupMgmtBroker().deleteGroup(group);
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
			throw new NotificationException(404, "Can't delete group");
		}
	}

	public void delete() throws NotificationException {
		try {
			BrokerFactory.getGroupMgmtBroker().deleteGroup(group);
		} catch (NotSupportedException e) {
			throw new NotificationException(NotificationException.FAILED, e
					.getMessage());
		}
		group = null;
	}
}
