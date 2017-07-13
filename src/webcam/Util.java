//https://developer.ibm.com/recipes/tutorials/integration-of-ibm-watson-conversation-service-to-your-java-application/
//Used for conversation
package webcam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

public class Util {

	public static Webcam webcam;

	public static String cr_visrec, cr_stt_u, cr_stt_p, cr_tts_u, cr_tts_p, cr_conv_u, cr_conv_p, cr_conv_wid,
			wc_source, voice;

	public static VisualRecognition service_visrec;
	public static SpeechToText service_stt;
	public static TextToSpeech service_tts;
	public static ConversationService service_conv;

	public static void initServices() {
		service_visrec = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service_visrec.setApiKey(cr_visrec);

		service_stt = new SpeechToText();
		service_stt.setUsernameAndPassword(cr_stt_u, cr_stt_p);

		service_tts = new TextToSpeech();
		service_tts.setUsernameAndPassword(cr_tts_u, cr_tts_p);

		service_conv = new ConversationService("2017-02-03");
		service_conv.setUsernameAndPassword(Util.cr_conv_u, Util.cr_conv_p);
	}
	
	public static MessageResponse conversationAPI(String input, Map context) {
		MessageRequest newMessage = new MessageRequest.Builder().inputText(input).context(context).build();
		MessageResponse response = service_conv.message(cr_conv_wid, newMessage).execute();
		return response;
	}

	public static VisualClassification getResult(VisualRecognition service, File img) {
		ClassifyImagesOptions options = new ClassifyImagesOptions.Builder().images(img).build();
		return service.classify(options).execute();
	}

	public static void speakProcessedResult(VisualClassification result) {
		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject) parser.parse(result.toString());
		JsonArray array = obj.getAsJsonArray("images");

		JsonObject obj2 = (JsonObject) parser.parse(array.get(0).toString());
		JsonArray array2 = obj2.getAsJsonArray("classifiers");

		JsonObject obj3 = (JsonObject) parser.parse(array2.get(0).toString());
		JsonArray array3 = obj3.getAsJsonArray("classes");

		String most = "";
		float record = 0;

		ArrayList<String> classes = new ArrayList<String>();
		ArrayList<Float> scores = new ArrayList<Float>();
		ArrayList<String> colors = new ArrayList<String>();
		ArrayList<String> possibilities = new ArrayList<String>();

		String possibility = "";
		float record2 = 0;

		for (JsonElement e : array3) {
			JsonObject obj4 = (JsonObject) parser.parse(e.toString());

			String score = obj4.get("score").getAsString();
			String cl = obj4.get("class").getAsString();

			classes.add("[" + Math.round(Float.parseFloat(score) * 100) + "%] " + cl);
			scores.add(Float.parseFloat(score));

			if (Float.parseFloat(score) > record && cl.endsWith("color") == false) {
				record = Float.parseFloat(score);
				most = cl;
			}

			if (cl.endsWith("color")) {
				colors.add(cl.replaceAll("color", ""));
			}

			if (obj4.has("type_hierarchy")) {
				possibilities.add(cl);
				if (Float.parseFloat(score) > record2) {
					record2 = Float.parseFloat(score);
					possibility = cl;
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < possibilities.size(); i++) {
			sb.append(possibilities.get(i) + ", ");
		}

		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < colors.size(); i++) {
			if (i < colors.size() - 1) {
				sb2.append(colors.get(i) + ", ");
			} else {
				sb2.append((colors.size() > 1 ? "and " : " ") + colors.get(i));
			}
		}
		
		Collections.sort(classes);

		ArrayList<Integer> remove = new ArrayList<Integer>();
		for (int i = 0; i < classes.size(); i++) {
			if (classes.get(i).contains("100%")) {
				remove.add(i);
			}
		}

		for (int i : remove) {
			classes.add(classes.get(i));
		}
		for (int i : remove) {
			classes.remove(i);
		}

		Collections.reverse(classes);

		webcam.jl.setListData(classes.toArray());
		webcam.validate();

		speak("This is a " + most
				+ (record2 > 0.5 && possibility != most
						? record2 > 0.75 ? ". It is likely that it is a " + possibility
								: ". It may be or contain one or more of the following: " + sb.toString()
						: "")
				+ " It's colours are " + sb2.toString());
	}

	public static VisualClassification getResultForImage(String url) throws IOException, InterruptedException {
		return getResult(service_visrec, new File(url));
	}

	public static void loadConfig() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));
			String s = br.readLine();

			while (s != null) {
				lines.add(s);
				s = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String s : lines) {
			if (s.contains("cr_visrec")) {
				cr_visrec = s.replace("cr_visrec=", "");
			} else if (s.contains("cr_stt_u")) {
				cr_stt_u = s.replace("cr_stt_u=", "");
			} else if (s.contains("cr_stt_p")) {
				cr_stt_p = s.replace("cr_stt_p=", "");
			} else if (s.contains("cr_tts_u")) {
				cr_tts_u = s.replace("cr_tts_u=", "");
			} else if (s.contains("cr_tts_p")) {
				cr_tts_p = s.replace("cr_tts_p=", "");
			} else if (s.contains("cr_conv_u")) {
				cr_conv_u = s.replace("cr_conv_u=", "");
			} else if (s.contains("cr_conv_p")) {
				cr_conv_p = s.replace("cr_conv_p=", "");
			} else if (s.contains("cr_conv_wid")) {
				cr_conv_wid = s.replace("cr_conv_wid=", "");
			} else if (s.contains("wc_source")) {
				wc_source = s.replace("wc_source=", "");
			}
			else if(s.contains("voice")) {
				voice = s.replace("voice=", "");
			}
		}
	}

	public static void speechToText() {
		File audio = new File("speech.wav");

		SpeechResults transcript = service_stt.recognize(audio).execute();
		System.out.println(transcript);
	}

	public static void speak(String text) {
		try {
			webcam.conv.append("Watson >> " + text + "\n");
			InputStream stream = service_tts.synthesize(text, (voice == "allison" ? Voice.EN_ALLISON : voice == "lisa" ? Voice.EN_LISA : Voice.EN_MICHAEL), AudioFormat.WAV).execute();
			InputStream in = WaveUtils.reWriteWaveHeader(stream);
			OutputStream out = new FileOutputStream("speech.wav");
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			out.close();
			in.close();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			AudioInputStream stream;
			javax.sound.sampled.AudioFormat format;
			DataLine.Info info;
			Clip clip;

			stream = AudioSystem.getAudioInputStream(new File("speech.wav"));
			format = stream.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			/*clip.addLineListener(new LineListener() {
				
				@Override
				public void update(LineEvent event) {
					if(event.getType() == LineEvent.Type.STOP) {
						
					}
				}
			});*/
			clip.open(stream);
			clip.start();
			//Thread.sleep(200);
		} catch (Exception e) {

		}
	}
}
