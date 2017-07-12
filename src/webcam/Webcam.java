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

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Webcam extends JFrame implements ActionListener, KeyListener {
	
	public JButton go, multi;
	public JRadioButton ibm, google, amazon;
	public JTextField source;
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
		go.setPreferredSize(new Dimension(140, 140));
		go.addActionListener(this);
		
		multi = new JButton("Multi-Shot");
		multi.setPreferredSize(new Dimension(140, 140));
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
		source.setPreferredSize(new Dimension(50, 24));
		source.setText(Util.wc_source);
		videoCap.source = Integer.parseInt(Util.wc_source);
		changeSource();
		source.setToolTipText("Webcam Source ID");
		source.addActionListener(this);
		source.addKeyListener(this);
		
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
		
		list = new JPanel();
		list.setPreferredSize(new Dimension(250, 400));
		list.setBackground(Color.GRAY);
		
		JLabel lbl = new JLabel("Webcam Object Recognition Project");
		JLabel lbl2 = new JLabel("              (WORP)                ");
		JLabel lbl3 = new JLabel("Kyle Mandell 2017");
		
		jl = new JList<String>();
		jl.setBackground(Color.LIGHT_GRAY);
		
		list.add(lbl);
		list.add(lbl2);
		list.add(lbl3);
		list.add(jl);
		
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == go) {
			try {
				Util.image();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == multi) {
			if(!running) {
				ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
				
			}
			else {
				running = false;
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
