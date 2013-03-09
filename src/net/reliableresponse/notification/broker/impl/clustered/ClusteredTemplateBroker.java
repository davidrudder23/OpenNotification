/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.broker.impl.clustered;

import net.reliableresponse.notification.broker.TemplateBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingTemplateBroker;

public class ClusteredTemplateBroker extends CachingTemplateBroker {

	public ClusteredTemplateBroker(TemplateBroker realBroker) {
		super (realBroker);		
	}
}
