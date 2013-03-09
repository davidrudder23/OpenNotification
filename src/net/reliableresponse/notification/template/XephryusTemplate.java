/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.template;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import xephyrus.jst.JstParser;
import xephyrus.jst.PolyProperties;

import net.reliableresponse.notification.broker.BrokerFactory;

public class XephryusTemplate extends AbstractTemplate {
	
	String templateContents;
	
	public XephryusTemplate() {
		
	}
	
	public void init (String memberTypeClassname, String senderClassname) {
		String filename = BrokerFactory.getConfigurationBroker().getStringValue("xephyrus.directory",
				BrokerFactory.getConfigurationBroker().getStringValue("tomcat.location")+"/webapps/notification/templates");
		filename += "/"+memberTypeClassname+"/"+senderClassname;
		

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			StringBuffer contents = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null) {
				contents.append (line);
				contents.append ("\n");
			}
			templateContents = contents.toString();
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logWarn("Could not load Xephyrus template for member type: "+memberTypeClassname+" and sender type: "+senderClassname+".\n"+e);
			templateContents = getDefaultTemplateContents();
		}

	}
	
	public String getDefaultTemplateContents() {
		// Todo: make a decent template content
		return "";
	}

	@Override
	public String getTemplateContents() {
		return templateContents;
	}

	public String processTemplate(Hashtable<String, String> input) {
		PolyProperties props = new PolyProperties();
		Enumeration<String> keys = input.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = input.get(key);
			props.addProperty(key, value);
		}
		JstParser parser = new JstParser(props);
		return parser.parseTokens(getTemplateContents());
	}

	public String processTemplate(String message) {
		// TODO Auto-generated method stub
		return null;
	}

}
