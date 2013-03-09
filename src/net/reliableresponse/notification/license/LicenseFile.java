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
	private boolean hasValidFile;
	
	private int maxUsers;
	
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
		hasValidFile = false;
		
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		
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
		if (in == null) {
			return false;
		}
		try {
			if (in.available() <= 0 ) return false;
		} catch (IOException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
			return false;
		}
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			factory.setValidating(false);
			Document document = builder.parse(new InputSource(in));
			
			Element licenseElement = (Element)document.getElementsByTagName("reliableresponse-License").item(0);
			Element licenseBody = (Element)licenseElement.getElementsByTagName("reliableresponse-LicenseBody").item(0);
			Element validityDates = (Element)licenseBody.getElementsByTagName("validityDates").item(0);
			validFrom = new Date(Long.parseLong(validityDates.getAttribute("from")));
			BrokerFactory.getLoggingBroker().logDebug("validfrom="+validFrom);
			validTo = new Date(Long.parseLong(validityDates.getAttribute("to")));
			BrokerFactory.getLoggingBroker().logDebug("validto="+validTo);
			
			Element productInfo = (Element)licenseBody.getElementsByTagName("productInfo").item(0);
			installClass = productInfo.getAttribute("installClass");
			
			// Read the digest
			Element digestElement = (Element)licenseElement.getElementsByTagName("reliableresponse-LicenseDigest").item(0);
			String algorithm = digestElement.getAttribute("digest-algorithm");
			BrokerFactory.getLoggingBroker().logDebug("algorithm="+algorithm);
			String digestValue = digestElement.getAttribute("digest-value");
			
			String maxUsersString = productInfo.getAttribute("maxUsers");
			if (maxUsersString != null) {
				try {
					maxUsers = Integer.parseInt (maxUsersString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logWarn("Could not parse max users in license: "+maxUsersString);
				}
			}
			// Check the signature
			OutputFormat format = new OutputFormat(document); //Serialize DOM
			format.setOmitXMLDeclaration(false);
			//format.setDoctype(null, "http://www.reliableresponse.net/license.dtd");
			StringWriter bodyBuffer = new StringWriter();
			XMLSerializer serial = new XMLSerializer(bodyBuffer, format);
			serial.asDOMSerializer(); // As a DOM Serializer
			serial.serialize(licenseBody);
			String bodyString = bodyBuffer.toString();
			BrokerFactory.getLoggingBroker().logDebug("Inner XML = "+bodyString);
			
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update (bodyString.getBytes());
			digest.update (password.getBytes());
			String confirmValue = net.reliableresponse.notification.util.Base64.byteArrayToBase64(digest.digest());
			
			if (!confirmValue.equals(digestValue)) {
				BrokerFactory.getLoggingBroker().logDebug("License file digest value mismatch");
				BrokerFactory.getLoggingBroker().logDebug("License file digest in xml   : "+digestValue);
				BrokerFactory.getLoggingBroker().logDebug("License file digest generated: "+confirmValue);
				return false;
			}
			hasValidFile = true;
			return isValid();
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (FactoryConfigurationError e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ParserConfigurationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SAXException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (NoSuchAlgorithmException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		return false;
	}
	
	/**
	 * Saves a pre-generated license file
	 */
	
	public void save (String licenseFile) throws Exception {
		if (!read(new StringBufferInputStream(licenseFile), "Reliable Response License kcjnsdk")) {
			throw new Exception ("Invalid license file");
		}
		
		URL resourceURL = new String().getClass().getResource("/conf/license.xml");
		if (resourceURL == null) {
			// Find it by hacking the Tomcat path 
			BrokerFactory.getLoggingBroker().logDebug("No resource URL for license file, using Tomcat hack");
			String outputDir = BrokerFactory.getConfigurationBroker().getStringValue("tomcat.location", "/opt/tomcat5");
			outputDir += "/webapps/notification/conf/license.xml";

			FileOutputStream out = new FileOutputStream(outputDir);
			out.write (licenseFile.getBytes());
			out.close();
		} else {
			// We have a resourceURL, so use that
			BrokerFactory.getLoggingBroker().logDebug("Resource URL="+resourceURL);
			BrokerFactory.getLoggingBroker().logDebug("Resource File="+resourceURL.getFile());
			// TODO
		}
	}
	
	/**
	 * Creates a new LicenseFile
	 *
	 */
	public String write (String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			Document doc =DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("reliableresponse-License");
			// Create Root Element
			root.setAttribute("licenseVersion", "1.0");
			Element body = doc.createElement("reliableresponse-LicenseBody");
			Element validityDates = doc.createElement("validityDates");
			validityDates.setAttribute("from", getValidFrom().getTime()+"");
			validityDates.setAttribute("to", getValidTo().getTime()+"");
			body.appendChild(validityDates);

			Element productInfo = doc.createElement("productInfo");
			productInfo.setAttribute("version", "1.0");
			productInfo.setAttribute("installClass", installClass);
			productInfo.setAttribute("maxUsers", maxUsers+"");
			body.appendChild(productInfo);

			root.appendChild(body);
			
			// Get the body as text
			OutputFormat format = new OutputFormat(doc); //Serialize DOM
			format.setOmitXMLDeclaration(false);
			//format.setDoctype(null, "http://www.reliableresponse.net/license.dtd");
			StringWriter bodyBuffer = new StringWriter();
			XMLSerializer serial = new XMLSerializer(bodyBuffer, format);
			serial.asDOMSerializer(); // As a DOM Serializer
			serial.serialize(body);
			String bodyString = bodyBuffer.toString();
			BrokerFactory.getLoggingBroker().logDebug("Inner XML = "+bodyString);
			
			// Get the digest
			digest.update (bodyString.getBytes());
			digest.update (password.getBytes());
			
			Element signatureElement = doc.createElement("reliableresponse-LicenseDigest");

			signatureElement.setAttribute("digest-algorithm", digest.getAlgorithm());
			signatureElement.setAttribute("digest-value", new sun.misc.BASE64Encoder().encode(digest.digest()));
			root.appendChild(signatureElement);
			doc.appendChild(root);
			
			format = new OutputFormat(doc); //Serialize DOM
			format.setOmitXMLDeclaration(true);
			//format.setDoctype(null, "http://www.reliableresponse.net/license.dtd");
			StringWriter docBuffer = new StringWriter();
			serial = new XMLSerializer(docBuffer, format);
			serial.asDOMSerializer(); // As a DOM Serializer
			serial.serialize(doc.getDocumentElement());
			String docString = docBuffer.toString();
			
			BrokerFactory.getLoggingBroker().logDebug("XML = "+docString);
			
			return docString;
		} catch (NoSuchAlgorithmException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (DOMException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;
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
		if (installClass == null) installClass ="Unspecified";
		return installClass;
	}
	public void setInstallClass(String installClass) {
		this.installClass = installClass;
	}
	
	public boolean isValid() {
		if (!hasValidFile) return false;
		if (installClass.equalsIgnoreCase("enterprise")) {
			return true;
		}
		
		long now = System.currentTimeMillis();
		long to = validTo.getTime();
		long from = validFrom.getTime();
		return ((now >= from) && (now <= to));
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
