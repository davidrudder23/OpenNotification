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
public class FindSilences {

	public static void main(String[] args) throws Exception {
		int moveSize = 25;
		int silenceLength = 8000;
		
		int original = 0;
		int silenceSize = 0;
		int index = 0;
		boolean inSilence = true;
		
		int length= 20;
		FileInputStream in = new FileInputStream(args[0]);
		original = in.read();
		byte[] b = new byte[length];
		int size = 0;
		index++;
		while ( (size = in.read(b, 0, length)) >= 0) {
			for (int i = 0; i < size; i++) {
				int n = b[i];
				if (n < 0) n+=256;
				boolean thisMove = Math.abs(n-original) > moveSize;
				original = n;
				if (inSilence && thisMove) {
					if (inSilence) {
						inSilence = false;
						System.out.println ("Sound started at index "+index);
					}
				}
				if ((!inSilence) && (thisMove)) {
					silenceSize = 0;
				}
				if ((!inSilence) && (!thisMove)) {
					silenceSize ++;
					if (silenceSize > silenceLength) {
						inSilence = true;
						System.out.println ("Silence started at index "+index);
					}
				}
				index++;
			}
		}
		System.out.println (index+" total indexes");
	}
}
