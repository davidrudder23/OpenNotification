// process the incomming connection and send the message to all interested plugins

package net.reliableresponse.notification.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;

interface commands {
	int none = 0;

	int helo = 1;

	int quit = 2;

	int mailfrom = 3;

	int rcptto = 4;

	int data = 5;

	int datafinished = 6;
};

public class Process extends Thread {
	Socket clientSocket;

	// someone just tried to make a connection store the socket and wait for the
	// thread to handle it
	public Process(Socket PassedclientSocket) {
		clientSocket = PassedclientSocket;
	}

	// process the commands in a separete thread
	public void run() {
		int intLastCommand; // last command issued
		String strTo;
		Vector vPlugIns; // all known plugins
		Vector vActivePlugIns; // a plugin used to send this message
		int i;
		Vector vStarted;
		boolean bCleanExit; // did the client disconnect cleanly? - if not then the message is discarded
		String strFrom;

		// initilize variables
		intLastCommand = commands.none;
		vPlugIns = null;
		strTo = null;
		vStarted = null;
		vActivePlugIns = new Vector();
		strFrom = null; // who the mail is from
		BrokerFactory.getLoggingBroker().logWarn("smtp - connection started");
		bCleanExit = true; // assume a clean exit
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			String strIn;

			// make an instance of all the plugins
			vPlugIns = new Vector();
			for (i = 0; i < SMTP.vNamesOfPlugIns.length; i++) {
				vPlugIns.addElement((Class.forName(SMTP.vNamesOfPlugIns[i]))
						.newInstance());
				BrokerFactory.getLoggingBroker()
						.logWarn(
								"+++load "
										+ SMTP.vNamesOfPlugIns[i]);
			}

			// i'm ready...
			intLastCommand = commands.none;
			out.println("220 - Reliable Response Notification mail server");
			out.flush();
			// proccess commands till a quit is issued
			while (intLastCommand != commands.quit) {
				try {
					// read the next command
					strIn = MyTimeout.TimedReadLine(in, 1000 * 60 * 3);
					//               strIn = in.readLine();
					BrokerFactory.getLoggingBroker().logWarn("----" + strIn);
				} catch (Exception e) {
					// if can't read then the client dropped so assume a quit
					strIn = "QUIT";
				}

				// if read a null then drop the connecion since the client does not know what he's doing
				if (strIn == null) {
					strIn = "QUIT";
				}

				// interpret the command 
				if (strIn.toUpperCase().startsWith("HELO")) {
					// must start with hello - if not then the other guy is not talking to who he thinks he's talking to
					// now i'm ready for commands
					out.println("250 - calvin's mail server");
					out.flush();
					intLastCommand = commands.helo;
				} else if (strIn.toUpperCase().startsWith("RSET")) {
					if (intLastCommand != commands.none) {
						// start over
						vActivePlugIns = new Vector(); // reset the list of people to send mail to
						// back to the hello command
						intLastCommand = commands.helo;
						// ready to go
						out.println("250 - reliable response mail server");
						out.flush();
					} else {
						out.println("503 - rset not allowed here");
						out.flush();
					}
				} else if (strIn.toUpperCase().startsWith("MAIL FROM")) {
					// anyone can send us mail - should do a check somewhere that only allows non-local mail
					// to come from a valid user
					if (intLastCommand == commands.helo) {
						out.println("250 - ok");
						out.flush();
						// store who the mail is from
						strFrom = GrabAddress(strIn.substring(10, strIn
								.length()));
						intLastCommand = commands.mailfrom;
						BrokerFactory.getLoggingBroker().logWarn(
								"smtp.process.-- MailFrom: " + strFrom);
					} else {
						out.println("503 - mail from not allowed here");
						out.flush();
					}
				} else if (strIn.toUpperCase().startsWith("RCPT TO")) {
					if ((intLastCommand == commands.mailfrom)
							|| (intLastCommand == commands.rcptto)) {
						strTo = GrabAddress(strIn.substring(8, strIn.length()));
						BrokerFactory.getLoggingBroker().logWarn(
								"+++plugin.size: " + vPlugIns.size());
						// check if mail can be sent to the person, strip off the command first
						for (i = 0; i < vPlugIns.size(); i++) {
							BrokerFactory.getLoggingBroker().logWarn(
									"+++i: " + i);
							// check if mail can be sent from the user
							if (((MailHandler) vPlugIns.elementAt(i))
									.CheckFromUser(strFrom) == true)
								if (((MailHandler) vPlugIns.elementAt(i))
										.CheckToUser(strTo) == true) {
									MailHandler mh = (MailHandler) vPlugIns
											.elementAt(i).getClass()
											.newInstance();
									// add this plugin to the active plug-in list
									vActivePlugIns.addElement(mh);
									// since a new plugin was created let it also do a check
									mh.CheckFromUser(strFrom);
									mh.CheckToUser(strTo);
								}
						}
						// check if any plugin wants the mail
						if (vActivePlugIns.size() != 0) {
							out.println("250 - ok");
							intLastCommand = commands.rcptto;
							BrokerFactory.getLoggingBroker().logWarn(
									"mail can be sent");
						} else {
							// user is unknown - since not plugin could handle it
							out.println("550 - no such user");
							BrokerFactory.getLoggingBroker().logWarn(
									"no such user");
						}
						out.flush();
					} else {
						out.println("503 - rcpt to not allowed here");
						out.flush();
					}
				} else if (strIn.toUpperCase().startsWith("DATA")) {
					if (intLastCommand == commands.rcptto) {
						vStarted = new Vector();
						for (i = 0; i < vActivePlugIns.size(); i++) {
							if (((MailHandler) vActivePlugIns.elementAt(i))
									.Start()) {
								vStarted.addElement(new Integer(i));
							}
						}
						if (vStarted.size() > 0) {
							out.println("354 - send the mail");
							out.flush();
							intLastCommand = commands.data;
							strIn = MyTimeout.TimedReadLine(in, 1000 * 60 * 3);
							//                     strIn = in.readLine();
							//                     while(!strIn.startsWith("."))
							while (!strIn.equals(".")) {
								for (i = 0; i < vStarted.size(); i++)
									((MailHandler) vActivePlugIns
											.elementAt(((Integer) vStarted
													.elementAt(i)).intValue()))
											.Line(strIn);
								strIn = MyTimeout.TimedReadLine(in,
										1000 * 60 * 3);
								//                        strIn = in.readLine();
							}
							for (i = 0; i < vStarted.size(); i++)
								try {
								((MailHandler) vActivePlugIns
										.elementAt(((Integer) vStarted
												.elementAt(i)).intValue()))
										.End();
								} catch (Exception e) {
									BrokerFactory.getLoggingBroker().logError(e);
								}
							out.println("250 - ok");
							out.flush();
							intLastCommand = commands.datafinished;
						} else {
							out.println("550 - no plug in could start");
							out.flush();
						}
					} else {
						out.println("503 - data not allowed here");
						out.flush();
					}
				} else if (strIn.startsWith("NOOP")) {
					// just a check to see if were still here - which we are
					// outlook does not use this but this spec says it's a 
					// requirment for a simple smpt server - so it's here
					// however this is untested
					if (intLastCommand != commands.none) {
						out.println("250 - ok");
						out.flush();
					} else {
						out.println("503 - noop not allowed here");
						out.flush();
					}
				} else if (strIn.toUpperCase().startsWith("QUIT")) {
					intLastCommand = commands.quit;
				}
				// left as comment so other commands can be added later
				//            else if(strIn.startsWith(""))
				//            {
				//            }
				else {
					out.println("502 - unknown command");
					out.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// on any exception the message is over
			// if the last command is not datafinished then the message is thrown away
			// this is since some programs don't send the quit command instead they
			// just quit ex. qsmtp.java
			if (intLastCommand == commands.datafinished) {
				BrokerFactory.getLoggingBroker()
						.logWarn("error - message kept");
			} else {
				BrokerFactory.getLoggingBroker().logWarn(
						"error - message deleted");
				bCleanExit = false; // something went wrong
			}
		} finally {
			// always close the socket
			try {
				clientSocket.close();
				// tell everyone the connection was closed
				for (i = 0; i < vActivePlugIns.size(); i++)
					((MailHandler) vActivePlugIns.elementAt(i))
							.ConnectionClosed(bCleanExit);
			} catch (IOException e) {
				// ignore it since can't do anything about it
			}
		}
		BrokerFactory.getLoggingBroker().logWarn(
				"smtp - connection finished - awaiting next");
	}

	//
	// private internal stuff
	//

	// parse the to command and strip out the user name
	private String GrabAddress(String strToCmd) {
		String strAddress;
		strAddress = strToCmd;
		if (strAddress == null) strAddress = "";
		int intFoundStart;
		int intFoundEnd;

		// kill white space
		strToCmd = strToCmd.trim();

		// check if the address contains a < if so then just use whats inside
		// otherwise use the address given
		intFoundStart = strToCmd.indexOf('<');
		if (intFoundStart != -1) {
			// found it strip everything from inside
			intFoundEnd = strToCmd.indexOf('>');
			if (intFoundEnd != -1) {
				strAddress = strToCmd.substring(intFoundStart + 1, intFoundEnd);
			} else {
				strAddress = strToCmd.substring(intFoundStart + 1, strToCmd
						.length());
			}
		} else {
			// not found - use as is
			strAddress = strToCmd;
		}

		return strAddress;
	}
}

