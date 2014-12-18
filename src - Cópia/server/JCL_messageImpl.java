package server;

import java.io.BufferedInputStream;
import java.util.LinkedList;
import java.util.Set;

public class JCL_messageImpl implements JCL_message{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1298874117877687170L;
	private int type;
	byte[] conteudo;
	String nome;
	String[] listaMusicas;
	Set<String> setMusicas;
	String clientIP;
	
	public JCL_messageImpl(){
		
	}
	
	public byte[] getConteudo(){
		return conteudo;
	}
	
	public void setConteudo(byte[] in){
		conteudo = in;
	}
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;		
	}

	@Override
	public String getNome() {
		// TODO Auto-generated method stub
		return nome;
	}

	@Override
	public void setNome(String n) {
		// TODO Auto-generated method stub
		nome = n;
	}

	@Override
	public String[] getListaMusicas() {
		// TODO Auto-generated method stub
		return listaMusicas;
	}

	@Override
	public void setListaMusicas(String[] ll) {
		// TODO Auto-generated method stub
		//listaMusicas2.clear();
		listaMusicas = ll;
	}

	public String getClientIP(){
		return this.clientIP;
	}
	
	public void setClientIP(String ip){
		clientIP = ip;
	}
	
	public Set<String> getSetMusicas(){
		return setMusicas;
	}
	
	public void setSetMusicas(Set<String> set){
		this.setMusicas = set;
	}
}
