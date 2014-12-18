package net.reliableresponse.notification.providers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.TwilioSMSDevice;
import net.reliableresponse.notification.util.IPUtil;

public class TwilioSMSNotificationProvider extends AbstractNotificationProvider {

	public boolean cancelPage(Notification notification) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		return "Twilio SMS";
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


	public Hashtable sendNotification(Notification notification, Device device) {
	    try {
			String accountSid = BrokerFactory.getConfigurationBroker().getStringValue("twilio.sid");
			String authToken = BrokerFactory.getConfigurationBroker().getStringValue("twilio.token");			
			BrokerFactory.getLoggingBroker().logDebug("Sid="+accountSid);
			BrokerFactory.getLoggingBroker().logDebug("token="+authToken);
			
			TwilioSMSDevice twilioDevice = (TwilioSMSDevice) device;

			
			String message = "Notification from " + notification.getSender() + ".  Subjectg: " + notification.getSubject()
					+ ".  Message: " + notification.getMessages()[0].getMessage() + ".";
			
			message += "\n";
			message += "Reply with: ";
			String[] responses = notification.getSender().getAvailableResponses(
					notification);
			message+="\n";
			for (int i = 0; i < responses.length; i++) {
				message += " - "+IPUtil.getExternalBaseURL()+
						BrokerFactory.getConfigurationBroker().getStringValue("contextPath","")+
						"/ResponseServlet/respond/"+notification.getUuid()+"/"
						+ URLEncoder.encode(responses[i]);
				message+="\n";
			}
	
		    TwilioRestClient client = new TwilioRestClient(accountSid, authToken);
		    
		    // Build a filter for the MessageList
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("Body", message));
		    params.add(new BasicNameValuePair("To", twilioDevice.getNormalizedPhoneNumber()));
		    params.add(new BasicNameValuePair("From", BrokerFactory.getConfigurationBroker().getStringValue("twilio.phone.from", "+17205481657")));
		     
		     
		    MessageFactory messageFactory = client.getAccount().getMessageFactory();
			Message twilioMessage = messageFactory.create(params);
			
			BrokerFactory.getLoggingBroker().logDebug("Twilio SMS message="+twilioMessage.toString());
		} catch (TwilioRestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return getParameters(notification, device);
	}

}
