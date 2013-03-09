/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PropertiesConfigurationBroker implements ConfigurationBroker {

	long lastLoaded;
	
	Properties props;
	public PropertiesConfigurationBroker() {
		lastLoaded = 0;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.ConfigurationBroker#getStringValue(java.lang.String)
	 */
	public String getStringValue(String key) {
		return props.getProperty(key);
	}
	
	public String getStringValue(String key, String defaultValue) {
		String value = getStringValue(key);
		if (value == null) return defaultValue;
		return value;
	}

	public String[] getStringValues(String key) {
        Vector values = new Vector();
        String allValues = props.getProperty(key);
        if (allValues != null) {
        	StringTokenizer tok = new StringTokenizer(allValues, ",");
        	while (tok.hasMoreElements()) {
        		values.addElement(tok.nextElement());
        	}
        }
        return (String[])values.toArray(new String[0]);
    }
	
	public String[] getStringValues(String key, String[] defaultValue) {
		String[] values = getStringValues(key);
		if ((values == null) || (values.length ==0)) {
			return defaultValue;
		}
		return values;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.ConfigurationBroker#getIntValue(java.lang.String)
	 */
	public int getIntValue(String key) {
		return getIntValue(key, -1);
	}

	public int getIntValue(String key, int defaultValue) {
		try {
			String value = props.getProperty(key);
			if (value == null) return defaultValue;
			return Integer.parseInt (value);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.ConfigurationBroker#getBooleanValue(java.lang.String)
	 */
	public boolean getBooleanValue(String key, boolean defaultValue) {
		String value = getStringValue(key);
		if (value == null) return defaultValue;
		
		if (value.toLowerCase().equals ("true")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean getBooleanValue (String key) {
		return getBooleanValue(key, false);
	}
	
	public void setBooleanValue(String key, boolean value) {
		props.setProperty(key, value?"true":"false");
		save();
	}
	public void setIntValue(String key, int value) {
		props.setProperty(key, ""+value);
		save();
	}
	public void setStringValue(String key, String value) {
		if (value == null) {
			props.setProperty(key, "");
		} else {
			props.setProperty(key, value);
		}
		save();
	}

	public void setStringValues(String key, String[] values) {
		if (values == null) return;
		StringBuffer value = new StringBuffer();
		if (values.length > 0) {
			value.append (values[0]);
		}
		
		for (int i = 1; i < values.length; i++) {
			value.append(",");
			value.append (values[i]);
		}
		props.setProperty(key, value.toString());
		save();
	}
	
	private void save() {
		Vector lines = new Vector();
		
		// Read in the existing file
		try {
			BufferedReader lineIn = new BufferedReader(new FileReader(getStringValue("tomcat.location")+"/webapps/notification/conf/reliable.properties"));
			String line = null;
			while ((line = lineIn.readLine()) != null) {
				lines.addElement(line);
			}
		} catch (FileNotFoundException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		} catch (IOException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		
		Properties tempProps = new Properties(props);
		
		
		try {
			props.store(new FileOutputStream (getStringValue("tomcat.location")+"/webapps/notification/conf/reliable.properties"), "Reliable Response Properties");
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public void addTemporaryBooleanValue(String key, boolean value) {
		props.setProperty(key, value?"true":"false");
	}
	
	public void addTemporaryStringValue(String key, String value) {
		props.setProperty(key, value);
	}
	
	public String[] getParameterNames() {
		Set keys = props.keySet();
		return ((String[])keys.toArray(new String[0]));
	}
	
	public String[] getParameterNames(String substring) {
		Vector foundKeys = new Vector();
		Enumeration keys = props.keys();
		while (keys.hasMoreElements()) {
			String keyName = (String)keys.nextElement();
			if (keyName.toLowerCase().indexOf(substring.toLowerCase()) >= 0) {
				foundKeys.addElement(keyName);
			}
		}
		
		return (String[])foundKeys.toArray(new String[0]);
	}
	
	public long getLastLoaded() {
		return lastLoaded;
	}
	
	public void setConfiguration(InputStream in) {
		lastLoaded = System.currentTimeMillis();
		if (props == null) props = new Properties();
		
		try {			
			props.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
