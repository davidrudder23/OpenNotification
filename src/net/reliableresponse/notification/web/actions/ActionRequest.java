/*
 * Created on Oct 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ActionRequest extends HttpServletRequestWrapper {

	HashMap params;
	public ActionRequest (HttpServletRequest request) {
		super (request);
		params = new HashMap();
		
		Map existing = request.getParameterMap();
		Iterator keys = existing.keySet().iterator();
		while (keys.hasNext()) {
			String name = (String)keys.next();
			String[] value = (String[])existing.get(name);
			params.put (name, value);
		}			
	}
 	
	
	public String getParameter(String arg0) {
		String[] values = (String[])params.get(arg0);
		if ((values == null) || (values.length == 0))
			return null;
		
		return values[0]; 
	}
	
	public Map getParameterMap() {
		return params;
	}
	
	public Enumeration getParameterNames() {
		return new ActionEnumeration(params.keySet());
	}
	
	public String[] getParameterValues(String arg0) {
		return (String[])params.get(arg0);
	}
	
	public void addParameter (String name, String value) {
		String[] current = getParameterValues(name);
		if (current == null) {
			current = new String[1];
			current[0] = value;
			params.put (name, current);
		} else {
			String[] newValues = new String[current.length+1];
			System.arraycopy(current, 0, newValues, 0, current.length);
			newValues[current.length] = value;
			params.put (name, newValues);
		}
	}
	
	public void setParameter (String name, String value) {
		String[] param = new String[1];
		param[0] = value;
		params.put(name, param);
	}

	public void setParameter (String name, String[] value) {
		params.put(name, value);
	}
	
	public void removeParameter (String name) {
		params.remove(name);
	}
}

class ActionEnumeration implements Enumeration {
	Object[] names;
	int index;
	
	public ActionEnumeration (Set names) {
		this.names = names.toArray();
		index = 0;
	}
	
	public boolean hasMoreElements() {
		return (index < names.length);
	}
	public Object nextElement() {
		return names[index++];
	}
}
