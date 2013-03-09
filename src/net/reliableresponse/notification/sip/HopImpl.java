package net.reliableresponse.notification.sip;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.sip.address.Hop;


public class HopImpl implements Hop {
     /** Creates new Hop
     *@param hop is a hop string in the form of host:port/Transport
     *@throws IllegalArgument exception if string is not properly formatted or
     * null.
     */
    private String host;
    private int port;
    private String transport;


    public String getHost() { return this.host; }

    public int getPort() { return this.port; }

    public String getTransport() { return this.transport; }
    
    public HopImpl(String hop) throws IllegalArgumentException {
        if (hop == null) throw new IllegalArgumentException("Null arg!");
        StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
        String hostPort = stringTokenizer.nextToken("/");
        transport = stringTokenizer.nextToken().trim();
        if (transport == null) transport = "UDP";
        else if (transport == "") transport = "UDP";
        if (transport.compareToIgnoreCase("UDP") != 0 &&
        transport.compareToIgnoreCase("TCP") != 0)  {
            throw new IllegalArgumentException(hop);
        }
        
        stringTokenizer = new StringTokenizer(hostPort+":");
        host = stringTokenizer.nextToken(":");
        if (host == null || host.equals( "") )
            throw new IllegalArgumentException("no host!");
        String portString = null;
        try {
            portString = stringTokenizer.nextToken(":");
        } catch (NoSuchElementException ex) {
            // Ignore.
        }
        if (portString == null || portString.equals("")) {
            port = 5060;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad port spec");
            }
        }
    }

}

