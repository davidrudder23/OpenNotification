package net.reliableresponse.notification.rest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.sun.media.rtsp.protocol.Request;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

public class RestUsersResource extends AbstractRestResource {

	
	public String getRepresentation(String contentType, String method, HttpServletRequest req) throws NotificationException {
				
		User[] users= new User[BrokerFactory.getUserMgmtBroker().getNumUsers()]; 
		BrokerFactory.getUserMgmtBroker().getUsers(users.length, 0, users);

		if (contentType.equalsIgnoreCase("text/xml")) {
			XStream xstream = new XStream();
			xstream.registerConverter(new HashtableConverter());
			StringBuffer xml = new StringBuffer();
			xml.append ("<users>\n");
			for (int notifNum = 0 ; notifNum < users.length; notifNum++) {
				User user = users[notifNum];
				xml.append(xstream.toXML(user));
			}
			xml.append ("</users>");
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/json")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.registerConverter(new HashtableConverter());
			StringBuffer xml = new StringBuffer();
			for (int notifNum = 0 ; notifNum < users.length; notifNum++) {
				User user = users[notifNum];
				xml.append(xstream.toXML(user));
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/javascript")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.registerConverter(new HashtableConverter());
			xstream.alias("user", User.class);
			StringBuffer xml = new StringBuffer();
			//xml.append ("var users=Array();\n");
			
			String callback = req.getParameter("callback");
			if (StringUtils.isEmpty(callback))
				callback = "addUser";
			
			for (int notifNum = 0 ; notifNum < users.length; notifNum++) {
				User user = users[notifNum];
				xml.append(callback+"("+xstream.toXML(user));
				xml.append(");\n");
			}
			xml.append ("\n");
			BrokerFactory.getLoggingBroker().logDebug("xml="+xml.toString());
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/plain")) {
			StringBuffer text = new StringBuffer();
			for (int notifNum = 0 ; notifNum < users.length; notifNum++) {
				User user = users[notifNum];
				text.append (user.toString());
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
		throw new NotificationException(NotificationException.UNAVAILABLE, "Can not delete lists of users");
	}
}
