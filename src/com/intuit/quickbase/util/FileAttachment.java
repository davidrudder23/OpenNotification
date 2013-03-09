/*
 * Created on Sep 11, 2008
 *
 *Copyright Reliable Response, 2008
 */
package com.intuit.quickbase.util;

import sun.misc.BASE64Encoder;

public class FileAttachment {

	String filename;
	byte[] contents;
	String encodedContents;
	
	public FileAttachment (String filename, String encodedContents) {
		this.filename = filename;
		this.encodedContents = encodedContents;
	}
	
	public FileAttachment (String filename, byte[] contents) {
		this.filename = filename;
		this.encodedContents = null;
		this.contents = contents;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		encodedContents = null;
		this.contents = contents;
	}

	public String getEncodedContents() {
		if (encodedContents == null) {
			encodedContents = new BASE64Encoder().encode(contents);
		}
		return encodedContents;
	}

	public void setEncodedContents(String encodedContents) {
		this.encodedContents = encodedContents;
	}
	
	
}
