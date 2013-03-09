/*
 * Created on Feb 14, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.dialogic;

import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.threebit.jvr.dx;
import net.threebit.jvr.jvr;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class DialogicTest {

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		int dxHandle;
		dxHandle = dx.open("dxxxB1C1", 0);
		boolean on = true;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("off")) {
				on = false;
			}
		}
		if (on) {
			BrokerFactory.getLoggingBroker().logDebug("Setting on hook");
			dx.sethook(dxHandle, jvr.DX_ONHOOK, dx.EV_SYNC);
		} else {
			BrokerFactory.getLoggingBroker().logDebug("Setting off hook");
			dx.sethook(dxHandle, jvr.DX_OFFHOOK, dx.EV_SYNC);
		}
			
		dx.close(dxHandle);
	}
}
