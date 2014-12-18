package appl;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import javazoom.jl.player.Player;

import server.GenericConsumer;
import server.GenericResource;
import server.JCL_message;
import server.JCL_messageImpl;

// exemplo de um consumidor !!!

public class SocketConsumer<S extends Socket> extends GenericConsumer<S>{
	//create any constructor type
	Map<Integer,String> slotsList = new HashMap<Integer,String>();
	Map<Integer,String> musicList = new HashMap<Integer,String>();
	int slotPort = 6868;
	
	public SocketConsumer(GenericResource<S> re) {		
		super(re);		
	}

	@Override
	protected void doSomething(S str) {
		try{
			// TODO Auto-generated method stub
			ObjectInputStream in = new ObjectInputStream(str.getInputStream());
			
			JCL_message msg = (JCL_message) in.readObject();
			System.err.println("Server: " + msg.getType());
			switch (msg.getType()){
			
			case 100:{//Pede musica para Slot
				String ipSlot = "";
				for(int i = 0; i < musicList.size(); i++){
					if(msg.getNome().equals(musicList.get(i))){
						ipSlot = slotsList.get(i);
					}
				}
				
				Socket s = new Socket(ipSlot,slotPort);
				JCL_message msg2 = new JCL_messageImpl();
				msg2.setType(101);
				msg2.setNome(msg.getNome());
				
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
				
				out.writeObject(msg);
				out.flush();
				
				ObjectInputStream in2 = new ObjectInputStream(s.getInputStream());
				
				JCL_message msg3 = (JCL_message) in.readObject();
				byte[] byteSound = msg3.getConteudo();
				
				ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(str.getOutputStream()));
				
				out2.writeObject(msg3);
				out2.flush();
				
				out2.close();;
				out.close();
				in.close();
				in2.close();
				break;
			}case 101:{//Envia lista de musicas para o cliente
				Set<String> musicas = new HashSet<String>();
				for(int i = 0; i < musicList.size(); i++){
					musicas.add(musicList.get(i));
				}
				
				JCL_message msg2 = new JCL_messageImpl();
				msg2.setSetMusicas(musicas);
				ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
				out.writeObject(msg2);
				out.flush();
				out.close();
				in.close();
				break;
			}case 200:{//Registrar Slave
				int slotNumber = slotsList.size();
				String[] lista = msg.getListaMusicas();
				slotsList.put(slotNumber,msg.getNome());				
				System.out.println("Slave registrado. No " + slotNumber + ". Ip: " + msg.getNome());
				
				for(int i = 0; i < lista.length; i++){
					String musicPath = "../" + lista[i];
					if(musicPath.endsWith(".mp3")){
						musicList.put(slotNumber, musicPath);
						System.out.println("Slot no: " + slotNumber + " / musica: " + musicPath);
					}
				}
				
				in.close();
				break;
			}
			}		
			
		str.close();
				
		}catch (Exception e){
			e.printStackTrace();
			
		}
				
	}	

}
