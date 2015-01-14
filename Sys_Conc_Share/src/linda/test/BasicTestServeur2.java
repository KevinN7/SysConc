package linda.test;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;

import linda.Linda;
import linda.Tuple;
import linda.server.LindaServer;

public class BasicTestServeur2 {
    public static void main(String[] a) throws RemoteException {
    	final LindaServer linda = new LindaServer("//localhost:8080/linda", 8080);
        //final Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");
        
        //Lancement de 3 Thread lisant un motif Integer String
        for (int i = 1; i <= 3; i++) {
            final int j = i;
            //Fait un read d'un motif Integer string
            new Thread() {  
                public void run() {
                	//ATTENTE
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    Tuple motif = new Tuple(Integer.class, String.class);
                    Tuple res = linda.read(motif);
                    System.out.println("("+j+") Resultat:" + res);
                    linda.debug("("+j+")");
                }
            }.start();
        }
                
        
        
        new Thread() {
            public void run() {
            	//ATTENTE
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(0) write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(0) write: " + t2);
                linda.write(t2);

                linda.debug("(0)");

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(0) write: " + t3);
                linda.write(t3);
                                
                linda.debug("(0)");

            }
        }.start();
                
    }
}
