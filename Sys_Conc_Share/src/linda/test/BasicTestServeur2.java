package linda.test;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import linda.server.LindaServer;

public class BasicTestServeur2 {
    public static void main(String[] a) throws RemoteException {
    	
    	//Lancement RMIregistry si pas lancé
		try{
			LocateRegistry.createRegistry(4000);
		}catch(Exception e) {}
    	
    	final LindaServer lindas = new LindaServer("//localhost:4000/linda", 4000);
    	final LindaClient linda = new LindaClient("//localhost:4000/linda");
        
    	int j=1;
        //Lancement de 3 Thread lisant un motif Integer String
        for (int i = 1; i <= 3; i++) {
        	
            //Fait un read d'un motif Integer string
            new Thread() {  
                public void run() {       
                    Tuple motif = new Tuple(Integer.class, String.class);
                    Tuple res = linda.read(motif);
                    System.out.println("(Thread:) Resultat:" + res);
                }
            }.start();
        }
                
        
        new Thread() {
            public void run() {
            	//ATTENTE////////////////////////////////////////////
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ////////////////////////////////////////////////////////
                
                Tuple t1 = new Tuple(4, 5);
                System.out.println("write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("write: " + t2);
                linda.write(t2);

                linda.debug("(0)");

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("write: " + t3);
                linda.write(t3);

            }
        }.start();
                
    }
}
