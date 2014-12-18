package appl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import server.GenericConsumer;
import server.GenericResource;
import server.JCL_message;
import server.JCL_messageImpl;
import slave.SlaveGenericConsumer;
import slave.SlaveServer;

public class MainSlave extends SlaveServer{
	private String mainServerHost = "127.0.0.1";
	private static int serverPort = 6969;
	private static int slavePort = 6666;
	private String slaveIP = "127.0.0.1";
			
	public static void main(String[] args) throws SocketException {
		new MainSlave(slavePort);
	}
	
	public MainSlave(int port){
		super(port);
		
		//ENVIAR MENSAGEM PRO SERVIDOR REGISTRANDO IP E ENVIANDO LISTA DE MUSICAS
		Socket s;
		try {
			//Registrar Slave
			String[] musicList;
			s = new Socket(mainServerHost, serverPort);
			File f = new File("../");
			musicList = f.list();
			JCL_message msg = new JCL_messageImpl();
			msg.setListaMusicas(musicList);
			msg.setNome(slaveIP);
			msg.setType(200);
			
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
						
			out.writeObject(msg);
			out.flush();
			out.close();
			s.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println("Slave ok!");
		
		this.begin();
	}
	
	protected SlaveGenericConsumer<Socket> createSocketConsumer(GenericResource<Socket> r){
		return new SlaveSocketConsumer<Socket>(r);
	}
}
