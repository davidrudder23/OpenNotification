package net.reliableresponse.notification.aggregation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.StringUtils;

public class Squelcher {
	
	private static HashMap<String, List<Squelch>> squelchesByMember;
	
	public static boolean isSquelched (Notification notification) {
		if (notification == null) return false;
		
		Member member = notification.getRecipient();
		
		if (member.getType() != Member.USER) {
			BrokerFactory.getLoggingBroker().logDebug(notification.getUuid()+" will not be squelched because recipient is not an individual");
		}
		
		List<Squelch> squelches = getSquelches(member);
		
		boolean squelched = squelches.stream().anyMatch(t->t.shouldSquelch(notification));
		
		if (squelches.size()<1) {
			squelch(notification);
		}
		
		return squelched;
	}
	
	public static List<Squelch> getSquelches(Member member) {
		if (squelchesByMember == null) {
			squelchesByMember = new HashMap<String, List<Squelch>>();
		}

		List<Squelch> squelches = squelchesByMember.get(member.getUuid());
		if (squelches == null) {
			squelches = new ArrayList<Squelch>();
		}
		return squelches;
	}
	
	public static void setSquelches(Member member, List<Squelch> squelches) {
		squelchesByMember.put(member.getUuid(), squelches);
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
		setSquelches(member, squelches);
		
		if (!StringUtils.isEmpty(message)) {
			notification.addMessage(message, notification.getRecipient());
		}
	}

}
