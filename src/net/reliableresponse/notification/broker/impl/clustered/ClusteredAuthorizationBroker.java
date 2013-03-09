/*
 * Created on Jan 17, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.clustered;

import java.util.Vector;

import net.reliableresponse.notification.broker.AuthorizationBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingAuthorizationBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class ClusteredAuthorizationBroker extends CachingAuthorizationBroker {
	public ClusteredAuthorizationBroker(AuthorizationBroker realBroker) {
		super(realBroker);
	}
	
	public void invalidateRole (String role) {
		roles.put (role, new Vector());
		notroles.put (role, new Vector());
	}
}
