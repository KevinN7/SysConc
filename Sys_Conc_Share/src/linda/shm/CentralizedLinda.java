package linda.shm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	Collection<Tuple> tuples;
	Lock lock;
	Map<Tuple,LinkedList<ProcessusBloque>> listeAttente;
	GestionnaireEvent gestionnaireEvent;
	
	public class ProcessusBloque {
		Condition cds;
		eventMode mode;
		
		public ProcessusBloque(Lock lock, eventMode m) {
			this.cds = lock.newCondition();
			this.mode = m;
		}
		
		public eventMode getMode() {
			return this.mode;
		}
		
		public Condition getCds() {
			return this.cds;
		}
	}
	
    public CentralizedLinda() {
    	this.tuples = new LinkedList<Tuple>();
    	this.lock = new ReentrantLock(true);
    	this.listeAttente = new HashMap<Tuple, LinkedList<ProcessusBloque>>();
    	this.gestionnaireEvent = new GestionnaireEvent();
    }

    //renvoi le premier tuple qui matche t
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
		this.lock.lock();
		
		//TRAITEMENT APPELS BLOQUE
		
		this.tuples.add(t);
		//Reveille des appels bloquants en attente (NON FIFO)

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
		
		this.lock.unlock();
	}

	@Override
	public Tuple tryRead(Tuple template) {

		this.lock.lock();
		Tuple res = find(template);
		this.lock.unlock();

		if(res!=null)
			res = res.deepclone();
		return res;
    }

	@Override
	public Tuple read(Tuple template) {
		this.lock.lock();
		Tuple res = this.find(template);

		while(res == null) {
			try {
				LinkedList<ProcessusBloque> list = this.listeAttente.get(template);
				if(list == null) {
					list = new LinkedList<ProcessusBloque>();
					this.listeAttente.put(template, list);
				}
				
				ProcessusBloque pb = new ProcessusBloque(this.lock, eventMode.READ);
				list.add(pb);
				pb.getCds().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			res = this.find(template);
		}
		this.lock.unlock();

		Tuple result = res.deepclone();
		//return res.deepclone();
		return result;
	}


	@Override
	public Tuple take(Tuple template) {
		this.lock.lock();
		
	    Tuple res = find(template);
	    while(res == null) {
	    	try {
				LinkedList<ProcessusBloque> list = this.listeAttente.get(template);
				if(list == null) {
					list = new LinkedList<ProcessusBloque>();
					this.listeAttente.put(template, list);
				}
				
				ProcessusBloque pb = new ProcessusBloque(this.lock, eventMode.TAKE);
				list.add(pb);
				pb.getCds().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			res = this.find(template);
	    	
	    }
	    
	    this.tuples.remove(res);
	    
	    this.lock.unlock();
	    
		return res;
	}



	@Override
	public Tuple tryTake(Tuple template) {
		this.lock.lock();
		Tuple t = this.find(template);
		
		if(t!=null)
			this.tuples.remove(t);
		this.lock.unlock();
		return t;
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
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
		
		//Traitement immediate au cas ou
		if(timing.equals(eventTiming.IMMEDIATE)) {
			
		}
		
		//Inscription sur liste
		
		this.lock.lock();
		if(mode.equals(eventMode.READ)) {
			this.gestionnaireEvent.addRead(template, callback);
		} else {
			this.gestionnaireEvent.setTake(template, callback);
		}
		
		this.lock.unlock();
		
	}

	@Override
	public void debug(String prefix) {

	}

}
