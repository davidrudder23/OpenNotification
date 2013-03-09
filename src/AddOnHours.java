import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.scheduling.OnHoursSchedule;
import net.reliableresponse.notification.scheduling.Schedule;

/*
 * Created on Apr 26, 2006
 *
 *Copyright Reliable Response, 2006
 */

public class AddOnHours {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		Schedule schedule = new OnHoursSchedule();
		BrokerFactory.getScheduleBroker().addSchedule(schedule);
	}

}
