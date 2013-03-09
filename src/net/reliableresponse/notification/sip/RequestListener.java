/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import javax.sip.RequestEvent;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface RequestListener {

	public void handleRequest(RequestEvent requestEvent);
}
