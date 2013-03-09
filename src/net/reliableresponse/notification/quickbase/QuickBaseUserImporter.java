/*
 * Created on Jul 31, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.quickbase;

import java.io.FileInputStream;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;

import com.intuit.quickbase.util.QuickBaseClient;
import com.intuit.quickbase.util.QuickBaseException;

public class QuickBaseUserImporter {
		
	private String username=null;
	private String password=null;
	private String dbid=null;
	private String query=null;
	private String firstnameField=null;
	private String lastnameField=null;
	private String emailField=null;
	private String phoneNumberField=null;
	private String cellNumberField=null;
	private String cellProviderField=null;
	private String aimField=null;
	private String msnField=null;
	private String sametimeField=null;
	private String yahooIMField=null;
	private String jabberNameField=null;
	private String jabberServerField=null;

	
	public QuickBaseUserImporter() {
	}
	
	public void doQuery() {
		QuickBaseClient qb = new QuickBaseClient(username, password);
		try {
			String clist = createClist();
			BrokerFactory.getLoggingBroker().logDebug("Running query against db: "+dbid+"\nclist: "+clist);
			Vector<Object> results = qb.doQuery(dbid, query, clist, "", "");
			BrokerFactory.getLoggingBroker().logDebug("We got "+results.size()+" results");
		} catch (QuickBaseException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	private void addField (StringBuffer string, String field) {
		if (field != null) {
			if (string.length()>0) {
				string.append(".");			
			}
			string.append (field);
		}
	}
	
	public String createClist() {
		StringBuffer clist = new StringBuffer();
		addField(clist, firstnameField);
		addField(clist, lastnameField);
		addField(clist, emailField);
		addField(clist, phoneNumberField);
		addField(clist, cellNumberField);
		addField(clist, cellProviderField);
		addField(clist, aimField);
		addField(clist, msnField);
		addField(clist, sametimeField);
		addField(clist, yahooIMField);
		addField(clist, jabberNameField);
		addField(clist, jabberServerField);
		
		return clist.toString();
	}
	
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbid() {
		return dbid;
	}


	public void setDbid(String dbid) {
		this.dbid = dbid;
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public String getFirstnameField() {
		return firstnameField;
	}


	public void setFirstnameField(String firstnameField) {
		this.firstnameField = firstnameField;
	}


	public String getLastnameField() {
		return lastnameField;
	}


	public void setLastnameField(String lastnameField) {
		this.lastnameField = lastnameField;
	}


	public String getEmailField() {
		return emailField;
	}


	public void setEmailField(String emailField) {
		this.emailField = emailField;
	}


	public String getPhoneNumberField() {
		return phoneNumberField;
	}


	public void setPhoneNumberField(String phoneNumberField) {
		this.phoneNumberField = phoneNumberField;
	}


	public String getCellNumberField() {
		return cellNumberField;
	}


	public void setCellNumberField(String cellNumberField) {
		this.cellNumberField = cellNumberField;
	}


	public String getCellProviderField() {
		return cellProviderField;
	}


	public void setCellProviderField(String cellProviderField) {
		this.cellProviderField = cellProviderField;
	}


	public String getAimField() {
		return aimField;
	}


	public void setAimField(String aimField) {
		this.aimField = aimField;
	}
	public String getJabberNameField() {
		return jabberNameField;
	}

	public void setJabberNameField(String jabberNameField) {
		this.jabberNameField = jabberNameField;
	}

	public String getJabberServerField() {
		return jabberServerField;
	}

	public void setJabberServerField(String jabberServerField) {
		this.jabberServerField = jabberServerField;
	}

	public String getMsnField() {
		return msnField;
	}


	public void setMsnField(String msnField) {
		this.msnField = msnField;
	}


	public String getSametimeField() {
		return sametimeField;
	}


	public void setSametimeField(String sametimeField) {
		this.sametimeField = sametimeField;
	}


	public String getYahooIMField() {
		return yahooIMField;
	}


	public void setYahooIMField(String yahooIMField) {
		this.yahooIMField = yahooIMField;
	}
	
	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(new FileInputStream("conf/reliable.properties"));
		QuickBaseUserImporter importer = new QuickBaseUserImporter();
		importer.setUsername("");
		importer.setPassword("");
		importer.setDbid("bcned3374");
		importer.setQuery("{'3'.GTE.'0'}");
		
		importer.setEmailField("3");
		importer.setMsnField("4");
		importer.setCellNumberField("5");
		importer.setCellProviderField("6");
		
		System.out.println (importer.createClist());
		importer.doQuery();
	}

}
