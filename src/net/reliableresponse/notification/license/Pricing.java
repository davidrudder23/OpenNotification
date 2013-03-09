package net.reliableresponse.notification.license;

import net.reliableresponse.notification.usermgmt.Member;

public class Pricing {
	
	private double baseMonthlyPrice;
	private double telephoneMonthlyPrice;

	private static Pricing instance;
	
	private Pricing() {
		baseMonthlyPrice = 40.00;
		telephoneMonthlyPrice = 10.00;
	}
	
	public static Pricing getInstance() {
		if (instance == null) {
			instance = new Pricing();
		}
		
		return instance;
	}

	public double getBaseMonthlyPrice() {
		return baseMonthlyPrice;
	}

	public double getTelephoneMonthlyPrice() {
		return telephoneMonthlyPrice;
	}

}
