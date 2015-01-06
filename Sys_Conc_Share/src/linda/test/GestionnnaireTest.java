package linda.test;

import static org.junit.Assert.*;
import linda.Tuple;
import linda.shm.GestionnaireTuple;

import org.junit.Before;
import org.junit.Test;

public class GestionnnaireTest {
	Tuple t;
	GestionnaireTuple g;

	@Before
	public void setUp() throws Exception {
		this.g = new GestionnaireTuple();
	}

	@Test
	public void testMotifParent() {
		this.t = new Tuple(56,"Mickey",42,"53");
		System.out.println(this.g.motifParent(this.t));
		System.out.println(this.t);
		for(Object o:this.t) {
			//o.getClass().
			System.out.println( ( (Class<?>)(o.getClass()) ).getName() );
		}
		System.out.println(Integer.class.getName());
		
		Tuple k = new Tuple(Integer.class,String.class);
		System.out.println(k);
		for(Object o:k)
			System.out.println(((Class<?>) o).getName());
		
	}

}
