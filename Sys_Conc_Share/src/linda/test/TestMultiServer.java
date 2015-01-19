package linda.test;

import static org.junit.Assert.*;
import linda.Callback;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.server.LindaClient;
import linda.server.LindaMultiServerImpl;

import org.junit.Before;
import org.junit.Test;

public class TestMultiServer {

	Tuple template1;
	Tuple template2;
	Tuple template3;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	
	LindaMultiServerImpl master;
	LindaMultiServerImpl slave1;
	LindaMultiServerImpl slave2;
	LindaMultiServerImpl slave3;
	
	LindaClient client1;
	LindaClient client2;
	
	@Before
	public void setUp() throws Exception {
		Integer port=4500;
		String lienMaster = "rmi://localhost:"+ port +"/basil";
		String lienSlave1 = "rmi://localhost:"+ port +"/bruckner";
		String lienSlave2 = "rmi://localhost:"+ port +"/mahler";
		String lienSlave3 = "rmi://localhost:"+ port +"/tchaikovsky";
		
		this.master = new LindaMultiServerImpl(lienMaster, port, true, null);
		this.slave1 = new LindaMultiServerImpl(lienSlave1, port, false, lienMaster);
		this.slave2 = new LindaMultiServerImpl(lienSlave2, port, false, lienMaster);
		this.slave3 = new LindaMultiServerImpl(lienSlave3, port, false, lienMaster);
		
		this.client1 = new LindaClient(lienSlave1);
		this.client2 = new LindaClient(lienSlave3);
		
		this.template1 = new Tuple(Integer.class, String.class,
				"Basil le serpent");
		this.template2 = new Tuple(Integer.class, String.class);
		this.template3 = new Tuple(56,String.class);
		this.tuple1 = new Tuple(56, "Salut");
		this.tuple2 = new Tuple(56, "Salut", "Basil le serpent");
		this.res = null;
	}

	@Test
	public void test() {
		
		
		new Thread() {
			public void run() {

				/*final class Callback1 implements Callback {
					public void call(Tuple t) {
						System.out.println("Thread0");
						test[0] = true;
					};
				}*/
				System.out.println("Client1");
				Tuple h = client1.read(template1);
				System.out.println("c1 recu"+h);
			}
		}.start();
		
		
		new Thread() {
			public void run() {
		    	//ATTENTE BIEN LONGUE COMME IL FAUT
		        try {
		            Thread.sleep(2000);
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }

				client2.write(tuple2);
				System.out.println("Client2:ajout termine");
			}
		}.start();
		
		
	}

}
