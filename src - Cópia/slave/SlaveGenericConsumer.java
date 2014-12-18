package slave;

import server.GenericResource;

public abstract class SlaveGenericConsumer<S> extends Thread {
	protected GenericResource<S> re;
	
	public SlaveGenericConsumer(GenericResource<S> re){
		this.re = re;
	}
	
	public void run(){
		try{
			S str = null;
			
			while((re.isFinished()==false)||(re.getNumOfRegisters()!=0)){
				if ((str = re.getRegister())!=null){
					//fazer algo com o recurso que foi consumido
					doSomething(str);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected abstract void doSomething(S str);
}
