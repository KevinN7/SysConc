package linda.shm;

import linda.*;

import java.util.*;

//Gestionnaire utilisant un stockage avec des clé en motif pour accelerer l'acces
//Oriente performance, peu importe la famine

public class GestionnaireTuple {
	private Map<Tuple,LinkedList<Tuple>> tuples;
	
	public GestionnaireTuple() {
		this.tuples = new HashMap<Tuple,LinkedList<Tuple>>();
	}
	
	//private Tuple motifParent(Tuple t) {
	public Tuple motifParent(Tuple t) {
		Tuple res;
		StringBuilder sb = new StringBuilder();
		
        sb.append("[");
        
        for (Object o : t) {
        	Object h = o;
            if (!(o instanceof Class)) {
            	h = o.getClass();
            }
            sb.append(" ?" + ((Class<?>)h).getName());
        }
        
        sb.append(" ]");
        res = Tuple.valueOf(sb.toString());
		return res;
	}
	
	public void add(Tuple t) {
		Tuple key = motifParent(t);
		
		//ajout du tuple
		if(!this.tuples.containsKey(key)) {
			LinkedList<Tuple> temp = new LinkedList<Tuple>();
			temp.add(t);
			this.tuples.put(key, temp);
		} else {
			this.tuples.get(key).addFirst(t);
		}
	}
	
	
	public Tuple search(Tuple motif) {
		Tuple res = null;
		
		LinkedList<Tuple> l = this.tuples.get(motifParent(motif));
		if(l!=null) {
			Integer index = l.indexOf(motif);
			if(index!=-1)
				res  = (Tuple)l.get(index);
		}
		
		return res;
	}
	
	public void rem(Tuple t) {
		LinkedList<Tuple> l = this.tuples.get(motifParent(t));
		if(l!=null)
			l.remove(t);
	}

}
