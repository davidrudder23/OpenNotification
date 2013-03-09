/*
 * Created on Aug 15, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sip;

import java.io.IOException;
import java.io.InputStream;

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class InputStreamDataSource extends PullDataSource {

	String contentType;
	InputStream in;
	
	public InputStreamDataSource (InputStream in, String contentType) {
		this.contentType = contentType;
		this.in = in;
	}
	/* (non-Javadoc)
	 * @see javax.media.protocol.PullDataSource#getStreams()
	 */
	public PullSourceStream[] getStreams() {
		PullSourceStream [] streams = new PullSourceStream [1];
		InputSourceStream iss = new InputSourceStream(in, 
				new FileTypeDescriptor(ContentDescriptor.RAW));
		streams[0] = iss;
		return streams;
	}

	/* (non-Javadoc)
	 * @see javax.media.protocol.DataSource#getContentType()
	 */
	public String getContentType() {
		return contentType;
	}

	/* (non-Javadoc)
	 * @see javax.media.protocol.DataSource#connect()
	 */
	public void connect() throws IOException {
	}

	/* (non-Javadoc)
	 * @see javax.media.protocol.DataSource#disconnect()
	 */
	public void disconnect() {
	}

	/* (non-Javadoc)
	 * @see javax.media.protocol.DataSource#start()
	 */
	public void start() throws IOException {
	}

	/* (non-Javadoc)
	 * @see javax.media.protocol.DataSource#stop()
	 */
	public void stop() throws IOException {
	}

	/* (non-Javadoc)
	 * @see javax.media.Controls#getControl(java.lang.String)
	 */
	public Object getControl(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.media.Controls#getControls()
	 */
	public Object[] getControls() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.media.Duration#getDuration()
	 */
	public Time getDuration() {
		return null;
	}

	
	public MediaLocator getLocator() {
		return null;
	}
}
