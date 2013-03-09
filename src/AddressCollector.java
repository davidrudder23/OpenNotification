import java.util.Properties;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;



/*
 * Created on May 17, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddressCollector {

	public static void main(String[] args) throws Exception {
		Vector knownAddresses = new Vector();
		Vector unknownAddresses = new Vector();
		
		Properties props = new Properties();
//		 fill props with any information
		Session session = Session.getDefaultInstance(props, null);
		
		Store store = session.getStore("imap");
		store.connect("10.10.10.1", "drig", "5isthe#");
		
		Folder folder = store.getFolder("nagios");
		folder.open(Folder.READ_WRITE);
		Message messages[] = folder.getMessages();
		
		for (int i = 0; i < messages.length; i++) {
			Message message = messages[i];
			if (message.isSet(Flags.Flag.SEEN) && false) {
				String address = ((InternetAddress)message.getFrom()[0]).getAddress();
				if (!knownAddresses.contains(address)) {
					knownAddresses.addElement(address);
					//System.out.println ("We've seen address: "+address);
				}
			}
		}

		for (int i = 0; i < messages.length; i++) {
			Message message = messages[i];
			if (!message.isSet(Flags.Flag.SEEN)) {
				String address = ((InternetAddress)message.getFrom()[0]).getAddress();
				if ((!knownAddresses.contains(address)) && ((!unknownAddresses.contains(address))) &&
						!address.endsWith("@gmail.com") &&
						!address.endsWith("@yahoo.com") &&
						!address.endsWith("@hotmail.com")){
					unknownAddresses.addElement(address);
					InternetAddress inetAddress = (InternetAddress) message.getFrom()[0]; 
					System.out.println ("Location:");
					System.out.println ("Contact: "+inetAddress.getPersonal());
					System.out.println ("Email: "+inetAddress.getAddress());
					System.out.println ();
				}
			}
		}

	}
}
