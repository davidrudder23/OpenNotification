package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.util.Hashtable;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.asterisk.AgiServer;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.AsteriskDevice;

public class AsteriskVoIPNotificationProvider extends
		AbstractNotificationProvider {

	String context;
	String id;
	String extension;
	String managerPassword;
	String managerId;
	String asteriskHost;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#init(java.util.Hashtable)
	 */
	public void init(Hashtable params) throws NotificationException {
		asteriskHost = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.hostname", "localhost");
		context = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.context", "reliableresponse");
		id = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.id", "spa3000");
		extension = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.extension", "999");
		managerId = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.managerId", "reliable");
		managerPassword = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.managerPassword", "reliable");
		
		// Make sure we start the AgiServer
		AgiServer.getInstance();
	}
	
	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		// Do the outbound
		Hashtable params = new Hashtable();
		String phoneNumber = "";
		if (!(device instanceof AsteriskDevice)) {
			throw new NotificationException(NotificationException.INTERNAL_ERROR, "Provided device is not a telephone device");
		}
		phoneNumber = ((AsteriskDevice)device).getPhoneNumber();
		try {
			BrokerFactory.getLoggingBroker().logDebug("Logging into asterisk server at "+asteriskHost+" with "+managerId+":"+managerPassword);
			ManagerConnection con = new ManagerConnectionFactory(asteriskHost, 
					managerId, managerPassword).createManagerConnection();
			con.login();
			OriginateAction origAction = new OriginateAction();
			origAction.setChannel("SIP/"+phoneNumber+"@"+id);
			origAction.setContext(context);
			BrokerFactory.getLoggingBroker().logDebug("Calling "+"SIP/"+phoneNumber+"@"+id);
			BrokerFactory.getLoggingBroker().logDebug("Extension "+extension+notification.getUuid()+device.getUuid());
			BrokerFactory.getLoggingBroker().logDebug("Context "+context);
			origAction.setExten(extension+notification.getUuid()+device.getUuid());
			origAction.setPriority(new Integer(1));
			ManagerResponse origResponse = con.sendAction(origAction);
			if (origResponse.getResponse().toLowerCase().indexOf("error") >= 0) {
				BrokerFactory.getLoggingBroker().logDebug("response="+origResponse);
				throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Error dialing (probably busy).  Will try again.");
			}
			con.logoff();
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
			throw new NotificationException(NotificationException.INTERNAL_ERROR, e.getMessage());
		}
		return params;
	}

	public Hashtable getParameters(Notification notification, Device device) {
		return new Hashtable();
	}

	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	public boolean cancelPage(Notification notification) {
		return false;
	}

	public String getName() {
		return "Asterisk(c) Telephony Provider";
	}

}
