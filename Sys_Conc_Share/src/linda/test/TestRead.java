package linda.test;

import static org.junit.Assert.*;
import linda.Linda;
import linda.Tuple;

import org.junit.Before;
import org.junit.Test;

public class TestRead {

	Linda linda;
	Tuple template1;
	Tuple template2;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.linda = new linda.shm.CentralizedLinda5();
		
		this.template1 = new Tuple(Integer.class,String.class,"Basil le serpent");
		this.template2 = new Tuple(Integer.class,String.class);
		this.tuple1 = new Tuple(56,"Salut");
		this.tuple2 = new Tuple(56,"Salut","Basil le serpent");
		this.res = null;
	}
	
	@Test
	public void test1() {
		this.linda.write(this.tuple2);
		this.res = this.linda.read(template1);
		assertTrue(this.res.matches(template1));
		assertFalse(this.res==this.tuple2);
	}

}
