package linda.shm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import linda.Callback;
import linda.Tuple;

public class GestionnaireEvent2 {

	private ConcurrentHashMap<Tuple, Events> events;

	public GestionnaireEvent2() {
		this.events = new ConcurrentHashMap<Tuple, Events>();
	}

	public void addRead(Tuple t, Callback call) {
		Events event = this.events.get(t);
		if (event == null) {
			event = new Events();
			event.addRead(call);
			this.events.put(t, event);
		} else {
			event.addRead(call);
		}
		
		
		this.events.replace(key, value)

	}

	public void setTake(Tuple t, Callback call) {
		Events event = this.events.get(t);
		if (event == null) {
			event = new Events();
			event.setTake(call);
			this.events.put(t, event);
		} else {
			event.setTake(call);
		}
	}

	
	public Events getEvents(Tuple t) {
		
		Events res = new Events();
		Events temp;
		
		for(Tuple key : this.events.keySet()) {
			if(t.matches(key)) {
				temp = this.events.get(key);
				for(Callback cb : temp.getRead())
					res.addRead(cb);
				
				if(temp.getTake() != null)
					res.setTake(temp.getTake());
			}
		}
		
		return res;
	}
	
	/*
	public Events getEvents(Tuple t) {
		//Events events = this.events.get(t);
		
		Events res = new Events();
		Events temp;
		
		for(Tuple key : this.events.keySet()) {
			if(t.matches(key)) {
				temp = this.events.get(key);
				for(Callback cb : temp.getRead())
					res.addRead(cb);
				
				if(temp.getTake() != null)
					res.setTake(temp.getTake());
			}
		}
		
		return res;
		
		//if(events == null)
		//	events = new Events();
		//return events;
		//Au cas ou aucun events
		//return new Events();
	}*/

}

