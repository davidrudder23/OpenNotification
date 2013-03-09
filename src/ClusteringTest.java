import java.io.FileInputStream;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredNotificationBroker;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.User;


public class ClusteringTest extends Thread{
	Notification notification;
	public ClusteringTest (Notification notification) {
		this.notification = notification;
	}

	public void run() {
		while (true) {
			System.out.println ("Status="+notification.getStatus());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001");
		Notification notification = new Notification
		(null, user, new EmailSender("drig@noses.org"), "test message", "testing notif updates");
		BrokerFactory.getNotificationBroker().addNotification(notification);
		ClusteringTest thread = new ClusteringTest(notification);
		thread.start();
		
		while (true) {
			Thread.sleep(1000);
			System.out.println ("Invalidating");
			((ClusteredNotificationBroker)BrokerFactory.getNotificationBroker()).invalidateNotification(notification.getUuid());
			
		}
	}

}
