/*
 * Created on Aug 31, 2004
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
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class UpdateGroupInfoServlet extends HttpServlet {

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
		Vector responses = new Vector();
		
		String groupUuid = request.getParameter("group");
		BrokerFactory.getLoggingBroker().logDebug("Updating group with uuid "+groupUuid);
		if ((groupUuid == null) || (groupUuid.length() == 0)) {
			request.getSession().setAttribute("Error", "You must specify a valid group to update");
			response.sendRedirect("index.jsp?notification=groupmgmt");
			return;
		}
		
		Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(groupUuid);
		BrokerFactory.getLoggingBroker().logDebug("Updating group "+group);
		if (group == null) {
			request.getSession().setAttribute("Error", "You must specify a valid group to update");
			response.sendRedirect("index.jsp?notification=groupmgmt");
			return;
		}
		String action = request.getParameter("action");
		if (action == null) action="updateGroup";
		BrokerFactory.getLoggingBroker().logDebug("Update Group action="+action);
		
		// Now, do the actions
		if (action.equals ("updateGroup")) {
			String name = request.getParameter("name");
			if ((name == null) || (name.length() == 0)){
				request.getSession().setAttribute("Error", "You must specify a valid name");
				response.sendRedirect("index.jsp?notification=groupmgmt");
				return;				
			}

			if (BrokerFactory.getGroupMgmtBroker().getGroupByName(name)==null) {
				group.setGroupName(name);
			}
			if (group instanceof EscalationGroup) {
				Member[] members = group.getMembers();
				for (int i = 0; i < members.length; i++) {
					String timeString = request.getParameter("esctime"+i);
					if (timeString != null) {
						try {
							int time = Integer.parseInt(timeString);
							((EscalationGroup)group).setEscalationTime(i, time);
						} catch (NumberFormatException e) {
							BrokerFactory.getLoggingBroker().logDebug(e.getMessage());
						}
					}
				}
			}
			group.setGroupName (name);
		} else if (action.equals ("deleteMember")) {
			String memberUuid = request.getParameter("member");
			BrokerFactory.getLoggingBroker().logDebug("Deleting member with uuid="+memberUuid);
			if ((memberUuid == null) || (memberUuid.length() <= 0)) {
				request.getSession().setAttribute("Error", "You must specify a valid member to delete");
				response.sendRedirect("index.jsp?notification=groupmgmt");
				return;
			}
			
			Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
			BrokerFactory.getLoggingBroker().logDebug("Deleting member="+member);
			if (member == null) member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(memberUuid);
			BrokerFactory.getLoggingBroker().logDebug("Deleting member="+member);
			if (member == null) {
				request.getSession().setAttribute("Error", "You must specify a valid member to delete");
				response.sendRedirect("index.jsp?notification=groupmgmt");
				return;
			}
			
			BrokerFactory.getLoggingBroker().logDebug("Deleting "+member+" from "+group);
			//group.removeMemberFromGroup(member);
		} else if (action.equals ("deleteGroup")) {
			try {
				BrokerFactory.getGroupMgmtBroker().deleteGroup(group);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		} else if (action.equals ("moveMemberUp")) {		
			String memberUuid = request.getParameter("member");
			BrokerFactory.getLoggingBroker().logDebug("Moving member with uuid="+memberUuid+" up");
			if ((memberUuid == null) || (memberUuid.length() <= 0)) {
				request.getSession().setAttribute("Error", "You must specify a valid member to move");
				response.sendRedirect("index.jsp?notification=groupmgmt");
				return;
			}
			
			Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
			int memberNum = group.getMemberNumber(member);
			BrokerFactory.getGroupMgmtBroker().moveMemberUp(group, memberNum);
		} else if (action.equals ("moveMemberDown")) {	
			String memberUuid = request.getParameter("member");
			BrokerFactory.getLoggingBroker().logDebug("Moving member with uuid="+memberUuid+" down");
			if ((memberUuid == null) || (memberUuid.length() <= 0)) {
				request.getSession().setAttribute("Error", "You must specify a valid member to move");
				response.sendRedirect("index.jsp?notification=groupmgmt");
				return;
			}
			
			Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
			int memberNum = group.getMemberNumber(member);
			BrokerFactory.getGroupMgmtBroker().moveMemberDown(group, memberNum);
		}
		
		request.getSession().setAttribute("responses", responses);
		response.sendRedirect("index.jsp?notification=groupmgmt");
	}
}
