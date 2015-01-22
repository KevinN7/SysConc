package linda.test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaServer;

public class BasicTestServeur1 {


	public static void main(String[] a) throws RemoteException {
		
		//Registry reg = LocateRegistry.createRegistry(4001);
		final LindaServer lindas = new LindaServer("//localhost:4001/linda", 4001);
		try {
			Naming.rebind("//localhost:4001/linda", lindas);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final Linda linda = new linda.server.LindaClient("//localhost:4001/linda");

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
		
    	//ATTENTE COURTE
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        

	}
}
