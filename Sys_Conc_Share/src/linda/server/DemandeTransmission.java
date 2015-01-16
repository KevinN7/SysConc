package linda.server;

import linda.Callback;
import linda.Tuple;

public class DemandeTransmission implements Callback{
	
    private class Demande implements Callback {
    	private Linda4Server demandeur;
    	public Demande(Linda4Server s) {
    		this.demandeur = s;
    	}
    	
		@Override
		public void call(Tuple t) {
			this.demandeur.write(t);
		}
    }
    
    private Demande cb;
    
    public DemandeTransmission(Linda4Server demandeur) {
    	this.cb = new Demande(demandeur);
    }

    public void call(final Tuple t) {
        new Thread() {
            public void run() {
            	cb.call(t);
                //this.demandeur.write(t);
            }
        }.start();
    }
	

}
