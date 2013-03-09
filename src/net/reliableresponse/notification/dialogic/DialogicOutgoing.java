/*
 * Created on Jan 6, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.dialogic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.threebit.jvr.DV_TPT;
import net.threebit.jvr.JVRException;
import net.threebit.jvr.TN_GEN;
import net.threebit.jvr.dx;
import net.threebit.jvr.jvr;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class DialogicOutgoing extends Thread {

	Vector messages;

	int dxHandle;

	String deviceName;

	private static DialogicOutgoing instance = null;

	private boolean initialized;

	TN_GEN[] tones = new TN_GEN[12];

	DV_TPT[] tpt = null;

	private DialogicOutgoing() {
		initialized = false;
		messages = new Vector();
		start();
	}

	/**
	 *  
	 */
	private void initialize() {
		try {
			deviceName = BrokerFactory.getConfigurationBroker().getStringValue(
					"dialogic.outgoing.boardname", "dxxxB1C2");

			for (int i = 0; i < 10; i++) {
				dxHandle = dx.open(deviceName, 0);
				dx.sethook(dxHandle, jvr.DX_OFFHOOK, dx.EV_SYNC);
				dx.close(dxHandle);
				dxHandle = dx.open(deviceName, 0);
				dx.sethook(dxHandle, jvr.DX_ONHOOK, dx.EV_SYNC);
				dx.close(dxHandle);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
			dxHandle = dx.open(deviceName, 0);

			// These are the outgoing tones
			// *sheesh* you'd think Dialogic would ship these!
			tones[0] = dx.bldtngen(941, 1336, -10, -10, 50);
			tones[1] = dx.bldtngen(697, 1209, -10, -10, 50);
			tones[2] = dx.bldtngen(697, 1336, -10, -10, 50);
			tones[3] = dx.bldtngen(697, 1477, -10, -10, 50);
			tones[4] = dx.bldtngen(770, 1209, -10, -10, 50);
			tones[5] = dx.bldtngen(770, 1336, -10, -10, 50);
			tones[6] = dx.bldtngen(770, 1477, -10, -10, 50);
			tones[7] = dx.bldtngen(852, 1209, -10, -10, 50);
			tones[8] = dx.bldtngen(852, 1336, -10, -10, 50);
			tones[9] = dx.bldtngen(852, 1477, -10, -10, 50);
			tones[10] = dx.bldtngen(941, 1209, -10, -10, 50);
			tones[11] = dx.bldtngen(941, 1477, -10, -10, 50);

			tpt = DV_TPT.newArray(1);
			tpt[0].tp_type = dx.IO_EOT;
			tpt[0].tp_termno = dx.DX_MAXDTMF;
			tpt[0].tp_length = 1;
			tpt[0].tp_flags = dx.TF_MAXDTMF;

			BrokerFactory.getLoggingBroker().logDebug(
					"Dialogic Subsystem Initialized");

			initialized = true;
		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public static DialogicOutgoing getInstance() {
		if (instance == null) {
			instance = new DialogicOutgoing();
		}

		return instance;
	}

	public void addMessage(DialogicOutgoingMessage message) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Adding message to Dialogic outgoing queue: " + message);
		messages.addElement(message);
		BrokerFactory.getLoggingBroker().logDebug(
				"We have " + messages.size() + " in queue");
	}

	public String listenForTone() {
		StringBuffer tones = new StringBuffer();

		DV_TPT digit_tpt[] = DV_TPT.newArray(3);
		digit_tpt[0].tp_type = dx.IO_CONT;
		digit_tpt[0].tp_termno = dx.DX_MAXTIME;
		digit_tpt[0].tp_length = 100;
		digit_tpt[0].tp_flags = dx.TF_MAXTIME;
		digit_tpt[1].tp_type = dx.IO_CONT;
		digit_tpt[1].tp_termno = dx.DX_LCOFF;
		digit_tpt[1].tp_length = 1;
		digit_tpt[1].tp_flags = dx.TF_LCOFF;
		digit_tpt[2].tp_type = dx.IO_EOT;
		digit_tpt[2].tp_termno = dx.DX_MAXDTMF;
		digit_tpt[2].tp_length = 1;
		digit_tpt[2].tp_flags = dx.TF_MAXDTMF;

		try {
			return dx.getdig(dxHandle, digit_tpt, null, dx.EV_SYNC);
		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		return tones.toString();
	}

	public void run() {
		BrokerFactory.getLoggingBroker().logDebug(
				"Running the Dialogic outgoing queue processor");
		if (!initialized)
			initialize();

		while (true) {
			while (messages.size() <= 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			DialogicOutgoingMessage message = (DialogicOutgoingMessage) messages
					.elementAt(0);
			Notification notification = message.getNotification();
			if (makeCall(message.getPhoneNumber())) {
				if (message instanceof DialogicAudioMessage) {
					playWave(((DialogicAudioMessage) message).getWaveFile());
					String tone = listenForTone();
					if (tone.startsWith("1")) {
						notification.setStatus(Notification.CONFIRMED, notification.getRecipient());
						notification.addMessage(new NotificationMessage(
								"Message confirmed via telephone", message
										.getPhoneNumber(), new Date()));
						playWave(new ConfirmedDialogicMessage(null)
								.getWaveFilename());
					} else if (tone.startsWith("2")) {
						EscalationThread escThread = EscalationThreadManager
								.getInstance().getEscalationThread(
										notification.getID());
						if (escThread != null) {
							escThread
									.pass(((Group) notification.getRecipient())
											.getMembers()[escThread
											.getRecipientNumber()]);
							notification.addMessage(new NotificationMessage(
									"Message passed via telephone", message
											.getPhoneNumber(), new Date()));
							playWave(new PassedDialogicMessage(null)
									.getWaveFilename());
						}
					}
				} else if (message instanceof DialogicPage) {
					page(((DialogicPage) message).getPage());
				}
			} else {
				if (message instanceof DialogicAudioMessage) {
					message.getNotification().addMessage(
							"Sending via telephone at "
									+ message.getPhoneNumber() + " failed",
							null);
					BrokerFactory.getLoggingBroker().logWarn(
							"Sending via telephone at "
									+ message.getPhoneNumber() + " failed");
				} else if (message instanceof DialogicPage) {
					message.getNotification().addMessage(
							"Sending numeric page to "
									+ message.getPhoneNumber() + " failed",
							null);
					BrokerFactory.getLoggingBroker().logWarn(
							"Sending numeric page to "
									+ message.getPhoneNumber() + " failed");
				}
			}

			hangup();
			messages.remove(0);
		}
	}

	private boolean makeCall(String phoneNumber) {
		try {
			dx.sethook(dxHandle, jvr.DX_OFFHOOK, dx.EV_SYNC);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

			BrokerFactory.getLoggingBroker().logDebug(
					"Dialogic Dialing " + phoneNumber);
			long cpterm = dx.dial(dxHandle, phoneNumber, null, dx.EV_SYNC
					| dx.DX_CALLP);

			if (cpterm == dx.CR_BUSY) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=busy");
			} else if (cpterm == dx.CR_CEPT) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=cept");
			} else if (cpterm == dx.CR_CNCT) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=cnct");
			} else if (cpterm == dx.CR_ERROR) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=error");
			} else if (cpterm == dx.CR_FAXTONE) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=faxtone");
			} else if (cpterm == dx.CR_NOANS) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=noans");
			} else if (cpterm == dx.CR_NODIALTONE) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=nodialtone");
			} else if (cpterm == dx.CR_NORB) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=norb");
			} else if (cpterm == dx.CR_STOPD) {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=stopd");
			} else {
				BrokerFactory.getLoggingBroker().logDebug("cpterm=" + cpterm);
			}
			if ((cpterm == dx.CR_CNCT) || (cpterm == 0)) {
				return true;
			} else {
				return false;
			}

		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return false;
		}
	}

	private void hangup() {
		try {
			BrokerFactory.getLoggingBroker().logDebug(
					"Setting Dialogic on-hook");
			dx.sethook(dxHandle, jvr.DX_ONHOOK, dx.EV_SYNC);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			dx.close(dxHandle);
			dxHandle = dx.open(deviceName, 0);
		} catch (JVRException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
	}

	public void playWave(String wav) {
		try {
			dx.playwav(dxHandle, wav, tpt, dx.EV_SYNC);
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public void page(String numbers) {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		try {
			for (int i = 0; i < numbers.length(); i++) {
				char number = numbers.charAt(i);
				int numAsInt = number - '0';
				if ((numAsInt >= 0) && (numAsInt <= 9)) {
					BrokerFactory.getLoggingBroker().logDebug(
							"Playing tone " + numAsInt);
					dx.playtone(dxHandle, tones[numAsInt], tpt, dx.EV_SYNC);
					Thread.sleep(500);
				}

				if (number == '*') {
					BrokerFactory.getLoggingBroker().logDebug("Playing tone *");
					dx.playtone(dxHandle, tones[10], tpt, dx.EV_SYNC);
					Thread.sleep(500);
				}

				if (number == '#') {
					BrokerFactory.getLoggingBroker().logDebug("Playing tone #");
					dx.playtone(dxHandle, tones[11], tpt, dx.EV_SYNC);
					Thread.sleep(500);
				}
			}
		} catch (JVRException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		} catch (InterruptedException e1) {
			BrokerFactory.getLoggingBroker().logError(e1);
		}
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		DialogicOutgoing dia = DialogicOutgoing.getInstance();
		String cellNumber = "97205308877";
		String pagerNumber = "918774650793";

		DialogicAudioMessage audio = new DialogicAudioMessage(cellNumber,
				"/home/drig/workspace/Paging/sound/simpsons/homer_simpson_bologna.wav");
		DialogicPage page = new DialogicPage(pagerNumber, "112345##");

		dia.addMessage(audio);
		dia.addMessage(page);
		dia.addMessage(audio);

		System.out.println("Press enter to add 2 new messages");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		dia.addMessage(page);
		dia.addMessage(audio);
	}

}