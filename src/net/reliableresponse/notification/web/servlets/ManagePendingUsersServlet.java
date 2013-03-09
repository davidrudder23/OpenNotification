/*
 * Created on Aug 12, 2004
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

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;


/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class ManagePendingUsersServlet extends HttpServlet {

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

		String action = request.getParameter("action");
		if (action == null)
			action = "nothing";
		BrokerFactory.getLoggingBroker().logDebug(
				"ManagePendingUsers action=" + action);
		if (action.equals("addUser")) {
			String uuid = request.getParameter("uuid");
			BrokerFactory.getLoggingBroker().logDebug(
					"ManagePendingUsers uuid=" + uuid);
			if (uuid != null) {
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						uuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"ManagePendingUsers user=" + user);
				if (user != null) {
					SortedVector pendingUsers = (SortedVector) request.getSession()
							.getAttribute("pendingUsers");
					if (pendingUsers == null) {
						pendingUsers = new SortedVector();
					}
					// Make sure the user isn't already added. If not, add it
					if (pendingUsers.lastIndexOf(user) >= 0) {
						BrokerFactory
								.getLoggingBroker()
								.logDebug(
										user
												+ " is already in pending queue.  Won't add.");
					} else {
						pendingUsers.addElement(user);
						request.getSession().setAttribute("pendingUsers",
								pendingUsers);
					}
				}
			}
		} else if (action.equals("addGroup")) {
			String uuid = request.getParameter("uuid");
			BrokerFactory.getLoggingBroker().logDebug(
					"ManagePendingUsers uuid=" + uuid);
			if (uuid != null) {
				Group group = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(uuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"ManagePendingUsers group=" + group);
				if (group != null) {
					SortedVector pendingGroups = (SortedVector) request.getSession()
							.getAttribute("pendingGroups");
					if (pendingGroups == null) {
						pendingGroups = new SortedVector();
					}
					// Make sure the user isn't already added. If not, add it
					if (pendingGroups.lastIndexOf(group) >= 0) {
						BrokerFactory
								.getLoggingBroker()
								.logDebug(
										group
												+ " is already in pending queue.  Won't add.");
					} else {
						pendingGroups.addElement(group);
						request.getSession().setAttribute("pendingGroups",
								pendingGroups);
					}
				}
			}
		} else if (action.equals("removeUser")) {
			String uuid = request.getParameter("uuid");
			BrokerFactory.getLoggingBroker().logDebug(
					"ManagePendingUsers uuid=" + uuid);
			if (uuid != null) {
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						uuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"ManagePendingUsers user=" + user);
				if (user != null) {
					SortedVector pendingUsers = (SortedVector) request.getSession()
							.getAttribute("pendingUsers");
					if (pendingUsers != null) {
						pendingUsers.removeElement(user);
						request.getSession().setAttribute("pendingUsers",
								pendingUsers);
					}
				}
			}
		} else if (action.equals("removeGroup")) {
			String uuid = request.getParameter("uuid");
			BrokerFactory.getLoggingBroker().logDebug(
					"ManagePendingUsers uuid=" + uuid);
			if (uuid != null) {
				Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
						uuid);
				BrokerFactory.getLoggingBroker().logDebug(
						"ManagePendingUsers group=" + group);
				if (group != null) {
					SortedVector pendingGroups = (SortedVector) request.getSession().getAttribute("pendingGroups");
					if (pendingGroups != null) {
						if (!pendingGroups.removeElement(group)) {
							BrokerFactory.getLoggingBroker().logWarn("Unable to remove group "+group+" from pending list");
						}
						request.getSession().setAttribute("pendingGroups",
								pendingGroups);
					}
				}
			}
		}

		String returnURL = "index.jsp";
		String userPageNum = request.getParameter("userPageNum");
		BrokerFactory.getLoggingBroker().logDebug("userPageNum=" + userPageNum);
		if (userPageNum != null) {
			returnURL += "?userPageNum=" + userPageNum;
		}

		String groupPageNum = request.getParameter("groupPageNum");
		BrokerFactory.getLoggingBroker().logDebug("groupPageNum=" + groupPageNum);
		if (groupPageNum != null) {
				if (userPageNum != null) {
					returnURL += "&groupPageNum=" + groupPageNum;
				} else {
				returnURL += "?groupPageNum=" + groupPageNum;
			}
		}
		response.sendRedirect(returnURL);
	}
}