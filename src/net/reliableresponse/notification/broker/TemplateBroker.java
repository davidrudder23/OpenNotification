/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.template.Template;

public interface TemplateBroker {
	public Template[] getAllTemplates();
	public String[] getAllTemplateUuids();
	
	public Template getTemplateByUuid(String uuid);
	
	public void addTemplate (String templateClassName, String recipientType, String senderType);
}
