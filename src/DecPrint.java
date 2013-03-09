import java.io.FileInputStream;

/*
 * Created on Aug 29, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class DecPrint {

	public static void main(String[] args) throws Exception {
		int length= 20;
		FileInputStream in = new FileInputStream(args[0]);
		byte[] b = new byte[length];
		int size = 0;
		while ( (size = in.read(b, 0, length)) >= 0) {
			for (int i = 0; i < size; i++) {
				int n = (int)b[i];
				if (n < 0) n+=256;
				
				//n =Math.abs(b[i]);
				//n = (int)b[i];
				if (n < 100) System.out.print (" ");
				if (n < 10) System.out.print (" ");
				System.out.print (n);
				System.out.print (" ");
			}
			System.out.println();
		}
	}
}
