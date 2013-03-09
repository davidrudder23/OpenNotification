/*
 * Created on Apr 14, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.nagios.discovery;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class Discovery {
//	long networkStart, networkEnd;
//	int[] ports;
//	
//	public Discovery (long networkStart, long networkEnd, int[] ports) {
//	
//		this.networkStart = networkStart;
//		this.networkEnd = networkEnd;
//		this.ports = ports;
//	}
//	
//	private String hostNumberToName (long number) {
//		String hostString = null;
//		
//		hostString = ((number>>24) & 0xFF)+".";
//		hostString += ((number>>16) & 0xFF)+".";
//		hostString += ((number>>8) & 0xFF)+".";
//		hostString += (number & 0xFF);
//		return hostString;
//	}
//	
//	public void discover() {
//		for (long address = networkStart; address <= networkEnd; address++) {
//			String hostname = hostNumberToName(address);
//			System.out.println (hostname);
//			boolean pingable = false;
//			
//			try {
//				pingable = InetAddress.getByName(hostname).isReachable(1000);
//			} catch (IOException e) {
//			}
//			
//			if (!pingable) {
//				System.out.println ("Host is not up");
//			} else {
//				for (int port = 0; port < ports.length; port++) {
//					boolean up =checkAvailableTCP(hostname, ports[port]);
//					if (up)
//						System.out.println ("Port "+ports[port]+" is up");
//				}
//			}
//		}
//	}
//	
//	private boolean checkAvailableTCP (String hostname, int port) {
//		try {
//			Socket socket = new Socket (hostname, port);
//			socket.setSoTimeout(500);
//			socket.getOutputStream();
//			return true;
//		} catch (UnknownHostException e) {
//		} catch (IOException e) {
//		}
//		return false;
//	}
//
//	public static void main(String[] args) throws Exception {
//		BrokerFactory.getConfigurationBroker().setConfiguration(
//				new FileInputStream("conf/reliable.properties"));
//
//		long hostStart = 0x0A0A0A01L;
//		long hostEnd = 0x0A0A0AFEL;
//		int[] ports = {7, 11, 13, 17, 19, 21, 22, 23, 25, 53, 70, 79, 
//				80, 88, 110, 115, 119, 137, 143, 161, 177, 220, 389, 443,
//				547, 636, 873, 514, 517, 518, 1080, 2401, 6000
//		};
//		
//		Discovery d = new Discovery(hostStart, hostEnd, ports);
//		
//		d.discover();
//	}
}
