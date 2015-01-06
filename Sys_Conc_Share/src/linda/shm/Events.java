package linda.shm;

import java.util.Collection;
import java.util.LinkedList;

import linda.Callback;

public class Events {
	private Collection<Callback> callbackRead;
	private Callback callbackTake;

	public Events() {
		this.callbackRead = new LinkedList<Callback>();
		this.callbackTake = null;
	}

	public void addRead(Callback c) {
		this.callbackRead.add(c);
	}

	public void setTake(Callback c) {
		this.callbackTake = c;
	}

	public Callback getTake() {
		return this.callbackTake;
	}

	public Collection<Callback> getRead() {
		return this.callbackRead;
	}
}