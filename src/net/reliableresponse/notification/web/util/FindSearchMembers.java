/*
 * Created on Oct 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class FindSearchMembers {
	
	private int pageNum = 0;
	private int numPages = -1;
	private static int PAGE_SIZE = 101;
	HttpServletRequest request;
	
	public FindSearchMembers(HttpServletRequest request) {
		this.request = request;
	}
	
	public int getPageNum() {
		return pageNum;
	}
	
	public int getNumPages() {
		if (numPages == -1) {
			String numPagesString = request.getParameter("recipient_search_numpages");
			if (numPagesString == null) return 1;
			try {
				return Integer.parseInt(numPagesString);
			} catch (NumberFormatException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				return 1;
			}
		} else {
			return numPages;
		}
	}

	public  Vector findSearchMembers() {
		Vector uuids = new Vector();
		int numMembers = 0;

		String substring = request.getParameter("recipient_search_substring");
		if (substring == null) {
			return uuids;
		}

		// Check to see if the user hit the "next/previous" notification buttons
		int actionPageNum = 0	;
		String actionPageNumString = JSPHelper.getPageNumFromAction(request, "recipient_search_pagenum_");
		if (actionPageNumString != null) {
			try {
				actionPageNum = Integer.parseInt(actionPageNumString);
			} catch (NumberFormatException e) {
			}
		}
		
		if (request.getParameter("action_search_recipients.x") != null) {
			doSearch(request, uuids, substring, 0);
		} else if (actionPageNum >= 0) {
			doSearch(request, uuids, substring, actionPageNum);
			pageNum = actionPageNum;
		} else {
			// If we don't have a new search, check the stored values and load
			// the previous search
			
			// First, find the stored pageNum
			String pageNumString = request.getParameter ("recipient_search_pagenum");
			if (pageNumString != null) {
				try {
					pageNum = Integer.parseInt(pageNumString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			try {
				String numMembersString = request
						.getParameter("recipient_search_nummembers");
				if (numMembersString != null) {
					numMembers = Integer.parseInt(numMembersString);
				}
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logError(anyExc);
			}

			for (int i = 0; i < numMembers; i++) {
				String uuid = request
						.getParameter("recipient_search_uuid_" + i);
				if ((uuid != null) && (uuid.length() > 0)) {
					uuids.addElement(uuid);
				}
			}
		}

		BrokerFactory.getLoggingBroker().logDebug("FSM returning "+uuids.size()+" uuids");
		return uuids;
	}
	
	public Hashtable getUserDeviceList () {
		Hashtable deviceList = new Hashtable();
		
		// Get the users who were selected by user, not by device
		String[] userUuids = JSPHelper.getParameterEndings(request,
			"add_user_");
		// Prime the list with all the people we added
		//String[] sendtoByDefault = BrokerFactory.getConfigurationBroker().getStringValues("sendto.bydefault");

		for (int i = 0; i < userUuids.length; i++) {
			String userUuid = userUuids[i];
			User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(userUuid);
			if (user != null) {
				Vector devices = new Vector();
				List<Device> deviceArray = user.getDevices();
				for (Device device: deviceArray) {
					devices.addElement(device.getUuid());
				}
				deviceList.put (userUuid, devices);
			}
		}
		
		String[] userDeviceUUIDs = JSPHelper.getParameterEndings(request, "add_device_notification_");
		BrokerFactory.getLoggingBroker().logDebug("Found "+userDeviceUUIDs.length+" add_device_notifications");
		for (int i = 0; i < userDeviceUUIDs.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("Found add_device_notification_"+userDeviceUUIDs[i]);
			String userUuid = userDeviceUUIDs[i].substring (0, userDeviceUUIDs[i].indexOf("_"));
			String deviceUuid = userDeviceUUIDs[i].substring (userDeviceUUIDs[i].indexOf("_")+1, userDeviceUUIDs[i].length());
			
			Vector devices;
			if (!deviceList.containsKey(userUuid)) {
				devices = new Vector();
			} else {
				devices = (Vector) deviceList.get(userUuid);
			}
			
			if (!devices.contains(deviceUuid)) {
				devices.addElement(deviceUuid);
			}
			
			deviceList.put(userUuid, devices);
		}
		return deviceList;
	}

	/**
	 * @param request
	 * @param uuids
	 * @param substring
	 */
	private void doSearch(HttpServletRequest request, Vector uuids, String substring, int pageNum) {
		try {
		int numMembers = 0;
		String searchType = request.getParameter("recipient_search_type");
		if (searchType == null)
			searchType = "any";
		searchType = searchType.toLowerCase();

		if (searchType.equals ("any")) {
			int userIndex = pageNum*PAGE_SIZE;
			int numUsers = BrokerFactory.getUserMgmtBroker().getNumUsersLike(substring);
			int numGroups = BrokerFactory.getGroupMgmtBroker().getNumGroupsLike(substring);
			numMembers = numUsers+numGroups;
			int usersAvail = numUsers - userIndex;
			
			if (usersAvail >= PAGE_SIZE) {
				// We can fill the list w/ users
				String[] users = new String[PAGE_SIZE];
				int size= BrokerFactory.getUserMgmtBroker().getUuidsLike(PAGE_SIZE, pageNum, substring, users);
				for (int i = 0; i < size; i++) {
					BrokerFactory.getLoggingBroker().logDebug("found uuid "+users[i]);
					uuids.addElement(users[i]);
				}
			}
			
			if (usersAvail <= 0) {
				// We don't have any available users, so use groups
				// This method is inefficient, but it's the best I could come up with
				// Basically, I take every entry up to the last that I want, and strip off 
				// the extras at the beginning
				int leftOver = numUsers%PAGE_SIZE;
				BrokerFactory.getLoggingBroker().logDebug("left over="+leftOver);
				int groupPageNum = pageNum-(numUsers/PAGE_SIZE);
				BrokerFactory.getLoggingBroker().logDebug("groupageNum="+groupPageNum);
			
				if (groupPageNum > 0) {
						int totalRetrSize = (PAGE_SIZE * groupPageNum)
								+ (PAGE_SIZE-leftOver);

						String[] groups = new String[totalRetrSize];
						int size = BrokerFactory.getGroupMgmtBroker()
								.getGroupsUuidsLike(totalRetrSize, 0,
										substring, groups);
						int gottenSize = PAGE_SIZE-(totalRetrSize - size);
						for (int i = 0; i < gottenSize; i++) {
							uuids.addElement(groups[(size - gottenSize) + i]);
						}							
				} else {
						int totalRetrSize = PAGE_SIZE - leftOver;
						String[] groups = new String[totalRetrSize];
						int size = BrokerFactory.getGroupMgmtBroker()
								.getGroupsUuidsLike(totalRetrSize, 0,
										substring, groups);
						for (int i = 0; i < size; i++) {
							uuids.addElement(groups[i]);
						}
					}
			} else if (usersAvail <= PAGE_SIZE) {
				// We can fill the list w/ users, but we have some left over
				String[] users = new String[PAGE_SIZE];
				int size= BrokerFactory.getUserMgmtBroker().getUuidsLike(PAGE_SIZE, pageNum, substring, users);
				for (int i = 0; i < size; i++) {
					uuids.addElement(users[i]);
				}
				
				size = BrokerFactory.getGroupMgmtBroker().getGroupsUuidsLike((PAGE_SIZE-size), 0, substring, users);
				for (int i = 0; i < size; i++) {
					uuids.addElement(users[i]);
				}
			}
		}

		if (searchType.equals("any individual")) {
			numMembers = BrokerFactory.getUserMgmtBroker().getNumUsersLike(substring);
			String[] users = new String[PAGE_SIZE];
			int numUsers = BrokerFactory.getUserMgmtBroker().getUuidsLike(PAGE_SIZE, pageNum,
					substring, users);
			for (int i = 0; i < numUsers; i++) {
				BrokerFactory.getLoggingBroker().logDebug("Found user " + users[i]);
				uuids.addElement(users[i]);
			}
		}

		if (searchType.equals("any group")) {
			numMembers = BrokerFactory.getGroupMgmtBroker().getNumGroupsLike(substring);
			
			String[] groups = new String[PAGE_SIZE];
			int numGroups = BrokerFactory.getGroupMgmtBroker().getGroupsUuidsLike(
					PAGE_SIZE, pageNum, substring, groups);
			
			BrokerFactory.getLoggingBroker().logDebug("Found "+numGroups+"groups");
			
			for (int i = 0; i < numGroups; i++) {
				BrokerFactory.getLoggingBroker().logDebug("Found group " + groups[i]);

				Group group = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(groups[i]);
				uuids.addElement(groups[i]);
			}
		}
		
		if ((searchType.equals("escalation group"))
			|| (searchType.equals("broadcast group"))) {
			numMembers = BrokerFactory.getGroupMgmtBroker().getNumGroupsLike(substring);

			String[] groups = new String[PAGE_SIZE];
			int numGroups = BrokerFactory.getGroupMgmtBroker().getGroupsUuidsLike(
					PAGE_SIZE, pageNum, substring, groups);

			for (int i = 0; i < numGroups; i++) {
				BrokerFactory.getLoggingBroker().logDebug("Found group " + groups[i]);

				// This is a kludge, since we don't have an easy way to
				// filter the search by group type
				Group group = BrokerFactory.getGroupMgmtBroker()
						.getGroupByUuid(groups[i]);
				if ((searchType.startsWith("any"))
						|| ((searchType.equals("escalation group")) && (group
								.getType() == Group.ESCALATION))) {
					uuids.addElement(groups[i]);
				} else if ((searchType.startsWith("any"))
						|| ((searchType.equals("broadcast group")) && (group
								.getType() == Group.BROADCAST))) {
					uuids.addElement(groups[i]);
				}
			}
		}

		if (numMembers == 0) {
			numPages = 0;
		} else {
			numPages = numMembers/PAGE_SIZE;
			if ((numMembers%PAGE_SIZE) > 0) numPages++;
		}
		BrokerFactory.getLoggingBroker().logDebug("Setting num pages to "+numPages);
	} catch (Exception anyExc) {
		anyExc.printStackTrace();
	}
	}
}