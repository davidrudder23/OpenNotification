/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.template;

public class TemplateValue {
	
	String value;
	String defaultValue;

	public TemplateValue() {
		value = null;
		defaultValue = null;
	}

	public TemplateValue(String value, String defaultValue) {
		setValue(value);
		setDefaultValue(defaultValue);
	}

	public String getValue() {
		if (value == null) {
			return getDefaultValue();
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	
}
