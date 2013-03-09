/*
 * Created on Oct 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import net.reliableresponse.notification.broker.ActionBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.web.actions.Action;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PropertiesActionBroker implements ActionBroker {
	Properties properties;
	
	public PropertiesActionBroker() {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(BrokerFactory.getConfigurationBroker().getStringValue("tomcat.location")+"/webapps/notification/conf/actions.properties"));
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.ActionBroker#getActionsForPage(java.lang.String)
	 */
	public Action[] getActionsForPage(String page) {
		Vector actions = new Vector();
		String classes = (String)properties.get(page);
		if (classes != null) {
			StringTokenizer tokenizer = new StringTokenizer(classes, ",");

			while (tokenizer.hasMoreElements()) {
				try {
					String className = (String) tokenizer.nextElement();
					BrokerFactory.getLoggingBroker().logDebug("action for "+page+" = "+className);
					Action action = (Action) Class.forName(className)
							.newInstance();
					actions.addElement(action);
				} catch (InstantiationException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (IllegalAccessException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (ClassNotFoundException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		
		return (Action[])actions.toArray(new Action[0]);
	}

}
