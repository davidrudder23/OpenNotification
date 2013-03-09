package net.reliableresponse.notification.asterisk;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

public class OutboundAGI extends BaseAgiScript {
	private char playText (String text) throws AgiException {
		String soundsDir = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.sounds.directory",
				"/var/lib/asterisks/sounds")+"/tts/";
		String text2wavePath = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.text2wave", "/usr/bin/text2wave");
		String waveName = "RRN_TTS_"+System.currentTimeMillis();
		exec("System", "echo '"+text+"'|"+text2wavePath+" -F 8000 -o "+soundsDir+waveName+".wav");
		char digit = streamFile("tts/"+waveName, "0123456789*#");
		exec("System", "rm "+soundsDir+waveName+".wav");
		return digit;
	}
	
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		String extension = request.getExtension();
		String extStart = BrokerFactory.getConfigurationBroker().getStringValue("asterisk.outgoing.extension", "999");
		BrokerFactory.getLoggingBroker().logDebug("OutboundAGI started on extension "+request.getExtension());
		
		if (!extension.startsWith(extStart)) {
			throw new ExtensionException("Bad extension");
		}
		extension = extension.substring(extStart.length(), extension.length());
		if (extension.length() != 14) {
			throw new ExtensionException("Bad extension");
		}
		
		String notificationUuid = extension.substring(0, 7);
		String deviceUuid = extension.substring(7, 14);
		BrokerFactory.getLoggingBroker().logDebug("notif="+notificationUuid+", device="+deviceUuid);
		
		Notification notification = BrokerFactory.getNotificationBroker().getNotificationByUuid(notificationUuid);
		Device device = BrokerFactory.getDeviceBroker().getDeviceByUuid(deviceUuid);
		
		if (notification == null) {
			throw new ExtensionException("Extension did not resolve to a valid notification");
		}

		if (device == null) {
			throw new ExtensionException("Extension did not resolve to a valid device");
		}

		String message =
			"You have a new notification from "+notification.getSender()+"  The subject is "+
			notification.getSubject()+".  The message is "+notification.getMessages()[0].getMessage()+".  ";
		String[] responses;
		if (notification.isPersistent()) {
			responses = notification.getSender()
					.getAvailableResponses(notification);
			for (int i = 0; i < responses.length; i++) {
				message += "Please press " + (i+1) + " to " + responses[i]
						+ ".  ";
			}
		} else {
			responses = new String[0];
		}
		
		char digit = '0';
		while (digit != '*') {
			digit = playText(message);
			int digitNum = ((int)digit - (int)'0') - 1;
			if ((digitNum>= 0) && (digitNum< responses.length)) {
				notification.getSender().handleResponse(notification, null, responses[digitNum], "Responded to with "+responses[digitNum]+" via the telephone");
				playText("Thank you for responding to this notification with "+responses[digitNum]);
				return;
			}
		}
	}

}
