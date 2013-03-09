/*
 * Created on Jul 25, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.providers;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.device.TAPDevice;
import net.reliableresponse.notification.device.TwoWayPagerDevice;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.util.StringUtils;


public class TAPNotificationProvider extends AbstractNotificationProvider implements SerialPortEventListener {
	
	// ASCII defines
	final String ESC = ""+(char)(0x1B);
	final String STX = ""+(char)(2);
	final String ETX = ""+(char)(3);
	final String ETB = ""+(char)(0x17);
	
/*	
	String phoneNumber = "18002506325";
	phoneNumber = "18009464644";
*/
	//String phoneNumber = "18009464644";
	String phoneNumber = "18002506325";
	boolean hasData;
	int speed = 2400;
	String parity = "E";
	int stopbits = 1;
	int databits = 7;
	String password = "";
	
	public TAPNotificationProvider(String phoneNumber, int speed, int databits, String parity, int stopbits) {
		this.phoneNumber = phoneNumber;
		this.speed = speed;
		this.databits = databits;
		this.parity = parity;
		this.stopbits = stopbits;
		this.hasData = false;
	}
	
	public TAPNotificationProvider() {
		
	}

	public void init(Hashtable params) throws NotificationException {
		if (!ClusteredServiceManager.getInstance().willRun("MSN")) {
			return;
		}
		phoneNumber = (String)params.get("Phone Number");
		try {
			databits = Integer.parseInt ((String)params.get("Data Bits"));
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
		}
		
		try {
			stopbits = Integer.parseInt ((String)params.get("Stop Bits"));
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
		}
		
		parity = (String)params.get("Parity");
		
		hasData = false;
	}
	
	

	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			hasData = true;
		}
	}

	private int getHigh(int nibble) {
		int ret = nibble >> 4;
		if (ret < 0) ret +=256;
		
		return ret;
	}

	private int getLow(int nibble) {
		int ret = (int)(nibble & 0xF);
		if (ret < 0) ret +=256;
		
		return ret;
	}

	public String getChecksum (String pagerNum, String message) {
		char[] chars = {'0','1','2','3','4','5','6','7','8','9',':',';','<','=','>','?'};
		
		int total = 2;
		byte[] bytes = pagerNum.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 0) {
				total += 256;
			}
			total += bytes[i];
		}
		total +=13;
		bytes = message.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 0) {
				total += 256;
			}
			total += bytes[i];
		}
		total +=13;
		total +=3;
		
		StringBuffer ret = new StringBuffer();
		ret.append (chars[(total>>8)&0xf]);
		ret.append (chars[(total>>4)&0xf]);
		ret.append (chars[total&0xf]);
		return ret.toString();
	}
	
	private String[] getMessages (DataInputStream inputReader) throws IOException, NotificationException {
		Vector messages = new Vector();
		
		boolean gotControlCode = false;
		while (!gotControlCode) {
			String line = inputReader.readLine();
			BrokerFactory.getLoggingBroker().logDebug("Message line = "+line);
			byte[] bytes = line.getBytes();
			if (bytes.length == 1) {
				if (bytes[0] == 6) {
					// ACK
					gotControlCode = true;
					BrokerFactory.getLoggingBroker().logDebug("Got ACK from TAP interface");
				} else if (bytes[0] == 30) {
					// RS
					gotControlCode = true;
					BrokerFactory.getLoggingBroker().logDebug("Got RS from TAP interface");
					throw new NotificationException(NotificationException.FAILED, "Unknown number");
				} else if (bytes[0] == 21) {
					// NAK
					gotControlCode = true;
					BrokerFactory.getLoggingBroker().logInfo("Got NACK from TAP interface.  Treating as ACK");
					throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Retry");
				} else if (bytes[0] == 4) {
					// EOT
					throw new NotificationException(NotificationException.FAILED, "Server error");
				}
			} else {
				messages.addElement(line);
			}
		}
		return (String[])messages.toArray(new String[0]);
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		if (!ClusteredServiceManager.getInstance().willRun("TAP")) {
			ClusteredServiceManager.getInstance().sendNotificationToDevice
			(notification,
					"TAP",  
					notification.getDisplayText(), 
					device.getUuid());
			return new Hashtable();
		}
		
		boolean portFound = false;
		SerialPort serialPort;
	    PrintStream outputWriter;
	    DataInputStream inputReader;
	    boolean	outputBufferEmptyFlag = false;
		String  defaultPort = BrokerFactory.getConfigurationBroker().getStringValue("modem.port", "/dev/ttyS0");
		
		if (!(device instanceof PagerDevice)) {
			throw new NotificationException(NotificationException.FAILED, "Supplied device isn't a pager");
		}

		String pagerNumber = ((PagerDevice)device).getNormalizedNumber();
		
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
		    CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

		    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

			if (portId.getName().equals(defaultPort)) {
			    BrokerFactory.getLoggingBroker().logDebug("Using modem on port "+defaultPort+" with "+databits+parity+stopbits);

			    portFound = true;

			    try {
				serialPort = 
				    (SerialPort) portId.open("Reliable Response Notification", 2000);
			    } catch (PortInUseException e) {
			    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Modem in use");
			    } 

			    try {
			    	int parityInt = SerialPort.PARITY_EVEN;
			    	if (parity.equals ("N")) {
			    		parityInt = SerialPort.PARITY_NONE;
			    	} else if (parity.equals ("O")) {
			    		parityInt = SerialPort.PARITY_ODD;
			    	}
			    	serialPort.setSerialPortParams(speed, 
							       databits, 
							       stopbits, 
							       parityInt);
			    } catch (UnsupportedCommOperationException e) {
			    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Temporary modem problem");
			    }
		
			    try {
			    	outputWriter = new PrintStream(serialPort.getOutputStream());
			    	inputReader = new DataInputStream(serialPort.getInputStream());
			    } catch (IOException e) {
			    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Could not open modem");
			    }

			    try {
			    	// Write the modem init script
			    	String modemInitString = BrokerFactory.getConfigurationBroker().getStringValue("modem.init", "ATH");
			    	writeToModem(outputWriter, inputReader, modemInitString, "OK");
			    	writeToModem(outputWriter, inputReader, "ATZ", "OK");
			    	writeToModem(outputWriter, inputReader, "ATDT"+phoneNumber, "CONNECT");

			    	
			    	// Start the section where we look for ID=
				    int totalTime = 0;
				    int count = 0;
				    String idLine = "";
				    
				    while ((idLine.toLowerCase().indexOf("id=")<0) && (count <=3)) {
				    	outputWriter.print ("\r");
					    outputWriter.flush();
					    BrokerFactory.getLoggingBroker().logDebug("TAP Sent blank line");
				    	while ((!hasData) && (totalTime <= 2000)) {
				    		// sleep 1/10 of a second to wait for data
				    		Thread.sleep(100);
				    		totalTime +=100;
						    BrokerFactory.getLoggingBroker().logDebug("Waiting for response");
				    	}
					    BrokerFactory.getLoggingBroker().logDebug("Got some text, "+inputReader.available());
				    	byte[] b = new byte[1024];
				    	inputReader.read(b);
				    	idLine = new String(b);
					    BrokerFactory.getLoggingBroker().logDebug("idLine = "+idLine);
				    	count++;
				    	totalTime = 0;
				    }
				    
				    if (idLine.toLowerCase().indexOf("id=")<0) {
				    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Error during send to pager");
				    }
				    //idLine = idLine.substring(3);
				    outputWriter.print (ESC+"PG1000000\r");
				    BrokerFactory.getLoggingBroker().logDebug("Wrote ESC");
				    
				    String[] messageSequence = getMessages(inputReader);
				    readOK(inputReader, ESC+"[p");
				    
				    String textmessage = notification.getDisplayText().replace('\n', ' ');
				    //String textmessage = "test pager";
				    StringBuffer message = new StringBuffer();
				    message.append(STX);
				    message.append(pagerNumber);
				    message.append("\r");
				    message.append(textmessage);
				    message.append("\r");
				    message.append(ETX);
				    message.append(getChecksum(pagerNumber, textmessage));
				    message.append("\r");
				    
				    count = 0;
				    boolean sent = false;
				    while ( (count < 10) && (!sent)) {
						count++;
				    	try {
				    		BrokerFactory.getLoggingBroker().logDebug("Message: "+StringUtils.toHexString(message.toString().getBytes()));
							outputWriter.write(message.toString().getBytes());
							BrokerFactory.getLoggingBroker().logDebug("Write message: "+pagerNumber+":"+textmessage);
   
							String[] responseMessageSequence = getMessages(inputReader);
							sent = true;
						} catch (NotificationException e) {
							if (e.getCode() == NotificationException.TEMPORARILY_FAILED) {
								sent = false;
							}
							Thread.sleep(1000);
						}
				    }

			    } catch (InterruptedException e) {
			    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Error during send to pager");
			    } catch (IOException e) {
			    	throw new NotificationException(NotificationException.TEMPORARILY_FAILED, "Error during send to pager");
			    }

			    try {
			       Thread.sleep(1000);  // Be sure data is xferred before closing
			    } catch (Exception e) {}
			    serialPort.close();
			} 
		    } 
		} 

		if (!portFound) {
		    BrokerFactory.getLoggingBroker().logWarn("port " + defaultPort + " not found.");
		} 
		return null;
	}

	/**
	 * @param inputReader
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws NotificationException
	 */
	private void readOK(DataInputStream inputReader, String comparison) throws InterruptedException, IOException, NotificationException {
		
		Thread.sleep(300);  //wait for init to complete
		String line = null;
		while ( (line = inputReader.readLine()).equals ("")) {
			
		}
	    BrokerFactory.getLoggingBroker().logDebug("TAP READ: "+line);
		int count = 0;
		while (count < 10) {
		    if (line.toLowerCase().indexOf(comparison.toLowerCase())>=0) {
		    	BrokerFactory.getLoggingBroker().logDebug("Matched expected, "+comparison);
		    	return;
		    } else {
		    	BrokerFactory.getLoggingBroker().logDebug("Did not match expected, "+comparison);
		    }
			line = inputReader.readLine();
		    BrokerFactory.getLoggingBroker().logDebug("TAP READ: "+line);
			count++;
		}

	}
	
	public boolean writeToModem (PrintStream out, DataInputStream in, String output, String expected) throws IOException {
		BrokerFactory.getLoggingBroker().logDebug("TAP Out: "+output);
		out.print (output+"\r\n");
		out.flush();
		try {
			Thread.sleep (300);
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		String line = "";
		boolean found = false;
		
		while (!found) {
			line = in.readLine();
			BrokerFactory.getLoggingBroker().logDebug("read: "+line);
			if (line != null) {
				if (line.length()>0) {
					if (!line.startsWith(output)) {
						found = line.toLowerCase().indexOf(expected.toLowerCase())>=0;
						return found;
					}
				}
			}
		}
		return false;
	}

	public Hashtable getParameters(Notification notification, Device device) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	public boolean cancelPage(Notification notification) {
		return false;
	}

	public String getName() {
		return "One-way modem pager";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		TAPDevice device = new TAPDevice();
		Hashtable options = new Hashtable ();
		options.put("Pager Number", "7205308877");
		options.put("Provider", "Cingular");
		device.initialize(options);

		NotificationProvider provider = device.getNotificationProvider();
		Notification notif = new Notification(null, new UnknownUser(),  new EmailSender("drig@noses.org"),
				"test", "this is a test of cingular tap");
		
		provider.sendNotification(notif, device);
		
		//800-250-6325
	}

}
