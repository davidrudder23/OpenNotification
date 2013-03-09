/*
 * Created on Apr 6, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.web.util.JSPHelper;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ReportAction implements Action {
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Report Action running");
		
		Connection connection = BrokerFactory.getDatabaseBroker().getConnection();

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));
		String[] pdfReports = JSPHelper.getParameterEndings(request, "action_report_pdf_");
		String[] htmlReports = JSPHelper.getParameterEndings(request, "action_report_html_");
		String[] excelReports = JSPHelper.getParameterEndings(request, "action_report_excel_");
		
		if ((pdfReports != null) && (pdfReports.length > 0)) {
			BrokerFactory.getLoggingBroker().logDebug("Printing report to PDF");
			JasperPrint report = getReport(request, pdfReports[0], connection);
			if (report != null) { 
				response.setContentType("application/pdf");
				try {
					OutputStream out = response.getOutputStream();
					JasperExportManager.exportReportToPdfStream(report, out);
				} catch (IOException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (JRException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}		
		} else if ((excelReports != null) && (excelReports.length > 0)) {
				BrokerFactory.getLoggingBroker().logDebug("Printing report to Excel");
				JasperPrint report = getReport(request, excelReports[0], connection);
				if (report != null) { 
					response.setContentType("application/vnd.ms-excel");
					try {
						OutputStream out = response.getOutputStream();
						JRExporter exporter = new JRXlsExporter();
						exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
						exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
						exporter.exportReport();
						out.flush();
						out.close();
					} catch (IOException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} catch (JRException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
		} else if ((htmlReports != null) && (htmlReports.length > 0)) {
			BrokerFactory.getLoggingBroker().logDebug("Printing report to HTML");
			JasperPrint report = getReport(request, htmlReports[0], connection);
			if (report != null) { 
				response.setContentType("text/html");
				try {
					OutputStream out = response.getOutputStream();
					HashMap imagesMap = new HashMap();
					actionRequest.getSession().setAttribute("IMAGES_MAP", imagesMap);
					JRHtmlExporter exporter = new JRHtmlExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.TRUE); 
					exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER, "");
					exporter.setParameter(JRHtmlExporterParameter.HTML_FOOTER, "");
					exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "images.jsp?image=");
					exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, imagesMap);
					exporter.exportReport();
				} catch (IOException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} catch (JRException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return actionRequest;
	}

	/**
	 * @param request
	 */
	private JasperPrint getReport(ServletRequest request, String report_name, Connection connection) {
		JasperReport report = BrokerFactory.getReportBroker().getReport(report_name);
		
		if (report != null) {
			JRParameter[] reportParams = report.getParameters();
			HashMap filledParams = new HashMap();
			for (int p = 0; p < reportParams.length; p++) {
				if ((reportParams[p].isForPrompting())
						&& (!reportParams[p].isSystemDefined())) {
					BrokerFactory.getLoggingBroker().logDebug("Looking up report param "+reportParams[p].getName()+".");
					if (reportParams[p].getValueClass() == Date.class) {
						int month = 0;
						int day = 0;
						int year = 0;
						
						try {
							month = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_month"));
							day = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_day"));
							year = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_year"))-1900;
							
							filledParams.put (reportParams[p].getName(), new Date(year, month, day));
						} catch (NumberFormatException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
					} else if (reportParams[p].getValueClass() == Timestamp.class) {
							int month = 0;
							int day = 0;
							int year = 0;
							
							try {
								month = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_month"));
								day = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_day"));
								year = Integer.parseInt(request.getParameter(reportParams[p].getName()+"_year"))-1900;
								
								Timestamp timestamp = new Timestamp(new Date(year, month, day).getTime());
								if (reportParams[p].getName().toLowerCase().startsWith("to")) {
									timestamp.setHours(23);
									timestamp.setMinutes(59);
									timestamp.setSeconds(59);
								}
								BrokerFactory.getLoggingBroker().logDebug("timestamp for param "+reportParams[p].getName()+"= "+timestamp);
								filledParams.put (reportParams[p].getName(), timestamp);
							} catch (NumberFormatException e1) {
								BrokerFactory.getLoggingBroker().logError(e1);
							}
					} else {
						filledParams.put (reportParams[p].getName(), request.getParameter(reportParams[p].getName()));
					}
				}
			}
			try {
				JasperPrint print = JasperFillManager.fillReport(report,
						filledParams, connection);
				String name = print.getName();
				if (name.length()>31) {
					name = name.substring(0, 30);
					print.setName(name);
				}
				return print;
			} catch (JRException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		return null;
	}
}
