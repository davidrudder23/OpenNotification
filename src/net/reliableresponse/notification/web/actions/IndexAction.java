/*
 * Created on Oct 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.actions;

import java.math.BigInteger;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.aggregation.Squelcher;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class IndexAction implements Action {
	
	private boolean isVisible(User user, Notification notification) {
		boolean isAdmin = BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.ADMINISTRATOR) || BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.OBSERVER);
		if (isAdmin) return true;
		
		Member member = notification.getRecipient(); 
		if (member.getType() == Member.USER) {
			return member.equals(user);
		}
		
		Group group = (Group)member;
		return group.isMember(user);
	}
	
	private boolean getIsVisible(ActionRequest actionRequest, String area, String type) {
		String viewString = actionRequest.getParameter("view_"+area+type);
		boolean view = true;
		if (viewString != null) view = viewString.toLowerCase().startsWith("t");
		if (actionRequest.getParameter("toggle_"+area+type+".x")!= null) {
			view = !view;
			actionRequest.setParameter("view_"+area+type, ""+view);
		}
		
		return view;
		
	}
	
	private String makeTitle(String prefix, String area, boolean viewActive, boolean viewConfirmed, boolean viewExpired, boolean viewOnhold, long pending, long confirmed, long expired, long onhold, int numHours) {
		String title = "<font color=\"#17A1e2\">"+prefix+"</font></td>";
		title += "<td align=\"right\" class=\"headercell\"><font color=\"#666666\"><input type=\"image\" src=\"images/led_";
		title += viewActive?"green":"disabled";
		title +=".gif\" width=\"11\" height=\"11\" name=\"toggle_"+area+"active\">&nbsp;active: ";
		title += pending;
		title += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		title += viewConfirmed?"yellow":"disabled";
		title += ".gif\" width=\"11\" height=\"11\" name=\"toggle_"+area+"confirmed\">&nbsp;confirmed: ";
		title += confirmed;
		title += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		title += viewExpired?"red":"disabled";
		title += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_"+area+"expired\">&nbsp;expired: ";
		title += expired;
		title += "&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"images/led_";
		title += viewOnhold?"blue":"disabled";
		title += ".gif\" width=\"11\" height=\"11\"  name=\"toggle_"+area+"onhold\">&nbsp;on hold: ";
		title += onhold;
		title += "</font><img src=\"images/spacer.gif\" width=\"20\" height=\"10\"><font color=\"#000000\"> <span class=\"identity\">display past </span>";
		title += "<input name=\"display_"+area+"past\" type=\"text\" class=\"identity\" value=\"";
		title += numHours;
		title += "\" size=\"3\" onchange=\"document.mainform.submit();\"><span class=\"identity\">hrs.</span></font>";
		
		return title;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */

	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug("Index Action running");

		ActionRequest actionRequest = new ActionRequest((HttpServletRequest) request);

		// -- Sent To Me section --

		// Check to see if the user has update the "display past" setting
		String displayPast = request.getParameter("display_past");
		if ((displayPast != null) && (displayPast.length() > 0)) {
			try {
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

		boolean viewActive = getIsVisible(actionRequest, "", "active");
		boolean viewConfirmed = getIsVisible(actionRequest, "", "confirmed");
		boolean viewExpired = getIsVisible(actionRequest, "", "expired");
		boolean viewOnhold = getIsVisible(actionRequest, "", "onhold");

		User user = (User)BrokerFactory.getUserMgmtBroker().getUserByUuid((String)actionRequest.getSession().getAttribute("user"));
		BrokerFactory.getLoggingBroker().logDebug("Current user = "+user);

		NotificationBroker broker = BrokerFactory.getNotificationBroker();
		BigInteger timeSince = new BigInteger(""+numHours).multiply(new BigInteger("3600")).multiply(new BigInteger("1000"));
		
		List<Notification> recentNotifications = broker.getNotificationsSince(timeSince.longValue()).stream().distinct().filter(n -> isVisible(user, n)).collect(Collectors.toList());
		
		// Add all the pending notifications
		List<Notification> pendingNotifications = broker.getAllPendingNotifications().stream().distinct().filter(n -> isVisible(user, n)).collect(Collectors.toList());

		BrokerFactory.getLoggingBroker().logDebug(pendingNotifications.size()+" pending notifs");
		
		long pending = recentNotifications.stream().filter(n->n.getParentUuid()==null).filter(n->n.getStatus()==Notification.PENDING).count();
		pending += recentNotifications.stream().filter(n->n.getParentUuid()==null).filter(n->n.getStatus()==Notification.NORMAL).count();
		long confirmed = recentNotifications.stream().filter(n->n.getParentUuid()==null).filter(n->n.getStatus()==Notification.CONFIRMED).count();
		long expired = recentNotifications.stream().filter(n->n.getParentUuid()==null).filter(n->n.getStatus()==Notification.EXPIRED).count();
		long onhold = recentNotifications.stream().filter(n->n.getParentUuid()==null).filter(n->n.getStatus()==Notification.ONHOLD).count();


		String notifsTitle = makeTitle("Notifications Sent To Me", "", viewActive, viewConfirmed, viewExpired, viewOnhold, pending, confirmed, expired, onhold, numHours);

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
				actionRequest.getSession().setAttribute("notification_byme_hours", displayPast);
			} catch (NumberFormatException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		
		// Get the stored "display past" value
		numHours = StringUtils.getInteger((String) actionRequest.getSession().getAttribute("notification_byme_hours"), 2);

		viewActive = getIsVisible(actionRequest, "byme_", "active");
		viewConfirmed = getIsVisible(actionRequest, "byme_", "confirmed");
		viewExpired = getIsVisible(actionRequest, "byme_", "expired");
		viewOnhold = getIsVisible(actionRequest, "byme_", "onhold");

		List<Notification> myNotifications = broker.getNotificationsSentBy(user);
		BrokerFactory.getLoggingBroker().logDebug("We have "+myNotifications.size()+" my notifs");

		pending = myNotifications.stream().filter(n->n.getStatus()==Notification.PENDING).count();
		pending += myNotifications.stream().filter(n->n.getStatus()==Notification.NORMAL).count();
		confirmed = myNotifications.stream().filter(n->n.getStatus()==Notification.CONFIRMED).count();
		expired = myNotifications.stream().filter(n->n.getStatus()==Notification.EXPIRED).count();
		onhold = myNotifications.stream().filter(n->n.getStatus()==Notification.ONHOLD).count();

		String sentNotifsTitle = makeTitle("Notifications Sent By Me", "byme_", viewActive, viewConfirmed, viewExpired, viewOnhold, pending, confirmed, expired, onhold, numHours);

		systemMessage = request
				.getParameter("sent_notification_message");
		if ((systemMessage != null) && (systemMessage.length() > 0)) {
			sentNotifsTitle += "</tr><tr><td colspan=\"2\" class=\"headercell\" width=\"100%\"><span class=\"systemalert\">";
			sentNotifsTitle += systemMessage;
			sentNotifsTitle += "</span></td>";
		}

		actionRequest.addParameter("sentNotifsTitle", sentNotifsTitle);

		viewActive = getIsVisible(actionRequest, "squelched_", "active");
		viewConfirmed = getIsVisible(actionRequest, "squelched_", "confirmed");
		viewExpired = getIsVisible(actionRequest, "squelched_", "expired");
		viewOnhold = getIsVisible(actionRequest, "squelched_", "onhold");
		
		
		// -- Squelched notifications --
		displayPast = request.getParameter("display_squelched_past");
		if ((displayPast != null) && (displayPast.length() > 0)) {
			try {
				actionRequest.getSession().setAttribute("notification_squelched_hours", displayPast);
			} catch (NumberFormatException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		
		int squelchedHours = StringUtils.getInteger((String) actionRequest.getSession().getAttribute("notification_squelched_hours"), 2);

		List<Notification> squelchedNotifications = Squelcher.getSquelcherNotifications(user).stream().filter(n->n.getTime().getTime()>System.currentTimeMillis()-(squelchedHours*3600000)).collect(Collectors.toList());
		
		BrokerFactory.getLoggingBroker().logDebug(pendingNotifications.size()+" pending notifs");
		
		viewActive = getIsVisible(actionRequest, "squelched_", "active");
		viewConfirmed = getIsVisible(actionRequest, "squelched_", "confirmed");
		viewExpired = getIsVisible(actionRequest, "squelched_", "expired");
		viewOnhold = getIsVisible(actionRequest, "squelched_", "onhold");
		
		pending = squelchedNotifications.stream().filter(n->n.getStatus()==Notification.PENDING).count();
		pending += squelchedNotifications.stream().filter(n->n.getStatus()==Notification.NORMAL).count();
		confirmed = squelchedNotifications.stream().filter(n->n.getStatus()==Notification.CONFIRMED).count();
		expired = squelchedNotifications.stream().filter(n->n.getStatus()==Notification.EXPIRED).count();
		onhold = squelchedNotifications.stream().filter(n->n.getStatus()==Notification.ONHOLD).count();
		
		String squelchedNotifsTitle = makeTitle("Squelched Notifications", "squelched_", viewActive, viewConfirmed, viewExpired, viewOnhold, pending, confirmed, expired, onhold, squelchedHours);
		systemMessage = request.getParameter("squelched_notification_message");
		if (!StringUtils.isEmpty(systemMessage)) {
			squelchedNotifsTitle += "</tr><tr><td colspan=\"2\" class=\"headercell\" width=\"100%\"><span class=\"systemalert\">";
			squelchedNotifsTitle += systemMessage;
			squelchedNotifsTitle += "</span></td>";
		}

		actionRequest.addParameter("squelchedNotifsTitle", squelchedNotifsTitle);

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