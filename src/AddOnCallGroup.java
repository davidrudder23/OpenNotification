import java.io.FileInputStream;
import java.util.Date;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;

/*
 * Created on Apr 10, 2006
 *
 *Copyright Reliable Response, 2006
 */

public class AddOnCallGroup {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		OnCallGroup group = new OnCallGroup();
		
		group.setAutocommit(false);
		group.setGroupName("Test OnCallGroup");
		group.addMember(BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001"), -1);
		BrokerFactory.getGroupMgmtBroker().addGroup(group);
		OnCallSchedule schedule = new OnCallSchedule();
		schedule.setRepetition(OnCallSchedule.REPEAT_DAILY);
		schedule.setAllDay(false);
		schedule.setFromDate(new Date());
		schedule.setToDate(new Date());
		group.setOnCallSchedule(schedule, 0);

	}

}
