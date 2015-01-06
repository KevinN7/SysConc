/**
 * 
 */
package linda.test;

import static org.junit.Assert.*;
import linda.Linda;
import linda.Tuple;

import org.junit.Before;
import org.junit.Test;

/**
 * @author kevin
 *
 */
public class TestTryTake {

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
		this.linda = new linda.shm.CentralizedLinda2();
		
		this.template1 = new Tuple(Integer.class,String.class,"Basil le serpent");
		this.template2 = new Tuple(Integer.class,String.class);
		this.tuple1 = new Tuple(56,"Salut");
		this.tuple2 = new Tuple(56,"Salut","Basil le serpent");
		this.res = null;
	}

	/**
	 * Test method for {@link linda.shm.CentralizedLinda#tryTake(linda.Tuple)}.
	 */
	@Test
	public void test1() {
		this.res = this.linda.tryTake(template1);
		assertNull(res);
	}
	
	@Test
	public void test2(){
		this.linda.write(tuple1);
		this.res = this.linda.tryTake(template1);
		assertNull(res);
	}
	
	@Test
	public void test3(){
		this.linda.write(this.tuple2);
		this.res = this.linda.tryTake(this.template1);
		assertTrue(res.matches(this.template1));
		assertTrue(res.equals(this.tuple2));
		this.res = this.linda.tryTake(this.template1);
		assertNull(res);
	}
	
	

}
