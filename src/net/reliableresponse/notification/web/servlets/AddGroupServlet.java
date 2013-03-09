/*
 * Created on Aug 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.OnCallGroup;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddGroupServlet extends HttpServlet {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BrokerFactory.getLoggingBroker().logDebug("Add User Servlet");
		String name = request.getParameter("name");
		String type = request.getParameter("type"); 
		
		if ((name == null) | (name.length()<1)) {
			request.getSession().setAttribute("Error", "Please enter an email address");
			response.sendRedirect("index.jsp?notification=addGroup&name="+name);
			return;
		}
		
		if (type == null) {
			type="notification";
		}
		type = type.toLowerCase();
		
		Group group = null;
		
		if (type.equals ("notification")) {
			group = new BroadcastGroup();
		} else if (type.equals ("escalation")) {
			group = new EscalationGroup();
		} else if (type.equals ("rollover")) {
			group = new OnCallGroup();
		}
		group.setGroupName(name);
		
		BrokerFactory.getLoggingBroker().logInfo("Adding group "+group);
		
		try {
			BrokerFactory.getGroupMgmtBroker().addGroup(group);
			request.getSession().setAttribute("Info", group+" added");
			response.sendRedirect("index.jsp?notification=addGroup");
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			request.getSession().setAttribute("Error", e.getMessage());
			response.sendRedirect("index.jsp?notification=addGroup&name="+name);
			return;
		}
	}
}
