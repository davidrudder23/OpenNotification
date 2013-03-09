/*
 * Created on Mar 7, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.usermgmt;

import java.util.Date;

import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.caching.CacheException;
import net.reliableresponse.notification.broker.impl.caching.Cacheable;
import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.license.Pricing;

public class Account implements UniquelyIdentifiable, Cacheable {

	private double baseRate;

	private double phoneRate;

	private String paymentSecret;

	private boolean authorized;

	private Date lastPaid;

	private String uuid;

	private boolean autocommit;

	public Account() {
		setAutocommit(false);

		setUuid(null);
		setAuthorized(false);
		setBaseRate(Pricing.getInstance().getBaseMonthlyPrice());
		setPaymentSecret("*");
		setPhoneRate(Pricing.getInstance().getTelephoneMonthlyPrice());
		setLastPaid(new Date(0));
	}

	public void initializeNewAccount() {
		setAutocommit(false);

		setUuid(BrokerFactory.getUUIDBroker().getUUID(this));
		setAuthorized(false);
		setBaseRate(Pricing.getInstance().getBaseMonthlyPrice());
		setPaymentSecret("*");
		setPhoneRate(Pricing.getInstance().getTelephoneMonthlyPrice());
		setLastPaid(new Date(0));

		setAutocommit(true);
		BrokerFactory.getAccountBroker().addAccount(this);
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;

		if (autocommit) {
			BrokerFactory.getAccountBroker().updateAccount(this);
		}
	}

	public double getBaseRate() {
		return baseRate;
	}

	public void setBaseRate(double baseRate) {
		this.baseRate = baseRate;
		if (autocommit) {
			BrokerFactory.getAccountBroker().updateAccount(this);
		}
	}

	public Date getLastPaid() {
		return lastPaid;
	}

	public void setLastPaid(Date lastPaid) {
		this.lastPaid = lastPaid;
		if (autocommit) {
			BrokerFactory.getAccountBroker().updateAccount(this);
		}
	}

	public String getPaymentSecret() {
		return paymentSecret;
	}

	public void setPaymentSecret(String paymentSecret) {
		this.paymentSecret = paymentSecret;
		if (autocommit) {
			BrokerFactory.getAccountBroker().updateAccount(this);
		}
	}

	public double getPhoneRate() {
		return phoneRate;
	}

	public void setPhoneRate(double phoneRate) {
		this.phoneRate = phoneRate;
		if (autocommit) {
			BrokerFactory.getAccountBroker().updateAccount(this);
		}
	}

	public double getTotalMonthlyBill(boolean calculateCoupons) {
		double total = getBaseRate();
		Member[] members = BrokerFactory.getAccountBroker().getAccountMembers(
				this);
		for (int i = 0; i < members.length; i++) {
			if (BrokerFactory.getAuthorizationBroker().isUserInRole(members[i], Roles.TELEPHONE_USER)) {
				total += getPhoneRate();
			}
		}
		if (calculateCoupons) {
			Coupon[] coupons = BrokerFactory.getCouponBroker()
					.getAccountsCoupons(this);
			for (int i = 0; i < coupons.length; i++) {
				Coupon coupon = coupons[i];
				if (coupon.isIndefinite()) {
					total = total - (total * (coupon.getPercentOff() / 100));
				} else {
					// Todo: count the months
					if (!coupon.isExpired(this)) {
						total = total
								- (total * (coupon.getPercentOff() / 100));
					}
				}
			}
		}
		return total;
	}

	public void refreshObject(Cacheable object) throws CacheException {
		if (!(object instanceof Account)) {
			return;
		}

		boolean autocommitSave = autocommit;
		autocommit = false;
		Account other = (Account) object;
		setAuthorized(other.isAuthorized());
		setBaseRate(other.getBaseRate());
		setPhoneRate(other.getPhoneRate());
		setLastPaid(other.getLastPaid());
		setPaymentSecret(other.getPaymentSecret());

		autocommit = autocommitSave;
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isAutocommit() {
		return autocommit;
	}

	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}
	
	public String getName() {
		Member[] members = BrokerFactory.getAccountBroker().getAccountMembers(this);
		return members[0]+"'s account";
	}

	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("Account " + getUuid() + " is "
				+ (isAuthorized() ? "" : "not") + "authorized\n");
		string.append("Base Rate : " + getBaseRate() + "\n");
		string.append("Phone Rate: " + getPhoneRate());
		return string.toString();
	}
}
