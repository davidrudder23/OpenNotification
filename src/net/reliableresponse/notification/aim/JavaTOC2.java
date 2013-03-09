package net.reliableresponse.notification.aim;

import java.net.*;
import java.io.*;
import java.util.*;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
  * The JavaTOC framework is a set of classes used to allow
  * a Java program to communicate with AOL's TOC2 protocol.
  * The Chatable interface and JavaTOC2 classes can easily
  * be moved to any program needing TOC abilities.
  *
  *  Copyright 2005 by Jeff Heaton
  *
  *   This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * @author Jeff Heaton(http://www.heatonresearch.com)
  * @version 2.0
  */


public class JavaTOC2 {

   /**
    * The host address of the TOC server
    */
   public String tocHost = "toc.oscar.aol.com";

   /**
    * The port used to connect to the TOC server
    */
   public int tocPort = 9898;

   /**
    * The OSCAR authentication server
    */
   public String authHost = "login.oscar.aol.com";

   /**
    * The OSCAR authentication server's port
    */
   public int authPort = 5190;

   /**
    * What language to use
    */
   public String language = "english";

   /**
    * The version of the client
    */
   public String version = "TIC:JavaTOC by Jeff Heaton";

   /**
    * The string used to "roast" passwords. See
    * the roastPassword method for more info.
    */
   public String roastString = "Tic/Toc";

   /**
    * The sequence number used for FLAP packets.
    */
   protected short sequence;

   /**
    * The connection to the TOC server.
    */
   protected Socket connection;

   /**
    * An InputStream to the connection
    */
   protected InputStream is;

   /**
    * An OutputStream to the connection
    */
   protected OutputStream os;

   /**
    * Screen name of current user
    */
   protected String id;

   /**
    * The ChatBuddies object that owns this object.
    */
   protected Chatable owner;

   /**
    * The ID number for a SIGNON packet(FLAP)
    */
   public static final int SIGNON = 1;

   /**
    * The ID number for a DATA packet (flap)
    */
   public static final int DATA = 2;
   
   /**
    * This stores the password for reconnecting
    */
   String password;

   /**
    * The constructor. Specifies the Chatable interface class
    * to report to.
    *
    * @param owner The object to report events to
    */
   public JavaTOC2(Chatable owner)
   {
     this.owner = owner;
   }

   /**
    * Log in to TOC
    *
    * @param id The screen name to login with
    * @param password The screen name's password
    * @return true on success
    * @exception java.io.IOException
    */
   public boolean login(String id,String password)
   throws IOException
   {
	   BrokerFactory.getLoggingBroker().logDebug("Logging into AIM as "+id);
     this.id = id;
     this.password = password;
     connection = new Socket(tocHost,tocPort);
     is = connection.getInputStream();
     os = connection.getOutputStream();
     sendRaw("FLAPON\r\n\r\n");
     getFlap();
     sendFlapSignon();
     String command = "toc2_signon " +
                      authHost + " " +
                      authPort + " " +
                      id + " " +
                      roastPassword(password) + " " +
                      language + " \"" +
                      this.version + "\" 160 " + calculateCode(id,password);



     sendFlap(DATA,command);
     String str = getFlap();

     if ( str.toUpperCase().startsWith("ERROR:") ) {
       handleError(str);
       return false;
     }

     this.sendFlap(DATA,"toc_add_buddy " + this.id);
     this.sendFlap(DATA,"toc_init_done");
     this.sendFlap(DATA,"toc_set_caps 09461343-4C7F-11D1-8222-444553540000 09461348-4C7F-11D1-8222-444553540000");
     this.sendFlap(DATA,"toc_add_permit ");
     this.sendFlap(DATA,"toc_add_deny ");
     return true;
   }

   /**
    * Logout of toc and close the socket
    */
   public void logout()
   {
     try {
       connection.close();
       is.close();
       os.close();
     } catch ( IOException e ) {

     }
   }

   /**
    * Called to roast the password.
    *
    * Passwords are roasted when sent to the host.  This is done so they
    * aren't sent in "clear text" over the wire, although they are still
    * trivial to decode.  Roasting is performed by first xoring each byte
    * in the password with the equivalent modulo byte in the roasting
    * string.  The result is then converted to ascii hex, and prepended
    * with "0x".  So for example the password "password" roasts to
    * "0x2408105c23001130"
    *
    * @param str The password to roast
    * @return The password roasted
    */
   protected String roastPassword(String str)
   {
     byte xor[] = roastString.getBytes();
     int xorIndex = 0;
     String rtn = "0x";

     for ( int i=0;i<str.length();i++ ) {
       String hex = Integer.toHexString(xor[xorIndex]^(int)str.charAt(i));
       if ( hex.length()==1 )
         hex = "0"+hex;
       rtn+=hex;
       xorIndex++;
       if ( xorIndex==xor.length )
         xorIndex=0;
     }
     return rtn;
   }

   /**
    * Calculate a login security code from the user id and
    * password.
    *
    * @param uid The user id to encode
    * @param pwd The password to encoude
    * @return The code, which is used to login
    */
   protected int calculateCode(String uid,String pwd)
   {
     int sn = uid.charAt(0)-96;
     int pw = pwd.charAt(0)-96;

     int a = sn * 7696 + 738816;
     int b = sn * 746512;
     int c = pw * a;

     return( c - a + b + 71665152 );
   }

   /**
    * Send a string over the socket as raw bytes
    *
    * @param str The string to send
    * @exception java.io.IOException
    */
   protected void sendRaw(String str)
   throws IOException
   {
     os.write(str.getBytes());
   }

   /**
    * Write a little endian word
    *
    * @param word A word to write
    * @exception java.io.IOException
    */
   protected void writeWord(short word)
   throws IOException
   {
     os.write((byte) ((word >> 8) & 0xff) );
     os.write( (byte) (word & 0xff) );

   }

   /**
    * Send a FLAP signon packet
    *
    * @exception java.io.IOException
    */
   protected void sendFlapSignon()
   throws IOException
   {
     int length = 8+id.length();
     sequence++;
     os.write((byte)'*');
     os.write((byte)SIGNON);
     writeWord(sequence);
     writeWord((short)length);

     os.write(0);
     os.write(0);
     os.write(0);
     os.write(1);

     os.write(0);
     os.write(1);

     writeWord((short)id.length());
     os.write(id.getBytes());
     os.flush();

   }

   /**
    * Send a FLAP packet
    *
    * @param type The type DATA or SIGNON
    * @param str The string message to send
    * @exception java.io.IOException
    */
   public void sendFlap(int type,String str)
   throws IOException
   {
     int length = str.length()+1;
     sequence++;
     os.write((byte)'*');
     os.write((byte)type);
     writeWord(sequence);
     writeWord((short)length);
     os.write(str.getBytes());
     os.write(0);
     os.flush();
   }

   /**
    * Get a FLAP packet
    *
    * @return The data as a string
    * @exception java.io.IOException
    */
   protected String getFlap()
   throws IOException
   {
	 int firstByte = -1;
	 try {
		firstByte = is.read();
	} catch (IOException e1) {
		BrokerFactory.getLoggingBroker().logWarn(e1);
	}
	 if (firstByte == -1) {
		 // The connection is closed.  1st sleep, then try to reconnect
		 try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
		}
		login (id, password);
	 } else if ( firstByte !='*' ) {
       return null;
	 }
     is.read();
     is.read();
     is.read();
     int length = (is.read()*0x100)+is.read();
     byte b[] = new byte[length];
     is.read(b);
     return new String(b);
   }
   /**
    * Begin processing all TOC events and sending them to the
    * Chatable class. Usually called from a thread.
    *
    * @exception java.io.IOException
    */

   public void processTOCEvents()
   throws IOException
   {
     for ( ;; ) {
       String str = this.getFlap();
       if ( str==null )
         continue;

       if ( str.toUpperCase().startsWith("IM_IN2:") ) {
         handleIM(str);
       } else if ( str.toUpperCase().startsWith("ERROR:") ) {
         handleError(str);
       } else {
         owner.unknown(str);
       }
     }
   }

   /**
    * Extract the next element, bounded by a ':', and
    * return that element. This has the advantage over StringTokenizer,
    * in that at any point you can pull the remaining string, just
    * by using what is left of the StringBuffer. This is good
    * for when there is a fixed number of tokens, yet the remaining
    * String can still use the token. For example:
    *
    * JeffHeatonDotCom:F:F:Please remember to get: this, that and that.
    *
    * The final : is not a token because this string only has 4 fields.
    *
    * @param str The string to parse, will be modified
    * @return The next element found
    */
   protected String nextElement(StringBuffer str)
   {
     String result="";
     int i = str.indexOf(":",0);
     if(i==-1)
     {
       result = str.toString();
       str.setLength(0);
     }
     else
     {
       result = str.substring(0,i).toString();
       String a = str.substring(i+1);
       str.setLength(0);
       str.append(a);
     }
     return result;
   }

   /**
    * Parse an error packet and send it back to the Chatable class
    *
    * @param str The error
    */
   protected void handleError(String str)
   {
     StringBuffer sb = new StringBuffer(str);
     String e = nextElement(sb);
     String v = nextElement(sb);
     owner.error( e,v);
   }

   /**
    * Parse an instant message and send it back to the Chatable
    * class
    *
    * @param str The instant message
    */
   protected void handleIM(String str)
   {
     StringBuffer sb = new StringBuffer(str);

     nextElement(sb);

     // get from
     String from = nextElement(sb);

     // get a
     String a = nextElement(sb);

     // get b
     String b = nextElement(sb);

     // get message
     String message = sb.toString();

     owner.im(from,message);
   }

   /**
    * Send a IM
    *
    * @param to Screen name to send an IM to
    * @param msg The instant message
    */
   public void send(String to,String msg)
   {
     try {
       this.sendFlap(DATA,"toc_send_im " + normalize(to) + " \"" +
encode(msg) + "\"");
     } catch ( java.io.IOException e ) {
     }
   }
   /**
    * Called to normalize a screen name. This removes all spaces
    * and converts the name to lower case.
    *
    * @param name The screen name
    * @return The normalized screen name
    */

   protected String normalize(String name)
   {
     String rtn="";
     for ( int i=0;i<name.length();i++ ) {
       if ( name.charAt(i)==' ' )
         continue;
       rtn+=Character.toLowerCase(name.charAt(i));
     }

     return rtn;

   }

   /**
    * Called to encode a message. Convert carige returns to <br>'s
    * and put \'s infront of quotes, etc.
    *
    * @param str The string to be encoded
    * @return The string encoded
    */
   protected String encode(String str)
   {
     String rtn="";
     for ( int i=0;i<str.length();i++ ) {
       switch ( str.charAt(i) ) {
       case '\r':
         rtn+="<br>";
         break;
       case '{':
       case '}':
       case '\\':
       case '"':
         rtn+="\\";

       default:
         rtn+=str.charAt(i);
       }
     }
     return rtn;

   }


}