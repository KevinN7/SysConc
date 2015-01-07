package linda.shm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda5 implements Linda {
	
	ConcurrentLinkedDeque<Tuple> tuples;
	ConcurrentHashMap<Tuple,ConcurrentLinkedQueue<ProcessusBloque>> listeAttente;
	Lock lock;
	GestionnaireEvent gestionnaireEvent;
	
    public CentralizedLinda5() {
    	this.tuples = new ConcurrentLinkedDeque<Tuple>();
    	this.lock = new ReentrantLock(true);
    	this.listeAttente = new ConcurrentHashMap<Tuple, ConcurrentLinkedQueue<ProcessusBloque>>();
    	this.gestionnaireEvent = new GestionnaireEvent();
    }

    //renvoi la reference du premier tuple qui matche t
    //N'assure pas qu'à la fin le tuple soit toujours present dans la bdd et n'est pas déjà etait supprimé
    //Renvoit null si aucun ne matche
    private Tuple find(Tuple t) {
    	Tuple res=null;
    	boolean trouve = false;
		java.util.Iterator<Tuple> i = this.tuples.iterator();
		
		while(i.hasNext() && !trouve) {
			res = i.next();
			trouve = res.matches(t);
		}
		if(!trouve)
			res=null;
		
    	return res;
    }
     
    
	@Override
	public void write(Tuple t) {
		
		//TRAITEMENT APPELS BLOQUE
		
		//Ajout du tuple BDD
		this.tuples.addFirst(t);
		
		//Reveille des appels bloquants en attente (NON FIFO)

		//Construction des motif bloqués compatible avec le tuple t
		Collection<Tuple> motifBloques = new LinkedList<Tuple>();
		
		for(Tuple motifsBloquesCompatibles : this.listeAttente.keySet())
			if(t.matches(motifsBloquesCompatibles))
				motifBloques.add(motifsBloquesCompatibles);
		
		
		
		boolean toujoursPresent = true;
		ProcessusBloque processusBloqueCourant;

		Iterator<Tuple> i = motifBloques.iterator();
		while(toujoursPresent && i.hasNext()) {
			LinkedList<ProcessusBloque> ProcessAttente = this.listeAttente.get(i.next());
			Iterator<ProcessusBloque> j = ProcessAttente.iterator();
			while(toujoursPresent && j.hasNext()) {
				processusBloqueCourant = j.next();
				processusBloqueCourant.getCds().signal();
				if(processusBloqueCourant.getMode().equals(eventMode.TAKE)) {
					toujoursPresent = false;
				}
			}
		}
		
		
		
		//PRIORITE : 	APPEL BLOQUANT, EVENT READ, EVENT TAKE
		//TRAITEMENT EVENT
		
		Events events = this.gestionnaireEvent.getEvents(t);
		for(Callback c:events.getRead())
			c.call(t);
		
		Callback cbTake = events.getTake(); 
		if(cbTake != null)
			cbTake.call(t);
		
	}

	@Override
	public Tuple tryRead(Tuple template) {
		
		Tuple res = find(template);

		if(res!=null)
			res = res.deepclone();
		return res;
    }
	
	private void blockRead(Tuple motif) {
		ProcessusBloque pb = new ProcessusBloque(this.lock, eventMode.READ);
		
		ConcurrentLinkedQueue<ProcessusBloque> queue = new ConcurrentLinkedQueue<ProcessusBloque>();
		
		queue.add(pb);
		if(this.listeAttente.putIfAbsent(motif,new ConcurrentLinkedQueue<ProcessusBloque>()) != null) {
			//La clé était déjà présente
			this.listeAttente.get(motif).add(pb);
		}
		
		try {
			pb.getCds().await();
		} catch (InterruptedException e) {e.printStackTrace();}

	}

	@Override
	public Tuple read(Tuple template) {
		
		Tuple res = this.find(template);

		while(res == null) {
			blockRead(template);
			res = this.find(template);
		}

		return res.deepclone();
	}


	private void blockTake(Tuple motif) {
		ProcessusBloque pb = new ProcessusBloque(this.lock, eventMode.TAKE);
		
		ConcurrentLinkedQueue<ProcessusBloque> queue = new ConcurrentLinkedQueue<ProcessusBloque>();
		
		queue.add(pb);
		if(this.listeAttente.putIfAbsent(motif,new ConcurrentLinkedQueue<ProcessusBloque>()) != null) {
			//La clé était déjà présente
			this.listeAttente.get(motif).add(pb);
		}
		
		try {
			pb.getCds().await();
		} catch (InterruptedException e) {e.printStackTrace();}

	}
	
	@Override
	public Tuple take(Tuple template) {
	    Tuple res = find(template);
	    while(res == null) {
	    	blockTake(template);
			res = this.find(template);
			
		    if(this.tuples.remove(res)) {
		    	//L élement a bien était supprimé, on peut retourner res
		    }else{
		    	//L élément a été pris entre temps par un autre thread, on reboucle
		    	res = null;
		    }    
	    }
	    
		return res;
	}


	@Override
	public Tuple tryTake(Tuple template) {
		Tuple res = null;
		
		Tuple recherche = this.find(template);
		
		while(recherche!=null) {
			//Un tuple matche le template
			//Tentative de prise
		    if(this.tuples.remove(recherche)) {
		    	//L élement a bien était supprimé
		    	res = recherche;
		    	recherche = null;
		    }else{
		    	//L élément a été pris entre temps par un autre thread, on cherche si il y en a un autre
			    recherche = this.find(template);
		    }
		}
		
		return res;
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		//Le take all doit exclure les autres take et takeall
		Collection<Tuple> res = new LinkedList<Tuple>();
		Tuple t = this.tryTake(template);
		while(t!=null) {
			res.add(t);
			t = this.tryTake(template);
		}
		return res;
	}

	@Override
    public Collection<Tuple> readAll(Tuple template) {
		Collection<Tuple> res = new LinkedList<Tuple>();
		Tuple t = tryRead(template);
		while(t!=null){
            res.add(t);
            t = tryRead(template);
		}

		return res;
	}
	
	/*
	 Tuple motif;
	motif = new Tuple(Integer.class, String.class);
	Callback cb = new AsynchronousCallback(new MyCallback());
	linda.eventRegister(...,...,motif, cb);
	 */
	
	
	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, Callback callback) {
		
		Tuple recherche;
		if( timing.equals(eventTiming.IMMEDIATE) ) {
			if(mode.equals(eventMode.READ)) {
				recherche = tryRead(template);
			} else {
				recherche = tryTake(template);
			}
			
			if(recherche != null) {
				callback.call(recherche);
			}else{
				//Inscription
				if(mode.equals(eventMode.READ)) {
					this.gestionnaireEvent.addRead(template, callback);
				} else {
					this.gestionnaireEvent.setTake(template, callback);
				}
			}
		} else {
			//Inscription
			if(mode.equals(eventMode.READ)) {
				this.gestionnaireEvent.addRead(template, callback);
			} else {
				this.gestionnaireEvent.setTake(template, callback);
			}
		}
		
		
		/*Thread.currentThread().getId();
		
		//Traitement immediate au cas ou
		if(timing.equals(eventTiming.IMMEDIATE)) {
			
		}*/
		
	}

	@Override
	public void debug(String prefix) {

	}

}