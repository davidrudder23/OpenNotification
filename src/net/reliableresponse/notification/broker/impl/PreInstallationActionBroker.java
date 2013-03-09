/*
 * Created on Nov 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import net.reliableresponse.notification.broker.ActionBroker;
import net.reliableresponse.notification.web.actions.Action;
import net.reliableresponse.notification.web.actions.InstallerAction;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PreInstallationActionBroker implements ActionBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.ActionBroker#getActionsForPage(java.lang.String)
	 */
	public Action[] getActionsForPage(String page) {
		Action[] actions = new Action[1];
		actions[0] = new InstallerAction();
		
		return actions;
	}

}
