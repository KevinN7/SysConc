package linda.test;

import static org.junit.Assert.*;
import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

import org.junit.Before;
import org.junit.Test;

public class TestTake {

	Linda linda;
	Tuple template1;
	Tuple template2;
	Tuple template3;
	Tuple tuple1;
	Tuple tuple2;
	Tuple res;
	Long start;
	boolean[] test;

	@Before
	public void setUp() throws Exception {
		this.linda = new linda.shm.CentralizedLinda2();

		this.template1 = new Tuple(Integer.class, String.class,
				"Basil le serpent");
		this.template2 = new Tuple(Integer.class, String.class);
		this.template3 = new Tuple(56,String.class);
		this.tuple1 = new Tuple(56, "Salut");
		this.tuple2 = new Tuple(56, "Salut", "Basil le serpent");
		this.res = null;
		this.start = System.nanoTime();
		this.test = new boolean[5];
		for(int i=0;i<5;i++)
			test[i] = false;
	}

	@Test
	//On a deux abonnements reads et deux abonnements takes
	//A la fin 3 abonnements doivent etre actif
	public void test() {

		new Thread() {
			public void run() {

				final class Callback1 implements Callback {
					public void call(Tuple t) {
						System.out.println("Thread0");
						test[0] = true;
					};
				}
				System.out.println("0");
				linda.eventRegister(eventMode.READ, eventTiming.FUTURE,
						template2, new Callback1());
			}

		}.start();
    
        new Thread() {
            public void run() {
                
                final class Callback1 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread1");
                		test[1] = true;
                	};
                }
                System.out.println("1");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template1, new Callback1());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback2 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread2");
                		test[2] = true;
                	};
                }
                System.out.println("2");
                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template1, new Callback2());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback3 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread3");
                		test[3] = true;
                	};
                }
                System.out.println("3");
                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template1, new Callback3());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback4 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread4");
                		test[4] = true;
                	};
                }
                System.out.println("4");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template1, new Callback4());
            }
					
        }.start();
        
        
    	//ATTENTE BIEN LONGUE COMME IL FAUT
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        this.linda.write(this.tuple2);
        
    	//ATTENTE BIEN LONGUE COMME IL FAUT
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue(!test[0] && test[1] && test[4]);
        assertTrue((!test[2] && test[3])||(test[2] && !test[3]));
        
	}
	
	//Faire des test avec des abonnements sur des motifs differents mais pouvant etre satisfait en meme temps
	@Test
	public void Test2() {
        new Thread() {
            public void run() {
                
                final class Callback1 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread0");
                	};
                }
                System.out.println("0");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template1, new Callback1());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback1 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread1");
                	};
                }
                System.out.println("1");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template2, new Callback1());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback2 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread2");
                	};
                }
                System.out.println("2");
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
                System.out.println("3");
                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template3, new Callback3());
            }
					
        }.start();
        
        new Thread() {
            public void run() {
                
                final class Callback4 implements Callback {
                	public void call(Tuple t) {
                		System.out.println("Thread4");
                	};
                }
                System.out.println("4");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template3, new Callback4());
            }
					
        }.start();
        
        
    	//ATTENTE BIEN LONGUE COMME IL FAUT
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        this.linda.write(this.tuple1);
        
    	//ATTENTE BIEN LONGUE COMME IL FAUT
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}

