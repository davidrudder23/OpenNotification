/*
 * Created on Sep 26, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import java.io.InputStream;
import java.util.Map;

import net.sf.jasperreports.engine.JasperReport;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ReportBroker {

	public String[] getReportNames();
	
	public String getReportDescription(String name);
	
	public Map getParameterTypes (String name);
		
	public InputStream getReportStream(String name);
	
	public JasperReport getReport (String name);
}
