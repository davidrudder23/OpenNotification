import java.math.BigInteger;

/*
 * Created on Apr 27, 2005
 *
 *Copyright Reliable Response, 2005
 */

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class TestNumbers {

	public static void main (String[] args) {
		BigInteger bigint = new BigInteger("2000");
		bigint = bigint.multiply(new BigInteger("3600"));
		bigint = bigint.multiply(new BigInteger("1000"));
		System.out.println (bigint.longValue());
	}
}
