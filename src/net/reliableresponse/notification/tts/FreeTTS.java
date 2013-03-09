/*
 * Created on Feb 15, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.tts;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.Locale;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.speech.EngineCreate;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.xml.parsers.DocumentBuilderFactory;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.threebit.jvr.DV_TPT;
import net.threebit.jvr.JVRException;
import net.threebit.jvr.dx;
import net.threebit.jvr.jvr;

import org.w3c.dom.Document;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class FreeTTS {
	Synthesizer synthesizer;

	public FreeTTS() {

	}

	public void createSynthesizer() {
		try {
			SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", /*
																				 * use
																				 * "time"
																				 * or
																				 * "general"
																				 */
			Locale.US, Boolean.FALSE, null);

			FreeTTSEngineCentral central = new FreeTTSEngineCentral();
			EngineList list = central.createEngineList(desc);

			if (list.size() > 0) {
				EngineCreate creator = (EngineCreate) list.get(0);
				synthesizer = (Synthesizer) creator.createEngine();
			}
			if (synthesizer == null) {
				System.err.println("Cannot create synthesizer");
				System.exit(1);
			}
			synthesizer.allocate();
			synthesizer.resume();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] getWav(String message) {
		BrokerFactory.getLoggingBroker().logDebug("Speaking " + message);
		String voiceName = "kevin16";

		VoiceManager voiceManager = VoiceManager.getInstance();
		Voice voice = voiceManager.getVoice(voiceName);

		if (voice == null) {
			BrokerFactory.getLoggingBroker().logWarn("FreeTTS cannot find a voice named " + voiceName
					+ ".  Please specify a different voice.");
			return null;
		}

		/*
		 * Allocates the resources for the voice.
		 */
		voice.allocate();

		ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer();
		voice.setAudioPlayer(audioPlayer);
		//audioPlayer.setAudioFormat(new
		// AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,11025,8,1,1,11025,true));
		voice.speak(message);
		
		voice.deallocate();

		return audioPlayer.getSample();
	}

	public byte[] getWav(Document message) {
		BrokerFactory.getLoggingBroker().logDebug("Speaking Document " + message);
		String voiceName = "kevin16";

		VoiceManager voiceManager = VoiceManager.getInstance();
		Voice voice = voiceManager.getVoice(voiceName);

		if (voice == null) {
			BrokerFactory.getLoggingBroker().logWarn("FreeTTS cannot find a voice named " + voiceName
					+ ".  Please specify a different voice.");
			return null;
		}

		/*
		 * Allocates the resources for the voice.
		 */
		voice.allocate();

		ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer();
		voice.setAudioPlayer(audioPlayer);
		//audioPlayer.setAudioFormat(new
		// AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,11025,8,1,1,11025,true));
		voice.startBatch();
		BrokerFactory.getLoggingBroker().logDebug("speak="+voice.speak(message));
		voice.endBatch();
		voice.deallocate();

		return audioPlayer.getSample();
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		FreeTTS tts = new FreeTTS();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        // Create the builder and parse the file
        Document doc = factory.newDocumentBuilder().parse
			(new StringBufferInputStream
				("<jsml><div>Sentence One.  Sentence Two.</div><div>This is the third sentence.</div>"+
						"<sayas class=\"net:email\">system.administrator@notif-test.reliableresponse.net</sayas></jsml>"));
        
       
		byte[] wav = tts.getWav(doc);

		FileOutputStream out = new FileOutputStream("/tmp/test.wav");
		out.write(wav, 0, wav.length);
		out.close();

		System.out.println("Press enter to try dialogic");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		try {

			String deviceName = "dxxxB1C1";

			int dxHandle = dx.open(deviceName, 0);
			dx.sethook(dxHandle, jvr.DX_OFFHOOK, dx.EV_SYNC);

			DV_TPT[] tpt = DV_TPT.newArray(1);
			tpt[0].tp_type = dx.IO_EOT;
			tpt[0].tp_termno = dx.DX_MAXDTMF;
			tpt[0].tp_length = 1;
			tpt[0].tp_flags = dx.TF_MAXDTMF;

			BrokerFactory.getLoggingBroker().logDebug("Playing wav");
			dx.playwav(dxHandle, "/tmp/test.wav", tpt, dx.EV_SYNC);

		} catch (JVRException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

}

class ByteArrayAudioPlayer implements AudioPlayer {

	private ByteArrayOutputStream out;

	AudioFormat format;

	public ByteArrayAudioPlayer() {
		out = new ByteArrayOutputStream();
		format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
				(float) 8000.0, 8, 1, 1, (float) 8000, true);

	}

	public void begin(int arg0) {
	}

	public void cancel() {
	}

	public void close() {
	}

	public boolean drain() {
		return false;
	}

	public boolean end() {
		return true;
	}

	public AudioFormat getAudioFormat() {
		return format;
	}

	public long getTime() {
		return -1L;
	}

	public float getVolume() {
		return 100;
	}

	public void pause() {
	}

	public void reset() {
	}

	public void resetTime() {
	}

	public void resume() {
	}

	public void setAudioFormat(AudioFormat arg0) {
		this.format = arg0;
	}

	public void setVolume(float arg0) {
	}

	public void showMetrics() {
	}

	public void startFirstSampleTimer() {
	}

	public boolean write(byte[] arg0, int arg1, int arg2) {
		out.write(arg0, arg1, arg2);
		return true;
	}

	public boolean write(byte[] arg0) {
		return write(arg0, 0, arg0.length);
	}

	public byte[] convert(byte[] data, AudioFormat oldformat,
			AudioFormat newformat) throws IOException {
		BrokerFactory.getLoggingBroker().logDebug("Converting to " + newformat);
		AudioInputStream ain = AudioSystem.getAudioInputStream(newformat,
				new AudioInputStream(new ByteArrayInputStream(data), oldformat,
						data.length));

		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		AudioSystem.write(ain, AudioFileFormat.Type.WAVE, newOut);
		byte[] outData = newOut.toByteArray();
		return newOut.toByteArray();
	}

	public byte[] getSample() {
		byte[] data = out.toByteArray();

		AudioFormat newformat;

		try {
			newformat = new AudioFormat(
					AudioFormat.Encoding.PCM_UNSIGNED, 
					format.getSampleRate(),
					8, 
					format.getChannels(), 
					format.getFrameSize(), 
					format.getFrameRate(), 
					format.isBigEndian());

			data = convert(data, format, newformat);
			format = newformat;
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		try {
			newformat = new AudioFormat(
					format.getEncoding(), 
					11025,
					format.getSampleSizeInBits(), 
					format.getChannels(), 
					1, 
					11025, 
					format.isBigEndian());

			data = convert(data, format, newformat);
			format = newformat;
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		return data;
	}
}