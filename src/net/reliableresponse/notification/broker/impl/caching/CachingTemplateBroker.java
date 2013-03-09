/*
 * Created on May 1, 2004
 *
 * Copyright 2004 - David Rudder
 */

package net.reliableresponse.notification.broker.impl.caching;

import java.util.Vector;

import net.reliableresponse.notification.broker.TemplateBroker;
import net.reliableresponse.notification.template.Template;


/**
 * @author drig
 * 
 * This is a simple in-memory broker, mostly used for testing
 */
public class CachingTemplateBroker implements TemplateBroker {

	protected Cache templates;
	
	protected TemplateBroker realBroker;
	
	public CachingTemplateBroker(TemplateBroker realBroker) {
		templates = new Cache(1200, 36000, Cache.METHOD_FIFO);
		this.realBroker = realBroker;
	}
	
	public Cache getCache() {
		return templates;
	}
	
	public Template[] getAllTemplates() {
		String[] uuids = getAllTemplateUuids();
		Vector<Template> templates = new Vector<Template>();
		for (int i = 0; i < uuids.length; i++) {
			Template template = getTemplateByUuid(uuids[i]);
			if (template != null) {
				templates.add(template);
			}
		}
		return templates.toArray(new Template[0]);
	}

	

	public Template getTemplateByUuid(String uuid) {
		Template template = (Template)templates.getByUuid(uuid);
		if (template != null) {
			return template;
		}
		return realBroker.getTemplateByUuid(uuid);
	}

	public String[] getAllTemplateUuids() {
		return realBroker.getAllTemplateUuids();
	}

	public void addTemplate(String templateClassName, String recipientType,
			String senderType) {
		realBroker.addTemplate(templateClassName, recipientType, senderType);
		
	}

}