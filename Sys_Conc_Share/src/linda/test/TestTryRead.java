package linda.test;

import static org.junit.Assert.*;
import linda.Linda;
import linda.Tuple;

import org.junit.Before;
import org.junit.Test;

public class TestTryRead {

	Linda linda;
	Tuple template1;
	Tuple template2;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	long start;
	
	/**
	 * @throws java.lang.Exception
	 */
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
	
	public void remplissage(Integer n) {
		Tuple l;
		for(int i=0;i<=n;i++) {
			this.linda.write(new Tuple(i,"Basil the snake!"));
		}
	}

	@Test
	public void test1() {
		this.res = this.linda.tryTake(template1);
		assertNull(res);
		System.out.println("Run time : " + (System.nanoTime() - start));
	}
	
	@Test
	public void test2(){
		this.linda.write(tuple1);
		this.res = this.linda.tryRead(template1);
		assertNull(res);
		System.out.println("Run time : " + (System.nanoTime() - start));
	}
	
	@Test
	public void test3(){
		this.linda.write(this.tuple2);
		this.res = this.linda.tryRead(this.template1);
		System.out.println(this.res.toString());
		assertTrue(res.matches(this.template1));
		assertFalse(res==this.tuple2);//Ce doit etre une copie
		
		this.res = this.linda.tryRead(this.template1);
		assertTrue(res.matches(this.template1));
		assertFalse(res==this.tuple2);
		System.out.println("Run time : " + (System.nanoTime() - start));
	}
	
	@Test
	public void test4(){
		remplissage(10000);
		this.linda.write(new Tuple(42,"Mickey"));
		Tuple res = this.linda.take(new Tuple(42,"Mickey"));
		assertTrue(res.matches(new Tuple(42,"Mickey")));
		System.out.println("Run time : " + (System.nanoTime() - start));
	}

}
