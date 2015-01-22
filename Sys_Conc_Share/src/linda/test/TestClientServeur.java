package linda.test;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import linda.server.LindaServer;

import org.junit.Before;
import org.junit.Test;

public class TestClientServeur {

	LindaClient client ;
	LindaServer serveur;
	int port ;
	int num = 1;
	boolean rmi = true;
	String lien;
	Tuple template1;
	Tuple template2;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	Long start;

	@Before
	public void setUp() throws Exception {
		this.port=4500;
		this.lien = "//localhost:"+ port +"/basil";
		String lien2 = "//localhost:"+ port +"/basil";
		num++;
		this.serveur = new LindaServer(lien,port);
		this.client = new LindaClient(lien2);

		this.template1 = new Tuple(Integer.class,String.class,"Basil le serpent");
		this.template2 = new Tuple(Integer.class,String.class);
		this.tuple1 = new Tuple(56,"Salut");
		this.tuple2 = new Tuple(56,"Salut","Basil le serpent");
		this.res = null;
		this.start = System.nanoTime();
	}

	@Test
	public void test1() {
		this.res = this.client.tryTake(template1);
		assertNull(res);
	}
	
	@Test
	public void test2(){
		this.client.write(tuple1);
		this.res = this.client.tryTake(template1);
		assertNull(res);
	}
	
	@Test
	public void test3(){
		this.client.write(this.tuple2);
		this.res = this.client.tryTake(this.template1);
		assertTrue(res.matches(this.template1));
		assertTrue(res.equals(this.tuple2));
		this.res = this.client.tryTake(this.template1);
		assertNull(res);
	}
}
