package linda.test;

import linda.*;

public class TestPerformanceTemps {
	
	static public class Salope extends Thread{
		private Linda linda;
		public Salope(Linda lin) {
			this.linda=lin;
		}
		public void run(){
            Tuple t1 = new Tuple(4, 5);
            linda.write(t1);

            Tuple t11 = new Tuple(4, 5);
            linda.write(t11);

            Tuple t2 = new Tuple("hello", 15);
            linda.write(t2);

            Tuple t3 = new Tuple(4, "foo");
            linda.write(t3);
            
            Tuple motif = new Tuple(Integer.class, Integer.class);
            linda.take(motif);
            
            motif = new Tuple(Integer.class, String.class);
            linda.take(motif);             
		}
	}

    public static void main(String[] a) {
                
        final Linda lindam = new linda.shm.CentralizedLindaMultiThread();
        final Linda linda = new linda.shm.CentralizedLinda();
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        long start = System.nanoTime();
                
        Thread[] t = new Thread[1000];
        for(int i=0;i<1000;i++) {
        	t[i] = new Salope(linda);
        }
        
        for(int i=0;i<1000;i++) {
        	t[i].start();
        }
        
        for(int i=0;i<1000;i++) {
        	try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
                
        System.out.println("Run time final:             " + (System.nanoTime() - start));
        
        
        //////////////////////////////////////////////////////////////////////////////////////////
        start = System.nanoTime();
        
        for(int i=0;i<1000;i++) {
        	t[i] = new Salope(lindam);
        }
        
        for(int i=0;i<1000;i++) {
        	t[i].start();
        }
        
        for(int i=0;i<1000;i++) {
        	try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
                
        System.out.println("Run time final multithread: " + (System.nanoTime() - start));
    }
}