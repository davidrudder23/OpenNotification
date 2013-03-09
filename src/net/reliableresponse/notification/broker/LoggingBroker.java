/*
 * Created on May 1, 2004
 *
 */
package net.reliableresponse.notification.broker;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface LoggingBroker {
	
	/**
	 * This is used to log a discreet action, like a notification was sent,
	 * a user was added, or a group was modified.  These should be 
	 * significant actions that can be used to track the state of 
	 * the application  
	 * 
	 * @param message The action to log
	 */
	public void logAction (String message);

	// These are standard Log4J-esq logging functions
	public void logDebug (String message);
	
	public void logInfo (String message);
	
	public void logWarn (String message);
	
	public void logWarn (Error error);
	
	public void logWarn (Exception exception);

	public void logError (String message);

	public void logError (Error error);
	
	public void logError (Exception exception);
	
	/**
	 * This function causes the broker to re-read it's configuration
	 *
	 */
	public void reset();
}
