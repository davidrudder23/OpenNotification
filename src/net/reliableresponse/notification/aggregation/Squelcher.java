package net.reliableresponse.notification.aggregation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.LoggingBroker;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.StringUtils;

public class Squelcher {
	
	private static HashMap<String, List<Squelch>> squelchesByMember;
	
	public static boolean isSquelched (Notification notification) {
		if (notification == null) return false;
		
		Member member = notification.getRecipient();
		
		List<Squelch> squelches = getSquelches(member);
		
		// Expire old squelches
		squelches = squelches.stream().filter(s->!s.isExpired()).collect(Collectors.toList());
		
		// should it squelch?
		boolean squelched = squelches.stream().anyMatch(t->t.shouldSquelch(notification));
		
		logStats();
		
		return squelched;
	}
	
	public static List<Notification> getSquelcherNotifications(Member member) {
		return getSquelches(member).stream().map(s->s.getNotification()).collect(Collectors.toList());
	}
	
	public static List<Squelch> getSquelches(Member member) {
		if (squelchesByMember == null) {
			squelchesByMember = new HashMap<String, List<Squelch>>();
		}

		List<Squelch> squelches = squelchesByMember.get(member.getUuid());
		if (squelches == null) {
			squelches = new ArrayList<Squelch>();
			squelchesByMember.put(member.getUuid(), squelches);

		}
		return squelches;
	}
	
	
	public static void squelch(Notification notification) {
		squelch (notification, null);
	}

	public static void squelch(Notification notification, String message) {
		Member member = notification.getRecipient();
		if (member.getType() != Member.USER) {
			BrokerFactory.getLoggingBroker().logWarn("Can not squelch "+notification.getUuid()+" because recipient is not an individual");
		}
		List<Squelch> squelches = getSquelches(member);
		squelches.add(new Squelch(notification, new Date(), 30));
			
		if (!StringUtils.isEmpty(message)) {
			notification.addMessage(message, notification.getRecipient());
		}
		logStats();
	}
	
	public static void unsquelch(Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("Unsquelch called");
		Member member = notification.getRecipient();
		if (member.getType() != Member.USER) {
			BrokerFactory.getLoggingBroker().logWarn("Can not unsquelch "+notification.getUuid()+" because recipient is not an individual");
			return;
		}
		
		List<Squelch> squelches = getSquelches(member);

		squelches = squelches.stream().filter(s->!s.shouldSquelch(notification)).collect(Collectors.toList());
		squelchesByMember.put(member.getUuid(), squelches);

		logStats();
	}
	
	public static void logStats() {
		LoggingBroker log = BrokerFactory.getLoggingBroker();
		log.logDebug("Squelches");
		squelchesByMember.keySet().stream().forEach(m->squelchesByMember.get(m).stream().forEach(s->log.logDebug("  "+m+": "+s.getNotification().getSubject())));
	}


}
