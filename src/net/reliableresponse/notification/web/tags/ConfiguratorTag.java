/*
 * Created on Feb 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.tags;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ConfiguratorTag extends TagSupport {
	
	public ConfiguratorTag() {
		BrokerFactory.getLoggingBroker().logDebug("Configurator Constructor");
	}
	
	public int doStartTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();

			BrokerFactory.getLoggingBroker().logDebug("Running configurator");
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			
			String configuratorName = request.getParameter("configurator");
			BrokerFactory.getLoggingBroker().logDebug("configurator="+configuratorName);
			if (configuratorName == null) configuratorName = "logging";
			
			out.write ("<tr><td><img src=\"images/spacer.gif\" width=\"18\"></td>\n");
			out.write ("<td><table>\n");
			
			String[] errors = request.getParameterValues("configuration.errors");
			if ((errors != null) && (errors.length > 0)) {
				BrokerFactory.getLoggingBroker().logDebug("We have "+errors.length+" config errors");
				for (int i = 0; i < errors.length; i++) {
					BrokerFactory.getLoggingBroker().logDebug("error["+i+"]="+errors[i]);							
					out.write ("<tr><td colspan=\"25\"><font color=\"#FF0000\">"+errors[i]+"</font></td></tr>\n");
				}
			}
			try {
				pageContext.include(configuratorName+".jsp");
			} catch (RuntimeException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			out.write ("</table></td>\n");
			out.write ("<td>&nbsp</td><td align=\"center\"><input type=\"image\" name=\"action_configuration\" src=\"images/btn_save.gif\"></td></tr>\n");
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return super.doStartTag();
	}
}
