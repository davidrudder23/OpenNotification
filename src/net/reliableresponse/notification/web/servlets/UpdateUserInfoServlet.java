/*
 * Created on Aug 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.Vector;

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
public class UpdateUserInfoServlet extends HttpServlet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Vector responses = new Vector();

		String uuid = request.getParameter("uuid");
		BrokerFactory.getLoggingBroker().logDebug(
				"Updating user with uuid " + uuid);
		if ((uuid == null) || (uuid.length() == 0)) {
			request.getSession().setAttribute("Error",
					"You must specify a valid user to update");
			response.sendRedirect("index.jsp");
			return;
		}
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		if (user == null) {
			request.getSession().setAttribute("Error",
					"You must specify a valid user to update");
			response.sendRedirect("index.jsp");
			return;
		}

		String action = request.getParameter("action");
		if ((action == null) || (action.length() == 0)) {
			action = "updateUser";
		}

		if (action.equals("updateUser")) {
			BrokerFactory.getLoggingBroker().logDebug("Updating user " + user);

			String firstName = request.getParameter("firstName");
			BrokerFactory.getLoggingBroker().logDebug(
					"Updating user's first name to " + firstName);
			if ((firstName != null) && (firstName.length() > 0)
					&& (!firstName.equals(user.getFirstName()))) {
				user.setFirstName(firstName);
				responses.addElement("First name changed to " + firstName);
			}

			String lastName = request.getParameter("lastName");
			BrokerFactory.getLoggingBroker().logDebug(
					"Updating user's last name to " + lastName);
			if ((lastName != null) && (lastName.length() > 0)
					&& (!lastName.equals(user.getLastName()))) {
				user.setLastName(lastName);
				responses.addElement("Last name changed to " + lastName);
			}

			String department = request.getParameter("department");
			BrokerFactory.getLoggingBroker().logDebug(
					"Updating user's department to " + department);
			if ((department != null) && (department.length() > 0)
					&& (!department.equals(user.getDepartment()))) {
				user.setDepartment(department);
				responses.addElement("Department changed to " + department);
			}
		} else if (action.equals("deleteUser")) {
			try {
				BrokerFactory.getUserMgmtBroker().deleteUser(user);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		request.getSession().setAttribute("responses", responses);
		response.sendRedirect("index.jsp?notification=usermgmt");
	}
}