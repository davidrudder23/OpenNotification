package net.reliableresponse.notification.rest.converters;

import java.util.Enumeration;
import java.util.Hashtable;

import net.reliableresponse.notification.NotificationMessage;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class HashtableConverter extends Object implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		Hashtable hashtable = (Hashtable)obj;
		
		Enumeration keys = hashtable.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key != null) {
				String value = (String)hashtable.get(key);
				if (value == null) value = "";
				writer.startNode(key);
				writer.setValue(value);
				writer.endNode();
			}
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {
		Hashtable hashtable = new Hashtable();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			hashtable.put(reader.getNodeName(), reader.getValue());
			reader.moveUp();
		}
		return hashtable;
	}

	public boolean canConvert(Class clazz) {
		return clazz.equals(Hashtable.class);
	}

}
