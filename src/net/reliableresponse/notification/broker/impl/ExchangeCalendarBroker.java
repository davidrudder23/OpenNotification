/*
 * Created on Sep 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CalendarBroker;
import net.reliableresponse.notification.usermgmt.User;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Administrator
 *
 */
public class ExchangeCalendarBroker implements CalendarBroker {

	String hostname;

	int port;

	String username;

	String password;
	
	String domain;

	private static final int FREE = 0;

	private static final int TENTATIVE = 1;

	private static final int BUSY = 2;

	private static final int OUTOFOFFICE = 3;
	
	private boolean enabled;
	
	public ExchangeCalendarBroker() {
		
		enabled = BrokerFactory.getConfigurationBroker().getBooleanValue("calendar.exchange", false);
		
		if (enabled) {
			String hostname = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.hostname");
			int port = BrokerFactory.getConfigurationBroker().getIntValue("calendar.exchange.port");
			String username = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.username");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.password");
			String domain =  BrokerFactory.getConfigurationBroker().getStringValue("calendar.exchange.domain");
			
			if (hostname == null) {
				BrokerFactory.getLoggingBroker().logError("Exchange hostname is not set");
				enabled = false;
			}

			if (port == -1) {
				port = 80;
			}

			if (username == null) {
				BrokerFactory.getLoggingBroker().logError("Exchange username is not set");
				enabled = false;
			}

			if (password == null) {
				BrokerFactory.getLoggingBroker().logError("Exchange password is not set");
				enabled = false;
			}
			
			if (domain == null) {
				BrokerFactory.getLoggingBroker().logError("Exchange domain is not set");
				enabled = false;
			}


		
			BrokerFactory.getLoggingBroker().logDebug("Exchange is "+(enabled?"":"not ")+"enabled");
		
			if (enabled) {
				setServer(hostname,port);
				BrokerFactory.getLoggingBroker().logDebug("Setting Exchange username to "+username);
				setCredentials(username, password, domain);
			}
		}
	}


	public void setServer(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public void setCredentials(String username, String password, String domain) {
		this.username = username;
		this.password = password;
		this.domain = domain;
	}

	public boolean isInMeeting(User user) {
		int currentStatus = getCurrentStatus(user);
		return ((currentStatus == ExchangeCalendarBroker.BUSY) ||
				(currentStatus == ExchangeCalendarBroker.TENTATIVE));
	}
	
	public boolean isOutOfOffice(User user) {
		return (getCurrentStatus(user) == ExchangeCalendarBroker.OUTOFOFFICE);
	}
	
	public boolean isFree(User user) {
		return (getCurrentStatus(user) == ExchangeCalendarBroker.FREE);
	}

	private int getCurrentStatus(User user) {
		if (!enabled) {
			BrokerFactory.getLoggingBroker().logDebug("Exchange not enabled");
			return -1;
		}
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date now = new Date();
			int tzoff = (((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET)) / (36600000)));
			
			// TODO - Java seems to give the timezones negative
			// from their actual value.  This is a kludge to 
			// accomodate that. 
			tzoff = 0-tzoff;
			String tzoffString = ""+Math.abs(tzoff);
			if ((tzoff < 10) && (tzoff > -10)) {
				tzoffString = "0"+tzoffString;
			}
			if (tzoff >= 0) {
				tzoffString = "+"+tzoffString;
			} else {
				tzoffString = "-"+tzoffString;
			}
			tzoffString+=":00";

			Calendar then = Calendar.getInstance();
			
			then.set(Calendar.MINUTE, then.get(Calendar.MINUTE) + 5);
			
			// TODO: The email address here might wrong.  
			// We use the LDAP email address, which might not be 
			// available.
			String email = user.getInformation("LDAP-Email");
			if (email == null) email = user.getEmailAddress();
			String protocol = "http";
			if (port == 443) protocol = "https";
			String urlString = protocol+"://"+hostname+":"+port+"/public/?Cmd=freebusy&start="
					+ formatter.format(now) + tzoffString + "&end="
					+ formatter.format(then.getTime()) + tzoffString
					+ "&interval=10&u=SMTP:"+email;
			BrokerFactory.getLoggingBroker().logDebug("Exchange URL="+urlString);

			HttpClient httpclient = new HttpClient();
			
			// Exchange is so very Microsoft.  It will only work if you pretend to be Internet Explorer
			httpclient
					.getParams()
					.setParameter("http.useragent",
							"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.1.4322)");

			HttpURL url = null;
			if (protocol.equalsIgnoreCase("https")) {
				url = new HttpsURL(urlString);
			} else {
				url = new HttpURL(urlString);				
			}
			BrokerFactory.getLoggingBroker().logDebug("Logging into Exchange calendar with username "+username);
			NTCredentials creds = new NTCredentials(username, password,
					hostname, domain);
			httpclient.getState().setCredentials(AuthScope.ANY, creds);

			HttpMethod method = new GetMethod();
			method.setURI(url);
			method.addRequestHeader("Translate", "t");
			httpclient.executeMethod(method);

			// Now, we get the XML output and parse it for the return.
			String output = method.getResponseBodyAsString();
			BrokerFactory.getLoggingBroker().logDebug("Exchange XML="+output);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					output)));
			NodeList nodeList = document.getElementsByTagName("a:response");
			if (nodeList.getLength() > 0) {
				Element response = (Element) nodeList.item(0);
				nodeList = response.getElementsByTagName("a:recipients");
				if (nodeList.getLength() > 0) {
					Element recipients = (Element) nodeList.item(0);
					nodeList = recipients.getElementsByTagName("a:item");
					if (nodeList.getLength() > 0) {
						Element item = (Element) nodeList.item(0);
						nodeList = item.getElementsByTagName("a:fbdata");
						if (nodeList.getLength() > 0) {
							Node data = nodeList.item(0);
							String dataString = data.getChildNodes().item(0).getNodeValue();
							BrokerFactory.getLoggingBroker().logDebug("Returning "+dataString+" from Exchange Calendar Broker");
							return Integer.parseInt (dataString.substring (0,1));
						}
					}
				}
			}
		} catch (URIException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (HttpException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (DOMException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (FactoryConfigurationError e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ParserConfigurationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SAXException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		BrokerFactory.getLoggingBroker().logDebug("Returning -1 from Exchange Calendar Broker");
		return -1;
	}
	
	public boolean isCalendaringEnabled() {
		return enabled;
	}
}