package net.reliableresponse.notification.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.rest.AbstractRestResource;
import net.reliableresponse.notification.rest.RestResource;
import net.reliableresponse.notification.util.StringUtils;

public class RestServlet extends HttpServlet {

	public RestServlet() {

	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			RestResource restResource = AbstractRestResource.getInstance(req
					.getRequestURI());

			String method = req.getParameter("method");
			if (StringUtils.isEmpty(method)) {
				method = req.getMethod();
			}

			if (method.equalsIgnoreCase("get")) {
				restGET(req, resp, restResource);
			} else if (method.equalsIgnoreCase("post")) {
				restUpdate(req, resp, restResource);
			} else if (method.equalsIgnoreCase("put")) {
				restAdd(req, resp, restResource);
			} else if (method.equalsIgnoreCase("delete")) {
				restDELETE(req, resp, restResource);
			}
			
			String returnURL = req.getParameter("returnURL");
			if (!StringUtils.isEmpty(returnURL)) {
				resp.sendRedirect(returnURL);
			}

		} catch (NotificationException notifExc) {
			resp.setStatus(notifExc.getCode());
			resp.getOutputStream().write(notifExc.getMessage().getBytes());
		}
	}

	private void restGET(HttpServletRequest req, HttpServletResponse resp,
			RestResource restResource) throws ServletException, IOException,
			NotificationException {
		String accept = getAcceptedContentType(req);
		resp.setContentType(accept);
		if ((accept.equalsIgnoreCase("text/json")) || (accept.equalsIgnoreCase("text/javascript")) ) {
			resp.getOutputStream().write("/* Serialization by XStream http://xstream.codehaus.org */".getBytes());
		} else if (accept.equalsIgnoreCase("text/xml")) {
			resp.getOutputStream().write("<!-- Serialization by XStream http://xstream.codehaus.org -->".getBytes());
		}
		resp.getOutputStream().write(
				restResource.getRepresentation(accept, "GET", req).getBytes());
	}

	private void restUpdate(HttpServletRequest req, HttpServletResponse resp,
			RestResource restResource) throws ServletException, IOException,
			NotificationException {
		String accept = getAcceptedContentType(req);
		resp.setContentType(accept);
		restResource.doUpdate(accept, "UPDATE", req);
		resp.getOutputStream().write(
				restResource.getRepresentation(accept, "GET", req).getBytes());
	}

	private void restAdd(HttpServletRequest req, HttpServletResponse resp,
			RestResource restResource) throws ServletException, IOException,
			NotificationException {
		String accept = getAcceptedContentType(req);
		resp.setContentType(accept);
		restResource.doAdd(accept, "ADD", req);
		resp.getOutputStream().write(
				restResource.getRepresentation(accept, "GET", req).getBytes());
	}

	private void restDELETE(HttpServletRequest req, HttpServletResponse resp,
			RestResource restResource) throws ServletException, IOException,
			NotificationException {
		String accept = getAcceptedContentType(req);
		resp.setContentType(accept);
		restResource.doDelete(accept, "DELETE", req);
		resp.getOutputStream().write(
				restResource.getRepresentation(accept, "GET", req).getBytes());
	}

	private String getAcceptedContentType(HttpServletRequest req) {
		String accept = req.getParameter("accept");
		if (StringUtils.isEmpty(accept)) {
			String acceptFromBrowser = req.getHeader("accept").toLowerCase();
			if (acceptFromBrowser.indexOf("text/html") >= 0) {
				accept = "text/html";
			} else if (acceptFromBrowser.indexOf("text/xml") >= 0) {
				accept = "text/xml";
			} else if (acceptFromBrowser.indexOf("text/plain") >= 0) {
				accept = "text/plain";
			} else {
				accept = "text/xml";
			}
		}
		BrokerFactory.getLoggingBroker().logDebug("accept=" + accept);
		return accept;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
