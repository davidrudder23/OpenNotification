import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.asteriskjava.fastagi.ClassNameMappingStrategy;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

public class AsteriskDialOutAgi extends BaseAgiScript {

	private char playText (String text) throws AgiException {
		String waveName = "RRN_Sound_"+System.currentTimeMillis();
		int retCode = exec("System", "echo "+text+
				"|/usr/bin/text2wave -F 8000 -o /var/lib/asterisk/sounds/tts/"+waveName+".wav");
		char digit = streamFile("tts/"+waveName, "0123456789*#");
		retCode = exec("System", "rm /var/lib/asterisk/sounds/tts/"+waveName+".wav");
		return digit;
	}
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		System.out.println ("Dial-out AGI starting");
		String text = "Press a number to test DTMF";
		char digit = '0';
		while (digit != '*') {
			digit = playText(text);
			if (digit != 0)
				text = "You have pressed "+digit;
		}
		
		hangup();

	}
}
