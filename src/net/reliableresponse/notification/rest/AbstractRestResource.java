package net.reliableresponse.notification.rest;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;

public abstract class AbstractRestResource implements RestResource {

	public AbstractRestResource() {

	}

	public String wrapXML(String innerXML, int errorCode, String errorMessage) {
		StringBuffer xml = new StringBuffer();

		xml.append("<rrnapi>");
		xml.append(innerXML);

		xml.append("</rrnapi>");
		return xml.toString();
	}

	public static AbstractRestResource getInstance(String request) throws NotificationException {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting REST resource for " + request);

		// split the request into it's components
		// like /notification/rest/users/0000002 should end up {'',
		// 'notification', 'rest','users','0000002'}
		String[] requestComponents = request.split("/");

		for (int i = 0; i < requestComponents.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Rest[" + i + "=" + requestComponents[i]);
		}

		String mainResource = null;
		String resourceInstance = null;
		String subordinateResourceList = null;
		String subordinateResource = null;

		int index = 2;
		if (requestComponents.length > (index + 1)) {
			index++;
			mainResource = requestComponents[index];
		}
		if (requestComponents.length > (index + 1)) {
			index++;
			resourceInstance = requestComponents[index];
		}
		if (requestComponents.length > (index + 1)) {
			index++;
			subordinateResourceList = requestComponents[index];
		}
		if (requestComponents.length > (index + 1)) {
			index++;
			subordinateResource = requestComponents[index];
		}

		if (mainResource.equals("notifications")) {
			if (resourceInstance == null) {
				return new RestNotificationsResource();
			} else {
				return new RestNotificationResource(resourceInstance);
			}
		} else if (mainResource.equals("groups")) {
			if (resourceInstance == null) {
				return new RestGroupsResource();
			} else if (subordinateResource == null) {
				return new RestGroupResource(resourceInstance);
			}
		} else if (mainResource.equals("users")) {
			if (resourceInstance == null) {
				return new RestUsersResource();
			} else if (subordinateResourceList == null) {
				return new RestUserResource(resourceInstance);
			} else if (subordinateResource == null) {
				return new RestDevicesResource(resourceInstance);
			} else {
				return new RestDeviceResource(subordinateResource);
			}
		} else if (mainResource.equals("devices")) {
			if (resourceInstance == null) {
				throw new NotificationException(404, "You must specify a user to get a device list");
			} else if (subordinateResourceList == null) {
				return new RestDeviceResource(resourceInstance);
			}
		}

		throw new NotificationException (404, "Could not load resource for "+request);
	}

	protected String transform(String xml, String xslName)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		Source xsltSrc = new StreamSource(this.getClass().getClassLoader()
				.getResourceAsStream(xslName));

		String transformedHTML = "";
		Source xmlSrc = new StreamSource(xml);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(xsltSrc);
		ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(resultBytes);
		transformer.transform(xmlSrc, result);
		transformedHTML = new String(resultBytes.toByteArray());
		return transformedHTML;
	}
}
