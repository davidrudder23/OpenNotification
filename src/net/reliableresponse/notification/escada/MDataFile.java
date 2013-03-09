/*
 * Created on Oct 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.escada;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * @author David Rudder &lt; david@reliableresponse.net &gt;
 * 
 * Copyright 2005 - Reliable Response, LLC
 */
public class MDataFile {
	byte[] tempTripHi, tempTripLo;

	boolean[] temperatureConfigured;

	boolean[] voltageMainConfigured;

	boolean[] voltageAuxConfigured;

	boolean[] mainConditionConfigured;
	boolean[] mainConditionHL;

	boolean[] powerStripAddressed;

	boolean[][] powerStripConfigured;

	int[] powerStripCycleTime;

	boolean[] switchAddressed;

	boolean[] switchConfigured;

	boolean[] switchCycleEvent;

	int switchCycleTime;

	boolean[][] externalSensorConfigured;
	boolean[][] externalSensorHL;
	int[] auxTimer;

	byte[][] voltageMeterTripHi;

	byte[][] voltageMeterTripLo;

	// Status variables
	byte[][] voltageMeter;
	
	boolean[][] externalCondition;

	double[] temperature;

	boolean[] mainCondition;
	
	private boolean[] getConfigured(byte input) {
		boolean[] configured = new boolean[8];
		for (int i = 0; i < 8; i++) {
			configured[i] = (input & 1) == 1;
			input >>= 1;
		}
		return configured;
	}

	public void parse(byte[] dataFile) {

		// Initialize variables
		tempTripHi = new byte[8];
		tempTripLo = new byte[8];

		// Get whether each temperature module is configured
		temperatureConfigured = getConfigured(dataFile[139]);
		// Get the temperature trip high values
		System.arraycopy(dataFile, 140, tempTripHi, 0, 8);
		// Get the temperature trip low values
		System.arraycopy(dataFile, 148, tempTripLo, 0, 8);

		// Get whether each main voltage module is configured
		voltageMainConfigured = getConfigured(dataFile[156]);
		// Get whether each auxillary voltage module is configured
		voltageAuxConfigured = getConfigured(dataFile[157]);

		// Get whether each main condition module is configured
		mainConditionConfigured = getConfigured(dataFile[195]);

		// Get whether each main condition module is normally closed
		mainConditionHL = getConfigured(dataFile[196]);

		// Do the power strips
		// Get whether each power strip is addressed
		powerStripAddressed = getConfigured(dataFile[200]);
		// Get whether each power strip and each outlet is configured
		powerStripConfigured = new boolean[8][8];
		for (int i = 0; i < 8; i++) {
			powerStripConfigured[i] = getConfigured(dataFile[201 + i]);
		}

		powerStripCycleTime = new int[8];
		for (int i = 0; i < 8; i++) {
			powerStripCycleTime[i] = dataFile[217 + i];
			if (powerStripCycleTime[i] < 0)
				powerStripCycleTime[i] += 256;
		}

		// Do the switches
		// Get whether each switch is addressed
		switchAddressed = getConfigured(dataFile[225]);
		// Get whether each switch is configured
		switchAddressed = getConfigured(dataFile[226]);
		// Get whether each switch is cycling
		switchCycleEvent = getConfigured(dataFile[234]);

		switchCycleTime = dataFile[242];

		// Get whether each external modules and each sensor is configured
		// We append the aux as the 9th input
		externalSensorConfigured = new boolean[7][9];
		boolean[] auxConfigured = getConfigured(dataFile[257]);
		for (int i = 0; i < 7; i++) {
			boolean temp[] = getConfigured(dataFile[250 + i]);
			System.arraycopy(temp, 0, externalSensorConfigured[i], 0, 8);
			externalSensorConfigured[i][8] = auxConfigured[i];
		}

		externalSensorHL = new boolean[7][9];
		boolean[] auxHL = getConfigured(dataFile[265]);
		for (int i = 0; i < 7; i++) {
			boolean temp[] = getConfigured(dataFile[258 + i]);
			System.arraycopy(temp, 0, externalSensorHL[i], 0, 8);
			externalSensorHL[i][8] = auxConfigured[i];
		}

		auxTimer = new int[7];
		for (int i = 0; i < 7; i++) {
			auxTimer[i] = dataFile[274+i];
			if (auxTimer[i] < 0) auxTimer[i]+=256;
		}

		// Voltage trip
		voltageMeterTripHi = new byte[7][8];
		voltageMeterTripLo = new byte[7][8];
		for (int i = 0; i < 7; i++) {
			System.arraycopy(dataFile, 287 + (i * 16), voltageMeterTripHi[i],
					0, 8);
			System.arraycopy(dataFile, 295 + (i * 16), voltageMeterTripLo[i],
					0, 8);
		}

		// Status section
		voltageMeter = new byte[7][8];
		for (int i = 0; i < 7; i++) {
			System.arraycopy(dataFile, 469 + (i * 8), voltageMeter[i], 0, 8);
		}

		// External cond
		externalCondition = new boolean[7][9];
		boolean[] auxStatus = getConfigured(dataFile[468]);
		for (int i = 0; i < 7; i++) {
			boolean temp[] = getConfigured(dataFile[461 + i]);
			System.arraycopy(temp, 0, externalCondition[i], 0, 8);
			externalCondition[i][8] = auxConfigured[i];
		}
		
		// Temperature inputs
		boolean[] addDecimal = getConfigured(dataFile[449]);
		temperature = new double[8];
		for (int i = 0; i < 8; i++) {
			temperature[i] = (double) dataFile[441 + i];
			if (addDecimal[i])
				temperature[i] += .5; 
		}

		// Main (on-board) condition status
		mainCondition = getConfigured(dataFile[453]);
	}

	public static void main(String[] args) throws Exception {
		URL ftp = new URL("ftp://SITE:S7K5Z1@"+args[0]+"/mdata");
		Object content = ftp.getContent();
		System.out.println("Content="+content);
		File dataFile = new File("mdata");
		byte[] data = new byte[(int) dataFile.length()];
		FileInputStream in = new FileInputStream(dataFile);
		in.read(new byte[4], 0, 4);
		in.read(data, 0, (int) dataFile.length());

		MDataFile mdataFile = new MDataFile();
		mdataFile.parse(data);

		for (int i = 0; i < 8; i++) {
			System.out.println("temperature[" + i + "]configured  = "
					+ mdataFile.temperatureConfigured[i]);
		}
		for (int i = 0; i < 8; i++) {
			if (mdataFile.temperatureConfigured[i]) {
				System.out.println("temperature   [" + i + "] = "
						+ mdataFile.temperature[i]);
				System.out.println("temp trip low [" + i + "] = "
						+ mdataFile.tempTripLo[i]);
				System.out.println("temp trip high[" + i + "] = "
						+ mdataFile.tempTripHi[i]);
			}
		}
		for (int i = 0; i < 8; i++) {
			System.out.println("voltage main[" + i + "] configured = "
					+ mdataFile.voltageMainConfigured[i]);
		}
		for (int i = 0; i < 8; i++) {
			System.out.println("voltage aux[" + i + "] configured = "
					+ mdataFile.voltageAuxConfigured[i]);
		}
		
		for (int i = 0; i < 8; i++) {
			System.out.println("main condition["+i+"] configured = "+mdataFile.mainConditionConfigured[i]);
		}
		for (int i = 0; i < 8; i++) {
			if(mdataFile.mainConditionConfigured[i]) {
				// TODO: Is this logic correct? 
				System.out.println("main condition["+i+"] = "+(mdataFile.mainConditionHL[i]== mdataFile.mainCondition[i]));
			}
		}
		for (int i = 0; i < 8; i++) {
			System.out.println("pstrip[" + i + "] addressed = "
					+ mdataFile.powerStripAddressed[i]);
		}
		for (int i = 0; i < 8; i++) {
			if (mdataFile.powerStripAddressed[i]) {
				System.out.println("pstrip[" + i + "] cyc time = "
						+ mdataFile.powerStripCycleTime[i]);
				for (int o = 0; o < 8; o++) {
					System.out.println("pstrip[" + i + "][" + o
							+ "] configured = "
							+ mdataFile.powerStripConfigured[i][o]);
				}
			}
		}
		System.out.println("onboard switch enabled = "
				+ mdataFile.switchAddressed[0]);
		if (mdataFile.switchAddressed[0]) {
			System.out
					.println("switch cyc time = " + mdataFile.switchCycleTime);
			for (int i = 0; i < 8; i++) {
				System.out.println("switch[" + i + "] configured = "
						+ mdataFile.switchConfigured[i]);
			}
			for (int i = 0; i < 8; i++) {
				if (mdataFile.switchConfigured[i]) {
					System.out.println("switch[" + i + "] event = "
							+ mdataFile.switchCycleEvent[i]);
				}
			}
		}
		
		for (int i = 0; i < 7; i++) {
			for (int o = 0; o < 9; o++) {
				System.out.println ("Ext senser["+i+"]["+o+"] configured ="+mdataFile.externalSensorConfigured[i][o]);
			}
		}
	} 
}