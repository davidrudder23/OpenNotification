/*
 * Created on Nov 22, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.providers;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import com.echomine.common.SendMessageFailedException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.SMSDevice;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.usermgmt.User;

public class SMSNotificationProvider extends AbstractNotificationProvider {

	private static SMSNotificationProvider instance;

	public SMSNotificationProvider() {
		SMSInternalProvider.getInstance();
	}

	public void init(Hashtable params) throws NotificationException {
		if (!ClusteredServiceManager.getInstance().willRun("SMS")) {
			return;
		}
		SMSInternalProvider.getInstance();
	}

	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		if (!(device instanceof SMSDevice)) {
			throw new NotificationException(NotificationException.FAILED,
					"Supplied device isn't a cell phone");
		}
		if (!ClusteredServiceManager.getInstance().willRun("SMS")) {
			ClusteredServiceManager.getInstance().sendNotificationToDevice
			(notification,
					"SMS",  
					notification.getDisplayText(), 
					device.getUuid());
			return new Hashtable();
		}
		StringBuffer text = new StringBuffer();
		text.append(notification.getDisplayText());
		text.append("\n");
		text.append("Reply with: ");
		String[] responses = notification.getSender().getAvailableResponses(
				notification);
		for (int i = 0; i < responses.length; i++) {
			text.append("\n    " + responses[i] + " " + notification.getUuid());
		}

		SMSInternalProvider.getInstance().sendSMS(
				((SMSDevice) device).getNormalizedNumber(), text.toString());
		return getParameters(notification, device);
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable hashtable = new Hashtable();
		return hashtable;
	}

	public String[] getResponses(Notification notification) {
		SMSInternalProvider.getInstance().getNewMessages();
		return new String[0];
	}

	public boolean cancelPage(Notification notification) {
		return false;
	}

	public String getName() {
		return "SMS";
	}

}

class SMSInternalProvider {
	private static SMSInternalProvider instance;

	private SerialPort serialPort;

	private PrintStream outputWriter;

	private DataInputStream inputReader;

	Object serialPortLock;

	private SMSInternalProvider() {

	}

	public static SMSInternalProvider getInstance() {
		if (instance == null) {
			instance = new SMSInternalProvider();
			instance.init();
		}
		return instance;
	}

	public void init() {
		BrokerFactory.getLoggingBroker().logDebug(
				"Before init, serial port=" + serialPort);
		if (serialPort == null) {
			serialPortLock = new Object();
			try {
				serialPort = openSerialPort();
				BrokerFactory.getLoggingBroker().logDebug(
						"After init, serial port=" + serialPort);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	/**
	 * This is run when we get some text on the serial port
	 */
public void getNewMessages() {
		synchronized (serialPortLock) {
			BrokerFactory.getLoggingBroker().logDebug("Checking for new SMS messages");
			try {
				// Print out the command to read all unread messages
				outputWriter.println ("AT+CMGL=\"REC UNREAD\"");
				outputWriter.flush();
				String line = "";
				
				while (line.equals("")) {
					line = inputReader.readLine();
					BrokerFactory.getLoggingBroker().logDebug("Read from SMS modem: "+line);
				}
				
				while (!line.equalsIgnoreCase("OK")) { 
					BrokerFactory.getLoggingBroker().logDebug(
							"Read from SMS modem: " + line);
					if (line.toLowerCase().startsWith("+CMGL:")) {
						String info = line;
						String response = inputReader.readLine();
						BrokerFactory.getLoggingBroker().logDebug(
							"Response: " + response);

						// Parse out the return phone number and find the user
						// with that phone
						StringTokenizer tok = new StringTokenizer(info, ",");
						tok.nextElement();
						tok.nextElement();
						String phoneNum = (String)tok.nextElement();
						if (phoneNum != null) {
							if (phoneNum.startsWith("\"")) {
								phoneNum = phoneNum.substring(1, phoneNum
										.length());
							}
							if (phoneNum.endsWith("\"")) {
								phoneNum = phoneNum.substring(0, phoneNum
										.length() - 1);
							}

							phoneNum = SMSDevice.normalize(phoneNum);
							BrokerFactory.getLoggingBroker().logDebug(
									"Return phone # = " + phoneNum);
						} else {
							phoneNum = "UNKNOWN";
						}
						User user = new UnknownUser();
						User[] users = BrokerFactory
								.getUserMgmtBroker()
								.getUsersWithDeviceType(
										"net.reliableresponse.notification.device.SMSDevice");
						BrokerFactory.getLoggingBroker().logDebug(
								"We found " + users.length
										+ " users with SMS phones");
						if ((users != null) && (users.length > 0)) {
							for (int userNum = 0; userNum < users.length; userNum++) {
								List<Device> devices = users[userNum].getDevices();
								for (Device device: devices) {
									if (device instanceof SMSDevice) {
										SMSDevice smsDevice = (SMSDevice) device;
										if (smsDevice.getNormalizedNumber().equals(phoneNum)) {
											user = users[userNum];
										}
									}
								}
							}
						}

						// Do something with the text
						Pattern pattern = Pattern.compile("\\b(\\d{7})\\b");
						Matcher matcher = pattern.matcher(response);
						if (matcher.find()) {
							BrokerFactory.getLoggingBroker().logDebug(
									"Response=" + response);
							BrokerFactory.getLoggingBroker().logDebug(
									"Start=" + matcher.start());
							BrokerFactory.getLoggingBroker().logDebug(
									"End=" + matcher.end());
							String uuid = response.substring(matcher.start(),
									matcher.end());
							BrokerFactory.getLoggingBroker().logDebug(
									"Found uuid " + uuid);
							Notification notification = BrokerFactory
									.getNotificationBroker()
									.getNotificationByUuid(uuid);
							if (notification == null) {
								BrokerFactory
										.getLoggingBroker()
										.logInfo(
												"Could not find notification "
														+ uuid
														+ "  referenced by SMS message "
														+ response);
								return;
							}
							String[] responses = notification.getSender()
									.getAvailableResponses(notification);
							BrokerFactory.getLoggingBroker().logDebug(
									"We found " + responses.length
											+ " responses");
							for (int i = 0; i < responses.length; i++) {
								BrokerFactory.getLoggingBroker().logDebug(
										"Checking response " + responses[i]);
								if (response.toLowerCase().indexOf(
										responses[i].toLowerCase()) >= 0) {
									notification.getSender().handleResponse(
											notification, user, responses[i],
											response);
								}
							}
						}

						// Check for commands
						if (user != null) {
							String responseToAction = AbstractNotificationProvider
									.getResponseToAction(user, response);
							if (responseToAction != null) {
								try {
									sendSMS(phoneNum, responseToAction);
								} catch (Exception e) {
									BrokerFactory.getLoggingBroker()
											.logError(e);
								}
							}

						}
					}
					line = "";
					while (line.equals("")) {
						line = inputReader.readLine();
					}
				}
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}
	public boolean writeToModem(PrintStream out, DataInputStream in,
			String output, String expected) throws IOException {
		BrokerFactory.getLoggingBroker().logDebug("SMS Out: " + output);
		out.print(output + "\r\n");
		out.flush();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		String line = "";
		boolean found = false;

		while (!found) {
			line = in.readLine();
			BrokerFactory.getLoggingBroker().logDebug("read: " + line);
			if (line != null) {
				if (line.length() > 0) {
					if (!line.startsWith(output)) {
						found = line.toLowerCase().indexOf(
								expected.toLowerCase()) >= 0;
						return found;
					}
				}
			}
		}
		return false;
	}

	public void sendSMS(String phoneNumber, String message)
			throws NotificationException {
		BrokerFactory.getLoggingBroker().logDebug("Sending SMS message to "+phoneNumber);
		synchronized (serialPortLock) {
			if (serialPort == null) {
				serialPort = openSerialPort();
			}
			try {
				serialPort.notifyOnDataAvailable(false);
				if (!writeToModem(outputWriter, inputReader, "ATE0", "OK")) {
					outputWriter.print((char) 0x1a);

					if (!writeToModem(outputWriter, inputReader, "ATE0", "OK")) {
						serialPort = openSerialPort();
						if (!writeToModem(outputWriter, inputReader, "ATE0",
								"OK")) {
							throw new NotificationException(
									NotificationException.INTERNAL_ERROR,
									"Could not open serial port");
						}
					}
				}

				// clear out the input reader
				while (inputReader.available() > 0) {
					inputReader.read();
				}
				BrokerFactory.getLoggingBroker()
						.logDebug("Sending SMS message");
				outputWriter.println("AT+CMGS=\"" + phoneNumber + "\"");
				String outputMessage = message + (char) (0x1a);
				outputWriter.println(message);
				outputWriter.flush();
				// Let that go through before we sent the ctrl-z to finish the
				// message
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
				// Flush out any spurious >'s
				while (inputReader.available() > 0) {
					inputReader.read();
				}
				outputWriter.print((char) (0x1a));
				outputWriter.flush();
				String line = "";
				int count = 0;
				while ((line.equals("")) && (count < 5)) {
					line = inputReader.readLine();
					count++;
				}
				// if (!line.startsWith("+CMGS")) {
				// throw new NotificationException(NotificationException.FAILED,
				// "Bad modem response: "+line);
				// }
			} catch (IOException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				throw new NotificationException(
						NotificationException.TEMPORARILY_FAILED, e
								.getMessage());
			}
		}
	}

	/**
	 * @param device
	 * @throws NotificationException
	 */
	private SerialPort openSerialPort() throws NotificationException {
		SerialPort serialPort;
		String defaultPort = BrokerFactory.getConfigurationBroker()
				.getStringValue("modem.port", "/dev/ttyS0");

		Enumeration portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

				if (portId.getName().equals(defaultPort)) {
					BrokerFactory.getLoggingBroker().logDebug(
							"Using modem on port " + defaultPort + " with 8N1");

					try {
						serialPort = (SerialPort) portId.open(
								"Reliable Response Notification", 2000);

						// This is stupid. JavaComm needs to have this
						// System.out.println in
						// order to initialize properly
						System.out.println("setSerialPortParams()");

						serialPort.setSerialPortParams(2400,
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						outputWriter = new PrintStream(serialPort
								.getOutputStream());
						inputReader = new DataInputStream(serialPort
								.getInputStream());
						writeToModem(outputWriter, inputReader, "ATE0", "OK");
						writeToModem(outputWriter, inputReader, "AT+CNMI=2,0,0,0,0",
						"OK");

						return serialPort;
					} catch (IOException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						throw new NotificationException(
								NotificationException.INTERNAL_ERROR,
								"Can't get output stream");
					} catch (PortInUseException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						throw new NotificationException(
								NotificationException.TEMPORARILY_FAILED,
								"Modem in use");
					} catch (UnsupportedCommOperationException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						throw new NotificationException(
								NotificationException.TEMPORARILY_FAILED,
								"Temporary modem problem");
					}
				}
			}
		}

		return null;
	}
}
