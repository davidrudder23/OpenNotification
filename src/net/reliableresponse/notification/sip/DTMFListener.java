/*
 * Created on Aug 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface DTMFListener {
	
	public void handleDTMF (String digit);

}
