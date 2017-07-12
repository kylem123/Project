package webcam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

	public static String cr_visrec, cr_stt_u, cr_stt_p, cr_tts_u, cr_tts_p, wc_source;

	public static void image() throws IOException, InterruptedException {
		VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey(cr_visrec);

		ImageIO.write(webcam.videoCap.getOneFrame(), "png", new File("save.png"));
		webcam.progress = "Analysing Image";
		//System.out.println("Before");
		ClassifyImagesOptions options = new ClassifyImagesOptions.Builder().images(new File("save.png")).build();
		VisualClassification result = service.classify(options).execute();
		//System.out.println("After");
		System.out.println(result);

		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject) parser.parse(result.toString());
		JsonArray array = obj.getAsJsonArray("images");

		// System.out.println(array);

		JsonObject obj2 = (JsonObject) parser.parse(array.get(0).toString());
		JsonArray array2 = obj2.getAsJsonArray("classifiers");

		// System.out.println(array2);

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
			
			if(obj4.has("type_hierarchy")) {
				possibilities.add(cl);
				if(Float.parseFloat(score) > record2) {
					record2 = Float.parseFloat(score);
					possibility = cl;
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < possibilities.size(); i++) {
			sb.append(" " + possibilities.get(i));
		}

		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < colors.size(); i++) {
			if (i < colors.size() - 1 && colors.size() != 1) {
				sb2.append(colors.get(i) + ", ");
			} else {
				sb2.append("and " + colors.get(i));
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

		// System.out.println(result);

		speak("This is a " + most + (record2 > 0.5 && possibility != most ? record2 > 0.75 ? ". It is likely that it is a " + possibility : ". It may be or contain one or more of the following " + sb.toString() : "") + " It's colours are " + sb2.toString());
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
			}
			else if(s.contains("wc_source")) {
				wc_source = s.replace("wc_source=", "");
			}
		}
	}

	public static void speechToText() {
		SpeechToText service = new SpeechToText();
		service.setUsernameAndPassword(cr_stt_u, cr_stt_p);

		File audio = new File("speech.wav");

		SpeechResults transcript = service.recognize(audio).execute();
		System.out.println(transcript);
	}

	public static void speak(String text) {
		TextToSpeech service = new TextToSpeech();
		service.setUsernameAndPassword(cr_tts_u, cr_tts_p);

		try {
			webcam.progress = "Synthesizing Response";
			InputStream stream = service.synthesize(text, Voice.EN_ALLISON, AudioFormat.WAV).execute();
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
		}
		catch (Exception e) {
		  e.printStackTrace();
		}
		
		try {
		    File yourFile;
		    AudioInputStream stream;
		    javax.sound.sampled.AudioFormat format;
		    DataLine.Info info;
		    Clip clip;

		    stream = AudioSystem.getAudioInputStream(new File("speech.wav"));
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    clip = (Clip) AudioSystem.getLine(info);
		    clip.open(stream);
		    clip.start();
		}
		catch (Exception e) {
		    
		}
	}
}
