package appl;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import javazoom.jl.player.Player;

import server.JCL_message;
import server.JCL_messageImpl;

public class Client extends JFrame implements ActionListener {
	JButton playButton = new JButton("play");
	JButton pauseButton = new JButton("pause");
	// JButton resumeButton = new JButton("resume");
	JButton stopButton = new JButton("stop");
	JButton nextButton = new JButton("next");
	JButton backButton = new JButton("back");
	static DefaultListModel mylist = new DefaultListModel();
	JList list = new JList(mylist);
	String[] mf;
	Player player;
	String host = "192.168.0.102";
	int serverPort = 6969;
	private static int no = 0, lastno = 30000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Client();
	}
	
	public Client(){
		try{
			JFrame myFrame = new JFrame();
			Container   c=myFrame.getContentPane(); 
			c.setLayout(new FlowLayout(FlowLayout.CENTER));
			myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			myFrame.setSize(400, 200);
			myFrame.setTitle("Distribuited MP3 Player");
			myFrame.setVisible(true);

			playButton.addActionListener(this); 
			pauseButton.addActionListener(this); 
			// resumeButton.addActionListener(this); 
			stopButton.addActionListener(this); 
			nextButton.addActionListener(this); 
			backButton.addActionListener(this); 
			// list.addListSelectionListener(this);
			list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			myFrame.getContentPane().add(playButton);
			myFrame.getContentPane().add(pauseButton);
			// myFrame.getContentPane().add(resumeButton);
			myFrame.getContentPane().add(stopButton);
			myFrame.getContentPane().add(nextButton);
			myFrame.getContentPane().add(backButton);
			myFrame.getContentPane().add(list);
			
			//Chama a Lista de músicas
			Socket s = new Socket(host, serverPort);
			JCL_message msg = new JCL_messageImpl();
			msg.setType(101);
			
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));		
			out.writeObject(msg);
			out.flush();

			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			
			JCL_message msg2 = (JCL_message) in.readObject();
			
			Set<String> musicas = msg2.getSetMusicas();
			System.out.println("TTT"+musicas.size());
			mf = (String[]) musicas.toArray();
			for(int i = 0; i < mf.length; i++){
				if (mf[i].endsWith(".mp3")) {
					System.out.println(mf[i]);
					mylist.addElement(mf[i]);
				}
			}
			
			in.close();
			out.close();			
			s.close();
						
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		 if (e.getSource()==playButton){
			 no=list.getSelectedIndex() + 1;
			 
			 lastno = no;
				for (int i = no + 1;;) {
					if (i >= mf.length) {
						i = 0;
					}
					if (mf[i].endsWith(".mp3")) {
						no = i;
						break;
					}

					++i;

					if (i == no + 1) {
						System.out.println("No Songs Available!");
						System.exit(0);
					}
				}
				//playByName("../" + mf[no]);
				try{
					Socket s = new Socket(host, serverPort);
					JCL_message msg = new JCL_messageImpl();
					msg.setType(100);
					msg.setNome(mf[no]);
					
					ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
								
					out.writeObject(msg);
					out.flush();
					
					ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
					JCL_message msg2 = (JCL_message) in.readObject();
					//BufferedInputStream bis = (BufferedInputStream) in.readObject();
					byte[] boxOfBytes = msg2.getConteudo();
					ByteArrayInputStream bais = new ByteArrayInputStream(boxOfBytes);
					
					
					player = new Player(bais);
					
					new Thread() {
						public void run() {
							try {
								player.play();
								if (player.isComplete() == true) {
									player.close();
									playNext();
								}
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}.start();
					
					in.close();
					out.close();			
					s.close();
				}catch(Exception e1){
					e1.printStackTrace();
				}
		 }
		 
		 if (e.getSource()==pauseButton){
        	if(pauseButton.getText()=="pause"){
        		//mp3.pause(); FALTA
            	pauseButton.setText("resume");
            	 
        	}
        	else if(pauseButton.getText()=="resume"){
        		//mp3.resume(); FALTA
            	pauseButton.setText("pause");
        	}
        }
		
		 if (e.getSource()==stopButton){
	        	if (player != null)
					player.close();
	     }
		 
		if (e.getSource()==nextButton){
			player.close();
			playNext();
	    }
		
		if (e.getSource()==backButton){
			player.close();
        	playBack();
        }
	}
	
	public void playNext(){
		lastno = no;
		for (int i = no + 1;;) {
			if (i >= mf.length) {
				i = 0;
			}
			if (mf[i].endsWith(".mp3")) {
				no = i;
				break;
			}

			++i;

			if (i == no + 1) {
				System.out.println("No Songs Available!");
				System.exit(0);
			}
		}
		//playByName("../" + mf[no]);
		try{
			Socket s = new Socket(host, serverPort);
			JCL_message msg = new JCL_messageImpl();
			msg.setType(100);
			msg.setNome(mf[no]);
			
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
						
			out.writeObject(msg);
			out.flush();
			
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			JCL_message msg2 = (JCL_message) in.readObject();
			//BufferedInputStream bis = (BufferedInputStream) in.readObject();
			byte[] boxOfBytes = msg2.getConteudo();
			ByteArrayInputStream bais = new ByteArrayInputStream(boxOfBytes);
			
			
			player = new Player(bais);
			
			
			new Thread() {
				public void run() {
					try {
						player.play();
						if (player.isComplete() == true) {
							player.close();
							playNext();
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}.start();
			
			in.close();
			out.close();			
			s.close();
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}

	public void playBack(){
		if (lastno == 30000) {
			System.out.println("No Playback!\n");
			return;
		}
		int t = lastno;
		lastno = no;
		no = t;
		
		try{
			Socket s = new Socket(host, serverPort);
			JCL_message msg = new JCL_messageImpl();
			msg.setType(100);
			msg.setNome(mf[no]);
			
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
						
			out.writeObject(msg);
			out.flush();
			
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			JCL_message msg2 = (JCL_message) in.readObject();
			//BufferedInputStream bis = (BufferedInputStream) in.readObject();
			byte[] boxOfBytes = msg2.getConteudo();
			ByteArrayInputStream bais = new ByteArrayInputStream(boxOfBytes);
			
			
			player = new Player(bais);
			
			
			new Thread() {
				public void run() {
					try {
						player.play();
						if (player.isComplete() == true) {
							player.close();
							playNext();
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}.start();
			
			in.close();
			out.close();			
			s.close();
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}
}
