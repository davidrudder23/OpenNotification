/*
 * Created on May 13, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.meterware.httpunit.Base64;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class LicenseFile {
	
	private Date validFrom;
	private Date validTo;
	private String installClass;
	
	private int maxUsers = 0;
	
	private static LicenseFile instance = null;
	private SimpleDateFormat dateFormat;
	
	public static LicenseFile getInstance() {
		if (instance == null) {
			BrokerFactory.getLoggingBroker().logDebug("Initializing License File");
			instance = new LicenseFile();
		}
		
		return instance;
	}
	
	public LicenseFile () {
		maxUsers = -1;
	}
	
	public boolean read (String filename, String password) {
		return read (new File (filename), password);
	}

	public boolean read(File file, String password) {
		try {
			return read(new FileInputStream(file), password);
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return false;
	}
	
	public boolean read(InputStream in , String password) {
		return true;
	}
	
	/**
	 * Saves a pre-generated license file
	 */
	
	public void save (String licenseFile) throws Exception {

	}
	
	/**
	 * Creates a new LicenseFile
	 *
	 */
	public String write (String password) {
		return "";
	}
	
	

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public Date getValidFrom() {
		return validFrom;
	}
	
	public void setValidFrom(Date date) {
		this.validFrom = date;
	}
	
	public String getFormattedDate (Date date) {
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		return dateFormat.format(date);
		
	}
	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date date) {
		this.validTo = date;
	}
	
	public int getMaxUsers() {
		return maxUsers;
		//return 5;
	}

	public String getInstallClass() {
		return "Enterprise";
		//if (installClass == null) installClass ="Unspecified";
		//return installClass;
	}
	public void setInstallClass(String installClass) {
		this.installClass = installClass;
	}
	
	public boolean isValid() {
		return true;
	}
	
	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		LicenseFile license = new LicenseFile();
		license.setInstallClass("Demo");
		license.setValidFrom(new Date(System.currentTimeMillis()-10000000));
		long to = 1000; // milliseconds
		to *= 60; //seconds
		to *= 60; // minutes
		to *= 24; // hours 
		to *= 35; // days
		to += System.currentTimeMillis();
		license.setValidTo(new Date(to));
		
		String output = license.write("Reliable Response License kcjnsdk");
		FileOutputStream out = new FileOutputStream("/tmp/license.xml");
		out.write (output.getBytes());
		out.flush();
		out.close();
		
		boolean confirmed = license.read(new FileInputStream("/tmp/license.xml"), "Reliable Response License kcjnsdk");
		BrokerFactory.getLoggingBroker().logInfo("License File did"+(confirmed?"":" not")+" validate");
	}
}
