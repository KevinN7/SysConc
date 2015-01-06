package linda.shm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import linda.Tuple;

//Gestionnaire limitant la famine mais moins performant
public class GestionnaireTupleDate {
	
	private class TupleDate{
		private Tuple tuple;
		private Integer id;
		
		public TupleDate(Tuple t,Integer id) {
			this.tuple = t;
			this.id = id;
		}

		public Tuple getTuple() {
			return tuple;
		}

		public Integer getId() {
			return id;
		}
	}
	
	private LinkedList<TupleDate> tuples;
	private Integer dernierId;
	
	public GestionnaireTupleDate() {
		this.tuples = new LinkedList<TupleDate>();
		this.dernierId = 0;
	}
	
	//Retourne le tuple correspondant le plus vieux
	public Tuple search (Tuple motif) {
		Tuple res = null;
		Iterator<TupleDate> i = this.tuples.iterator();
		Integer meilleur = Integer.MAX_VALUE;
		TupleDate tupleCourant;
		
		while(i.hasNext()) {
			tupleCourant = i.next();
			if(tupleCourant.getTuple().matches(motif) && tupleCourant.getId() < meilleur) {
				res = tupleCourant.getTuple();
			}
		}
		
		return res;
	}
	
	public void add(Tuple t) {
		//Pr accelerer la suppression, on met les nouveaux tuples susceptibles d'etre supprime en premier
		this.tuples.addFirst(new TupleDate(t, this.dernierId));
		dernierId++;
	}
	
	public void rem(Tuple t) {
		Iterator<TupleDate> i = this.tuples.iterator();
		boolean trouve = false;
		TupleDate tupleCourant;
		
		while(i.hasNext() && !trouve) {
			tupleCourant = i.next();
			if(tupleCourant.getTuple().matches(t)) {
				this.tuples.remove(tupleCourant);
				trouve = true;
			}
		}
	}

}
