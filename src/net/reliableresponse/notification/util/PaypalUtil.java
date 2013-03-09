package net.reliableresponse.notification.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

public class PaypalUtil {


	public static String getPaypalUpdateURL(Account account, User[] telephoneUsers) {

		String myHostname = BrokerFactory.getConfigurationBroker().getStringValue("base.url");
		String payPalURL = "";
		boolean sandbox = BrokerFactory.getConfigurationBroker().getBooleanValue("development.mode", false);
		String hostname = "www.paypal.com";
		if (sandbox) {
			hostname = "www.sandbox.paypal.com";
		}
		payPalURL = "https://" + hostname
		+ "/cgi-bin/webscr?cmd=_xclick-subscriptions";
		
		StringBuffer orderInfo = new StringBuffer();
		
		for (int i = 0; i < telephoneUsers.length; i++) {
			if (i>0) {
				orderInfo.append("+");
			}
			orderInfo.append ("phone=");
			orderInfo.append(telephoneUsers[i].getUuid());
		}
		
		double amount = account.getTotalMonthlyBill(false)+
			(account.getPhoneRate()*telephoneUsers.length);
		try {
			payPalURL += "&business=paypal_sandbox%40reliableresponse%2enet&no_shipping=1&no_note=1"
					+ "&currency_code=USD&lc=US&bn=PP%2dSubscriptionsBF&charset=UTF%2d8&a3="
					+ URLEncoder.encode((amount+""), "UTF-8")
					+ "&p3=1&t3=M"
					+ "&src=1&sra=1&item_name=Notify+And+Acknowledge"
					+ "&custom="+account.getUuid()
					+ "&item_number="+orderInfo.toString()
					+ "&return="
					+ URLEncoder.encode(myHostname + "/hosted/login.jsp",
							"UTF-8")
					+ "&cancel_return="
					+ URLEncoder.encode(myHostname + "/hosted/login.jsp","UTF-8")
					+ "&modify=2"
					+ "&notify_url="
					+ URLEncoder.encode(myHostname
							+ "/IPNServlet?secret=" + account.getPaymentSecret(),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		BrokerFactory.getLoggingBroker().logDebug("PayPal Update URL = " + payPalURL);
		return payPalURL;
	}
	
	public static String getInitialPaypalSubscriptionURL(Account account, Coupon coupon, double amount) {
		String myHostname = BrokerFactory.getConfigurationBroker().getStringValue("base.url");
		String secret = createPaymentSecret(account);

		// I worry that this method allows the customer to see the secret, which
		// they can use
		// with IPN. I suppose I'll have to implement IPN security
		String payPalURL = "";

		boolean sandbox = BrokerFactory.getConfigurationBroker()
				.getBooleanValue("development.mode", false);
		String hostname = "www.paypal.com";
		if (sandbox) {
			hostname = "www.sandbox.paypal.com";
		}
		payPalURL = "https://" + hostname
				+ "/cgi-bin/webscr?cmd=_xclick-subscriptions";

		if (coupon != null) {
			int realPrice = (int) (amount - ((amount * coupon.getPercentOff()) / 100));
			realPrice *= coupon.getNumMonths();
			String stringPrice = ""+amount;
			try {
				stringPrice = URLEncoder.encode(realPrice + ".00", "UTF-8");
			} catch (UnsupportedEncodingException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			if ((coupon.getPercentOff() > 0) && (coupon.getNumMonths() > 0)) {
				payPalURL += "&a1=" + stringPrice + "&t1=M&p1="
				+ coupon.getNumMonths();
				BrokerFactory.getCouponBroker().useCoupon(account, coupon);
			}
		}
		String emailAddress = "david@reliableresponse.net";
		if (sandbox) {
			emailAddress = "paypal_sandbox@reliableresponse.net";
		}
				
		try {
			emailAddress = URLEncoder.encode(emailAddress, "UTF-8");
			payPalURL += "&business="+emailAddress
					+ "&no_shipping=1&no_note=1"
					+ "&currency_code=USD&lc=US&bn=PP%2dSubscriptionsBF&charset=UTF%2d8&a3="
					+ URLEncoder.encode((amount+""), "UTF-8")
					+ "&p3=1&t3=M"
					+ "&src=1&sra=1&item_name=Notify+And+Acknowledge"
					+ "&custom="+ account.getUuid()
					+ "&return="
					+ URLEncoder.encode(myHostname + "/hosted/login.jsp",
							"UTF-8")
					+ "&cancel_return="
					+ URLEncoder.encode(myHostname + "/hosted/login.jsp",
							"UTF-8")
					+ "&notify_url="
					+ URLEncoder.encode(myHostname
							+ "/IPNServlet?secret=" + secret,
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		BrokerFactory.getLoggingBroker().logDebug("PayPal URL = " + payPalURL);
		return payPalURL;
	}

	/**
	 * @param loginUser
	 * @return
	 */
	private static String createPaymentSecret(Account account) {
		String secret = StringUtils.toHexString(account.getUuid().getBytes())+
			System.currentTimeMillis();
		account.setPaymentSecret(secret);
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(account.getUuid().getBytes());
			secret = StringUtils.toHexString(md5.digest());
		} catch (NoSuchAlgorithmException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
		
		account.setPaymentSecret(secret);
		return secret;
	}

	
	public static String getCancelURL(Account account) {
		return "https://www.paypal.com/cgi-bin/webscr?cmd=_subscr-find&alias=email%40domain%2ecom";
	}
}
