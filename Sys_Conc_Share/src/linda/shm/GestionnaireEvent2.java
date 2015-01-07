package linda.shm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Tuple;

public class GestionnaireEvent2 {

	private Lock lock;
	private ConcurrentHashMap<Tuple, Events2> events;

	public GestionnaireEvent2() {
		this.events = new ConcurrentHashMap<Tuple, Events2>();
		this.lock = new ReentrantLock();
	}

	public void addRead(Tuple t, Callback call) {

		Events2 e = new Events2();
		e.addRead(call);
		Events2 h = this.events.putIfAbsent(t, e);
		if(h==null) {
			//La clé vient d'être ajouté
		}else{
			//La clé existait dejà
			h.addRead(call);
		}
		
	}

	public void setTake(Tuple t, Callback call) {
		Events2 event = this.events.get(t);
		if (event == null) {
			event = new Events2();
			event.setTake(call);
			this.events.put(t, event);
		} else {
			event.setTake(call);
		}
	}

	
	public Events2 getEvents(Tuple t) {
		
		Events2 res = new Events2();
		Events2 temp;
		Callback cb;
		boolean takeTrouve = false;
		this.lock.lock();
		
		for(Tuple key : this.events.keySet()) {
			if(t.matches(key)) {
				temp = this.events.remove(key);
				for(Callback c : temp.getRead())
					res.addRead(c);
				
				cb = temp.getTake();
				if(cb!=null) {
					if(!takeTrouve) {
						//On retire l abonnement take
						res.setTake(cb);
					} else {
						//On le remet dans l'ens
						setTake(key,cb);
					}
				} else {
					//Il n'y avait pas de Take associé
				}
					
			}
		}
		
		this.lock.unlock();
		
		return res;
	}

}

