import java.io.FileInputStream;
import java.util.Hashtable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.rest.converters.HashtableConverter;
import net.reliableresponse.notification.rest.converters.NotificationMessageConverter;
import net.reliableresponse.notification.usermgmt.User;

/*
 * Created on May 27, 2008
 *
 *Copyright Reliable Response, 2008
 */

public class SerializationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001");
		user.getDevices();
		user.getAllInformation();
		XStream xstream = new XStream();
		xstream.registerConverter(new HashtableConverter());
		String xml = xstream.toXML(user);
		System.out.println ("***XML***");
		System.out.println (xml);

		System.out.println ("***JSON***");
		xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.registerConverter(new HashtableConverter());
		xml = xstream.toXML(user);
		System.out.println (xml);
		
		user = (User)xstream.fromXML(xml);
		System.out.println ("user deserialized from JSON="+user);
		xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.registerConverter(new HashtableConverter());
		xml = xstream.toXML(user);
		System.out.println (xml);
		
		Hashtable hashtable = new Hashtable();
		hashtable.put("foo", "bar");
		hashtable.put("baz", "quux");
		xstream = new XStream();
		xstream.registerConverter(new HashtableConverter());
		xml = xstream.toXML(hashtable);
		System.out.println ("***hashtable***");
		System.out.println (xml);
		
	}

}
