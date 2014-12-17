/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.actions;

import java.util.List;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EscalationThread extends Thread {
	EscalationGroup group;

	Notification notification;

	boolean confirmed, passed;

	int recipientNum;

	int[] recipientMap;

	int loopCount;
	
	public EscalationThread(EscalationGroup group, Notification page) {
		this.group = group;
		this.notification = page;
		confirmed = false;
		recipientNum = 0;
		passed = false;
		loopCount = 0;
	}

	private Member[] getOrderedMembers(Group group) {
		Member member = null;

		Member[] members = group.getMembers();
		recipientMap = new int[members.length];

		Vector<Member> orderedMembers = new Vector<Member>();
		// 1st, add the members who are not on vacation
		for (int i = 0; i < members.length; i++) {
			member = members[i];
			if (member instanceof User) {
				if (!((User) member).isOnVacation()) {
					orderedMembers.addElement(member);
					recipientMap[orderedMembers.size() - 1] = i;
				}
			} else {
				orderedMembers.addElement(member);
				recipientMap[orderedMembers.size() - 1] = i;
			}
		}
		// Now, add the vacationing members to the bottom
		for (int i = 0; i < members.length; i++) {
			member = members[i];
			if (member instanceof User) {
				if (((User) member).isOnVacation()) {
					orderedMembers.addElement(member);
					recipientMap[orderedMembers.size() - 1] = i;
				}
			}
		}
		return (Member[]) orderedMembers.toArray(new Member[0]);
	}

	public void run() {
		Member[] members = getOrderedMembers(group);

		while (((group.getLoopCount() <= 0)
				|| (loopCount < group.getLoopCount())) 
				&& ((notification.getUltimateParent().getStatus() == Notification.NORMAL) ||
					(notification.getUltimateParent().getStatus() == Notification.PENDING))){
			while ((recipientNum < members.length) && (!isConfirmed())) {
				passed = false;
				Member escalationMember = members[recipientNum];
				BrokerFactory.getLoggingBroker().logDebug("Sending to recipient num "+recipientNum+" "+escalationMember);
				Notification individualNotification = new Notification(
						notification.getUuid(), escalationMember, notification
								.getSender(), notification.getSubject(),
						new NotificationMessage[0]);
				try {
					BrokerFactory.getNotificationBroker()
							.setNotificationStatus(individualNotification,
									"pending");
				} catch (Exception anyExc) {
					BrokerFactory.getLoggingBroker().logWarn(anyExc);
				}
				individualNotification.setAutocommit(false);
				int priority = notification.getPriority();
				if (escalationMember.getType() == Member.USER) {
					priority = BrokerFactory.getUserMgmtBroker()
							.getPriorityOfGroup((User) escalationMember,
									(Group) notification.getRecipient());
				}
				individualNotification.setPriority(priority);

				try {
					List<NotificationProvider> originalProviders = notification.getNotificationProviders();
					if (originalProviders != null) {
						for (NotificationProvider originalProvider: originalProviders) {
							individualNotification.addNotificationProvider(originalProvider);
						}
					}

					individualNotification.addOption("Confirm");
					individualNotification.addOption("Pass");
					individualNotification.setAutocommit(true);
					SendNotification.getInstance().doSend(
							individualNotification);

					int totalTime = group.getEscalationTime(recipientMap[recipientNum]) * 1000 * 60;
					int spentTime = 0;
					while ((spentTime < totalTime) && (!passed)
							&& (!isConfirmed())) {
						Thread.sleep(1000);
						spentTime += 1000;
						checkConfirmed();
					}

					if (passed) {
						if (recipientNum < (group.getMembers().length - 1)) {
							BrokerFactory
									.getNotificationLoggingBroker()
									.logPassed(
											escalationMember,
											group.getMembers()[recipientNum + 1],
											individualNotification);
							BrokerFactory.getNotificationBroker().logPassed(
									escalationMember,
									group.getMembers()[recipientNum + 1],
									individualNotification);
						} else {
							BrokerFactory.getNotificationLoggingBroker()
									.logPassed(escalationMember,
											escalationMember,
											individualNotification);
							BrokerFactory.getNotificationBroker().logPassed(
									escalationMember, escalationMember,
									individualNotification);
						}
					} else {
						Member to = escalationMember;
						if ((recipientNum + 1) < group.getMembers().length)
							to = group.getMembers()[recipientNum + 1];
						BrokerFactory.getNotificationLoggingBroker()
								.logEscalation(escalationMember, to,
										individualNotification);
						BrokerFactory.getNotificationBroker().logEscalation(
								escalationMember, to, individualNotification);
					}
					recipientNum++;
				} catch (NotificationException e) {
					BrokerFactory.getLoggingBroker().logError(
							"Could not send escalation notification: "
									+ e.getMessage());
					e.printStackTrace();
				} catch (InterruptedException intExc) {
					BrokerFactory.getLoggingBroker().logError(
							"Could not send escalation notification: "
									+ intExc.getMessage());
					intExc.printStackTrace();
				}
			}
			loopCount++;
			recipientNum = 0;
		}
		if (!isConfirmed()) {
			notification.setStatus(Notification.EXPIRED);
			BrokerFactory.getNotificationBroker().setNotificationStatus(
					notification, "expired");
		}
	}

	public void confirm(Member confirmer) {
		confirmed = true;
		// getNotification().setStatus(Notification.CONFIRMED);
	}

	public void pass(Member passer) {
		passed = true;
	}

	public void checkConfirmed() {
		Notification notification = getNotification();
		if (notification.getStatus() == Notification.CONFIRMED) {
			confirm(notification.getRecipient());
			return;
		}

		// NotificationProvider providers[] =
		// notification.getNotificationProviders();
		// for (int i = 0; i < providers.length; i++) {
		// if (providers[i].isConfirmed(notification))
		// confirm (notification.getRecipient());
		// if (providers[i].isPassed(notification))
		// pass (notification.getRecipient());
		// }
	}

	/**
	 * 
	 * @return Whether this escalation notification has been confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	public Notification getNotification() {
		return notification;
	}

	public EscalationGroup getGroup() {
		return group;
	}

	/**
	 * Use this to determine which recipient we're on.  For instance, recipient 
	 * number 0 means that we're waiting for the first of the list to confirm.
	 * 
	 * @return The index into the member list of the group we're paging that 
	 * corresponds to who is currently being notified.
	 */
	public int getRecipientNumber() {
		if ((recipientNum<0) || (recipientNum>= recipientMap.length)) {
			return 0;
		}
		return recipientMap[recipientNum];
	}

}
