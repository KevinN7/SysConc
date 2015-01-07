package linda.shm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import linda.Linda.eventMode;

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