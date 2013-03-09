<jsp:directive.page import="java.util.Vector" />
<jsp:directive.page
	import="net.reliableresponse.notification.util.StringUtils" />
<jsp:directive.page
	import="net.reliableresponse.notification.usermgmt.EscalationGroup" />
<jsp:directive.page
	import="net.reliableresponse.notification.usermgmt.User" />
<jsp:directive.page
	import="net.reliableresponse.notification.broker.BrokerFactory" />
<jsp:directive.page import="java.net.URLEncoder" />
<jsp:directive.page import="java.security.MessageDigest" />
<jsp:directive.page
	import="net.reliableresponse.notification.usermgmt.Group" />
<jsp:directive.page import="java.security.SecureRandom" />
<jsp:directive.page
	import="net.reliableresponse.notification.license.Coupon" />
<jsp:directive.page
	import="net.reliableresponse.notification.util.PaypalUtil" />
<jsp:directive.page import="net.reliableresponse.notification.license.Pricing"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Account"/>
<jsp:directive.page import="net.reliableresponse.notification.usermgmt.Roles"/>
<%
	String groupName = request.getParameter("addDepartment");
	String login = request.getParameter("addLogin");
	String password = request.getParameter("addPassword");
	String confirmPassword = request.getParameter("addConfirmPassword");
	String couponString = request.getParameter("addCoupon");

	boolean passed = true;
	Vector messages = new Vector();

	Coupon coupon = null;
	if (!StringUtils.isEmpty(couponString)) {
		coupon = BrokerFactory.getCouponBroker().getCouponByName(
		couponString);
		BrokerFactory.getLoggingBroker().logDebug(
		"Registrant " + login + " requested coupon " + coupon);
		if (coupon == null) {
			messages
			.addElement("I'm sorry, but that coupon is not in our records.");
			passed = false;
		}
	}


	if (StringUtils.isEmpty(groupName)) {
		messages.addElement("Please supply a valid group name.");
		passed = false;
	}
	if (StringUtils.isEmpty(login)) {
		messages.addElement("Please supply a valid login name.");
		passed = false;
	} else {
		if (BrokerFactory.getAuthenticationBroker()
		.getUserByIdentifier(login) != null) {
			messages
			.addElement("That login name is already taken.  Please try another one.");
			passed = false;
		}
	}

	if (StringUtils.isEmpty(password)) {
		messages.addElement("Please supply a valid password.");
		passed = false;
	} else {
		if (StringUtils.isEmpty(confirmPassword)) {
			messages
			.addElement("Your password and confirmation do not match");
			passed = false;
		} else {
			if (!confirmPassword.equals(password)) {
		messages
				.addElement("Your password and confirmation do not match");
		passed = false;
			}
		}
	}

	//Now, check to make sure the group isn't a duplicate

	Group existingGroup = BrokerFactory.getGroupMgmtBroker()
			.getGroupByName(groupName);
	if (existingGroup != null) {
		messages
		.addElement("That account already exists.  Please choose a new name.");
		passed = false;
	}

	if (!passed) {
		session.setAttribute("register_messages", messages
		.toArray(new String[0]));
		response.sendRedirect("../register.jsp");
		return;
	}

	// Add the group
	EscalationGroup escGroup = new EscalationGroup();
	escGroup.setGroupName(groupName);
	BrokerFactory.getGroupMgmtBroker().addEscalationGroup(escGroup);
	escGroup.setAutocommit(true);
	
	// Add the account
	Account account = new Account();
	BrokerFactory.getAccountBroker().addAccount(account);
	account.setAutocommit(true);
	int newUserCount = 0;
	int newUserIndex = 1;
	while (newUserCount < 4) {
		BrokerFactory.getLoggingBroker().logDebug(
				"new user index="+newUserIndex);
		String[] testUser = BrokerFactory.getUserMgmtBroker()
		.getUuidsByName("User #" + newUserIndex, groupName);
		BrokerFactory.getLoggingBroker().logDebug(
		"Test User=" + testUser);
		if (testUser != null) {
			BrokerFactory.getLoggingBroker().logDebug(
			"Test User length=" + testUser.length);
		}
		if ((testUser == null) || (testUser.length <= 0)) {
			User user = new User();
			user.setInformation("account", account.getUuid());
			user.setFirstName("User #" + newUserIndex);
			user.setLastName(groupName);
			user.setDepartment(groupName);
			user.setInformation("Timezone", "America/Denver");
			BrokerFactory.getUserMgmtBroker().addUser(user);
			escGroup.addMember(user, -1);
			user.setAutocommit(true);
			BrokerFactory.getAuthorizationBroker().addUserToRole(user, Roles.MANAGED);
			escGroup.setOwner(newUserCount);
			newUserCount++;
		}
		newUserIndex++;
	}
	// This is the user we'll use for logging in
	User loginUser = (User) escGroup.getMembers()[0];
	BrokerFactory.getAuthorizationBroker().addUserToRole(loginUser,
			"Team Leader");
	//	 Add the login user
	BrokerFactory.getAuthenticationBroker().addUser(login, password,
			loginUser);
	account.setAuthorized(false);
	BrokerFactory.getLoggingBroker().logDebug("coupon=" + coupon);
	if ((coupon != null) && (coupon.isIndefinite()) && (coupon.getPercentOff() == 100)) {
		account.setAuthorized(true);
		BrokerFactory.getCouponBroker().useCoupon(account, coupon);
		response.sendRedirect("../login.jsp");
		return;
	}

	String payPalURL = PaypalUtil.getInitialPaypalSubscriptionURL(account,
			coupon, Pricing.getInstance().getBaseMonthlyPrice());
	response.sendRedirect(payPalURL);
%>
