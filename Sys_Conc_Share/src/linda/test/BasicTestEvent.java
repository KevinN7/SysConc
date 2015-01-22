package linda.test;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class BasicTestEvent {



		public static void main(String[] a) {
			
			final Linda linda;
			final Tuple template1;
			final Tuple template2;
			final Tuple tuple1;
			final Tuple tuple2;
			final Tuple res;
			final Long start;
			
			
			linda = new linda.shm.CentralizedLinda();

			template1 = new Tuple(Integer.class, String.class,
					"Basil le serpent");
			template2 = new Tuple(Integer.class, String.class);
			tuple1 = new Tuple(56, "Salut");
			tuple2 = new Tuple(56, "Salut", "Basil le serpent");
			res = null;
			start = System.nanoTime();
			
	        new Thread() {
	            public void run() {
	                
	                final class Callback1 implements Callback {
	                	public void call(Tuple t) {
	                		System.out.println("Thread1");
	                	};
	                }
	                
	                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template1, new Callback1());
	            }
						
	        }.start();
	        
	        new Thread() {
	            public void run() {
	                
	                final class Callback2 implements Callback {
	                	public void call(Tuple t) {
	                		System.out.println("Thread2");
	                	};
	                }
	                
	                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template1, new Callback2());
	            }
						
	        }.start();
	        
	        new Thread() {
	            public void run() {
	                
	                final class Callback3 implements Callback {
	                	public void call(Tuple t) {
	                		System.out.println("Thread3");
	                	};
	                }
	                System.out.println("tets fin3");
	                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template1, new Callback3());
	            }
						
	        }.start();
	        
	        new Thread() {
	            public void run() {
	                
	                final class Callback4 implements Callback {
	                	public void call(Tuple t) {
	                		System.out.println("Thread4");
	                	};
	                }
	                
	    	        System.out.println("tets fin4");
	                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template1, new Callback4());
	            }
						
	        }.start();
	        
        	//ATTENTE BIEN LONGUE COMME IL FAUT
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
	        linda.write(tuple2);
	        System.out.println("tets fin");
		}

}
