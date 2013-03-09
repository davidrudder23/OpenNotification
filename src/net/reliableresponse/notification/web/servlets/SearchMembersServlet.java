/*
 * Created on Aug 20, 2004
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

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SearchMembersServlet extends HttpServlet {
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
		String searchText = request.getParameter("search");
		if ((searchText == null) || (searchText.length() == 0)) {
			request.getSession().setAttribute("Error", "You didn't specify search text");
			response.sendRedirect("index.jsp?notification=usermgmt");
			return;
		}
		String memberType = request.getParameter("type");
		if ((memberType == null) || (memberType.length() == 0)) {
			memberType = "user";
		}
		String redirectpage = "index.jsp?notification=usermgmt";
		
		int total = 0;
		
		Vector results = new Vector();

		if (memberType.equals ("user")) {
			total = BrokerFactory.getUserMgmtBroker().getNumUsersLike(searchText);
			redirectpage = "index.jsp?notification=usermgmt&id="+request.getParameter("id");
		} else {
			total = BrokerFactory.getGroupMgmtBroker().getNumGroupsLike(searchText);
			redirectpage = "index.jsp?notification=groupmgmt";
		}

		BrokerFactory.getLoggingBroker().logDebug("Search returned "+total+" "+memberType+"s");
		if (total > 150) {
			request.getSession().setAttribute("Error", "Your search return "+total+" results please specify a stricter search");
			response.sendRedirect(redirectpage);
			return;				
		}			

		if (memberType.equals("user")) {
			User[] users = new User[total];
			BrokerFactory.getUserMgmtBroker().getUsersLike(total, 0,
					searchText, users);
			for (int i = 0; i < users.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug("Got user["+i+"] = "+users[i]);
				results.addElement(users[i]);
			}
		} else {
			Group[] groups = new Group[total];
			BrokerFactory.getGroupMgmtBroker().getGroupsLike(total, 0,
					searchText, groups);
			for (int i = 0; i < groups.length; i++) {
				results.addElement(groups[i]);
			}
		}
		
		request.getSession().setAttribute("searchMembers", results);
		
		response.sendRedirect(redirectpage);
	}
}
