package net.reliableresponse.notification.rest.converters;

import java.util.Date;

import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class NotificationMessageConverter extends Object implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		NotificationMessage message = (NotificationMessage)obj;
        writer.startNode("message");
        String contentType = message.getContentType();
        BrokerFactory.getLoggingBroker().logDebug("In NotificationMessageConverter, content-type="+contentType);
        if (contentType == null) contentType = "application/octet-stream";
        if (contentType.toLowerCase().startsWith("text/")) {
        	writer.setValue(message.getMessage());
        } else {
        	writer.setValue("");// TODO: should be a link
        }
        writer.endNode();
        writer.startNode("addedon");
        Date addedon = message.getAddedon();
        BrokerFactory.getLoggingBroker().logDebug("In NotificationMessageConverter, addedon="+addedon);
        if (addedon == null) addedon = new Date(0);
        writer.setValue(addedon.toString());
        writer.endNode();
        writer.startNode("addedby");
        String addedby = message.getAddedby();
        BrokerFactory.getLoggingBroker().logDebug("In NotificationMessageConverter, addedby="+addedby);
        if (addedby == null) addedby = "";
        writer.setValue(addedby);
        writer.endNode();
        writer.startNode("content-type");
        writer.setValue(contentType);
        writer.endNode();

	}

	public Object unmarshal(HierarchicalStreamReader arg0,
			UnmarshallingContext arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canConvert(Class arg0) {
		return arg0.equals(NotificationMessage.class);
	}

}
