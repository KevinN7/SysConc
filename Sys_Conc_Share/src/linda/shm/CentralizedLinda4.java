package linda.shm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class CentralizedLinda4 implements Linda{

	GestionnaireTupleDate tuples;
	Lock lock;
	Map<Tuple, Condition> cds;

	public CentralizedLinda4() {
		this.tuples = new GestionnaireTupleDate();
		this.lock = new ReentrantLock(true);
		this.cds = new HashMap<Tuple, Condition>();
	}

	@Override
	public void write(Tuple t) {
		this.lock.lock();
		
		this.tuples.add(t);
		
		Collection<Tuple> cles = this.cds.keySet();
		for (Tuple c : cles) {
			if (t.matches(c))
				this.cds.get(c).signalAll();
		}
		this.lock.unlock();
	}

	@Override
	public Tuple tryRead(Tuple template) {

		this.lock.lock();
		Tuple res = this.tuples.search(template);
		this.lock.unlock();

		if (res != null)
			res = res.deepclone();
		return res;
	}

	@Override
	public Tuple read(Tuple template) {
		Tuple res = this.tuples.search(template);

		this.lock.lock();

		while (res == null) {
			try {
				if (!this.cds.containsKey(template))
					this.cds.put(template, this.lock.newCondition());
				this.cds.get(template).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			res = this.tuples.search(template);
		}
		this.lock.unlock();

		Tuple result = res.deepclone();
		return result;
	}

	@Override
	public Tuple take(Tuple template) {
		this.lock.lock();
		Tuple res = this.tuples.search(template);
		while (res == null) {
			try {
				if (!this.cds.containsKey(template))
					this.cds.put(template, this.lock.newCondition());

				this.cds.get(template).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			res = this.tuples.search(template);

		}
		this.lock.unlock();

		this.tuples.rem(res);
		return res;
	}

	@Override
	public Tuple tryTake(Tuple template) {
		this.lock.lock();
		
		Tuple t = this.tuples.search(template);
		this.lock.unlock();

		if (t != null)
			this.tuples.rem(t);
		return t;
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		Collection<Tuple> res = new LinkedList<Tuple>();
		Tuple t = this.tryTake(template);
		while (t != null) {
			res.add(t);
			t = this.tryTake(template);
		}
		return res;
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		Collection<Tuple> res = new LinkedList<Tuple>();
		Tuple t = tryRead(template);
		while (t != null) {
			res.add(t);
			t = tryRead(template);
		}

		return res;
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, Callback callback) {

	}

	@Override
	public void debug(String prefix) {

	}
}
