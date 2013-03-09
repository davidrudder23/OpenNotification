package net.reliableresponse.notification.rest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

public class RestGroupsResource extends AbstractRestResource {

	
	public String getRepresentation(String contentType, String method, HttpServletRequest req) throws NotificationException {
		
		Group[] groups= new Group[BrokerFactory.getGroupMgmtBroker().getNumGroups()]; 
		BrokerFactory.getGroupMgmtBroker().getGroups(groups.length, 0, groups);


		if (contentType.equalsIgnoreCase("text/xml")) {
			XStream xstream = new XStream();
			StringBuffer xml = new StringBuffer();
			xml.append ("<groups>\n");
			for (int groupNum = 0 ; groupNum < groups.length; groupNum++) {
				Group group = groups[groupNum];
				xml.append(xstream.toXML(group));
			}
			xml.append ("</groups>");
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/json")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			StringBuffer xml = new StringBuffer();
			for (int groupNum = 0 ; groupNum < groups.length; groupNum++) {
				Group group = groups[groupNum];
				xml.append(xstream.toXML(group));
			}
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/javascript")) {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.alias("group", Group.class);
			StringBuffer xml = new StringBuffer();
			
			for (int groupNum = 0 ; groupNum < groups.length; groupNum++) {
				Group group = groups[groupNum];
				xml.append("addGroup("+xstream.toXML(group));
				xml.append(");\n");
			}
			xml.append ("\n");
			BrokerFactory.getLoggingBroker().logDebug("xml="+xml.toString());
			return xml.toString();
		} else if (contentType.equalsIgnoreCase("text/plain")) {
			StringBuffer text = new StringBuffer();
			for (int groupNum = 0 ; groupNum < groups.length; groupNum++) {
				Group group = groups[groupNum];
				text.append (group.toString());
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
		throw new NotificationException(500, "Update not supported");
		
	}

	public void doDelete(String contentType, String method,
			ServletRequest request) throws NotificationException {
		throw new NotificationException(500, "Update not supported");
		
	}

	public boolean isValidResource() {
		return true;
	}
	
	public void delete() throws NotificationException {
		throw new NotificationException(NotificationException.UNAVAILABLE, "Can not delete lists of groups");
	}
}
