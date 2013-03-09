package net.reliableresponse.notification.license;

import java.util.Date;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.caching.CacheException;
import net.reliableresponse.notification.broker.impl.caching.Cacheable;
import net.reliableresponse.notification.usermgmt.Account;

public class Coupon implements UniquelyIdentifiable, Comparable,
Cacheable{

	String uuid;
	private boolean indefinite;
	private int numMonths;
	private int percentOff;
	
	private String name;
	
	private Date fromDate;
	private Date toDate;
	
	public Coupon() {
		uuid = null;
	}

	public boolean isIndefinite() {
		return indefinite;
	}

	public void setIndefinite(boolean indefinite) {
		this.indefinite = indefinite;
	}

	public int getNumMonths() {
		return numMonths;
	}

	public void setNumMonths(int numMonths) {
		this.numMonths = numMonths;
	}

	public int getPercentOff() {
		return percentOff;
	}

	public void setPercentOff(int percentOff) {
		this.percentOff = percentOff;
	}

	public Date getFromDate() {
		if (fromDate == null) {
			return new Date();
		}
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getToDate() {
		if (toDate == null) {
			return new Date();
		}
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
	public int compareTo(Object compareTo) {
		if (compareTo instanceof Coupon) {
			Coupon other = (Coupon) compareTo;
			if (other.getUuid().equals(getUuid())) {
				return 0;
			}
			return getName().compareTo(other.getName());
		} else {
			return -1;
		}
	}

	public void refreshObject(Cacheable object) throws CacheException {
		if (object instanceof Coupon) {
			Coupon other = (Coupon) object;		
			setName(other.getName());
			setToDate(other.getToDate());
			setFromDate(other.getFromDate());
			setIndefinite(other.isIndefinite());
			setNumMonths(other.getNumMonths());
		} 
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
	
	public Date getUsedOn (Account account) {
		return BrokerFactory.getCouponBroker().getUsedOn(account, this);
	}
	
	public boolean isExpired (Account account) {
		if (isIndefinite()) return false;
		Date usedOn = getUsedOn(account);
		usedOn.setMonth (usedOn.getMonth()+getNumMonths());
		return (usedOn.before(new Date()));
	}
	
	public String toShortString() {
		StringBuffer string = new StringBuffer();
		
		string.append (getPercentOff()+"% off");
		if (!isIndefinite()) {
			string.append (" for "+getNumMonths()+" months");
		}
		return string.toString();		
	}

	public String toString() {
		StringBuffer string = new StringBuffer();
		
		string.append ("Coupon: starts on ");
		string.append (getFromDate().toString());
		string.append ("\nEnds on ");
		string.append (getToDate().toString());
		string.append ("\nLasts ") ;
		if (isIndefinite()) {
			string.append ("forever");
		} else {
			string.append (getNumMonths()+" months");
		}
		string.append ("\n for "+getPercentOff()+"% off");
		return string.toString();
	}
	
}
