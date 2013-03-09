/*
 * Created on Dec 8, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.broker.impl.caching;

public interface Cacheable {

	/**
	 * Causes the object to reload the object's persistent values
	 *
	 */
	public void refreshObject(Cacheable object) throws CacheException;
}
