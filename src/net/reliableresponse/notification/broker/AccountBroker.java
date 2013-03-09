/*
 * Created on Mar 7, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

public interface AccountBroker {

	public Account getAccountByUuid(String uuid);
	
	public Account getUsersAccount (User user);
	
	public void updateAccount(Account account);
	
	public void addAccount(Account account);
	
	public Member[] getAccountMembers(Account account);

}
