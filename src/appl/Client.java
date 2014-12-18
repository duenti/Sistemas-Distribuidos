//Vtp4:
//Add Upload Sincronizado no lugar certo
//Add Memoria Cache
//Add Replicação até tripla

//Falta TxtIp
//Falta Suportar slave que cai
package appl;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;
import org.omg.CORBA.portable.InputStream;

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
	JButton uploadButton = new JButton("Upload");
	static DefaultListModel mylist = new DefaultListModel();
	JList list = new JList(mylist);
	String[] mf;
	Player player;
	String host = "127.0.0.1";
	int serverPort = 6969;
	int slavePort = 6666;
	private static int no = 0, lastno = 30000;
	LinkedList<ByteArrayInputStream> musicCacheByte = new LinkedList<ByteArrayInputStream>();
	LinkedList<String> musicCacheMap = new LinkedList<String>();
	ByteArrayInputStream bais;
	
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
			myFrame.setSize(500, 300);
			myFrame.setTitle("MP3 Player Distribuido");
			myFrame.setVisible(true);

			playButton.addActionListener(this); 
			pauseButton.addActionListener(this); 
			// resumeButton.addActionListener(this); 
			stopButton.addActionListener(this); 
			nextButton.addActionListener(this); 
			backButton.addActionListener(this);
			uploadButton.addActionListener(this);
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
			myFrame.getContentPane().add(uploadButton);
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
			mf = musicas.toArray(new String[0]);
			
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
			 no=list.getSelectedIndex() -1;
			 
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
					//Conferir na Cache antes
					boolean found = false;
					for(int i = 0; i < musicCacheMap.size(); i++){
						if(musicCacheMap.get(i).equals(mf[no])){
							bais = musicCacheByte.get(i);
							i = musicCacheMap.size() + 1; //Encontrou, logo para de procurar
							found = true;
						}
					}
					
					if(!found){//Ai pede para o server
						//Pede ip do Slave pro cliente
						Socket s = new Socket(host, serverPort);
						JCL_message msg = new JCL_messageImpl();
						msg.setType(100);
						msg.setNome(mf[no]);
						
						ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
									
						out.writeObject(msg);
						out.flush();
						
						ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
						JCL_message msg2 = (JCL_message) in.readObject();
						String slaveIp = msg2.getNome();
						
						in.close();
						out.close();			
						s.close();
						System.out.print("Host: "+ slaveIp + "Porta " + slavePort);
						//Pede música para o Slave
						Socket s2 = new Socket(slaveIp,slavePort);
						
						JCL_message msg3 = new JCL_messageImpl();
						msg3.setType(101);
						msg3.setNome(mf[no]);
						
						ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(s2.getOutputStream()));
						
						out2.writeObject(msg3);
						out2.flush();
						
						ObjectInputStream in2 = new ObjectInputStream(new BufferedInputStream(s2.getInputStream()));
						JCL_message msg4 = (JCL_message) in2.readObject();
						
						byte[] boxOfBytes = msg4.getConteudo();
						bais = new ByteArrayInputStream(boxOfBytes);
						
						in2.close();
						out2.close();			
						s2.close();
						
						//Adiciona na cache
						musicCacheMap.add(mf[no]);
						musicCacheByte.add(bais);
					}
					
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
		
		if (e.getSource() == uploadButton){
			uploadMusic();
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
			//Conferir na Cache antes
			boolean found = false;
			for(int i = 0; i < musicCacheMap.size(); i++){
				if(musicCacheMap.get(i).equals(mf[no])){
					bais = musicCacheByte.get(i);
					i = musicCacheMap.size() + 1; //Encontrou, logo para de procurar
					found = true;
				}
			}
			
			if(!found){
				//Pede ip do Slave pro cliente
				Socket s = new Socket(host, serverPort);
				JCL_message msg = new JCL_messageImpl();
				msg.setType(100);
				msg.setNome(mf[no]);
				
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
							
				out.writeObject(msg);
				out.flush();
				
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
				JCL_message msg2 = (JCL_message) in.readObject();
				String slaveIp = msg2.getNome();
				
				in.close();
				out.close();			
				s.close();
				
				//Pede música para o Slave
				Socket s2 = new Socket(slaveIp,slavePort);
				
				JCL_message msg3 = new JCL_messageImpl();
				msg3.setType(101);
				msg3.setNome(mf[no]);
				
				ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(s2.getOutputStream()));
							
				out2.writeObject(msg3);
				out2.flush();
				
				ObjectInputStream in2 = new ObjectInputStream(new BufferedInputStream(s2.getInputStream()));
				JCL_message msg4 = (JCL_message) in2.readObject();
				
				byte[] boxOfBytes = msg4.getConteudo();
				bais = new ByteArrayInputStream(boxOfBytes);
				
				in2.close();
				out2.close();			
				s2.close();
				
				//Adiciona na cache
				musicCacheMap.add(mf[no]);
				musicCacheByte.add(bais);
			}			
			
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
			//Conferir na Cache antes
			boolean found = false;
			for(int i = 0; i < musicCacheMap.size(); i++){
				if(musicCacheMap.get(i).equals(mf[no])){
					bais = musicCacheByte.get(i);
					i = musicCacheMap.size() + 1; //Encontrou, logo para de procurar
					found = true;
				}
			}
			
			if(!found){
				//Pede ip do Slave pro cliente
				Socket s = new Socket(host, serverPort);
				JCL_message msg = new JCL_messageImpl();
				msg.setType(100);
				msg.setNome(mf[no]);
				
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
							
				out.writeObject(msg);
				out.flush();
				
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
				JCL_message msg2 = (JCL_message) in.readObject();
				String slaveIp = msg2.getNome();
				
				in.close();
				out.close();			
				s.close();
				
				//Pede música para o Slave
				Socket s2 = new Socket(slaveIp,slavePort);
				
				JCL_message msg3 = new JCL_messageImpl();
				msg3.setType(101);
				msg3.setNome(mf[no]);
				
				ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(s2.getOutputStream()));
							
				out2.writeObject(msg3);
				out2.flush();
				
				ObjectInputStream in2 = new ObjectInputStream(new BufferedInputStream(s2.getInputStream()));
				JCL_message msg4 = (JCL_message) in2.readObject();
				
				byte[] boxOfBytes = msg4.getConteudo();
				bais = new ByteArrayInputStream(boxOfBytes);
				
				//Adiciona na cache
				musicCacheMap.add(mf[no]);
				musicCacheByte.add(bais);
				
				in2.close();
				out2.close();			
				s2.close();
			}			
			
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
			
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}
	
	public void uploadMusic(){
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3", "mp3");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(this);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	File f = new File(chooser.getSelectedFile().getAbsolutePath());
		    String musicName = chooser.getSelectedFile().getName();
		    
		    try{
		    	//Pede os IPs dos slaves para salvarem o arquivo replicado:
			    Socket s = new Socket(host, serverPort);
				JCL_message msg = new JCL_messageImpl();
				msg.setNome("../"+musicName);
				msg.setType(102);
				
				
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
							
				out.writeObject(msg);
				out.flush();
				
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
				JCL_message msg2 = (JCL_message) in.readObject();
				Set<String> slaveIps = msg2.getSetMusicas();
				
				in.close();
				out.close();			
				s.close();
				
				//Envia música para os Slaves
				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				byte[] boxOfBytes = IOUtils.toByteArray(bis);
				Iterator<String> itIps = slaveIps.iterator();
				//String[] slaveips = (String[]) slaveIps.toArray();
				
				while(itIps.hasNext()){
					Socket s2 = new Socket(itIps.next(),slavePort);
					
					JCL_message msg3 = new JCL_messageImpl();
					msg3.setType(102);
					msg3.setConteudo(boxOfBytes);
					msg3.setNome(musicName);
				    
					ObjectOutputStream out2 = new ObjectOutputStream(s2.getOutputStream());
					out2.writeObject(msg3);
					out2.flush();
			
					out2.close();			
					s2.close();
				}
				
				//ATUALIZAR LISTA DE MUSICAS
				mf = Arrays.copyOf(mf, mf.length + 1);
				mf[mf.length-1] = "../"+musicName;
				mylist.addElement(mf[mf.length-1]);
				
				
				//Adiciona na Cache
				bais = new ByteArrayInputStream(boxOfBytes);
				
				musicCacheMap.add("../"+musicName);
				musicCacheByte.add(bais);
				
		    }catch(Exception e){
		    	e.printStackTrace();
		    }			
	    }
	}
}
