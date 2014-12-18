package appl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import server.GenericResource;
import server.JCL_message;
import server.JCL_messageImpl;
import slave.SlaveGenericConsumer;


public class SlaveSocketConsumer<S extends Socket> extends SlaveGenericConsumer<S> {
	String[] musicList;
	
	public SlaveSocketConsumer(GenericResource<S> re) {
		super(re);
		
		File f = new File("../");
		musicList = f.list();
	}
	
	protected void doSomething(S str){		
		try {
			ObjectInputStream in = new ObjectInputStream(str.getInputStream());
			
			JCL_message msg = (JCL_message) in.readObject();
			System.err.println("Server: " + msg.getType());
			switch (msg.getType()){
				case 101:
					File f = new File(msg.getNome());
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					byte[] boxOfBytes = IOUtils.toByteArray((InputStream)bis);
					JCL_message msg2 = new JCL_messageImpl();
					msg2.setType(99);
					msg2.setConteudo(boxOfBytes);
					
					ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
					out.writeObject(msg2);
					
					out.flush();
					out.close();
					in.close();
					
					
					break;
			}
			
			
			
			str.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
