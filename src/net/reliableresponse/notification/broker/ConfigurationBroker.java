/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import java.io.InputStream;
import java.util.List;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ConfigurationBroker {

	/**
	 * Gets a String from the configuration
	 * 
	 * @param key The name of the value to get
	 * @return The value, as a String
	 */
	public String getStringValue(String key);
	
	/**
	 * Gets a String from the configuration
	 * 
	 * @param key The name of the value to get
	 * @param defaultValue The default value to return
	 * @return The value, as a String
	 */
	public String getStringValue(String key, String defaultValue);
	/**
	 * Gets a list of comma-separated values
	 * @param key
	 * @return
	 */
	public String[] getStringValues(String key);

	/**
	 * Gets a list of comma-separated values
	 * @param key
	 * @return
	 */
	public String[] getStringValues(String key, String[] defaultValue);

	/**
	 * Gets a list of comma-separated values
	 * @param key
	 * @return
	 */
	public List<String> getStringValues(String key, List<String> defaultValue);

	/**
	 * Gets an integer from the configuration
	 * 
	 * @param key The name of the value to get
	 * @return The value, as an integer, defaults to -1 if no value for that key found
	 */
	public int getIntValue (String key);
	
	/**
	 * Gets an integer from the configuration
	 * 
	 * @param key The name of the value to get
	 * @param defaultValue The default value to return
	 * @return The value, as an integer
	 */
	public int getIntValue (String key, int defaultValue);
	
	/**
	 * Gets a boolean from the configuration
	 * 
	 * @param key The name of the value to get
	 * @return The value, as a boolean
	 */
	public boolean getBooleanValue (String key);

	/**
	 * Gets a String from the configuration
	 * 
	 * @param key The name of the value to get
	 * @param defaultValue The default value to return
	 * @return The value, as a boolean
	 */
	public boolean getBooleanValue (String key, boolean defaultValue);
	
	
	/**
	 * Sets a value in the persisten configuration
	 * 
	 * @param key The name of the value to set
	 * @param value The value to set
	 */
	public void setStringValue(String key, String value);

	/**
	 * Sets a value in the persisten configuration
	 * 
	 * @param key The name of the value to set
	 * @param value The value to set
	 */
	public void setStringValues(String key, String[] values);
	
	/**
	 * Sets a value in the persisten configuration
	 * 
	 * @param key The name of the value to set
	 * @param value The value to set
	 */
	public void setIntValue (String key, int value);
	
	/**
	 * Sets a value in the persisten configuration
	 * 
	 * @param key The name of the value to set
	 * @param value The value to set
	 */
	public void setBooleanValue (String key, boolean value);
	
	
	/**
	 * This sets a *volatile*, *non-persistent* value in the in-memory store
	 * @param key
	 * @param value
	 */
	public void addTemporaryStringValue (String key, String value);
	
	/**
	 * This sets a *volatile*, *non-persistent* value in the in-memory store
	 * @param key
	 * @param value
	 */
	public void addTemporaryBooleanValue (String key, boolean value);

	/**
	 * 
	 * @return All the names of the parameters
	 */
	public String[] getParameterNames();
	
	/**
	 * 
	 * @param substring The substring to search for
	 * @return All the parameter names that contain the substring
	 */
	public String[] getParameterNames(String substring);
	
	public long getLastLoaded();
	/**
	 * This is a boot-strap function that allows the configuration
	 * broker to set the configuration manually.
	 * @param in
	 * @return
	 */
	public void setConfiguration (InputStream in);
}
