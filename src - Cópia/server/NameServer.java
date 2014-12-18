package server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;

public class NameServer {
	public static LinkedList<String> realIP = new LinkedList<String>();
	public static LinkedList<String> nameSpace = new LinkedList<String>();
	
	public NameServer() throws SocketException{
		
	}
	
	private String findIP() throws SocketException{
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
        	Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        	for (InetAddress inetAddress : Collections.list(inetAddresses)) {
        		char[] temp = inetAddress.toString().toCharArray();
        		if (temp[1] == '2'){
        			return transformIP(temp);
        		}
        		else if(temp[1] == '1'){
        			if(temp[2] == '7' || temp[2] == '8' || temp[2] == '9'){
        				return transformIP(temp);
        			}
        		}
        	}
        }
        return "null";
	}
	
	private String transformIP(char[] ip){
		String newIp = "";
		for(int i = 1; i < ip.length; i++){
			newIp += ip[i];
		}
		return newIp;
	}
	
	public String getServerIP(String name){
		System.out.println("XXXXX"+realIP.size());
		for(int i = 0; i < realIP.size(); i++){
			if(name.equals(nameSpace.get(i))){
				return realIP.get(i);
			}
		}
		return "null";
	}
	
	public void add(String name) throws SocketException{
		nameSpace.add(name);
		
		String tempIP = this.findIP();
		if(tempIP == "null"){
			System.out.println("Não foi possivel descobrir o IP do servidor");
			System.out.println("Insira o IP do servidor manualmente");
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			realIP.add(s);
		}
		realIP.add(tempIP);
		System.out.println("XXX"+realIP.size());
	}
}
