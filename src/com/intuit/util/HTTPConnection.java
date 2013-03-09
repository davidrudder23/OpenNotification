// (c) 1999, Intuit, Inc
// $Id: HTTPConnection.java,v 1.35 2003/10/14 22:07:56 srevilak Exp $

/*
 * @(#)HTTPConnection
 *
 * Copyright (c) 1999 Intuit Inc. All Rights Reserved.
 *
 */

package com.intuit.util;

/**
 * IMPORTANT: DO NOT make this class dependent on any
 * com.intuit.* class.  We need to be able to distribute this
 * to other companies.  If you have config stuff you want to
 * depend on, enhance the WebClient class.  billo 12-oct-2000
 *
 * It's kind of wrong that Log class is used here.  But it would be too painful
 * to make the logging capability a parameter or something, so let's
 * live with it.  If you are trying to use this class outside
 * Intuit's class library, just replace all the //Log.whatever calls
 * with your own logging mechanism.  I isolated all calls to //Log.* in the 
 * methods: error, debug, and info.
 */
import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import javax.net.ssl.*;

/**
 * <p>HTTPConnection class is a simple instance-oriented HTTP client.
 * Unlike the crappy HttpUrlConnection class that comes with java,
 * you can set proxy settings on each connection you make, instead
 * of globally for your whole process.
 * 
 * <p>See the main() method for examples of using this class.
 *
 * <p>Note: Sun's JSSE Java SSL implementation is required to use this class.
 *
 */
public class HTTPConnection extends Thread
{

    String    hostName = null;
    int       port = 80;
    String    proxyHostName = null;
    int       proxyPort = 0;
    Vector    headers = new Vector();
    String    cookiedata = null;
    String    requestBody;

    String    requestHeaders;

    Vector    responseHeaders = new Vector();
    String    responseHeadersRaw = null;
    String    responseBody = null;
    String    responseStatus;
    int       responseBytes = 0;

    char[]    readbuf;
    byte[]    databuf;

    byte[]    responseBodyBinary = null;
    Exception caughtException = null;

    String    urlFile;
    URL       url;
    String    method = null;
    boolean   followRedirect = false;
    boolean   shutdown = false;

    int       maxRedirects = MAX_REDIRECT;
    int       redirectCount = 0;
    Object    sock;

    static final int BUFSIZE = 8192 * 16;
    static final int MAX_REDIRECT = 5;

    //static final int BUFSIZE = 128;
    int TIMEOUT = 180000;  // milliseconds
    static final String HTTP_VERSION = "HTTP/1.0";

    public HTTPConnection()
    {
        method = "GET";
        readbuf = new char[BUFSIZE];

        //Util.trace("httpconnection constructor");
    };

    public HTTPConnection (URL u)
    {
        this();
        setURL(u);
    }

    public HTTPConnection (String u)
    {
        this();
        setURL(u);
    }

    public void setURL(URL u) {
        url = u;
        hostName = url.getHost();
        urlFile = url.getFile();

        setDaemon(true);
        if (url.getPort() > 0) {
            port = url.getPort();
        } else if (u.getProtocol().equalsIgnoreCase("https")) {
            //addSSLProvider();
            port = 443;
            useSSL = true;
        }
    }

    public void setURL(String u) {
        boolean SSL = false;
        if (u.startsWith("https:")) {
            addSSLProvider();
            u = "http:" + u.substring(6);
            SSL = true;
            port = 443;
        }

        try {
            url = (new URL(u));
            hostName = url.getHost();
            urlFile = url.getFile();

            setDaemon(true);
            if (url.getPort() > 0) {
                port = url.getPort();
            } else if (url.getProtocol().equalsIgnoreCase("https")) {
                SSL = true;
                port = 443;
            }

            if (SSL) {
                useSSL = true;
            }
        } catch (MalformedURLException e) {
            // we're hosed...
            error(this, e);
        }
    }

    protected void debug(Object o, String s) 
    {
        //Log.debug(o, s);
    }
    protected void info(Object o, String s) 
    {
        //Log.info(o, s);
    }
    protected void error(Object o, Throwable t) 
    {
        //Log.error(o, t);
    }
    protected void error(Object o, String s) 
    {
        //Log.error(o, s);
    }

    /**
     * Set the timeout on connections.  CAUTION: do not mess with this 
     * except under special circumstances.  i.e. you know 
     * with absolute certainty that an HTTP server will NEVER be slower
     * than the timeout and succeed in the response.  The deault is 
     * 180000 ms (3 minutes), which is what most browsers use.
     */
    public void setTimeout(int ms) 
    {
        TIMEOUT = ms;
    }

    public void setMaxRedirects(int max) 
    {
        maxRedirects = max;
    }

    SSLSocketFactory factory = null;

    public void setFactory(SSLSocketFactory f)
    {
        factory = f;
    }

    protected OutputStream outputStream = null;

    /**
     * tell HTTPConnection to use this output stream
     * instead of writing the response body into 
     * a byte array.  It's up to the caller to open and close the
     * supplied stream.
     */
    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }
    
    /**
     * Parse a cookie header into an array of cookies as per
     * RFC2109 - HTTP Cookies
     *
     * @param cookieHdr The Cookie header value.
     * @return hash of cookie name-values. or null if
     * empty or null header
     */
    public static Hashtable parseCookieHeader(String cookieHdr) {
        return parseCookieHeader(cookieHdr, null);
    }

    /**
     * Parse a cookie header into an array of cookies as per
     * RFC2109 - HTTP Cookies
     *
     * @param cookieHdr The Cookie header value.
     * @param ht The Hashtable you want to store values in. If null, one is created.
     * @return hash of cookie name-values. or null if
     * empty or null header
     */
    public static Hashtable parseCookieHeader(String cookieHdr, Hashtable ht) 
    {
        if(cookieHdr == null || cookieHdr.length() == 0)
            return null;

        Hashtable jar = new Hashtable();

        if (ht == null) {
            jar = new Hashtable();
        } else {
            jar = ht;
        }

        StringTokenizer stok = new StringTokenizer(cookieHdr, ";");
        while (stok.hasMoreTokens()) {
            try {
                String tok = stok.nextToken();
                int equals_pos = tok.indexOf('=');
                if (equals_pos > 0) {
                    String name = tok.substring(0, equals_pos);
                    name = name.trim();
                    String value = tok.substring(equals_pos + 1);
                    jar.put(name, value);
                } else if ( tok.length() > 0 && equals_pos == -1 ) {
                    String name = tok;
                    jar.put(name, "");
                }
            } catch (IllegalArgumentException badcookie) {
            } catch (NoSuchElementException badcookie) {
            }
        }    
         
        return jar;

    }

    private SSLSocketFactory getFactory()
    {
        if (factory == null) {
            factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        }
        return factory;
    }
    
    /**
     * Select the proxy to use for the connection.
     *
     * @param host Host name or IP address.
     * @param port Port number
     */
    public void setProxy(String host, int port)
    {
        proxyPort = port;
        proxyHostName = host;
    }

    /**
     * select the method.
     *
     * @param m "GET" or "POST" or whatever.
     */
    public void setMethod(String m)
    {
        method = m;
    }

    /**
     * Tell the connection to follow redirects or not.
     *
     */
    public void setFollowRedirects(boolean enable) {
        followRedirect = enable;
    }

    /**
     * Set the request body.
     *
     * @param body  The body to be posted or put.
     */
    public void setBody(String body) {
        requestBody = body;
    }

    /**
     * retrieve the response body after connect() is called.
     * @return the response as a String, in ISO-8859-1 encoding.
     * 
     */
    public String getBody() {
        if (responseBody == null &&
            responseBodyBinary != null) {
            try {
                responseBody = new String(responseBodyBinary, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                responseBody = new String(responseBodyBinary);
            }
        }
        return responseBody;
    }

    /**
     * Returns the response body as a byte array.
     * Call this *after* connect()
     */
    public byte[] getBodyBytes() {
        return responseBodyBinary;
    }

    /** 
     * Return the response headers as a single String.
     * After connect(), obviously.
     */
    public String getHeaders() {
        return responseHeadersRaw;
    }

    /** 
     * Return the response headers as a vector of strings.
     * After connect(), obviously.
     */
    public Vector getHeadersVector() {
        return responseHeaders;
    }

    private boolean useSSL = false;

    /** 
     * Enable SSL using JSSE library.  See the JSSE documentation
     * for an explanation of how to use this.  These parameters 
     * are currently ignored.  Sorry.  Make sure you call addSSLProvider() 
     * first!
     *
     * @param certAlias
     * @param keyStore
     * @param keyStorePassword
     * @param keyPassword
     * @param ignoreHostnameMismatch
     * 
     */
    public void enableSSL(String certAlias,
                          String keyStore,
                          String keyStorePassword,
                          String keyPassword,
                          boolean ignoreHostnameMismatch)
    {
        useSSL = true;
    }


    public String getStatus() {
        return responseStatus;
    }

    public void addHeader(String h, String v)
    {
        headers.add(h + ": " + v);
    }

    /**
     * add a cookie name/value pair.
     * @param cookie should be a string like myvar=myval
     */
    public void addCookie (String cookie)
    {
        if (cookiedata != null) {
            cookiedata += "; " + cookie;
        } else { 
            cookiedata = cookie;
        }
    }

    long connectTime;

    /**
     * Perform the HTTP request and response.  After making this call
     * you can retrieve the status, body and headers with the
     * appropriate getWhatever() methods.
     */
    public void doConnect() throws Exception 
    {
        connectTime = System.currentTimeMillis();

        // check if it is malformed URL
       if ( (urlFile == null) || urlFile.equals("") ){
            urlFile = "/";
        }

        start();

        int byteCount = responseBytes;
        while (isAlive()) {
            if (responseBytes > byteCount) {
                byteCount = responseBytes;
                connectTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - connectTime > TIMEOUT) {
                // timeout with no new bytes received.
                break;
            }
            sleep(5);
        }

        if (isAlive()) {
            // We timed out.
            abort();
            throw new Exception("HTTP connection timeout.");
        } else if (caughtException != null) {
            throw caughtException;
        }
    }

    protected void abort()
    {
        shutdown = true;
        interrupt();
    }

    /**
     * Old method for compatibility: returns the entire response
     * including headers, status and body as a string.  Don't use this unless
     * You need to since it will allocate a big String.
     *
     * You should use doConnect() instead.  This method will be deprecated
     * on or before July 2001.
     */
    public String connect() throws Exception 
    {
        doConnect();
        responseBody = getBody();

        // for compatibility with old version
        return responseStatus + "\r\n" + 
            responseHeadersRaw + "\r\n" + responseBody;
    }

    protected void buildHeaders()
    {
        StringBuffer sb = new StringBuffer("");
        if ((proxyHostName == null) || useSSL) {
            sb.append("Host: " + hostName + "\r\n");
        }
        for (Enumeration e = headers.elements(); e.hasMoreElements(); ) {
            sb.append((String)e.nextElement() + "\r\n");
        }
        if (cookiedata != null) {
            sb.append("Cookie: " + cookiedata + "\r\n");
            cookiedata = null;
        }
        requestHeaders = sb.toString();
    }

    protected PrintWriter getPrintWriter () 
        throws IOException
    {
        
        if (useSSL) {
            SSLSocket ss = (SSLSocket)sock;
            return new PrintWriter(ss.getOutputStream());
        } else {
            Socket s = (Socket)sock;
            return new PrintWriter(s.getOutputStream());
        }
    }


    protected InputStream getTextReader()
        throws IOException
    {
        
        if (useSSL) {
            SSLSocket ss = (SSLSocket)sock;
            return ( ss.getInputStream() );
        } else {
            Socket s = (Socket)sock;
            return ( s.getInputStream() );
        }
    }

    protected BufferedInputStream getBinaryReader()
        throws IOException
    {
        
        if (useSSL) {
            SSLSocket ss = (SSLSocket)sock;
            return new BufferedInputStream( ss.getInputStream() );
        } else {
            Socket s = (Socket)sock;
            return new BufferedInputStream( s.getInputStream() );
        }
    }
            
            
    private Object getSocket(String host, int port) 
        throws UnknownHostException, IOException
    {
        if (useSSL) {
            if (proxyHostName == null) {
                SSLSocketFactory factory = getFactory();
                SSLSocket sslSock = (SSLSocket)factory.createSocket(host, port);
                return (Object)sslSock;
            } else {
                Socket tunnel = new Socket(proxyHostName, proxyPort);
                OutputStream out = tunnel.getOutputStream();
                String connect = "CONNECT " + host + ":" + port + " HTTP/1.0\r\n\r\n";
                byte buf[];
                try {
                    buf = connect.getBytes("ASCII7");
                } catch (UnsupportedEncodingException ignored) {
                    // This better never freaking happen.
                    buf = connect.getBytes();
                }
                out.write(buf);
                out.flush();

                byte            reply[] = new byte[200];
                int             replyLen = 0;
                int             newlinesSeen = 0;
                boolean         headerDone = false;     // Done on first newline
                InputStream     in = tunnel.getInputStream();
                boolean         error = false;
                
                while (newlinesSeen < 2) {
                    int i = in.read();
                    if (i < 0) {
                        throw new IOException("Unexpected EOF from proxy");
                    }
                    if (i == '\n') {
                        headerDone = true;
                        ++newlinesSeen;
                    } else if (i != '\r') {
                        newlinesSeen = 0;
                        if (!headerDone && replyLen < reply.length) {
                            reply[replyLen++] = (byte) i;
                        }
                    }
                }

                // Converting the byte array to a string is slightly wasteful
                // in the case where the connection was successful, but it's
                // insignificant compared to the network overhead.
                String replyStr;
                try {
                    replyStr = new String(reply, 0, replyLen, "ASCII7");
                } catch (UnsupportedEncodingException ignored) {
                    replyStr = new String(reply, 0, replyLen);
                }
                
                // We asked for HTTP/1.0, so we should get that back
                if (!replyStr.startsWith("HTTP/1.0 200")) {
                    throw new IOException("Unable to tunnel through "
                                          + proxyHostName + ":" + proxyPort
                                          + ".  Proxy returns \"" + replyStr + "\"");
                }

                SSLSocketFactory factory = getFactory();
                SSLSocket sslSock = (SSLSocket)factory.createSocket(tunnel, host, port,
                                                                    true);
                return (Object)sslSock;
            }
        } else {
            if (proxyHostName != null) {
                //System.err.println("Connecting thru proxy " + proxyHostName);
                Socket sock = new Socket(proxyHostName, proxyPort);
                return (Object)sock;
            } else {
                Socket sock = new Socket(host, port);
                return (Object)sock;
            }
        }
    }

    /**
     * It totally sucks that we have to do this, but since
     * the input stream implementation of sockets doesn't allow
     * us to put characters back, we have to read one char at a time, at least 
     * until we get all the HTTP headers.
     */
    protected String getLine (InputStream in) throws IOException
    {
        StringBuffer linebuf = new StringBuffer(80);
        int nextChar;
        boolean expectNewline = false;

        while ((nextChar = in.read()) != -1) {
            if (nextChar == 0x0a) {
                break;
            }
            if (expectNewline) {
                error(this, "Expected newline after carriage return. Bad webserver! Bad!" 
                          + linebuf.toString());
                break;
            }
            if (nextChar == 0x0d) {
                expectNewline = true;
                continue;
            }
            linebuf.append((char)nextChar);
        }
        return linebuf.toString();
    }
        
    
    /**
     * This method should probably be private.  Don't call it.
     */
    public void run()
    {
        Thread.currentThread().setName ("HTTPConnection:" + url.toString());
        //debug(HTTPConnection.class, "thread starts");

        doWork();
 
        if ( !shutdown && followRedirect ) {
            while ( (getStatus() != null) && 
                    ((getStatus().indexOf("301 Found") != -1) ||
                     (getStatus().indexOf("302 Found") != -1)) ) {
                redirectCount++;
                // only loop through redirect certain number of times,
                if (redirectCount > maxRedirects) {
                    info(this, "reached max redirects " + maxRedirects);
                    break;
                }
                // get Location header which has the new URL
                Vector hv = getHeadersVector();
                Enumeration e = hv.elements();
                String header;
                int index;
                String u = null;
                try {
                    while (e.hasMoreElements()) {
                        header = (String)e.nextElement();
                        index = header.indexOf("Location: ");
                        if (index != -1) {
                            u = header.substring(index + 10);
                            debug(this, "follow redirect to: " + u);
                            break;
                        }
                    }
                    if (u != null) {
                        debug(this, "reinitialize to " + u);
                        if (u.startsWith("https:")) {
                            addSSLProvider();
                            u = "http:" + u.substring(6);
                            useSSL = true;
                            port = 443;
                        }

                        url = (new URL(u));
                        hostName = url.getHost();
                        urlFile = url.getFile();

                        if (url.getPort() > 0) {
                            port = url.getPort();
                        } else if (url.getProtocol().equalsIgnoreCase("https")) {
                            useSSL = true;
                            port = 443;
                        }

                        // reinitialize all response variables
                        responseHeaders = new Vector();
                        responseBytes = 0;
                        responseBody = null;

                        doWork();
                    } else {
                        // no more redirect
                        break;
                    }
                } catch (MalformedURLException ex) {
                    // we're hosed..., no more redirect
                    error(this, ex);
                    break;
                }
            }
        }
    }

    protected void openConnectionObject(String hostName,
                                        int port)
        throws UnknownHostException, IOException
    {
        sock = getSocket(hostName, port);
    }
    
    protected void closeConnectionObject()
        throws IOException
    {
        try {
            if (sock != null) { 
                if (useSSL) {
                    SSLSocket ss = (SSLSocket)sock;
                    ss.close(); 
                } else {
                    Socket s = (Socket)sock;
                    s.close(); 
                }
            }
        } finally {
            sock = null;
        }
    }

    protected void doWork() {
        InputStream in = null;
        BufferedInputStream bin = null;
        PrintWriter out = null;
        ByteArrayOutputStream bout = null;
        int readlen = 0;

        shutdown = false;

        buildHeaders();

        while (true) {
            try {

                //System.out.println("1");
                
                openConnectionObject(hostName, port);
                
                if (shutdown) {
                    break;
                }

                //System.out.println("2");
                
                out = getPrintWriter();
                in = getTextReader();
                
                if (shutdown) {
                    break;
                }

                //System.out.println("4");
                
                if ((proxyHostName != null) && !useSSL) {
                    out.print(method + " " + url.toString() + " " + HTTP_VERSION);
                } else {
                    out.print(method + " " + urlFile + " " + HTTP_VERSION);
                }
                out.print("\r\n");
                
                out.print(requestHeaders);
                //debug(HTTPConnection.class, "finished writing headers:\n" + requestHeaders);
                if (requestBody != null) {
                    out.print("Content-length: " + requestBody.length());
                    out.print("\r\n");
                    out.print("\r\n");
                    out.print(requestBody);
                } else {
                    out.print("\r\n");
                }
                out.flush();
                
                if (shutdown) {
                    break;
                }

                //System.out.println("5");
                // get the headers and status
                boolean firstline = true;
                responseHeadersRaw = "";
                
                while (!shutdown) {
                    String line = getLine(in);
                    if (line == null || line.equals("")) {
                        break;
                    }
                    if (firstline == true) {
                        responseStatus = line;
                        firstline = false;
                    } else {
                        responseHeadersRaw += line + "\r\n";
                        responseHeaders.add(line);
                    }
                    //System.err.println("Read a line: " + line);
                    responseBytes += line.length();
                }
                
                //System.out.println("6");
                
                //debug(HTTPConnection.class, "status: " + responseStatus);
                //debug(HTTPConnection.class, "finished reading headers:\n" + responseHeadersRaw);
                
                /* if followRedirect and response is 302 or 301, 
                 * then disgard the response body from this run
                 * however read the response body if the number of
                 * redirects reach maxRedirects
                 * 2002/02/11, jinglei
                 */
                if ( (getStatus() != null) &&
                     ((getStatus().indexOf("301 Found") != -1) ||
                      (getStatus().indexOf("302 Found") != -1)) &&
                     followRedirect &&
                     (redirectCount <= maxRedirects) ) {
                    return;
                }
                
                // fixme: use content length, if supplied.
                if (outputStream == null) {
                    bout = new ByteArrayOutputStream();
                    outputStream = bout;
                }
                
                // OK now read the content
                bin = getBinaryReader();
                databuf = new byte[BUFSIZE];
                while (true) {
                    int n = bin.read(databuf, 0, BUFSIZE);
                    if (n < 0) {
                        break;
                    }
                    //System.err.println("****Read bytes: " + n + "--\n" +
                    //new String(databuf, 0, n));
                    outputStream.write(databuf, 0, n);
                }
                if (bout != null) {
                    responseBodyBinary = bout.toByteArray();
                    bout.close();
                }
                //debug(HTTPConnection.class, "finished reading body");
                // it's up to the caller to close outputStream if they supplied it.
                
            } catch (Exception e) {
                error(this, e);
                caughtException = e;
                
            } finally {
                try {
                    closeConnectionObject();
                    
                    if (out != null) { 
                        out.close(); 
                    }
                    if (in != null) { 
                        in.close(); 
                    }
                    if (bin != null) { 
                        bin.close(); 
                    }
                    if (bout != null) { 
                        bout.close(); 
                    }
                } catch (IOException e) {
                    // totally hosed
                    error(this, e);
                }
            }

            break;
        }
    }

    static private boolean sslProviderAdded = false;
    static private synchronized void addSSLProvider() {
        if (sslProviderAdded != true) {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            
            Properties sysProps = System.getProperties();
            sysProps.put("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
            System.setProperties(sysProps);
            sslProviderAdded = true;
        }
    }

    static public void main(String args[])
        throws Exception
    {
        PrintWriter out = null;
        String command = args[1];

        try {
            if (args[0].equals("-")) {
                out = new PrintWriter(System.out);
            } else {
                out = new PrintWriter(new FileWriter(args[0]));
            }

            HTTPConnection hc = null;
            if (command.equals("get")) {
                hc = new HTTPConnection(args[2]);
                
                hc.setMethod("GET");
                hc.setFollowRedirects(true);
                hc.setMaxRedirects(1);

                hc.doConnect();

                String h = hc.getHeaders();
                Vector hv = hc.getHeadersVector();
                Enumeration e = hv.elements();
                out.println("Status: " + hc.getStatus());
                out.println("Headers in Vector:");
                while (e.hasMoreElements()) {
                    out.println((String)e.nextElement());
                }
                String body = hc.getBody();

                out.println("Headers:");
                out.println(h);
                out.println("Body:");
                out.println(body);
            }

            if (command.equals("getfile")) {
                hc = new HTTPConnection(args[2]);

                String filename = args[3];
                
                hc.setMethod("GET");
                hc.setFollowRedirects(true);

                hc.doConnect();

                String h = hc.getHeaders();
                
                byte[] body = hc.getBodyBytes();

                out.println("Status: " + hc.getStatus());
                Vector hv = hc.getHeadersVector();
                Enumeration e = hv.elements();
                out.println("Headers in Vector:");
                while (e.hasMoreElements()) {
                    out.println((String)e.nextElement());
                }
                out.println("Headers:");
                out.println(h);
                
                FileOutputStream fout = new FileOutputStream(filename);
                fout.write(body, 0, body.length);
                fout.close();
                out.println("Wrote body to file " + filename + " " + body.length + " bytes");
            }

            if (command.equals("post")) {
                hc = new HTTPConnection(args[2]);
                
                hc.setMethod("POST");
                hc.setFollowRedirects(true);
                hc.addCookie("qbn.login_test=testme");
                hc.addHeader("Content-type", "application/x-www-form-urlencoded");
                hc.setProxy(args[3], 80);
                hc.setBody(args[4]);
                hc.doConnect();

                String h = hc.getHeaders();
                
                String body = hc.getBody();

                out.println("Headers:");
                out.println(h);
                out.println("Body:");
                out.println(body);
            }

                
        } catch (Exception e) {
            System.err.println("Exception");
            e.printStackTrace();
        } finally {
            if (null != out) {
                out.flush();    // BOTH OF THESE STEPS ARE VERY IMPORTANT!
                out.close();
            }
        }


    }
        
}
