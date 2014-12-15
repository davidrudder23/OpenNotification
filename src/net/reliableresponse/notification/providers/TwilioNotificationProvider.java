/*
 * Created on Apr 20, 2009
 *
 *Copyright Reliable Response, 2009
 */
package net.reliableresponse.notification.providers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioRestResponse;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.verbs.Dial;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.TwilioDevice;
import net.reliableresponse.notification.util.IPUtil;

public class TwilioNotificationProvider extends AbstractNotificationProvider {
	public static final String APIVERSION = "2010-04-01";

	public static TwilioNotificationProvider instance = null;

	public TwilioNotificationProvider() {

	}

	public static TwilioNotificationProvider getInstance() {
		if (instance == null) {
			instance = new TwilioNotificationProvider();
		}

		return instance;
	}

	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		return "Twilio";
	}

	public Hashtable<String, String> getParameters(Notification notification, Device device) {
		return new Hashtable<String, String>();
	}

	public String[] getResponses(Notification notification) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(Hashtable<String, String> params) throws NotificationException {
	}


	public Hashtable<String, String> sendNotification(Notification notification, Device device) throws NotificationException {
		try {
			if (!(device instanceof TwilioDevice)) {
				throw new NotificationException(NotificationException.NOT_ACCEPTABLE, "Supplied device is not a Twilio device");
			}

			String message = "You have a new notification from " + notification.getSender() + ".  The subject is " + notification.getSubject()
					+ ".  The message is " + notification.getMessages()[0].getMessage() + ".";
			//String url = "http://twimlets.com/message?Message%5B0%5D=" + URLEncoder.encode(message);
			String url = IPUtil.getExternalBaseURL()+"/TwilioServlet/twiml/"+notification.getUuid();

			TwilioDevice twilio = (TwilioDevice) device;

			String accountSid = BrokerFactory.getConfigurationBroker().getStringValue("twilio.sid");
			String authToken = BrokerFactory.getConfigurationBroker().getStringValue("twilio.token");
			
			BrokerFactory.getLoggingBroker().logDebug("Sid="+accountSid);
			BrokerFactory.getLoggingBroker().logDebug("token="+authToken);
			TwilioRestClient client = new TwilioRestClient(accountSid, authToken);
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("To", twilio.getPhoneNumber());
			params.put("From", BrokerFactory.getConfigurationBroker().getStringValue("twilio.phone.from", "(720)530-8877"));
			params.put("Url", url);

			TwilioRestResponse response;
			response = client.request("/" + APIVERSION + "/Accounts/" + client.getAccountSid() + "/Calls", "POST", params);
			if (response.isError())
				System.out.println("Error making outgoing call: " + response.getHttpStatus() + "\n" + response.getResponseText());
			else {
				BrokerFactory.getLoggingBroker().logDebug(response.getResponseText());
			}

			Hashtable<String, String> tracking = new Hashtable<String, String>();
			tracking.put("ID", "");
			return tracking;

		} catch (TwilioRestException e) {
			// TODO Auto-generated catch block
			throw new NotificationException(400, e.getErrorMessage());
		}
	}

}
