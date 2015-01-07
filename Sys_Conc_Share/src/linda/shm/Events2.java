package linda.shm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import linda.Callback;

public class Events2 {
	private Collection<Callback> callbackRead;
	
	private AtomicReference<Callback> cbTake;

	public Events2() {
		this.callbackRead = new LinkedList<Callback>();
		this.cbTake = new AtomicReference<Callback>(null) ;
	}

	public void addRead(Callback c) {
		this.callbackRead.add(c);
	}

	public void setTake(Callback c) {
		this.cbTake.set(c);
	}

	public Callback getTake() {
		return this.cbTake.getAndSet(null);//Peut etre pas obligé
	}

	public Collection<Callback> getRead() {
		return this.callbackRead;
	}
}