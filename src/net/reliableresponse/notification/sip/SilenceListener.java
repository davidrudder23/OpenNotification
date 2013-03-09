/*
 * Created on Aug 29, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface SilenceListener {

	public void handleSilenceStart();
	public void handleSilenceEnd();
}
