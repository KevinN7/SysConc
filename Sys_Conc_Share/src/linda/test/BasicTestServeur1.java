package linda.test;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import linda.server.LindaServer;

public class BasicTestServeur1 {


	public static void main(String[] a) throws RemoteException {
		
		try{
			LocateRegistry.createRegistry(4000);
		}catch(Exception e) {}
		
		final LindaServer lindas = new LindaServer("//localhost:4000/linda", 4000);
		final LindaClient linda = new LindaClient("//localhost:4000/linda");

		//Prend le tuple (Integer,String)
		new Thread() {
			public void run() {
				//ATTENTE BIEN LONGUE COMME IL FAUT
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Tuple motif = new Tuple(Integer.class, String.class);
				Tuple res = linda.take(motif);
				System.out.println("(1) Resultat:" + res);
				linda.debug("(1)");
			}
		}.start();

		//Ajout de 4 Tuple � la database toute les combinaisons de Integer et String
		new Thread() {
			public void run() {
				//ATTENTE COURTE
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Tuple t1 = new Tuple(4, 5);
				System.out.println("(2) write: " + t1);
				linda.write(t1);

				Tuple t11 = new Tuple(4, 5);
				System.out.println("(2) write: " + t11);
				linda.write(t11);

				Tuple t2 = new Tuple("hello", 15);
				System.out.println("(2) write: " + t2);
				linda.write(t2);

				Tuple t3 = new Tuple(4, "foo");
				System.out.println("(2) write: " + t3);
				linda.write(t3);

				linda.debug("(2)");

			}
		}.start();

	}
}
