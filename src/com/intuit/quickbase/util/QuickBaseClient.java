//QuickBase Client API
//version 1.16 relocated from $cvsroot/quickbase to $cvsroot/platform, 8/29/2002.
// $Id: QuickBaseClient.java,v 1.15 2006/10/17 12:18:04 cvonroes Exp $
// $Source: /v/releng/cvsroot/platform/java/com/intuit/quickbase/util/QuickBaseClient.java,v $

package com.intuit.quickbase.util;

import java.io.*;
import java.util.*;
import com.intuit.util.HTTPConnection;

// XML classes
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.CDATASection;


/**
 * <p>QuickBaseClient class is a wrapper for a substantial portion of 
 * the <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
   QuickBase HTTP API</a>.
 * 
 * <p>See the main() method for examples of using this class.<p>
 *
 * You'll need JDK 1.4 or higher or
 * if you're using JDK 1.3 or lower then you'll need:<ul>
 * <li>JAXP any version
 * <li>JSSE 1.0.1 or higher
 * </ul>
 * 
 * <p>Copyright (C) 2001-2003 Intuit Inc. All Rights Reserved.  
 * @author       claude_von_roesgen@intuit.com
 * @version      $Revision: 1.15 $
 */
  
public class QuickBaseClient
{
private Vector v_QuickBaseCookies=new Vector();
private String QuickBaseUrlPrefix = null;
private String QDBusername="";
private String QDBpassword="";
private String QDBerrorcode="";
private String QDBerrortext="";
private String QDBticket="";
private String QDBappToken="";
private String proxyHost = null;
private int    proxyPort = 0;
protected String httpConnectionClass = "com.intuit.util.HTTPConnection";
protected int timeout = 180000;


/**
     * Set the username and password for subsequent calls to the QuickBase HTTP API.
     * Please refer to the
     * <a href="http://www.quickbase.com/">
     * QuickBase home page</a> for more information on how to get a QuickBase account.   
     * Also sets the protocol (HTTP or HTTPS) for all requests to QuickBase.
     * Note: Sun's JSSE Java SSL implementation is required to use this class unless you 
     * call the constructor with an HTTP URL.
     * QuickBase databases are by default not accessible via HTTP. To allow HTTP access to a
     * QuickBase database go to its main page and click on "Administration" under "SHORTCUTS".
     * Then click on "Basic Properties". Next to "Options" you'll see a checkbox labeled 
     * "Allow non-SSL access (normally unchecked)". You'll need to check this box to allow HTTP 
     * access to the database. 
     *
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information.
     * @param username Either a QuickBase screen name or email address of a registered QuickBase user.
     * @param password The QuickBase password corresponding to the QuickBase username.
     * @param URL The URL prefix for all QuickBase calls. The default is "https://www.quickbase.com/db/".
     * 
     */
public QuickBaseClient(String username, String password, String URL)
{
  QuickBaseUrlPrefix=URL;
  QDBusername = username;
  QDBpassword = password;
}

/**
     * Set the username and password for subsequent calls to the QuickBase HTTP API.
     * Please refer to the
     * <a href="http://www.quickbase.com/">
     * QuickBase home page</a> for more information on how to get a QuickBase account.   
     * Also sets the protocol (HTTP or HTTPS) for all requests to QuickBase.
     * Note: Sun's JSSE Java SSL implementation is required to use this class unless you 
     * call the constructor with the HTTPS parameter set to false.
     * QuickBase databases are by default not accessible via HTTP. To allow HTTP access to a
     * QuickBase database go to its main page and click on "Administration" under "SHORTCUTS".
     * Then click on "Basic Properties". Next to "Options" you'll see a checkbox labeled 
     * "Allow non-SSL access (normally unchecked)". You'll need to check this box to allow HTTP 
     * access to the database. 
     *
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information.
     * @param username Either a QuickBase screen name or email address of a registered QuickBase user.
     * @param password The QuickBase password corresponding to the QuickBase username.
     * @param HTTPS True for HTTPS and false for HTTP.
     * 
     */
public QuickBaseClient(String username, String password, boolean HTTPS)
{
  if (HTTPS){
    QuickBaseUrlPrefix="https://www.quickbase.com/db/";
  }else{
    QuickBaseUrlPrefix="http://www.quickbase.com/db/";
  }
  QDBusername = username;
  QDBpassword = password;
}


/**
     * Set the username and password for subsequent calls to the QuickBase HTTP API.
     * Please refer to the
     * <a href="http://www.quickbase.com/">
     * QuickBase home page</a> for more information on how to get a QuickBase account.   
     * Note: Sun's JSSE Java SSL implementation is required to use this class.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information.
     * @param username Either a QuickBase screen name or email address of a registered QuickBase user.
     * @param password The QuickBase password corresponding to the QuickBase username.
     * 
     */

public QuickBaseClient(String username, String password)
    {
     this(username, password,"https://www.quickbase.com/db/");
    }
/**
     * Resets the username and password for subsequent calls to the QuickBase HTTP API.
     * Please refer to the
     * <a href="http://www.quickbase.com/">
     * QuickBase home page</a> for more information on how to get a QuickBase account.   
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information.
     * @param username Either a QuickBase screen name or email address of a registered QuickBase user.
     * @param password The QuickBase password corresponding to the QuickBase username.
     * 
     */
public void changeCredentials (String username, String password){

  QDBusername = username;
  QDBpassword = password;
  QDBticket = "";
  return;
}

/**
 * Set name of class to use for HTTPConnections.
 * Should be a fully qualified classname that is assignable
 * to type <code>com.intuit.util.HTTPConnection</code>.
 */
public void setHttpConnectionClass(String classname){
  httpConnectionClass = classname;
}

/**
 * Set the timeout on connections.  CAUTION: do not mess with this 
 * except under special circumstances.  i.e. you know 
 * with absolute certainty that an HTTP server will NEVER be slower
 * than the timeout and succeed in the response.  The deault 
 * 180000 ms (3 minutes), which is what most browsers use.
 */
public void setTimeout(int ms){
  timeout = ms;
}

/**
 * Sets the proxy hostname and port number for subsequent calls to the QuickBase HTTP API.
 * @param host The host name of the proxy server to use for API connections.
 * @param port The port number on the proxy host to use for API connections.
 * 
 */
public void setProxy (String host, int port){
  proxyHost = host;
  proxyPort = port;
  return;
}


/**
 * Adds the given qdbParameter/qdbValue pair to the qdbRequest XML document
 * to be passed as input to a QuickBase HTTP API call.
 * @param qdbRequest   The XML document containing the QuickBase API parameters.
 * @param qdbParameter The name of the API parameter to be added to the request.
 * @param qdbValue     The value of the API parameter to be added to the request.
 * 
 */
public void addRequestParameter(Document qdbRequest, String qdbParameter, String qdbValue){
  Element rootElement = qdbRequest.getDocumentElement();
  Element qdbapiElement = null;
  /* Don't assume that caller created the root element */
  if (rootElement == null){
    qdbapiElement = qdbRequest.createElement("qdbapi");
    qdbRequest.appendChild(qdbapiElement);
    }
  else{
    qdbapiElement = rootElement;
    }
  Element qdbapiParameter = qdbRequest.createElement(qdbParameter);
  Text qdbapiText = qdbRequest.createTextNode(qdbValue);
  qdbapiParameter.appendChild(qdbapiText);
  qdbapiElement.appendChild(qdbapiParameter);
}

/**
 * Adds the given qdbParameter/qdbValue pair with the given attName/attValue to
 * the qdbRequest XML document to be passed as input to a QuickBase HTTP API call.
 * @param qdbRequest   The XML document containing the QuickBase API parameters.
 * @param qdbParameter The name of the API parameter to be added to the request.
 * @param qdbValue     The value of the API parameter to be added to the request.
 * @param attName      The name of the XML attribute to add to this API parameter.
 * @param attValue     The value of the XML attribute to add to this API parameter.
 * 
 */
public void addRequestParameter(Document qdbRequest, String qdbParameter,
                                String qdbValue, String attName, String attValue){
  Element rootElement = qdbRequest.getDocumentElement();
  Element qdbapiElement = null;
  if (rootElement == null){
    qdbapiElement = qdbRequest.createElement("qdbapi");
    qdbRequest.appendChild(qdbapiElement);
    }
  else{
    qdbapiElement = rootElement;
    }
  Element qdbapiParameter = qdbRequest.createElement(qdbParameter);
  qdbapiParameter.setAttribute(attName, attValue);
  Text qdbapiText = qdbRequest.createTextNode(qdbValue);
  qdbapiParameter.appendChild(qdbapiText);
  qdbapiElement.appendChild(qdbapiParameter);
}

/**
 * Adds the given qdbParameter/qdbValue pair with the given attName/attValue to
 * the qdbRequest XML document to be passed as input to a QuickBase HTTP API call.
 * @param qdbRequest   The XML document containing the QuickBase API parameters.
 * @param qdbParameter The name of the API parameter to be added to the request.
 * @param qdbValue     The value of the API parameter to be added to the request.
 * @param attName      The name of the XML attribute to add to this API parameter.
 * @param attValue     The value of the XML attribute to add to this API parameter.
 *  @param filename The filename of the file attachment
 * 
 */
public void addRequestParameter(Document qdbRequest, String qdbParameter,
        String qdbValue, String attName, String attValue, String filename){
Element rootElement = qdbRequest.getDocumentElement();
Element qdbapiElement = null;
if (rootElement == null){
qdbapiElement = qdbRequest.createElement("qdbapi");
qdbRequest.appendChild(qdbapiElement);
}
else{
qdbapiElement = rootElement;
}
Element qdbapiParameter = qdbRequest.createElement(qdbParameter);
qdbapiParameter.setAttribute(attName, attValue);
qdbapiParameter.setAttribute("filename", filename);
Text qdbapiText = qdbRequest.createTextNode(qdbValue);
qdbapiParameter.appendChild(qdbapiText);
qdbapiElement.appendChild(qdbapiParameter);
}

/**
 * Adds the given qdbCSV string to the qdbRequest XML document
 * to be passed as input to a QuickBase HTTP API call.
 * @param qdbRequest   The XML document containing the QuickBase API parameters.
 * @param qdbCSV     The string containing the csv data.
 * 
 */
public void addCSVCdata(Document qdbRequest, String qdbCSV){
  Element rootElement = qdbRequest.getDocumentElement();
  Element qdbapiElement = null;
  /* Don't assume that caller created the root element */
  if (rootElement == null){
    qdbapiElement = qdbRequest.createElement("qdbapi");
    qdbRequest.appendChild(qdbapiElement);
    }
  else{
    qdbapiElement = rootElement;
    }
  Element qdbapiRecordsCSV = qdbRequest.createElement("records_csv");
  CDATASection  qdbapiCDATA = qdbRequest.createCDATASection(qdbCSV);
  qdbapiRecordsCSV.appendChild(qdbapiCDATA);
  qdbapiElement.appendChild(qdbapiRecordsCSV);
}

private Document xmlFromString (String XmlResponse) throws java.io.IOException, org.xml.sax.SAXException{
  InputSource quickbaseXMLstream;
  Document qdbResponse = null;
  StringReader qdbsr = new StringReader(XmlResponse);
  quickbaseXMLstream = new InputSource(qdbsr);
  qdbResponse = createXmlDocument (quickbaseXMLstream, false); // false = don't validate 
  return qdbResponse;
}

private NodeList getNodeList(Document xmlDoc, String select){
  String currentNodeName;
  Element el = xmlDoc.getDocumentElement();
  NodeList nl = null;
  StringTokenizer st = new StringTokenizer(select, "/");
  while (st.hasMoreTokens()){
    currentNodeName = st.nextToken();
    if (el ==null){return null;}
    nl = el.getElementsByTagName(currentNodeName);
    el = (Element) nl.item(0);
    }
  return nl;
}

  /**
     * Retrieve the database tables accessible to the current user.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_GrantedDBs.
     *
     * @param withEmbeddedTables will allow tables that are children of an application from being returned.
     * @param excludeParents will exclude parent tables from being returned. Parent tables never have any fields or records in them. You need their dbid to clone an application.
     * @param adminOnly will only include applications that the user has administration access to.
     * @return a HashMap with table names as keys and database identifiers
     * as values.
     * 
     */
public HashMap grantedDBs (boolean withEmbeddedTables, boolean excludeParents, boolean adminOnly) throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
if(!withEmbeddedTables)
  {
  addRequestParameter(qdbRequest, "withEmbeddedTables", "0"); 
  }
if(excludeParents)
  {
  addRequestParameter(qdbRequest, "excludeParents", "1");
  }
if(adminOnly)
  {
  addRequestParameter(qdbRequest, "adminOnly", "1");
  }
Document qdbResponse = postApiXml ("main","API_GrantedDBs",qdbRequest);
NodeList tables = getNodeList(qdbResponse, "dbinfo");
if (tables == null){return null;}
HashMap result = new HashMap();
  for (int tableCounter = 0; tableCounter < tables.getLength(); tableCounter++){
    Element elTable = (Element)tables.item(tableCounter);
    String dbname = elTable.getElementsByTagName("dbname").item(0).getChildNodes().item(0).getNodeValue();
    String dbid = elTable.getElementsByTagName("dbid").item(0).getChildNodes().item(0).getNodeValue();
    result.put(dbname, dbid);
  }
return result;
}



  /**
     * Retrieve the database id associated with the database name.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_FindDbByName.
     *
     * @param Name the complete name of a QuickBase database.
     * @return the QuickBase database ID
     * 
     */
public String findDbByName (String Name) throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
addRequestParameter(qdbRequest, "dbname", Name);
Document qdbResponse = postApiXml ("main","API_FindDbByName",qdbRequest);
return getNodeList(qdbResponse, "dbid").item(0).getChildNodes().item(0).getNodeValue();
}


 /**
     * Retrieve the xml database schema associated with the database id.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_GetSchema.
     *
     * @param dbid the QuickBase database identifier.
     * @return the QuickBase database schema as an XML document
     * 
     */
public Document getSchema (String dbid) throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
Document qdbResponse = postApiXml (dbid, "API_GetSchema", qdbRequest);
return qdbResponse;
}

public String getOneTimeTicket () throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
Document qdbResponse = postApiXml ("main", "API_GetOneTimeTicket", qdbRequest);
return getNodeList(qdbResponse, "ticket").item(0).getChildNodes().item(0).getNodeValue();
}

/**
     * Retrieve the HTML of a single database record.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_GetRecordAsHTML.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param rid The unique identifier of a QuickBase record. 
     * @return Two column HTML table of field names and field values.
     * 
     */

public String getRecordAsHTML(String dbid, String rid) throws IOException, Exception
{
Document qdbRequest = newXmlDocument();
addRequestParameter(qdbRequest, "rid", rid);
return postApi (dbid,"API_GetRecordAsHTML",qdbRequest);
}
     /**
     * Retrieve HTML representation of zero or more QuickBase records.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_GenResultsTable.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param query A QuickBase query that selects zero or more records. 
     * @param clist A period delimited list of field identifiers defining which fields to output. 
     * @param slist A period delimited list of field identifiers defining which fields to sort on. 
     * @param options A string indicating ascending vs descending sort order and data output format. 
     * @return  HTML table of field names across the top and one row for each record.
     * 
     */

public String getHTMLTable (String dbid, String query, String clist, String slist, String options) throws IOException, Exception
{
  if (query == null || query.length() == 0){ return "Please supply a query";}
  String tablePage;
  Document qdbRequest = newXmlDocument();
  addRequestParameter(qdbRequest, "options", options);
  addRequestParameter(qdbRequest, "clist", clist);
  addRequestParameter(qdbRequest, "slist", slist);
  
  if (query.startsWith("{") & query.endsWith("}")){
     addRequestParameter(qdbRequest, "query", query);
  }else{
    if (String.valueOf(Integer.parseInt(query)).equals(query)){
      addRequestParameter(qdbRequest, "qid", query);
    }else{
      addRequestParameter(qdbRequest, "qname", query);
    }
  }
  return postApi(dbid, "API_GenResultsTable", qdbRequest);
}
     /**
     * Retrieve the number of records in a particular QuickBase database.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_GetNumRecords.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @return  The number of records in the QuickBase database.
     * 
     */

public String getNumRecords(String dbid)throws QuickBaseException, Exception
{
  Document qdbRequest = newXmlDocument();
  Document qdbResponse = postApiXml(dbid, "API_GetNumRecords",qdbRequest);
  try{
      return getNodeList(qdbResponse, "num_records").item(0).getChildNodes().item(0).getNodeValue();
    }catch(Exception e){
      throw new NullPointerException("getNumRecords: "+QDBerrortext);
    }
  }

public Document doStructuredQuery(String dbid, String query, String clist,
		String slist, String options) throws QuickBaseException, Exception {

	Document qdbRequest = newXmlDocument();
	addRequestParameter(qdbRequest, "options", options);
	addRequestParameter(qdbRequest, "clist", clist);
	addRequestParameter(qdbRequest, "slist", slist);
	addRequestParameter(qdbRequest, "fmt", "structured");
	if (query.startsWith("{") && query.endsWith("}")) {
		addRequestParameter(qdbRequest, "query", query);
	} else {
		if (String.valueOf(Integer.parseInt(query)).equals(query)) {
			addRequestParameter(qdbRequest, "qid", query);
		} else {
			addRequestParameter(qdbRequest, "qname", query);
		}
	}
	Document qdbResponse = postApiXml(dbid, "API_DoQuery", qdbRequest);
	return qdbResponse;
}

     /**
     * Retrieve Vector of zero or more QuickBase records.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_DoQuery.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param query A QuickBase query that selects zero or more records. 
     * @param clist A period delimited list of field identifiers defining which fields to output. 
     * @param slist A period delimited list of field identifiers defining which fields to sort on. 
     * @param options A string indicating ascending vs descending sort order and data output format. 
     * @return  Vector of HashMaps, one HashMap per record, each HashMap contains pairs of field name and field value.
     * 
     */

public Vector doQuery (String dbid, String query, String clist, String slist, String options)throws QuickBaseException, Exception
  {
  Vector vResult = new Vector();
  String content;
  String field;
  Vector vlabels = new Vector();
  String fieldvalue;
  int counter = 0;
  int numfields;
  int i;
   
  String tablePage;
  Document qdbRequest = newXmlDocument();
  addRequestParameter(qdbRequest, "options", options);
  addRequestParameter(qdbRequest, "clist", clist);
  addRequestParameter(qdbRequest, "slist", slist);
  addRequestParameter(qdbRequest, "fmt", "structured"); 
  if (query.startsWith("{") && query.endsWith("}")){
     addRequestParameter(qdbRequest, "query", query);
  }else{
    if (String.valueOf(Integer.parseInt(query)).equals(query)){
      addRequestParameter(qdbRequest, "qid", query);
    }else{
      addRequestParameter(qdbRequest, "qname", query);
    }
  }

  Document qdbResponse = postApiXml(dbid, "API_DoQuery", qdbRequest);
  try{
  NodeList fields = getNodeList(qdbResponse, "table/fields/field");
  for (i = 0; i < fields.getLength(); i++){
    Element elField = (Element)fields.item(i);
    vlabels.addElement(elField.getElementsByTagName("label").item(0).getChildNodes().item(0).getNodeValue());
    }
  numfields = vlabels.size();
  NodeList records = getNodeList(qdbResponse, "table/records/record");
  if (records == null){return null;}
  for (int recordCounter = 0; recordCounter < records.getLength(); recordCounter++){
    Element elRecord = (Element)records.item(recordCounter);
    NodeList cells = elRecord.getElementsByTagName("f");
    HashMap record = new HashMap();
    for (i = 0; i < cells.getLength(); i++){
      Element elCell = (Element)cells.item(i);
      NodeList cellContents = elCell.getChildNodes();
      String cellValue = "";

      for (int j = 0; j < cellContents.getLength(); j++) {
        if (cellContents.item(j) != null){
          if (cellContents.item(j).getNodeValue() != null) {
            cellValue = cellValue + cellContents.item(j).getNodeValue();
          }
          else {
            /* file attachment fields have a value with a child <url> element.
             * append if present.  So, the child must be named url, and
             * have a child of it's own (the url value) */
            String kidName = cellContents.item(j).getNodeName();
            if (kidName != null && kidName.equals("url")
                && cellContents.item(j).hasChildNodes()) {
              String urlValue = cellContents.item(j).getFirstChild().getNodeValue();
              cellValue = cellValue + "<url>" + urlValue + "</url>";
            }
          }
        }
      }
      record.put((String)vlabels.elementAt(i),cellValue);

    }
    vResult.addElement(record);
   }
   return vResult;
   }catch( Exception e){ 
      throw new NullPointerException("doQuery: "+QDBerrortext);
    
    }
  }

  /**
     * Retrieve XML document from a QuickBase HTTP API call that returns XML.
     * For API calls that do not return XML please use postApi.
     * You can use this method to call QuickBase API calls that are not already wrapped in this class for you.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on the XML input and output formats.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param action The name of the particular API call e.g. 'API_DoQuery'. 
     * @param qdbRequest The XML document defining the input parameters to the QuickBase API call. 
     * @return  Document containing the output of the QuickBase API call.
     * 
     */

public Document postApiXml(String dbid, String action, Document qdbRequest) throws QuickBaseException, Exception
{
  Document qdbResponse= null;
  QDBerrorcode="-1";
  QDBerrortext="No response from QuickBase.";
  String qdbResp="";
//  HTTPConnection URLConnect = new HTTPConnection(QuickBaseUrlPrefix+dbid+"?act="+action);
  HTTPConnection URLConnect = null;
  try {
    URLConnect = (HTTPConnection) Class.forName(httpConnectionClass).newInstance();
  }
  catch (Exception e) {
    throw new QuickBaseException( "Could not instantiate HTTPConnection object of class " 
                                  + httpConnectionClass );
  }
  URLConnect.setURL(QuickBaseUrlPrefix+dbid+"?act="+action);
  System.out.println ("Posting to QuickBase at "+QuickBaseUrlPrefix+dbid+"?act="+action);
  URLConnect.setTimeout( timeout );
  
  URLConnect.setMethod("POST");
  URLConnect.setFollowRedirects(false);
  if (proxyHost != null && proxyHost.length() > 0) {
    if (proxyPort <= 0) proxyPort = (QuickBaseUrlPrefix.indexOf("https") == 0) ? 443 : 80;
    URLConnect.setProxy(proxyHost, proxyPort);
  }
  URLConnect.addHeader("UserAgent", "QuickBaseJavaAPI/2.0"); 
  URLConnect.addHeader("Accept", "text/html"); 
  URLConnect.addHeader("QUICKBASE-ACTION", action);
  URLConnect.addHeader("Content-Type", "text/xml");
  
  if (QDBticket == null || QDBticket.length() == 0)
    {
    addRequestParameter(qdbRequest, "username", QDBusername);
    addRequestParameter(qdbRequest, "password", QDBpassword);
  }else{
    URLConnect.addCookie("TICKET="+QDBticket+"; path=/");
    }
  if (QDBappToken != null && QDBappToken.length() > 0)
  {
  addRequestParameter(qdbRequest, "apptoken", QDBappToken);
  }
  if (qdbRequest == null || qdbRequest.getDocumentElement() == null){
    Element qdbapiElement = qdbRequest.createElement("qdbapi");
    qdbRequest.appendChild(qdbapiElement);
    }  
  StringWriter sw = new StringWriter ();
  writeDoc(qdbRequest, sw);
  sw.close();
  String content = sw.toString();
  System.out.println("----------------\n" + content + "\n----------------\n");
  URLConnect.setBody(content);
  URLConnect.doConnect();

  qdbResp = URLConnect.getBody();
  if (URLConnect.getHeaders().indexOf("/xml") > -1)
  {
    qdbResponse = xmlFromString(qdbResp);
    NodeList nlTicket=null;
    nlTicket = getNodeList(qdbResponse, "ticket");
    if (nlTicket != null){
      if (nlTicket.item(0) != null){
         QDBticket = nlTicket.item(0).getChildNodes().item(0).getNodeValue();
        }
      }     
    QDBerrorcode = getNodeList(qdbResponse, "errcode").item(0).getChildNodes().item(0).getNodeValue();
    QDBerrortext = getNodeList(qdbResponse, "errtext").item(0).getChildNodes().item(0).getNodeValue();
    NodeList nlErrDetail=null;
    nlErrDetail = getNodeList(qdbResponse, "errdetail");
    if (nlErrDetail != null){
      if (nlErrDetail.item(0) != null){
         QDBerrortext = nlErrDetail.item(0).getChildNodes().item(0).getNodeValue();
        }
      }     
    if (!QDBerrorcode.equals("0")){
      throw new QuickBaseException (QDBerrortext, QDBerrorcode);
      } 
  }
  else
  {
    throw new QuickBaseException("postApiXml did not return XML: " + qdbResp, QDBerrorcode);
  }
  return qdbResponse;
}


/**
 * A helper method for getAttachmentUrl.  If a this object has
 * not acquired a ticket, the method is used to obtain one.  The
 * credentials presented are those that were used at construction
 * time.
 */
Document getTicket() throws QuickBaseException, Exception {
  final String action = "API_Authenticate";
  Document qdbRequest = newXmlDocument();
  return postApiXml("main", action, qdbRequest);
}

public void setTicket(String ticket) {
	QDBticket = ticket;
}

public void setAppToken (String token) {
	QDBappToken = token;
}

/**
 * Retrieve a file attachment from QuickBase.  This method retrieves
 * the document given by <code>url</code>, returning the body as
 * a byte array.  <code>url</code> should be a location as given
 * in the
 *
 * <pre>
 *   &lt;url&gt; ... &lt;/url&gt;
 * </pre>
 * portion of a file attachment field value.
 *
 * <p>
 * This method will take care of supplying authorization credentials.  If a ticket
 * is available from a previous request, that ticket will be used.  Otherwise, the
 * username/password combination will be used to acquire a ticket.
 * 
 * <p>
 * Note that the body of the response is returned verbatim; there is no
 * attempt made to interpret it.  It's entirely possible to supply a bad
 * url, and receive an HTTP 200 response whose body consists of an error
 * page.  Thus, the caller is responsible for examining the contents of the
 * response to ensure that what they expected was indeed what they ended up with.
 *
 * @param url the attachment url to retrieve.
 * @return the body of the response given when <code>url</code> was
 *         requested.
 */
public byte[] getFile(String url) throws QuickBaseException, Exception {
    if (QDBticket == null || QDBticket.length() == 0) {
        getTicket();
    }

    // HTTPConnection URLConnect = new HTTPConnection(url);
    HTTPConnection URLConnect = null;
    try {
        URLConnect = (HTTPConnection) Class.forName(httpConnectionClass).newInstance();
    }
    catch (Exception e) {
        throw new 
            QuickBaseException("Could not instantiate HTTPConnection object of class " 
                               + httpConnectionClass );
    }
    URLConnect.setURL(url);
    URLConnect.setTimeout( timeout );

    URLConnect.setMethod("GET");
    URLConnect.setFollowRedirects(false);
    if (proxyHost != null && proxyHost.length() > 0) {
        if (proxyPort <= 0) {
            proxyPort = (QuickBaseUrlPrefix.indexOf("https") == 0) ? 443 : 80;
        }
        URLConnect.setProxy(proxyHost, proxyPort);
    }
    URLConnect.addHeader("UserAgent", "QuickBaseJavaAPI/2.0"); 
    URLConnect.addHeader("Accept", "*/*");   
    URLConnect.addCookie("TICKET=" + QDBticket + "; path=/");
    URLConnect.doConnect();
    return URLConnect.getBodyBytes();
}

     /**
     * Retrieve String from a QuickBase HTTP API call that returns HTML, CSV, TSV data.
     * For API calls that return XML please use postApiXml.
     * You can use this method to call QuickBase API calls that are not already wrapped in this class for you.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on the XML input formats.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param action The name of the particular API call e.g. 'API_DoQuery'. 
     * @param qdbRequest The XML document defining the input parameters to the QuickBase API call. 
     * @return  String containing the output of the QuickBase API call.
     * 
     */


public String postApi(String dbid, String action, Document qdbRequest)throws IOException, Exception
{
  QDBerrorcode="";
  QDBerrortext="";
  //HTTPConnection URLConnect = new HTTPConnection(QuickBaseUrlPrefix+dbid+"?act="+action);
  HTTPConnection URLConnect = null;
  try {
    URLConnect = (HTTPConnection) Class.forName(httpConnectionClass).newInstance();
  }
  catch (Exception e) {
    throw new QuickBaseException( "Could not instantiate HTTPConnection object of class " 
                                  + httpConnectionClass );
  }
  URLConnect.setURL(QuickBaseUrlPrefix+dbid+"?act="+action);
  URLConnect.setTimeout( timeout );

  URLConnect.enableSSL("","","","",false);
  URLConnect.setMethod("POST");
  URLConnect.setFollowRedirects(false);   
  URLConnect.addHeader("UserAgent", "QuickBaseJavaAPI/2.0"); 
  URLConnect.addHeader("Accept", "text/html"); 
  URLConnect.addHeader("QUICKBASE-ACTION", action);
  URLConnect.addHeader("Content-Type", "text/xml");

  if (proxyHost != null && proxyHost.length() > 0) {
    if (proxyPort <= 0) proxyPort = (QuickBaseUrlPrefix.indexOf("https") == 0) ? 443 : 80;
    URLConnect.setProxy(proxyHost, proxyPort);
  }

  if (QDBticket == null || QDBticket.length() == 0)
    {
    addRequestParameter(qdbRequest, "username", QDBusername);
    addRequestParameter(qdbRequest, "password", QDBpassword);
    }
  if (qdbRequest == null){
    Element qdbapiElement = qdbRequest.createElement("qdbapi");
    qdbRequest.appendChild(qdbapiElement);
    }  
  
  StringWriter sw = new StringWriter ();
  writeDoc(qdbRequest, sw);
  sw.close();
  String content = sw.toString();
  URLConnect.setBody(content);
  if (QDBticket != null && QDBticket.length() > 0 )
  {
    URLConnect.addCookie("TICKET="+QDBticket+"; path=/");
  }
  URLConnect.doConnect();
  return URLConnect.getBody(); 
}


     /**
     * Clone a QuickBase database and return the database id of the clone.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_CloneDatabase.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param Name The name for the new ('clone') database. 
     * @param Description for the the new ('clone') database. 
     * @return  String containing the database ID of the new ('clone') database. 
     * 
     */


public String cloneDatabase (String dbid, String Name, String Description) throws QuickBaseException, Exception
  {
  String newQuickBaseID;
  Document qdbRequest = newXmlDocument();
  addRequestParameter(qdbRequest, "newdbname", Name);
  addRequestParameter(qdbRequest, "newdbdesc", Description);
  Document qdbResponse = postApiXml (dbid,"API_CloneDatabase", qdbRequest);
  return getNodeList(qdbResponse, "newdbid").item(0).getChildNodes().item(0).getNodeValue();
 }

     /**
     * Add a new record to the QuickBase database and return the record ID of the new record.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_AddRecord.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param recorddata A HashMap of (field name or field identifier) and field value pairs containing the new recorded to be added.
     * @return  String containing the record ID of the new record.
     * Note that this method will not work with field names that are composed of only numeric characters (0-9).
     * 
     */

public String addRecord(String dbid, HashMap recorddata) throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
Set keys = recorddata.keySet();
for (Iterator i = keys.iterator(); i.hasNext();)
  {
  String nextKey=(String)i.next();
  String APIfieldName=nextKey;
  if(isFid(APIfieldName))
    {
	  Object data = recorddata.get(nextKey);
	  if (data instanceof FileAttachment) {
		  FileAttachment file = (FileAttachment) data;
		  addRequestParameter(qdbRequest, "field", file.getEncodedContents(), "fid", APIfieldName, file.getFilename() );
	  } else {
		  addRequestParameter(qdbRequest, "field", (String)data, "fid", APIfieldName );
	  }
    }
  else
    {
    APIfieldName=removeNonAlphaNumerics(APIfieldName);
	  Object data = recorddata.get(nextKey);
	  if (data instanceof FileAttachment) {
		  FileAttachment file = (FileAttachment) data;
		  addRequestParameter(qdbRequest, "field", file.getEncodedContents(), "name", APIfieldName, file.getFilename() );
	  } else {
		  addRequestParameter(qdbRequest, "field", (String)data, "name", APIfieldName );
	  }
    }
  }
Document qdbResponse = postApiXml(dbid, "API_AddRecord", qdbRequest);
  return getNodeList(qdbResponse, "rid").item(0).getChildNodes().item(0).getNodeValue();
}


     /**
     * Add one or more records to the QuickBase database and return the record IDs of the new records.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_ImportFromCSV.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param qdbCSV A String containing the CSV comma separated values of new records to be added.
     * @return  Vector containing the record IDs of the new records.
     * 
     */

public Vector importFromCSV(String dbid, String qdbCSV, String clist, boolean skipfirst) throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
if(skipfirst)
  {
  addRequestParameter(qdbRequest, "skipfirst", "1");
  }
else
  {
  addRequestParameter(qdbRequest, "skipfirst", "0");
  }
addRequestParameter(qdbRequest, "clist", clist);
addCSVCdata(qdbRequest, qdbCSV);
Document qdbResponse = postApiXml(dbid, "API_ImportFromCSV", qdbRequest);
NodeList rids = qdbResponse.getElementsByTagName("rid");
Vector result = new Vector();
for(int i = 0; i < rids.getLength(); i++)
  {
  result.addElement(rids.item(i).getChildNodes().item(0).getNodeValue());
  }
return result;
}

     /**
     * Change the permissions of a particular user or group on a particular QuickBase database.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_ChangePermission.
     * The keys of the input HashMap, along with their valid values is as follows:
     * view none, own, any
     * modify none, own, any
     * create false, true
     * admin false, true
     * If one or more of the four permission is not included in the input HashMap, its setting is not changed.
     * If you just want to check on the permission settings for a user or group, just call changePermission with an empty HashMap.
     * The return HashMap follows the same convention as the input HashMap except that every permission level is reported.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param usergroup A QuickBase screen name, or QuickBase group, or email address of a registered QuickBase user.
     * @param permissions Contains the new permissions for the user or group. 
     * @return HashMap of the permissions after the change.
     * 
     */




public HashMap changePermission(String dbid, String usergroup, HashMap permissions)throws QuickBaseException, Exception
{
Document qdbRequest = newXmlDocument();
addRequestParameter(qdbRequest, "uname",usergroup);
Set keys = permissions.keySet();
for (Iterator i = keys.iterator(); i.hasNext();){
  String nextKey=(String)i.next();
  String permission = (String)permissions.get(nextKey);
  if(nextKey.toLowerCase().startsWith("modify"))
    {
    if(permission.equals("") | permission.toLowerCase().startsWith("no"))
      {
      addRequestParameter(qdbRequest, "modify","none");
      }
    else if(permission.toLowerCase().startsWith("own"))
      {
      addRequestParameter(qdbRequest, "modify","own");
      }
    else
      {
      addRequestParameter(qdbRequest, "modify","any");
      }
    }
  else if(nextKey.toLowerCase().startsWith("read")|nextKey.toLowerCase().startsWith("view"))
    {
    if(permission.equals("") | permission.toLowerCase().startsWith("no") )
      {
      addRequestParameter(qdbRequest, "view","none");
      }
    else if(permission.toLowerCase().startsWith("own"))
      {
      addRequestParameter(qdbRequest, "view","own");
      }
    else
      {
      addRequestParameter(qdbRequest, "view","any");
      }
    }
  else if(nextKey.toLowerCase().startsWith("create") | nextKey.toLowerCase().startsWith("add") | nextKey.toLowerCase().startsWith("new"))
    {
    if(permission.equals("") | permission.toLowerCase().startsWith("no") | permission.toLowerCase().startsWith("false"))
      {
      addRequestParameter(qdbRequest, "create","false");
      }
    else
      {
      addRequestParameter(qdbRequest, "create","true");
      }   
    }
  else if(nextKey.toLowerCase().startsWith("admin"))
    {
    if(permission.equals("") | permission.toLowerCase().startsWith("no") | permission.toLowerCase().startsWith("false"))
      {
      addRequestParameter(qdbRequest, "admin","false");
      }
    else
      {
      addRequestParameter(qdbRequest, "admin","true");
      }   
    }
  }
Document qdbResponse = postApiXml (dbid,"API_ChangePermission", qdbRequest);
HashMap newPermissions = new HashMap();
    NodeList nl=null;
    newPermissions.put ("view", "none");
    nl = getNodeList(qdbResponse, "view");
    if (nl.getLength() > 0){
      newPermissions.put ("view",nl.item(0).getChildNodes().item(0).getNodeValue());
      }
    newPermissions.put ("modify", "none");
    nl = getNodeList(qdbResponse, "modify");
    if (nl.getLength() > 0){
      newPermissions.put ("modify",nl.item(0).getChildNodes().item(0).getNodeValue());
      }

    newPermissions.put ("create", "false");
    nl = getNodeList(qdbResponse, "create");
    if (nl.getLength() > 0){
      newPermissions.put ("create",nl.item(0).getChildNodes().item(0).getNodeValue());
      }

    newPermissions.put ("admin", "false");
    nl = getNodeList(qdbResponse, "admin");
    if (nl.getLength() > 0){
      newPermissions.put ("admin",nl.item(0).getChildNodes().item(0).getNodeValue());
      }
return newPermissions;
}

     /**
     * Edit a record in a QuickBase database and return the number of modified fields.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_EditRecord.
     * Not all fields can be modified. Built-in and formula (virtual) fields cannot be modified.
     * If you attempt to modify them with editRecord you will get an error and no part of the record will have been modified. 

     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param recorddata A HashMap of (field name or field identifier) and field value pairs containing the fields to be modified.
     * @param rid String containing the record ID of the record to be edited.  
     * @return  String containing the record ID of the new record.
     * Note that this method will not work with field names that are composed of only numeric characters (0-9).
     * 
     */


public String editRecord(String dbid, HashMap recorddata, String rid)throws QuickBaseException, Exception
{
Set keys = recorddata.keySet();
Document qdbRequest = newXmlDocument();
addRequestParameter(qdbRequest, "rid", rid);
for (Iterator i = keys.iterator(); i.hasNext();)
  {
  String nextKey=(String)i.next();
  String APIfieldName=nextKey;
  if(isFid(APIfieldName))
    {
    addRequestParameter(qdbRequest, "field", (String)recorddata.get(nextKey), "fid", APIfieldName );
    }
  else
    {
    APIfieldName=removeNonAlphaNumerics(APIfieldName);
    addRequestParameter(qdbRequest, "field", (String)recorddata.get(nextKey), "name", APIfieldName );
    }
 }
Document qdbResponse = postApiXml(dbid, "API_EditRecord", qdbRequest);
  try
    {
    return getNodeList(qdbResponse, "num_fields_changed").item(0).getChildNodes().item(0).getNodeValue();
    }
  catch(Exception e)
    {
    throw new NullPointerException("editRecord: "+QDBerrortext);
    }
}

     /**
     * Delete a record from the QuickBase database.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_DeleteRecord.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param rid String containing the record ID of the record to be deleted. 
     * 
     */


public void deleteRecord(String dbid, String rid)throws QuickBaseException, Exception
{
  Document qdbRequest = newXmlDocument();
  addRequestParameter(qdbRequest, "rid", rid);
  Document xmlResponse = postApiXml(dbid, "API_DeleteRecord",qdbRequest);
  return;
}

 /**
     * Change the record owner of a record from the QuickBase database.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information on API_ChangeRecordOwner.
     *
     * @param dbid The unique identifier of a QuickBase database.
     * @param rid String containing the record ID of the record to be deleted. 
     * @param newowner String containing the screenname or email address of the new record owner. 
     * 
     */

public void changeRecordOwner(String dbid, String rid, String newowner)throws QuickBaseException, Exception
{
  Document qdbRequest = newXmlDocument();
  addRequestParameter(qdbRequest, "rid", rid);
  addRequestParameter(qdbRequest, "newowner", newowner);
  Document xmlResponse = postApiXml(dbid, "API_ChangeRecordOwner",qdbRequest);
  return;
}

     /**
     *  Removes characters from a field name
     *  The list of remaining characters are as follows:
     *  a-z
     *  A-Z
     *  0-9
     *  _
     */
  private String removeNonAlphaNumerics(String stringInput) {
  StringBuffer buffer = new StringBuffer();
  char character;
  String fieldName = stringInput.toLowerCase();
  for(int i = 0; i < fieldName.length(); i++) {
      character = fieldName.charAt(i);
      
      // Check for a-z
    if((character >= 'a') && (character <= 'z')) {
      buffer.append(character);
        }
        
        // Check for 0-9
    else if((character >= '0') && (character <= '9')) {
      buffer.append(character);
        }
  
    else{
      buffer.append('_');
        }
  }
  return buffer.toString();
 }
 
 private boolean isFid(String fieldName)
  {
  char character;
  for(int i = 0; i < fieldName.length(); i++)
    {
    character = fieldName.charAt(i);  
    // Check for 0-9
    if((character < '0') || (character > '9'))
      {
      return false;
      }
    }
  return true;
  }
  
protected Document newXmlDocument(){
    Document document = null;
   try{
     document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
     Element qdbapiElement = document.createElement("qdbapi");
     document.appendChild(qdbapiElement);
     return document;
   }catch(Exception e){
     throw new RuntimeException(e.getMessage());
   }
 }
  
 protected Document createXmlDocument(InputSource src, boolean needsValidation){
   try{
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setValidating(needsValidation);
     return factory.newDocumentBuilder().parse(src);

   }catch(Exception e){
     throw new RuntimeException(e.getMessage());
   }
 }
  
 protected void writeDoc(Node node, Writer out){
   try{
     Source src = new DOMSource(node);
     Transformer t = TransformerFactory.newInstance().newTransformer();
     t.transform(src, new StreamResult(out));
   }catch(Exception e){
     throw new RuntimeException(e.getMessage());
   }
 }
 
   /**
     * Excercise many of the methods of this class.
     * Please refer to the
     * <a href="https://www.quickbase.com/up/6mztyxu8/g/rc7/en/">
     * QuickBase HTTP API</a> for more information.
     *
     * @param args Specify the username, password, and optionally a QuickBase domain as the first three command line arguments.
     * 
     */

static public void main(String args[])
        throws Exception
    {
	
		QuickBaseClient qbc = new QuickBaseClient("username", "password");
		System.out.println ("ticket="+qbc.getOneTimeTicket());

        PrintWriter out = null;
        String className = "QuickBaseClient";
        try {
            out = new PrintWriter(System.out);
            out.println("Welcome to QuickBase\n");

            if (args.length < 2 ){
                out.println("Please specify the username, password as the first two command line arguments.");
                out.println("You can include an optional third command line argument of the form: 'https://hostname.yourdomain.com/db/'");
                out.println("This will set the QuickBase domain. The default domain is: 'https://www.quickbase.com/db/'");
                return;
                }
            String username = args[0];
            String password = args[1];
            String strURL = "https://www.quickbase.com/db/";
            QuickBaseClient qdb = null;
            if(args.length > 2 && args[2].startsWith("http"))
              {
              strURL = args[2];
              qdb = new QuickBaseClient(username, password, strURL);
              }
            else
              {
              qdb = new QuickBaseClient(username, password);
              }
            out.println(className + "\n" + strURL + ": Authenticating with " + username + " and <password>\n");
            HashMap tables = (HashMap)qdb.grantedDBs(false, false, true);
            if(tables == null)
              {
              out.println("No tables belong to this user.");
              }
            Set tableNames = tables.keySet();
            String tableName = "";
            String tableDbid = "";
            for (Iterator i = tableNames.iterator(); i.hasNext();){
                  tableName = (String)i.next();
                  tableDbid = (String)tables.get(tableName);
                  out.println("Name: " + tableName + " DBID: " + tableDbid);
                 }
            Document schema = qdb.getSchema(tableDbid);
            NodeList childTables = schema.getElementsByTagName("chdbid");
            out.println("The QuickBase application " + tableName + " has " + childTables.getLength() + " child tables.");
            out.println("The table aliases and identifiers are listed below.");
            for (int i = 0; i < childTables.getLength(); i++)
              {
              out.println("Child Table Alias: " + childTables.item(i).getAttributes().item(0).getNodeValue());
              out.println("Child Table DBID: " + childTables.item(i).getChildNodes().item(0).getNodeValue());
              }
        } catch (QuickBaseException qdbe) {
            System.err.println("Exception in main "+ qdbe.toString()+ " error code: "+qdbe.getErrorCode() );
            qdbe.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception in main "+ e.toString() );
            e.printStackTrace();

        } finally {
            if (null != out) {
                out.flush();    
                out.close();
            }
        }
    }

}

