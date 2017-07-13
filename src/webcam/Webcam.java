package webcam;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

public class Webcam extends JFrame implements ActionListener, KeyListener {
	
	public JButton go, multi;
	public JRadioButton ibm, google, amazon;
	public JTextField source, size;
	public static JList jl;
	public JPanel webcam, config, list;
	public JLabel lbl;
	public Image watson = ImageIO.read(new File("watson.png")).getScaledInstance(80, 80, 0);
	public boolean running = false;
	
	public String progress = "";
	
	public Webcam() throws IOException {
		Util.loadConfig();
		Util.webcam = this;
		setTitle("WORP");
		setIconImage(ImageIO.read(new File("src/webcam/icon.png")));
		setResizable(false);
		setSize(800, 400);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		webcam = new JPanel();
		webcam.setPreferredSize(new Dimension(400, 400));
		
		config = new JPanel();
		config.setLayout(new GridBagLayout());
		config.setPreferredSize(new Dimension(150, 400));
		config.setBackground(Color.LIGHT_GRAY);
		
		go = new JButton("What is this?");
		go.setPreferredSize(new Dimension(140, 122));
		go.addActionListener(this);
		
		multi = new JButton("Multi-Shot");
		multi.setPreferredSize(new Dimension(140, 122));
		multi.addActionListener(this);
		
		ibm = new JRadioButton("IBM (Watson)");
		ibm.setPreferredSize(new Dimension(150, 30));
		ibm.addActionListener(this);
		
		google = new JRadioButton("Google");
		google.setBackground(Color.RED);
		google.setPreferredSize(new Dimension(150, 30));
		google.addActionListener(this);
		
		amazon = new JRadioButton("Amazon");
		amazon.setBackground(Color.RED);
		amazon.setPreferredSize(new Dimension(150, 30));
		amazon.addActionListener(this);
		
		source = new JTextField();
		source.setPreferredSize(new Dimension(50, 30));
		source.setText(Util.wc_source);
		videoCap.source = Integer.parseInt(Util.wc_source);
		changeSource();
		source.setToolTipText("Webcam Source ID");
		source.addActionListener(this);
		source.addKeyListener(this);
		
		size = new JTextField();
		size.setPreferredSize(new Dimension(50, 30));
		size.setText("2");
		size.setToolTipText("Number of images for multi-shot");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(ibm);
		bg.add(google);
		bg.add(amazon);
		bg.setSelected(ibm.getModel(), true);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		config.add(go, c);
		
		c.gridy = 1;
		config.add(multi, c);
		
		c.gridy = 2;
		config.add(ibm, c);
		
		c.gridy = 3;
		config.add(google, c);
		
		c.gridy = 4;
		config.add(amazon, c);
		
		c.gridy = 5;
		config.add(source, c);
		
		c.gridy = 6;
		config.add(size, c);
		
		list = new JPanel();
		list.setPreferredSize(new Dimension(250, 400));
		list.setBackground(Color.GRAY);
		
		JLabel lbl = new JLabel("Webcam Object Recognition Project");
		JLabel lbl2 = new JLabel("              (WORP)                ");
		JLabel lbl3 = new JLabel("Kyle Mandell 2017");
		
		jl = new JList<String>();
		jl.setBackground(Color.LIGHT_GRAY);
		
		JScrollPane scroll = new JScrollPane(jl);
		scroll.setPreferredSize(new Dimension(250, 300));
		
		//scroll.add(jl);
		
		list.add(lbl);
		list.add(lbl2);
		list.add(lbl3);
		list.add(scroll);
		
		add(webcam, BorderLayout.WEST);
		add(config, BorderLayout.CENTER);
		add(list, BorderLayout.EAST);
		
		pack();
		validate();
		
		new MyThread().start();
	}
	
	public void updateUI() {
		validate();
	}
	
	VideoCap videoCap = new VideoCap();
	 
    public void paint(Graphics g){
        g = webcam.getGraphics();
        g.drawImage(videoCap.getOneFrame(), 0, 0, this);
        g.drawImage(watson, 10, 10, this);
        
        config.paint(config.getGraphics());
        list.paint(list.getGraphics());
        
        //System.out.println(webcam.getSize());
    }
 
    class MyThread extends Thread{
        @Override
        public void run() {
            for (;;){
                repaint();
                try { Thread.sleep(30);
                } catch (InterruptedException e) {    }
            }  
        } 
    }
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Webcam frame = new Webcam();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
	}

	BufferedImage[] bimg;
	ArrayList<VisualClassification> results;
	int count = 0;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == go) {
			try {
				ImageIO.write(videoCap.getOneFrame(), "png", new File("save.png"));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				Util.speakProcessedResult(Util.getResultForImage("save.png"));
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == multi) {
			System.out.println(size.getText());
			if(!running) {
				running = true;
				bimg = new BufferedImage[Integer.parseInt(size.getText())];
				results = new ArrayList<VisualClassification>();
				count = 0;
			}
			if(running) {
				if(count < Integer.parseInt(size.getText())) {
					bimg[count] = videoCap.getOneFrame();
					try {
						ImageIO.write(bimg[count], "png", new File("multi_" + count + ".png"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					count++;
					//System.out.println(count);
				}
				if(count >= Integer.parseInt(size.getText())) {
					for(int i = 0; i < count; i++) {
						//System.out.println("Looping" + i);
						try {
							VisualClassification result = Util.getResultForImage("multi_" + i + ".png");
							results.add(result);
						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					ArrayList<ResultObj> classes = new ArrayList<ResultObj>();
					ArrayList<String>  output = null;

					for(VisualClassification result : results) {
						//System.out.println(result);

						JsonParser parser = new JsonParser();
						JsonObject obj = (JsonObject) parser.parse(result.toString());
						JsonArray array = obj.getAsJsonArray("images");

						JsonObject obj2 = (JsonObject) parser.parse(array.get(0).toString());
						JsonArray array2 = obj2.getAsJsonArray("classifiers");

						JsonObject obj3 = (JsonObject) parser.parse(array2.get(0).toString());
						JsonArray array3 = obj3.getAsJsonArray("classes");

						String most = "";
						float record = 0;

						
						
						String possibility = "";
						float record2 = 0;

						for (JsonElement el : array3) {
							JsonObject obj4 = (JsonObject) parser.parse(el.toString());

							String score = obj4.get("score").getAsString();
							String cl = obj4.get("class").getAsString();
							
							boolean found = false;
							for(int i = 0; i < classes.size(); i++) {
								//System.out.println("Check " + cl + " and " + classes.get(i).cl);
								if(cl.equals(classes.get(i).cl)) {
									//System.out.println("Repeated " + cl);
									found = true;
									classes.get(i).score += Float.parseFloat(score);
									classes.get(i).count++;
								}
							}
							
							if(!found) {
								String s = obj4.has("type_hierarchy") ? obj4.get("type_hierarchy").getAsString() : "";
								classes.add(new ResultObj(cl, score, s));
							}	
						}
						
						output = new ArrayList<String>();
						
						for(ResultObj r : classes) {
							output.add("[" + Math.round((r.score / r.count) * 100) + "%] " + r.cl/* + "{" + r.type_hierarchy + "}"*/);
						}
						
						Collections.sort(output);
						
						ArrayList<Integer> remove = new ArrayList<Integer>();
						for (int i = 0; i < output.size(); i++) {
							if (output.get(i).contains("100%")) {
								remove.add(i);
							}
						}

						for (int i : remove) {
							output.add(output.get(i));
						}
						for (int i : remove) {
							output.remove(i);
						}
						
						Collections.reverse(output);
						
						jl.setListData(output.toArray());
						validate();
					}
					
					String most = "";
					float record = 0;
					
					ArrayList<Float> scores = new ArrayList<Float>();
					ArrayList<String> colors = new ArrayList<String>();
					ArrayList<String> possibilities = new ArrayList<String>();
					
					String possibility = "";
					float record2 = 0;
					
					for(String s : output) {
						String score = s.substring(s.indexOf("[") + 1, s.indexOf("%"));
						String cl = s.substring(s.indexOf("] "));
						
						scores.add(Float.parseFloat(score));
						
						/*if(!s.substring(s.indexOf("{") + 1, s.indexOf("}")).equals("")) {
							possibilities.add(cl);
							if(Float.parseFloat(score) > record2) {
								record2 = Float.parseFloat(score);
								possibility = cl;
							}
						}*/
						
						//cl = cl.substring(0, cl.indexOf("{") - 1);

						if (Float.parseFloat(score) > record && cl.endsWith("color") == false) {
							record = Float.parseFloat(score);
							most = cl;
						}

						if (cl.endsWith("color")) {
							colors.add(cl.replaceAll("color", ""));
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
					
					Util.speak("This is a " + most +/* (record2 > 50 && possibility != most ? record2 > 75 ? ". It is likely that it is a " + possibility : ". It may be or contain one or more of the following " + sb.toString() : "") +*/ " It's colours are " + sb2.toString());
					
					running = false;
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
	}
	
	private void changeSource() {
		videoCap.cap.release();
		videoCap = new VideoCap();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(source.getText().equals("")) {
			source.setText("0");
		}
		int i = Integer.parseInt(source.getText());
		if(i != videoCap.source) {
			videoCap.source = i;
			changeSource();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
}
