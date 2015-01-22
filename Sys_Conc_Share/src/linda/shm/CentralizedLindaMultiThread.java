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
public class CentralizedLindaMultiThread implements Linda {
	
	ConcurrentLinkedDeque<Tuple> tuples;
	ConcurrentLinkedDeque<Tuple> tuplesBuffer;
	ConcurrentHashMap<Tuple,ConcurrentLinkedQueue<ProcessusBloque>> listeAttente;
	Lock lock;
	GestionnaireEvent2 gestionnaireEvent;
	
    public CentralizedLindaMultiThread() {
    	this.tuples = new ConcurrentLinkedDeque<Tuple>();
    	this.lock = new ReentrantLock(true);
    	this.listeAttente = new ConcurrentHashMap<Tuple, ConcurrentLinkedQueue<ProcessusBloque>>();
    	this.gestionnaireEvent = new GestionnaireEvent2();
    	this.tuplesBuffer = new ConcurrentLinkedDeque<Tuple>();
    }

    //Renvoi la reference du premier tuple qui matche t
    //N'assure pas qu'a la fin le tuple soit toujours present dans la bdd et n'est pas deja etait supprim�
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
    
    //Cherche un tuple dans le buffer
    private Tuple findBuffer(Tuple t) {
    	Tuple res=null;
    	boolean trouve = false;
		java.util.Iterator<Tuple> i = this.tuplesBuffer.iterator();
		
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
		
		//Prob:si un nouveau take ou tryTake ou TakeAll se ramene pendant un write, prends le tuple venant d'�tre ajout�
		//		alors que ce tuple aurait du �tre consomm� par le deblocage d'un Take ou d'un eventTake pour garder la coherence
		//Soluce:ne pas ajouter direct le tuple � la base de donn�e principale
		
		
		//TRAITEMENT EVENT READ/////////////////////////////////////////////////////
		
		Events2 events = this.gestionnaireEvent.getEvents(t);
		for(Callback c:events.getRead())
			c.call(t);
		
		//TRAITEMENT READ BLOQUE/////////////////////////////////////////////////////
		
		//Construction des motif bloqu�s compatible avec le tuple t
		Collection<Tuple> motifBloques = new LinkedList<Tuple>();
		
		for(Tuple motifsBloquesCompatibles : this.listeAttente.keySet())
			if(t.matches(motifsBloquesCompatibles))
				motifBloques.add(motifsBloquesCompatibles);
		
		Condition premierTakeBloque = null;
		boolean toujoursPresent = true;
		ProcessusBloque processusBloqueCourant;

		Iterator<Tuple> i = motifBloques.iterator();
		while(toujoursPresent && i.hasNext()) {
			ConcurrentLinkedQueue<ProcessusBloque> ProcessAttente = this.listeAttente.get(i.next());
			Iterator<ProcessusBloque> j = ProcessAttente.iterator();
			while(toujoursPresent && j.hasNext()) {
				processusBloqueCourant = j.next();
				if(processusBloqueCourant.getMode().equals(eventMode.READ)) {
					lock.lock();
					processusBloqueCourant.getCds().signal();
					lock.unlock();
				} else {
					//C'est un take que l'on reveillera apres
					if(premierTakeBloque==null)
						premierTakeBloque = processusBloqueCourant.getCds();
				}
			}
		}
		
		//TRAITEMENT TAKE BLOQUE/////////////////////////////////////////////////////////////
		
		//Reveille du premier take bloque si present
		this.tuplesBuffer.add(t);
		if(premierTakeBloque!=null) {
			lock.lock();
			premierTakeBloque.signal();
			lock.unlock();
		} else {
			//On peut executer un event Take
			Callback cbTake = events.getTake(); 
			if(cbTake != null) {
				cbTake.call(t);
			} else {
				//Aucun Take n'a et� fait
				
				//CES DEUX INSTRUCTIONS DOIVENT PEUT ETRE MISE EN ATOMIQUE
				//Ajout du tuple BDD
				this.tuples.addFirst(t);
				//Retrait du buffer
				this.tuplesBuffer.remove(t);
			}
		}
		
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
			//La cl� �tait d�j� pr�sente
			this.listeAttente.get(motif).add(pb);
		}
		
		try {
			lock.lock();
			pb.getCds().await();
			lock.unlock();
		} catch (InterruptedException e) {e.printStackTrace();}

	}

	@Override
	public Tuple read(Tuple template) {
		
		Tuple res = this.find(template);

		while(res == null) {
			blockRead(template);
			res = this.find(template);
			res = this.findBuffer(template);
		}

		return res.deepclone();
	}


	private void blockTake(Tuple motif) {
		ProcessusBloque pb = new ProcessusBloque(this.lock, eventMode.TAKE);
		
		ConcurrentLinkedQueue<ProcessusBloque> queue = new ConcurrentLinkedQueue<ProcessusBloque>();
		
		queue.add(pb);
		if(this.listeAttente.putIfAbsent(motif,new ConcurrentLinkedQueue<ProcessusBloque>()) != null) {
			//La cl� �tait d�j� pr�sente
			this.listeAttente.get(motif).add(pb);
		}
		
		try {
			lock.lock();
			pb.getCds().await();
			lock.unlock();
		} catch (InterruptedException e) {e.printStackTrace();}

	}
	
	@Override
	public Tuple take(Tuple template) {
	    Tuple res = find(template);
	    while(res == null) {
	    	blockTake(template);
			res = this.find(template);
			res = this.findBuffer(template);
			
		    if(this.tuples.remove(res)) {
		    	//L element a bien etait supprime, on peut retourner res
		    }else{
		    	//L element a ete pris entre temps par un autre thread, on reboucle
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
		    	//L �lement a bien �tait supprim�
		    	res = recherche;
		    	recherche = null;
		    }else{
		    	//L �l�ment a �t� pris entre temps par un autre thread, on cherche si il y en a un autre
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
	
	}

	@Override
	public void debug(String prefix) {

	}

}