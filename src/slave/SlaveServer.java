package slave;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.CoresAutodetect;
import server.GenericConsumer;
import server.GenericResource;

public abstract class SlaveServer {
	protected SlaveGenericConsumer<Socket>[] serverThreads;
	protected GenericResource<Socket> serverR;
	protected int port;
	protected ServerSocket serverSocket;
	protected long initialTime;
	
	private int numOfThreads;
	
	public SlaveServer(int port){
		this.initialTime = System.nanoTime();		
		this.port = port;
		numOfThreads = CoresAutodetect.detect();
		serverThreads = new SlaveGenericConsumer[numOfThreads];
		serverR = new GenericResource<Socket>();		
	}
	
	public void begin(){
		try{
			for(int i=0; i<numOfThreads; i++){
				serverThreads[i] = createSocketConsumer(serverR); 
				serverThreads[i].start();
			}
			
			//start listening 
			listen();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected void listen(){
		openServerSocket();
		 		
        while(! serverR.isStopped()){
            
            try {
            	Socket clientSocket = this.serverSocket.accept();
                serverR.putRegister(clientSocket);
            } catch (IOException e) {
                if(serverR.isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            } 
            
            
        }
        System.out.println("Server Stopped.") ;
	}
	
	private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + port, e);
        }
    }
	
	protected abstract SlaveGenericConsumer<Socket> createSocketConsumer( GenericResource<Socket> r);
}
