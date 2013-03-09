/*
 * Created on Aug 14, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.Date;

import javax.naming.ldap.InitialLdapContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.LDAPAuthenticationBroker;
import net.reliableresponse.notification.broker.impl.MultiRealmAuthenticationBroker;
import net.reliableresponse.notification.ldap.LDAPLibrary;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.actions.ActionRequest;


/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AuthenticationServlet extends HttpServlet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(arg0, arg1);
	}
	
	private void sendResetEmail(User user, String token) {
		String message = 
			"Someone has asked Reliable Response to reset your password.  \n"+
			"If that was you, please go to this URL:\n"+
			BrokerFactory.getConfigurationBroker().getStringValue("base.url")+
			"/ForgotPasswordServlet?token="+token;
		
		Notification notification = new Notification(null, user, 
				new EmailSender("passwordchange@reliableresponse.net"),
				"Reset Your Password", message);
		try {
			SendNotification.getInstance().doSend(notification);
			BrokerFactory.getLoggingBroker().logDebug("Sent password reset notification to "+user);
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public boolean isLDAPEnabled() {
		String ldapLogin = BrokerFactory.getConfigurationBroker()
				.getStringValue("ldap.authn.compare");
		if (StringUtils.isEmpty(ldapLogin)) {
			return false;
		}
		
		String host = BrokerFactory.getConfigurationBroker().getStringValue("ldap.host");
		if (StringUtils.isEmpty(host)) {
			return false;
		}
		
		AuthenticationBroker authnBroker = BrokerFactory.getAuthenticationBroker();
		if (authnBroker instanceof MultiRealmAuthenticationBroker) {
			MultiRealmAuthenticationBroker multiAuthn = (MultiRealmAuthenticationBroker)authnBroker;
			AuthenticationBroker[] realBrokers =multiAuthn.getAuthenticationBrokers();
			for (int i = 0; i < realBrokers.length; i++) {
				if (realBrokers[i] instanceof LDAPAuthenticationBroker) {
					return true;
				}
			} 
		} else if (authnBroker instanceof LDAPAuthenticationBroker) {
			return true;
		}

		return false;

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		RequestDispatcher loginPage = request.getRequestDispatcher("login.jsp");
		
		// Check to see if the user hit "forgot password"
		if (request.getParameter("forgot.x") != null) {
			String username = request.getParameter("username");
			if ((username == null) || (username.length() == 0)) {
	    	    actionRequest.addParameter("authentication.message", "Please enter a user ID.");
				loginPage.forward(actionRequest, response);
				return;
			}

			User user = BrokerFactory.getAuthenticationBroker().getUserByIdentifier(username);
			String token = "";
			if (user == null) {
				// TODO: This is insecure, but it's good ease-of-use.  I need to review this to see if the
				// security considerations outweight ease of use.
				
				if (isLDAPEnabled()) {					
					actionRequest.addParameter("authentication.message", "Your user could not be found.  "+
							"If your account is stored on a corporate directory, please check "+
							"with your directory administrator.");
				} else {
					actionRequest.addParameter("authentication.message", "Your user could not be found.  Please check your ID.");
				}				
				loginPage.forward(actionRequest, response);
				return;
			} else {
				AuthenticationBroker authnBroker = BrokerFactory.getAuthenticationBroker();
				token = authnBroker.getPasswordChangeToken(user);
			}
			
			// Mail out the info
			sendResetEmail(user, token);
			
			// Return to the login page
			actionRequest.addParameter("authentication.message", "A notification has been sent to you with instructions on setting your password");
			loginPage.forward(actionRequest, response);
			return;
		}

		// Handle the login request
		String username = request.getParameter("username");
		String originatingAddress = request.getRemoteAddr();
		if ((username == null) || (username.length() == 0)) {
    	    actionRequest.addParameter("authentication.message", "Your password was not accepted.  Please try again.");
			loginPage.forward(actionRequest, response);
			BrokerFactory.getAuthenticationBroker().logAuthentication(false, username, null, originatingAddress, new Date());
			return;
		}

		String password = request.getParameter("password");
		if ((password == null) || (password.length() == 0)) {
    	    actionRequest.addParameter("authentication.message", "Your password was not accepted.  Please try again.");
			loginPage.forward(actionRequest, response);
			BrokerFactory.getAuthenticationBroker().logAuthentication(false, username, null, originatingAddress, new Date());
			return;
		}
		
		User user = BrokerFactory.getAuthenticationBroker().authenticate(username, password);
		if (user != null) {
			request.getSession().setAttribute("user", user.getUuid());
			
			BrokerFactory.getAuthenticationBroker().logAuthentication(true, username, user, originatingAddress, new Date());
			
			String referer = (String)request.getSession().getAttribute("referer");
			
			// If this is a managed (ie, hosted) user, just go tot he index page.  Or else,
			// we get into AJAX problems.
			if (BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.MANAGED)) {
				referer = "index.jsp";
			}
			if ((referer == null) || (referer.endsWith("login.jsp"))) referer = "index.jsp";
			response.sendRedirect(referer);
			return;
		} else {
			actionRequest.addParameter("authentication.message", "Your password was not accepted.  Please try again.");
			loginPage.forward(actionRequest, response);
			BrokerFactory.getAuthenticationBroker().logAuthentication(false, username, null, originatingAddress, new Date());
			return;
		}
	}
}