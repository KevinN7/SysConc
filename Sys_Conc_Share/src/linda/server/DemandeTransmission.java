package linda.server;

import java.rmi.RemoteException;

import linda.Callback;
import linda.Tuple;

public class DemandeTransmission implements Callback{
	
    private class Demande implements Callback {
    	private LindaMultiServerImpl demandeur;
    	public Demande(LindaMultiServerImpl s) {
    		this.demandeur = s;
    	}
    	
		@Override
		public void call(Tuple t) {
			try {
				this.demandeur.write(t);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }
    
    private Demande cb;
    
    public DemandeTransmission(LindaMultiServerImpl demandeur) {
    	this.cb = new Demande(demandeur);
    }

    public void call(final Tuple t) {
        new Thread() {
            public void run() {
            	cb.call(t);
            }
        }.start();
    }
	

}
