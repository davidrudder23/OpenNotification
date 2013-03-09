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
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AddUserServlet extends HttpServlet {
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
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String email = request.getParameter("email");
		String department = request.getParameter("department");
		String phone = request.getParameter("phone");
		
		if ((email == null) | (email.length()<1)) {
			request.getSession().setAttribute("Error", "Please enter an email address");
			response.sendRedirect("index.jsp?notification=addUser&email="+email+"&firstName="+firstName+
					"&lastName="+lastName+"&department="+department+"&phone="+phone);
			return;
		}
		
		User user = new User();
		user.addEmailAddress(email);
		user.setPhoneNumber(phone);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setDepartment(department);
		
		BrokerFactory.getLoggingBroker().logInfo("Adding user "+user);
		
		try {
			BrokerFactory.getUserMgmtBroker().addUser(user);
			request.getSession().setAttribute("Info", user+" added");
			response.sendRedirect("index.jsp?notification=addUser");
			return;
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			request.getSession().setAttribute("Error", e.getMessage());
			response.sendRedirect("index.jsp?notification=addUser&email="+email+"&firstName="+firstName+
					"&lastName="+lastName+"&department="+department+"&phone="+phone);
			return;
		}
	}
}
