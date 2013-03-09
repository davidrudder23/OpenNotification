/*
 * Created on May 2, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.test;

import net.reliableresponse.notification.broker.impl.caching.Cache;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class CacheSpeedTest extends Thread {
	int id;
	public CacheSpeedTest(int id) {
		this.id = id;
	}
	
	public void run() {
		Cache cache = new Cache(200, 3600, Cache.METHOD_FIFO);
		int group = 1000;
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < group; i++) {
				cache.addElement("Test");
				cache.elementAt(i%200);
			}
			long end = System.currentTimeMillis();			
			System.out.println ("Thread "+id+": Group took "+(end-start)+" millis");
		}
		
	}

	public static void main(String[] args) {
		int numThreads = 50;
		for (int i = 0; i < numThreads; i++) {
			CacheSpeedTest st = new CacheSpeedTest(i);
			st.start();
		}
	}
}
