package linda.server;

import linda.Tuple;

public class CBDemand {
	private Linda4Server demandeur;
	
	public CBDemand(Linda4Server s) {
		this.demandeur = s;
	}
    public void call(final Tuple t) {
        new Thread() {
            public void run() {
            	demandeur.write(t);
            }
        }.start();
    }

}
