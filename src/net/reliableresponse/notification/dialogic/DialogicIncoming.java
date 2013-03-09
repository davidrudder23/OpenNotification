/*
 * Created on Nov 26, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.dialogic;

import java.io.FileInputStream;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.threebit.jvr.DV_TPT;
import net.threebit.jvr.JVRException;
import net.threebit.jvr.dx;
import net.threebit.jvr.jvr;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class DialogicIncoming extends Thread {

	int dxHandle;

	public DialogicIncoming() {
		try {
			String devName = BrokerFactory.getConfigurationBroker()
					.getStringValue("dialogic.incoming.boardname");
			if (devName == null)
				devName = "dxxxB1C1";
			dxHandle = dx.open(devName, 0);
			BrokerFactory.getLoggingBroker().logDebug("Dialogic Subsystem Initialized");
		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	public void run() {
		BrokerFactory.getLoggingBroker()
				.logDebug("Starting Dialogic Subsystem");

		try {
			while (BrokerFactory.getConfigurationBroker().getBooleanValue("dialogic.incoming")) {
				dx.sethook(dxHandle, jvr.DX_ONHOOK, dx.EV_SYNC);
				dx.wtring(dxHandle, 1, jvr.DX_OFFHOOK, -1);

				DialogicMessage message = new WelcomeDialogicMessage();

				boolean finished = false;
				while ((message != null) && (!finished)) {
					DV_TPT wav_tpt[] = DV_TPT.newArray(3);
					wav_tpt[0].tp_type = dx.IO_CONT;
					wav_tpt[0].tp_termno = dx.DX_MAXTIME;
					wav_tpt[0].tp_length = 1200;
					wav_tpt[0].tp_flags = dx.TF_MAXTIME;
					wav_tpt[1].tp_type = dx.IO_CONT;
					wav_tpt[1].tp_termno = dx.DX_LCOFF;
					wav_tpt[1].tp_length = 1;
					wav_tpt[1].tp_flags = dx.TF_LCOFF;
					wav_tpt[2].tp_type = dx.IO_EOT;
					wav_tpt[2].tp_termno = dx.DX_MAXDTMF;
					wav_tpt[2].tp_length = 1;
					wav_tpt[2].tp_flags = dx.TF_MAXDTMF;

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

					String waveName = message.getWaveFilename();
					dx.clrdigbuf(dxHandle);

					dx.playwav(dxHandle, waveName, wav_tpt, dx.EV_SYNC);
					String digits = "";
					for (int i = 0; (i < message.getExpectedDigits())
							&& (!digits.equals("*")); i++) {
						String digit = dx.getdig(dxHandle, digit_tpt, null,
								dx.EV_SYNC);

						if ((digit == null) || (digit.length() == 0)) {
							finished = true;
						} else {
							digits += digit;
						}
					}
					if (digits.equals("*")) {
						message = new WelcomeDialogicMessage();
					} else {
						message = message.getNextMessage(digits);
					}
				}
				BrokerFactory.getLoggingBroker().logDebug("setting on-hook");
				dx.sethook(dxHandle, jvr.DX_ONHOOK, dx.EV_SYNC);
			}
		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				BrokerFactory.getLoggingBroker().logDebug("closing device");
				dx.close(dxHandle);
			} catch (JVRException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		DialogicIncoming in = new DialogicIncoming();
		in.start();
	}
}