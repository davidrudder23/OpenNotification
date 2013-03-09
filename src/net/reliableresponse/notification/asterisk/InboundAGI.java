package net.reliableresponse.notification.asterisk;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.dialogic.DialogicMessage;
import net.reliableresponse.notification.dialogic.WelcomeDialogicMessage;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

public class InboundAGI extends BaseAgiScript {

	/**
	 * This replaces the super getData.  It returns on a '*', so we
	 * can do the whole "press * at any time to return to the main menu" 
	 * thing
	 */
	protected String getData(String file, int timeout, int maxdigits) throws AgiException {
		StringBuffer digits = new StringBuffer();
		char digit;
		
		digit = streamFile(file, "0123456789#*");
		if (digit == '*') {
			return "*";
		};
		digits.append (digit);
		
		while ((digits.length() < maxdigits) && 
				((digit = waitForDigit(timeout)) != '#') ) {
			BrokerFactory.getLoggingBroker().logDebug("Got digit "+digit);
			if (digit == '*') {
				return "*";
			}
			if (digit == 0) {
				return null;
			}
			digits.append(digit);
		}
		return digits.toString();
	}

	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		DialogicMessage dialogicMessage = new WelcomeDialogicMessage();
		String digits = "";
		char digit;
		
		while (true) {
			digits = "";
			if (dialogicMessage.getExpectedDigits() == 0) {
				// If we're not expecting digits, just play the wave
				digit = streamFile(dialogicMessage.getAsteriskFilename(), "0123456789*#");
				if (digit != 0) {
					digits = ""+digit;
				}
			} else {
				digits = getData(dialogicMessage.getAsteriskFilename(), 20000, dialogicMessage.getExpectedDigits());
			}
			BrokerFactory.getLoggingBroker().logDebug("DTMF="+digits);
			if ((digits == null) || (digits.toLowerCase().indexOf("timeout")>=0)) {
				return;
			} else if (digits.indexOf("*")>=0) {
				dialogicMessage = new WelcomeDialogicMessage();
			} else {
				dialogicMessage = dialogicMessage.getNextMessage(digits);
			}
		}
		
	}

}
