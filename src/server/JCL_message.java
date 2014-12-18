/**
 * 
 */
package server;

import java.io.BufferedInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Joubert
 * @version 1.0
 * 
 * enables any type of messages in Java Ca&La
 */
public interface JCL_message extends Serializable{
	
	public abstract int getType();
	public abstract void setType(int type);
	public abstract byte[] getConteudo();
	public abstract void setConteudo(byte[] in);
	public abstract String getNome();
	public abstract void setNome(String n);
	public abstract String[] getListaMusicas();
	public abstract void setListaMusicas(String[] ll);
	public abstract String getClientIP();
	public abstract void setClientIP(String ip);
	public abstract Set<String> getSetMusicas();
	public abstract void setSetMusicas(Set<String> set);
}
