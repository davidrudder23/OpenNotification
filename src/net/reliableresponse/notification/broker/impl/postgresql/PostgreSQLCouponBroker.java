package net.reliableresponse.notification.broker.impl.postgresql;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLCouponBroker;

public class PostgreSQLCouponBroker extends GenericSQLCouponBroker {


	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLAuthenticationBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}
