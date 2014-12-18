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
import java.util.Random;
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
	static Map<Integer,String> slotsList = new HashMap<Integer,String>();
	static LinkedList<String> musicList = new LinkedList<String>();
	static LinkedList<Integer> codMusicList = new LinkedList<Integer>();
	int slavePort = 6666;
	
	public SocketConsumer(GenericResource<S> re) {		
		super(re);		
	}

	@Override
	protected void doSomething(S str) {
		try{
			System.out.println("111"+musicList.size());
			// TODO Auto-generated method stub
			ObjectInputStream in = new ObjectInputStream(str.getInputStream());
			
			JCL_message msg = (JCL_message) in.readObject();
			System.err.println("Server: " + msg.getType());
			switch (msg.getType()){
			
			case 100:{//Devolve ip do Slave para Cliente
				String ipSlave = "";
				
				for(int i = 0; i < musicList.size(); i++){
					if(msg.getNome().equals(musicList.get(i))){
						ipSlave = slotsList.get(codMusicList.get(i));
					}
				}
				
				JCL_message msg2 = new JCL_messageImpl();
				msg2.setNome(ipSlave);
				ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
				
				out.writeObject(msg2);
				out.flush();
				out.close();
				in.close();
				
				break;
			}case 101:{//Envia lista de musicas para o cliente
				Set<String> musicas = new HashSet<String>();
				/*for(int i = 0; i < musicList.size(); i++){
					musicas.add(musicList.get(i));
				}*/
				for(int i = 0; i < musicList.size(); i++){
					musicas.add(musicList.get(i));
				}
				System.out.println("list "+musicList.size()+"music"+musicas.size());
				JCL_message msg2 = new JCL_messageImpl();
				msg2.setSetMusicas(musicas);
				ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
				out.writeObject(msg2);
				out.flush();
				out.close();
				in.close();
				break;
			}case 102:{//Retorna até três slaves para salvar música replicada
				Map<Integer,String> slotsListcopy = new HashMap<Integer,String>(slotsList);
				Set<String> slaveIps = new HashSet<String>(0);
				String musicName = msg.getNome();
				Random rnd = new Random();
				int lastRnd;
				
				//Aloca primeiro slave para receber a música
				System.out.println("TTTTTT"+slotsListcopy.size());
				System.out.println("UUUUUU"+slotsList.size());
				int i = rnd.nextInt(slotsListcopy.size());
				System.out.println("teste");
				slaveIps.add(slotsListcopy.get(i));
				slotsListcopy.remove(i);
				lastRnd = i;
				
				//Tenta alocar o segundo Slave para replicar (se existir)
				if(slotsListcopy.size() > 0){
					i = rnd.nextInt(slotsListcopy.size());
					while(i == lastRnd)
						i = rnd.nextInt(slotsListcopy.size());
					
					slaveIps.add(slotsListcopy.get(i));
					slotsListcopy.remove(i);
					lastRnd = i;
					
					//Tenta alocar o terceiro slave caso exista
					if(slotsListcopy.size() > 0){
						i = rnd.nextInt(slotsListcopy.size());
						while(i == lastRnd)
							i = rnd.nextInt(slotsListcopy.size());
						
						slaveIps.add(slotsListcopy.get(i));
					}
				}
				
				musicList.add(musicName);
				codMusicList.add(i);
				
				JCL_message msg2 = new JCL_messageImpl();
				msg2.setSetMusicas(slaveIps);
				ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
				out.writeObject(msg2);
				out.flush();
				
				out.close();
				in.close();
				
				break;
			}case 200:{//Registrar Slave
				int slaveNumber = slotsList.size();
				String[] lista = msg.getListaMusicas();
				slotsList.put(slaveNumber,msg.getNome());				
				
				for(int i = 0; i < lista.length; i++){
					String musicPath = "../" + lista[i];
					if(musicPath.endsWith(".mp3")){
						musicList.add(musicPath);
						codMusicList.add(slaveNumber);
					}
				}
				for(int i = 0; i < musicList.size(); i++){
					System.out.println("Slave no: " + codMusicList.get(i) + " / musica: " + musicList.get(i));
				}
				
				System.out.println("musicList"+musicList.size());
				
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
