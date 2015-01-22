package linda.test;

import static org.junit.Assert.*;
import linda.Linda;
import linda.Tuple;

import org.junit.Before;
import org.junit.Test;

public class LindaShmTest {
	
	Linda linda;
	Tuple template1;
	Tuple template2;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	Long start;

	@Before
	public void setUp() throws Exception {
		this.linda = new linda.shm.CentralizedLindaMultiThread();
		
		this.template1 = new Tuple(Integer.class,String.class,"Basil le serpent");
		this.template2 = new Tuple(Integer.class,String.class);
		this.tuple1 = new Tuple(56,"Salut");
		this.tuple2 = new Tuple(56,"Salut","Basil le serpent");
		this.res = null;
		this.start = System.nanoTime();
	}

	@Test
	public void test() {
	}

}
