import java.sql.Connection;
import java.lang.*;

import org.apache.commons.dbcp.BasicDataSource;

/*
 * Created on Oct 12, 2006
 *
 *Copyright Reliable Response, 2006
 */

public class DumpBlob {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BasicDataSource ds = null;
		ds = new BasicDataSource();
		ds.setMaxActive(25);
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setUsername("notification");
        ds.setPassword("notification");
        ds.setUrl("jdbc:oracle:thin:@localhost:1521:infp01");  

        Connection connection = ds.getConnection();
        String sql = "SELECT message FROM notificationmessages WHERE notification='0035104'";
        
        //ResultSet rs = connection.
	}

}
