/*
 * Created on Oct 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.actions;

import java.math.BigInteger;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class IndexAction implements Action {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */

	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Index Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		// -- Sent To Me section --

		// Check to see if the user has update the "display past" setting
		String displayPast = request.getParameter("display_past");
		if ((displayPast != null) && (displayPast.length() > 0)) {
			try {
				int displayNum = Integer.parseInt (displayPast);
				actionRequest.getSession().setAttribute("notification_hours", displayPast);
			} catch (NumberFormatException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		
		// Get the stored "display past" value
		String numHoursString = (String) actionRequest.getSession()
				.getAttribute("notification_hours");
		if ((numHoursString == null) || (numHoursString.length() == 0)) {
			numHoursString = "2";
		}
		int numHours = 2;
		try {
			numHours = Integer.parseInt(numHoursString);
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		String viewActiveString = actionRequest.getParameter("view_active");
		boolean viewActive = true;
		if (viewActiveString != null) viewActive = viewActiveString.toLowerCase().startsWith("t");
		if (request.getParameter("toggle_active.x")!= null) {
			viewActive = !viewActive;
			actionRequest.setParameter("view_active", ""+viewActive);
		}

		String viewConfirmedString = actionRequest.getParameter("view_confirmed");
		boolean viewConfirmed = true;
		if (viewConfirmedString != null) viewConfirmed = viewConfirmedString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_confirmed.x")!= null) {
			viewConfirmed = !viewConfirmed;
			actionRequest.setParameter("view_confirmed", ""+viewConfirmed);
		}

		String viewExpiredString = actionRequest.getParameter("view_expired");
		boolean viewExpired = true;
		if (viewExpiredString != null) viewExpired = viewExpiredString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_expired.x")!= null) {
			viewExpired = !viewExpired;
			actionRequest.setParameter("view_expired", ""+viewExpired);
		}

		String viewOnholdString = actionRequest.getParameter("view_onhold");
		boolean viewOnhold = true;
		if (viewOnholdString != null) viewOnhold = viewOnholdString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_onhold.x")!= null) {
			viewOnhold = !viewOnhold;
			actionRequest.setParameter("view_onhold", ""+viewOnhold);
		}

		User user = (User)BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user"));
		BrokerFactory.getLoggingBroker().logDebug("Current user = "+user);
		//BrokerFactory.getLoggingBroker().logDebug("Current user's email = "+user.getEmailAddress());

		boolean isAdmin = BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.ADMINISTRATOR) ||
		BrokerFactory.getAuthorizationBroker().isUserInRole (user, Roles.OBSERVER);

		NotificationBroker broker = BrokerFactory.getNotificationBroker();
		BigInteger bigint = new BigInteger(""+numHours);
		bigint = bigint.multiply(new BigInteger("3600"));
		bigint = bigint.multiply(new BigInteger("1000"));
		Notification[] recentNotifications = broker.getNotificationsSince(bigint.longValue());
		Vector sorted = new SortedVector();
		for (int i = 0; i < recentNotifications.length; i++) {
			if (recentNotifications[i].getParentUuid() == null) {
				if (isAdmin) {
						sorted.addElement(recentNotifications[i]);
				} else {
					Member recipient = recentNotifications[i].getRecipient();
					if (recipient.getType() == Member.USER) {
						if (recipient.equals(user)) {
							sorted.addElement(recentNotifications[i]);
						}
					} else {
						Group group= (Group)recipient;
						if (group.isMember(user)) {
							sorted.addElement(recentNotifications[i]);
						}
					}
				}
			}
		}
		
		// Add all the pending notifications
		recentNotifications = broker.getAllPendingNotifications();
		BrokerFactory.getLoggingBroker().logDebug(recentNotifications.length+" pending notifs");
		for (int i = 0; i < recentNotifications.length; i++) {
			if (recentNotifications[i].getParentUuid() == null) {
				if (!sorted.contains(recentNotifications[i])) {
					if (isAdmin) {
						sorted.addElement(recentNotifications[i]);
					} else {
						Member recipient = recentNotifications[i].getRecipient();
						if (recipient.getType() == Member.USER) {
							if (recipient.equals(user)) {
								sorted.addElement(recentNotifications[i]);
							}
						} else {
							Group group= (Group)recipient;
							if (group.isMember(user)) {
								sorted.addElement(recentNotifications[i]);
							}
						}
					}
				}
			}
		}
		
		Notification[] usersNotifs = (Notification[]) sorted.toArray(new Notification[0]);

		int pending = 0;
		int confirmed = 0;
		int expired = 0;
		int onhold = 0;

		for (int i = 0; i < usersNotifs.length; i++) {
			int status = usersNotifs[i].getStatus();
			switch (status) {
			case Notification.NORMAL:
			case Notification.PENDING:
				pending++;
				break;
			case Notification.CONFIRMED:
				confirmed++;
				break;
			case Notification.EXPIRED:
				expired++;
				break;
			case Notification.ONHOLD:
				onhold++;
				break;
			}
		}

		String notifsTitle = "<font color=\"#17A1e2\">Notifications Sent To Me</font></td>";
		notifsTitle += "<td align=\"right\" class=\"headercell\"><font color=\"#666666\"><input type=\"image\" src=\"images/led_";
		notifsTitle += viewActive?"green":"disabled";
		notifsTitle +=".gif\" width=\"11\" height=\"11\" name=\"toggle_active\">&nbsp;active: ";
		notifsTitle += pending;
		notifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		notifsTitle += viewConfirmed?"yellow":"disabled";
		notifsTitle += ".gif\" width=\"11\" height=\"11\" name=\"toggle_confirmed\">&nbsp;confirmed: ";
		notifsTitle += confirmed;
		notifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		notifsTitle += viewExpired?"red":"disabled";
		notifsTitle += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_expired\">&nbsp;expired: ";
		notifsTitle += expired;
		notifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		notifsTitle += viewOnhold?"blue":"disabled";
		notifsTitle += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_onhold\">&nbsp;on hold: ";
		notifsTitle += onhold;
		notifsTitle += "</font><img src=\"images/spacer.gif\" width=\"20\" height=\"10\"><font color=\"#000000\"> <span class=\"identity\">display past </span>";
		notifsTitle += "<input name=\"display_past\" type=\"text\" class=\"identity\" value=\"";
		notifsTitle += numHours;
		notifsTitle += "\" size=\"3\" onchange=\"document.mainform.submit();\"><span class=\"identity\">hrs.</span></font>";

		String systemMessage = request
				.getParameter("pending_notification_message");
		if ((systemMessage != null) && (systemMessage.length() > 0)) {
			notifsTitle += "</tr><tr><td colspan=\"2\" class=\"headercell\" width=\"100%\"><span class=\"systemalert\">";
			notifsTitle += systemMessage;
			notifsTitle += "</span></td>";
		}

		actionRequest.addParameter("notifsTitle", notifsTitle);
		
		
		// -- Sent By Me section --
		
		// Check to see if the user has update the "display past" setting
		displayPast = request.getParameter("display_byme_past");
		if ((displayPast != null) && (displayPast.length() > 0)) {
			try {
				int displayNum = Integer.parseInt (displayPast);
				actionRequest.getSession().setAttribute("notification_byme_hours", displayPast);
			} catch (NumberFormatException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		
		// Get the stored "display past" value
		numHoursString = (String) actionRequest.getSession()
				.getAttribute("notification_byme_hours");
		if ((numHoursString == null) || (numHoursString.length() == 0)) {
			numHoursString = "2";
		}
		numHours = 2;
		try {
			numHours = Integer.parseInt(numHoursString);
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		viewActiveString = actionRequest.getParameter("view_byme_active");
		viewActive = true;
		if (viewActiveString != null) viewActive = viewActiveString.toLowerCase().startsWith("t");
		if (request.getParameter("toggle_byme_active.x")!= null) {
			viewActive = !viewActive;
			actionRequest.setParameter("view_byme_active", ""+viewActive);
		}

		viewConfirmedString = actionRequest.getParameter("view_byme_confirmed");
		viewConfirmed = true;
		if (viewConfirmedString != null) viewConfirmed = viewConfirmedString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_byme_confirmed.x")!= null) {
			viewConfirmed = !viewConfirmed;
			actionRequest.setParameter("view_byme_confirmed", ""+viewConfirmed);
		}

		viewExpiredString = actionRequest.getParameter("view_byme_expired");
		viewExpired = true;
		if (viewExpiredString != null) viewExpired = viewExpiredString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_byme_expired.x")!= null) {
			viewExpired = !viewExpired;
			actionRequest.setParameter("view_byme_expired", ""+viewExpired);
		}

		viewOnholdString = actionRequest.getParameter("view_byme_onhold");
		viewOnhold = true;
		if (viewOnholdString != null) viewOnhold = viewOnholdString.toLowerCase().startsWith("t"); 
		if (request.getParameter("toggle_byme_onhold.x")!= null) {
			viewOnhold = !viewOnhold;
			actionRequest.setParameter("view_byme_onhold", ""+viewOnhold);
		}

		Notification[] myNotifications = broker.getNotificationsSentBy (user);
		BrokerFactory.getLoggingBroker().logDebug("We have "+myNotifications.length+" my notifs");

		pending = 0;
		confirmed = 0;
		expired = 0;
		onhold = 0;

		for (int i = 0; i < myNotifications.length; i++) {
			if (myNotifications[i].getTime().getTime() > (System
					.currentTimeMillis() - (numHours * 60 * 60 * 1000))) {
				int status = myNotifications[i].getStatus();
				switch (status) {
				case Notification.NORMAL:
				case Notification.PENDING:
					pending++;
					break;
				case Notification.CONFIRMED:
					confirmed++;
					break;
				case Notification.EXPIRED:
					expired++;
					break;
				case Notification.ONHOLD:
					onhold++;
					break;
				}
			}
		}

		String sentNotifsTitle = "<font color=\"#17A1e2\">Notifications Sent By Me</font></td>";
		sentNotifsTitle += "<td align=\"right\" class=\"headercell\"><font color=\"#666666\"><input type=\"image\" src=\"images/led_";
		sentNotifsTitle += viewActive?"green":"disabled";
		sentNotifsTitle +=".gif\" width=\"11\" height=\"11\" name=\"toggle_byme_active\">&nbsp;active: ";
		sentNotifsTitle += pending;
		sentNotifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		sentNotifsTitle += viewConfirmed?"yellow":"disabled";
		sentNotifsTitle += ".gif\" width=\"11\" height=\"11\" name=\"toggle_byme_confirmed\">&nbsp;confirmed: ";
		sentNotifsTitle += confirmed;
		sentNotifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		sentNotifsTitle += viewExpired?"red":"disabled";
		sentNotifsTitle += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_byme_expired\">&nbsp;expired: ";
		sentNotifsTitle += expired;
		sentNotifsTitle += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		sentNotifsTitle += viewOnhold?"blue":"disabled";
		sentNotifsTitle += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_byme_onhold\">&nbsp;on hold: ";
		sentNotifsTitle += onhold;
		sentNotifsTitle += "</font><img src=\"images/spacer.gif\" width=\"20\" height=\"10\"><font color=\"#000000\"> <span class=\"identity\">display past </span>";
		sentNotifsTitle += "<input name=\"display_byme_past\" type=\"text\" class=\"identity\" value=\"";
		sentNotifsTitle += numHours;
		sentNotifsTitle += "\" size=\"3\" onchange=\"document.mainform.submit();\"><span class=\"identity\">hrs.</span></font>";

		systemMessage = request
				.getParameter("sent_notification_message");
		if ((systemMessage != null) && (systemMessage.length() > 0)) {
			sentNotifsTitle += "</tr><tr><td colspan=\"2\" class=\"headercell\" width=\"100%\"><span class=\"systemalert\">";
			sentNotifsTitle += systemMessage;
			sentNotifsTitle += "</span></td>";
		}

		actionRequest.addParameter("sentNotifsTitle", sentNotifsTitle);

		// Handle the send title
		String sendTitle = "<font color=\"#17A1e2\">Send A New Notification</font></td>";
		systemMessage = actionRequest.getParameter("send_system_message");
		if ((systemMessage != null) && (systemMessage.length() > 0)) {
			sendTitle += "<td class=\"headercell\"><span class=\"systemalert\">";
			sendTitle += systemMessage;
			sendTitle += "</span></td>";
		} else {
			sendTitle += "<td class=\"headercell\"></td>";
		}
		actionRequest.addParameter("sendTitle", sendTitle);
		return actionRequest;
	}

}