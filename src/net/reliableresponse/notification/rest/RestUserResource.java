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
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.UUIDBroker;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

public class RestUserResource extends AbstractRestResource {
	User user = null;
	String htmlXslURL = "xsl/notificationHtml.xsl";
	String textXslURL = "xsl/notificationText.xsl";

	public RestUserResource(String uuid) {
		user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);

	}

	public boolean isValidResource() {
		return (user != null);
	}

	public String getRepresentation(String contentType, String method, HttpServletRequest req)
			throws NotificationException {
		if (user == null) {
			throw new NotificationException(NotificationException.UNAVAILABLE,
					"Can not find specified user");
		}
		user.getDevices();
		user.getInformation("preload");
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("user", User.class);
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(user);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.alias("user", User.class);
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(user);
				return xml;
			} else if (contentType.equalsIgnoreCase("text/javascript")) {
				String callback = req.getParameter("callback");
				if (StringUtils.isEmpty(callback))
					callback = "addUser";

				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.alias("user", User.class);
				xstream.registerConverter(new HashtableConverter());
				String xml = xstream.toXML(user);
				return callback+"(" + xml + ");\n";
			} else if (contentType.equalsIgnoreCase("text/html")) {
				String xml = user.getAsXML();
				return transform(xml, htmlXslURL);
			} else if (contentType.equalsIgnoreCase("text/plain")) {
				return user.toString();
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(400, "Representation failed: "
					+ anyExc.getMessage());
		}

		throw new NotificationException(400, "Representation " + contentType
				+ " is not available");
	}
	
	private User getUser(String contentType, String method,
			ServletRequest request) throws NotificationException {
		try {
			if (contentType.equalsIgnoreCase("text/xml")) {
				XStream xstream = new XStream();
				xstream.alias("user", User.class);
				User user = (User) xstream.fromXML(request.getReader());
				return user;
			} else if (contentType.equalsIgnoreCase("text/json")) {
				XStream xstream = new XStream(
						new JettisonMappedXmlDriver());
				xstream.registerConverter(new HashtableConverter());
				xstream.alias("user", User.class);
				BrokerFactory.getLoggingBroker().logDebug("{\"user\":"+request.getParameter("json")+"}");
				User user = (User) xstream
						.fromXML("{\"user\":"+request.getParameter("json")+"}");
				return user;
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(500, anyExc.getMessage());
		}
		
		return null;
	}

	public void doUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		User user = getUser(contentType, method, request);
		try {
			if (user == null) {
				throw new NotificationException(404, "Can't load user");
			}
			BrokerFactory.getUserMgmtBroker().updateUser(user);
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
		}
		try {
			if (contentType.equalsIgnoreCase("text/html")) {
				doHTMLUpdate(contentType, method, request);
				return;
			} else {
				throw new NotificationException(500,
						"Deserialization is not support for content-type: "
								+ contentType);
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(500, anyExc.getMessage());
		}
	}
	
	public void doAdd (String contentType, String method, ServletRequest request) throws NotificationException {
		User user = getUser(contentType, method, request);
		try {
			if (user == null) {
				throw new NotificationException(404, "Can't add user");
			}
			BrokerFactory.getUserMgmtBroker().addUser(user);
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
			throw new NotificationException(404, "Can't add user");
		}
	}
	
	public void doDelete (String contentType, String method, ServletRequest request) throws NotificationException {
		User user = getUser(contentType, method, request);
		try {
			if (user == null) {
				throw new NotificationException(404, "Can't delete user");
			}
			BrokerFactory.getUserMgmtBroker().deleteUser(user);
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
			throw new NotificationException(404, "Can't delete user");
		}
	}

	public void doHTMLUpdate(String contentType, String method,
			ServletRequest request) throws NotificationException {
		String editFirstName = request.getParameter("firstname");
		String editLastName = request.getParameter("lastname");
		String editEndDepartment = request.getParameter("department");
		String editEmail = request.getParameter("email");

		String editObserverString = request.getParameter("observer");
		boolean editObserver = false;
		if ((editObserverString != null)
				&& (editObserverString.equalsIgnoreCase("on"))) {
			editObserver = true;
		}

		String editCachedString = request.getParameter("cached");
		boolean editCached = false;
		if ((editCachedString != null)
				&& (editCachedString.equalsIgnoreCase("on"))) {
			editCached = true;
		}

		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmpassword");
		if ((password != null) && (password.length() > 0)) {
			if (confirmPassword == null) {
				throw new NotificationException(401,
						"Your passwords do not match");
			}
			if (!password.equals(confirmPassword)) {
				throw new NotificationException(401,
						"Your passwords do not match");
			}

			BrokerFactory.getAuthenticationBroker().changePassword(user,
					password);
		}

		if (editFirstName != null)
			user.setFirstName(editFirstName);
		if (editLastName != null)
			user.setLastName(editLastName);
		if (editEndDepartment != null)
			user.setDepartment(editEndDepartment);

		if (editEmail != null)
			user.setEmailAddress(editEmail);

		if ((editObserver)
				&& (!BrokerFactory.getAuthorizationBroker().isUserInRole(user,
						Roles.OBSERVER))) {
			BrokerFactory.getAuthorizationBroker().addUserToRole(user,
					Roles.OBSERVER);
		}

		if ((!editObserver)
				&& (BrokerFactory.getAuthorizationBroker().isUserInRole(user,
						Roles.OBSERVER))) {
			BrokerFactory.getAuthorizationBroker().removeMemberFromRole(user,
					Roles.OBSERVER);
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"Setting user in cache = " + editCached);
		user.setInPermanentCache(editCached);

	}

}
