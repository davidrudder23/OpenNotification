package net.reliableresponse.notification.broker;


import java.util.Date;

import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;

public interface CouponBroker {

	public Coupon getCouponByName(String name);

	public void addCoupon (Coupon coupon);
	
	public void updateCoupon (Coupon coupon);
	
	public void deleteCoupon (Coupon coupon);
	
	public void useCoupon (Account account, Coupon coupon);
	
	public Coupon[] getAccountsCoupons(Account account);
	
	public Date getUsedOn (Account account, Coupon coupon);
	
}
