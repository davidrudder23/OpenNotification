/*
 * Created on Apr 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.reports;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class IndividualDataSource implements JRDataSource {
	
	Member member;
	int index = 0;
	
	public IndividualDataSource(String uuid) {
		member = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		if (member == null)
			member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);
		
		// Retrieve the notifications and associated info
		if (member != null) {
			
		}
	}
	

	public Object getFieldValue(JRField field) throws JRException {
		if (member == null) {
			return null;
		}
		
		String name = field.getName();
		return name;
	}
	
	public boolean next() throws JRException {
		if (index < 5) {
			index++;
			return true;
		}

		return false;
	}
}
