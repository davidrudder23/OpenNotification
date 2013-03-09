package net.reliableresponse.notification.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.license.LicenseFile;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.broker.BrokerFactory;

public class LicenseServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String licenseString = request.getParameter("license");
		BrokerFactory.getLoggingBroker().logDebug("licenseString="+licenseString);
		if (StringUtils.isEmpty(licenseString)) {
			response.sendRedirect("license.jsp");
			return;
		}
		try {
			LicenseFile.getInstance().save(licenseString);
		} catch (Exception anyExc) {
			request.getSession().setAttribute("errorMessage", anyExc.getMessage());
		}
		
		response.sendRedirect("index.jsp");
	}
	
	

}
