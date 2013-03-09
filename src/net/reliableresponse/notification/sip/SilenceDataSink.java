package net.reliableresponse.notification.sip;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.DataSink;
import javax.media.IncompatibleSourceException;
import javax.media.MediaLocator;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceStream;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * This SilenceDataSink class reads from a DataSource and checks
 * for periods of low volume, representing background noises
 */
public class SilenceDataSink implements DataSink, BufferTransferHandler {
	
	// Silence related vars
	long minimumSilenceLength = 800;
	int minimumSilenceThreshold = BrokerFactory.getConfigurationBroker().getIntValue("sip.silencethreshold", 10);
	
	int lastSample = -1;
	int silenceLength = 0;
	long currentIndex = 0;
	long silenceStartIndex = 0;
	boolean inSilence = true;
	boolean pickedUp = false;
	boolean pickedUpFirstPass = true;
	
	DataSource source;

	PullBufferStream pullStrms[] = null;

	PushBufferStream pushStrms[] = null;

	// Data sink listeners.
	private Vector listeners = new Vector(1);

	// Stored all the streams that are not yet finished (i.e. EOM
	// has not been received.
	SourceStream unfinishedStrms[] = null;

	// Loop threads to pull data from a PullBufferDataSource.
	// There is one thread per each PullSourceStream.
	Loop loops[] = null;

	Buffer readBuffer;

	Vector silenceListeners;
	Vector pickupListeners;
	
	FileOutputStream out;
	
	public SilenceDataSink() {
		silenceListeners = new Vector();
		pickupListeners = new Vector();
		
		try {
			out  = new FileOutputStream("/tmp/foo.pcm");
		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/**
	 * Sets the media source this <code>MediaHandler</code>
	 * should use to obtain content.
	 */
	public void setSource(DataSource source) throws IncompatibleSourceException {

		// Different types of DataSources need to handled differently.
		if (source instanceof PushBufferDataSource) {

			pushStrms = ((PushBufferDataSource) source).getStreams();
			unfinishedStrms = new SourceStream[pushStrms.length];

			// Set the transfer handler to receive pushed data from
			// the push DataSource.
			for (int i = 0; i < pushStrms.length; i++) {
				pushStrms[i].setTransferHandler(this);
				unfinishedStrms[i] = pushStrms[i];
			}

		} else if (source instanceof PullBufferDataSource) {

			pullStrms = ((PullBufferDataSource) source).getStreams();
			unfinishedStrms = new SourceStream[pullStrms.length];

			// For pull data sources, we'll start a thread per
			// stream to pull data from the source.
			loops = new Loop[pullStrms.length];
			for (int i = 0; i < pullStrms.length; i++) {
				loops[i] = new Loop(this, pullStrms[i]);
				unfinishedStrms[i] = pullStrms[i];
			}

		} else {

			// This handler only handles push or pull buffer datasource.
			throw new IncompatibleSourceException();

		}

		this.source = source;
		readBuffer = new Buffer();
	}

	/**
	 * For completeness, DataSink's require this method.
	 * But we don't need it.
	 */
	public void setOutputLocator(MediaLocator ml) {
	}

	public MediaLocator getOutputLocator() {
		return null;
	}

	public String getContentType() {
		return source.getContentType();
	}

	/**
	 * Our DataSink does not need to be opened.
	 */
	public void open() {
	}

	public void start() {
		try {
			source.start();
		} catch (IOException e) {
			System.err.println(e);
		}

		// Start the processing loop if we are dealing with a
		// PullBufferDataSource.
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].restart();
		}
	}

	public void stop() {
		try {
			source.stop();
		} catch (IOException e) {
			System.err.println(e);
		}

		// Start the processing loop if we are dealing with a
		// PullBufferDataSource.
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].pause();
		}
	}

	public void close() {
		stop();
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].kill();
		}
	}

	public void addDataSinkListener(DataSinkListener dsl) {
		if (dsl != null)
			if (!listeners.contains(dsl))
				listeners.addElement(dsl);
	}

	public void removeDataSinkListener(DataSinkListener dsl) {
		if (dsl != null)
			listeners.removeElement(dsl);
	}

	protected void sendEvent(DataSinkEvent event) {
		if (!listeners.isEmpty()) {
			synchronized (listeners) {
				Enumeration list = listeners.elements();
				while (list.hasMoreElements()) {
					DataSinkListener listener = (DataSinkListener) list
							.nextElement();
					listener.dataSinkUpdate(event);
				}
			}
		}
	}

	/**
	 * This will get called when there's data pushed from the
	 * PushBufferDataSource.
	 */
	public void transferData(PushBufferStream stream) {

		try {
			stream.read(readBuffer);
		} catch (IOException e) {
			System.err.println(e);
			sendEvent(new DataSinkErrorEvent(this, e.getMessage()));
			return;
		}

		printDataInfo(readBuffer);

		// Check to see if we are done with all the streams.
		if (readBuffer.isEOM() && checkDone(stream)) {
			sendEvent(new EndOfStreamEvent(this));
		}
	}

	/**
	 * This is called from the Loop thread to pull data from
	 * the PullBufferStream.
	 */
	public boolean readPullData(PullBufferStream stream) {
		try {
			stream.read(readBuffer);
		} catch (IOException e) {
			System.err.println(e);
			return true;
		}

		printDataInfo(readBuffer);

		if (readBuffer.isEOM()) {
			// Check to see if we are done with all the streams.
			if (checkDone(stream)) {
				System.err.println("All done!");
				close();
			}
			return true;
		}
		return false;
	}

	/**
	 * Check to see if all the streams are processed.
	 */
	public boolean checkDone(SourceStream strm) {
		boolean done = true;

		for (int i = 0; i < unfinishedStrms.length; i++) {
			if (strm == unfinishedStrms[i])
				unfinishedStrms[i] = null;
			else if (unfinishedStrms[i] != null) {
				// There's at least one stream that's not done.
				done = false;
			}
		}
		return done;
	}

	private void fireSilenceChange (boolean inSilence) {
		BrokerFactory.getLoggingBroker().logDebug("Silence "+(inSilence?"Start":"End")+": "+currentIndex);
		for (int i = 0; i < silenceListeners.size(); i++) {
			SilenceListener listener = (SilenceListener)silenceListeners.elementAt(i);
			if (inSilence) {
				listener.handleSilenceStart();
			} else {
				listener.handleSilenceEnd();
			}
		}
		
		if (inSilence) {
			if (!pickedUp) {
				long totalLength = currentIndex - silenceStartIndex;
//				BrokerFactory.getLoggingBroker().logDebug("silence start = "+silenceStartIndex);
//				BrokerFactory.getLoggingBroker().logDebug("current index = "+currentIndex);
//				BrokerFactory.getLoggingBroker().logDebug("total length  = "+totalLength);
				if ((totalLength <14000) || (totalLength >18000)) {
					if (!pickedUpFirstPass) {
						pickedUpFirstPass = true;
					} else {
						pickedUp = true;
						firePickup();
					}
				}
			}
		}
		silenceStartIndex = currentIndex;
	}
	
	private void firePickup () {
		BrokerFactory.getLoggingBroker().logDebug("PICKED UP!");
		for (int i = 0; i < pickupListeners.size(); i++) {
			PickupListener listener = (PickupListener)pickupListeners.elementAt(i);
			listener.handlePickup();
		}
	}

	void printDataInfo(Buffer buffer) {


		byte[] data = (byte[])buffer.getData(); 
		try {
			out.write (data, buffer.getOffset(), buffer.getLength());
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		for (int i = buffer.getOffset(); i < buffer.getLength(); i++) {
			int n = data[i];
			if (n < 0) n+=256;
			if (lastSample == -1) lastSample = n;

			boolean thisMove = Math.abs(n-lastSample) > minimumSilenceThreshold;
			lastSample = n;
			
			if (inSilence && thisMove) {
				if (inSilence) {
					inSilence = false;
					fireSilenceChange(inSilence);
				}
			}
			
			if ((!inSilence) && (thisMove)) {
				silenceLength = 0;
			}
			if ((!inSilence) && (!thisMove)) {
				silenceLength ++;
				if (silenceLength > minimumSilenceLength) {
					inSilence = true;
					fireSilenceChange(inSilence);
				}
			}
			
			currentIndex++;
		}
		if (buffer.isEOM())
			System.err.println("  Got EOM!");
	}

	public Object[] getControls() {
		return new Object[0];
	}

	public Object getControl(String name) {
		return null;
	}
	
	public void addSilenceListener(SilenceListener silenceListener) {
		silenceListeners.addElement(silenceListener);
	}

	public void addPickupListener(PickupListener pickupListener) {
		pickupListeners.addElement(pickupListener);
	}
}

/**
 * A thread class to implement a processing loop.
 * This loop reads data from a PullBufferDataSource.
 */

class Loop extends Thread {

	SilenceDataSink handler;

	PullBufferStream stream;

	boolean paused = true;

	boolean killed = false;

	public Loop(SilenceDataSink handler, PullBufferStream stream) {
		this.handler = handler;
		this.stream = stream;
		start();
	}

	public synchronized void restart() {
		paused = false;
		notify();
	}

	/**
	 * This is the correct way to pause a thread; unlike suspend.
	 */
	public synchronized void pause() {
		paused = true;
	}

	/**
	 * This is the correct way to kill a thread; unlike stop.
	 */
	public synchronized void kill() {
		killed = true;
		notify();
	}

	/**
	 * This is the processing loop to pull data from a 
	 * PullBufferDataSource.
	 */
	public void run() {
		while (!killed) {
			try {
				while (paused && !killed) {
					wait();
				}
			} catch (InterruptedException e) {
			}

			if (!killed) {
				boolean done = handler.readPullData(stream);
				if (done)
					pause();
			}
		}
	}
}

