package net.reliableresponse.notification.util;

import java.io.FileInputStream;
import java.io.IOException;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class IPUtil {
	
	private static String externalIP = null;
	private static String externalURL = null;

	public static String getExternalIPAddress() {
		if (!StringUtils.isEmpty(externalIP)) return externalIP;
		
		externalIP = BrokerFactory.getConfigurationBroker().getStringValue("external.ip", k->{
			HttpClient client = new HttpClient();
			GetMethod get = new GetMethod("http://icanhazip.com/");
			try {
				client.executeMethod(get);
				return get.getResponseBodyAsString().trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		});
		return externalIP;
	}
	
	public static String getExternalBaseURL() {
		externalURL = BrokerFactory.getConfigurationBroker().getStringValue("external.url", 
				k->"http://"+getExternalIPAddress()+":8080"+BrokerFactory.getConfigurationBroker().getStringValue("contextPath","/notification"));
		return externalURL;
	}
	
	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(new FileInputStream("conf/reliable.properties"));
		System.out.println ("My URL is "+IPUtil.getExternalBaseURL());
	}
}
