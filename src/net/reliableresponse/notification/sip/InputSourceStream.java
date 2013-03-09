/*
 * Created on Aug 15, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.IOException;
import java.io.InputStream;

import javax.media.Control;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceStream;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class InputSourceStream implements PullSourceStream {

	protected InputStream stream;
	protected boolean eosReached;
	ContentDescriptor contentType;
	
	public InputSourceStream(InputStream s, ContentDescriptor type) {
		stream = s;
		eosReached = false;
		contentType = type;
	}
	
	
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int bytesRead = stream.read(buffer, offset, length);
		if( bytesRead == -1) {
			eosReached = true;
		}
		return bytesRead;
	}
	
	public boolean willReadBlock() {
		return false;
	}
	public boolean endOfStream() {
		return eosReached;
	}
	public ContentDescriptor getContentDescriptor() {
		return contentType;
	}
	public long getContentLength() {
		return SourceStream.LENGTH_UNKNOWN;
	}
	
	public Object getControl(String arg0) {
		return null;
	}
	public Object[] getControls() {
		return new Control[0];
	}
}
