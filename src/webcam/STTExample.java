//https://developer.ibm.com/in/2017/04/04/connecting-pepper-robot-with-watson-speech-to-text-using-java/

package webcam;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

public class STTExample {

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
    	Util.loadConfig();
    	
    	SpeechToText service = new SpeechToText();
    	service.setUsernameAndPassword(Util.cr_stt_u, Util.cr_stt_p);

    	// Signed PCM AudioFormat with 16kHz, 16 bit sample size, mono
    	int sampleRate = 16000;
    	AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
    	DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

    	if (!AudioSystem.isLineSupported(info)) {
    	  System.out.println("Line not supported");
    	  System.exit(0);
    	}

    	TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
    	line.open(format);
    	line.start();

    	AudioInputStream audio = new AudioInputStream(line);

    	RecognizeOptions options = new RecognizeOptions.Builder()
    	  .continuous(true)
    	  .interimResults(true)
    	  .timestamps(true)
    	  .wordConfidence(true)
    	  //.inactivityTimeout(5) // use this to stop listening when the speaker pauses, i.e. for 5s
    	  .contentType(HttpMediaType.AUDIO_RAW + "; rate=" + sampleRate)
    	  .build();

    	service.recognizeUsingWebSocket(audio, options, new BaseRecognizeCallback() {
    	  @Override
    	  public void onTranscription(SpeechResults speechResults) {
    	    System.out.println(speechResults);
    	  }
    	});

    	System.out.println("Listening to your voice for the next 30s...");
    	Thread.sleep(10 * 1000);

    	// closing the WebSockets underlying InputStream will close the WebSocket itself.
    	line.stop();
    	line.close();

    	System.out.println("Fin.");
    }
}