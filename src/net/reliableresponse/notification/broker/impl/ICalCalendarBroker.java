/*
 * Created on Sep 15, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.broker.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CalendarBroker;
import net.reliableresponse.notification.usermgmt.User;

public class ICalCalendarBroker implements CalendarBroker {

	public boolean isCalendaringEnabled() {
		return true;
	}

	public boolean isInMeeting(User user) {
		String freebusyURL = user.getInformation("freebusyURL");
		BrokerFactory.getLoggingBroker().logDebug("Reading "+user+"'s freebusy info at "+freebusyURL);
		if (freebusyURL == null) {
			return false;
		}
		
		try {
			Date now = new Date();
			URL url = new URL (freebusyURL);
			Object contentObj = url.getContent();
			BufferedReader in = null;
			if (contentObj instanceof String) {
				in = new BufferedReader(new StringReader((String)contentObj));
			} else if (contentObj instanceof InputStream) {
				in = new BufferedReader
					(new InputStreamReader((InputStream)contentObj));
			}
			
			String line;
			while ((line = in.readLine()) != null) {
				BrokerFactory.getLoggingBroker().logDebug(user+"'s freebusy: "+line);
				if (line.startsWith("FREEBUSY")) {
					String value = line.substring (line.indexOf("=")+1, line.length());
					value = value.substring (value.indexOf(":")+1, value.length());
					Date start = getDate(value.substring (0, value.indexOf("/")));
					Date end = getDate(value.substring (value.indexOf("/")+1, value.length()));

					if ( (start != null) && (end != null)) {
						if ((now.after(start)) && (now.before(end))) {
							BrokerFactory.getLoggingBroker().logDebug(user+" is busy: "+line);
							return true;
						}
						if ( (start.equals(end)) && 
							(now.getYear() == start.getYear()) &&
							(now.getMonth() == start.getMonth()) &&
							(now.getDate() == start.getDate()) ) {
							BrokerFactory.getLoggingBroker().logDebug(user+" is busy all day: "+line);
							return true;
						}
					}
				}
				
			}
		} catch (MalformedURLException e) {
			// TODO:  I'd really like to find a way to bubble the error
			// TODO:  up to the user
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ParseException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		return false;
	}

	public boolean isOutOfOffice(User user) {
		return false;
	}

	public boolean isFree(User user) {
		return !isInMeeting(user);
	}
	
	private Date getDate (String timeString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("GMT-0:00"));
		return format.parse(timeString);
	}

}
