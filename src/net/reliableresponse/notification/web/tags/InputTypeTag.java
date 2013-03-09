/*
 * Created on Sep 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.tags;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class InputTypeTag extends TagSupport {

	String name;
	Object value;
	Vector options;
	
	public int doStartTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			if (value instanceof String) {
				if ((options != null) && (options.size() > 0)) {
					out.print ("<SELECT name=\"");
					out.print (name);
					out.println ("\">");
				for (int o = 0; o < options.size(); o++) {
						String selected = "";
						String option = (String)options.elementAt(o);
						if (option.toLowerCase().equals (((String)value).toLowerCase())) selected=" SELECTED";
						out.print ("<OPTION value=\"");
						out.print (option+"\" "+selected);
						out.print (">");
						out.println (option);
					}
					out.println ("</SELECT>");
				} else {
					out.print ("<input type=text name=\"");
					out.print (name+"\" value=\""+value+"\">");
				}
			} else {
				out.print ("<input type=text name=\"");
				out.print (name+"\" value=\""+value+"\">");
			}
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return super.doStartTag();
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	public void setOptions(Vector options) {
		this.options = options;
	}
}
