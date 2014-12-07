package net.reliableresponse.notification.aggregation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;

public class SquelchList {
	
	private static HashMap<String, List<Squelch>> squelchesByMember;
	
	public static boolean isSquelched (Member member, Notification notification) {
		if (notification == null) return false;
		
		if (notification.getParentUuid() != null) return false;
		
		if (squelchesByMember == null) {
			squelchesByMember = new HashMap<String, List<Squelch>>();
		}

		List<Squelch> squelches = squelchesByMember.get(member.getUuid());
		if (squelches == null) {
			squelches = new ArrayList<Squelch>();
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Found "+squelches.size()+" squelches");
		
		
		boolean squelched = squelches.stream().anyMatch(t->t.shouldSquelch(notification));
		
		if (squelches.size()<1) {
			squelches.add(new Squelch(notification, new Date(), 30));
			squelchesByMember.put(member.getUuid(), squelches);
		}
		
		return squelched;
	}

}
