/*
 * Created on Oct 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.web.actions.Action;
import net.reliableresponse.notification.web.actions.ActionRequest;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ActionServlet extends HttpServlet {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost (request, response);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map params = request.getParameterMap();
		
		Map existing = request.getParameterMap();
		Iterator keys = existing.keySet().iterator();
		while (keys.hasNext()) {
			String name = (String)keys.next();
			String[] value = (String[])existing.get(name);
			if (value != null) {
				for (int i = 0; i < value.length; i++) {
					BrokerFactory.getLoggingBroker().logDebug("HTTP Parameter: "+name+"["+i+"]="+value[i]);
				}
			}
		}			
		
		String page = request.getParameter("page");
		if (page == null) {
			page = "/index.jsp";
		}

		Action[] actions = BrokerFactory.getActionBroker().getActionsForPage(page);
		ServletRequest actionRequest = new ActionRequest(request);

		if (request.getParameter("action_mispress.x")!= null) {
			String realAction = request.getParameter("action");
			BrokerFactory.getLoggingBroker().logDebug("Got a mispress, real action="+realAction);
			((ActionRequest)actionRequest).setParameter(realAction+".x", "true");
		}
		

		for (int a = 0; a < actions.length; a++) {
			actionRequest = actions[a].doAction(actionRequest, response);
			if (actionRequest == null) return;
		}

		// Check to see if the actions changed the page
		page = actionRequest.getParameter("page");
		if (page == null) {
			page = "/index.jsp";
		}
	
		Boolean forward = (Boolean)actionRequest.getAttribute("forward_request");
		BrokerFactory.getLoggingBroker().logDebug("forward="+forward);
		if ((forward==null) || (forward.booleanValue())) {
			request.getRequestDispatcher(page).forward(actionRequest, response);
		}
	}
}
