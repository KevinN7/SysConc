package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.CentralizedLinda;
import linda.Tuple;

public class Linda4Server extends UnicastRemoteObject implements Linda4S {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	HashSet<Linda4Server> servs;
	HashSet<String> uris;
	Linda4Server master;
	boolean ismaster;
	
	CentralizedLinda linda;
	Registry registry;

	public Linda4Server(String uri, int port, boolean nismaster, String masterUri) throws RemoteException {
		//registry = LocateRegistry.createRegistry(port);
		linda = new CentralizedLinda();
		ismaster = nismaster;
		try {
			Naming.rebind(uri, this);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if(!ismaster) {
			try {
				master = (Linda4Server)Naming.lookup(masterUri);
				master.newServer(uri);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void write(Tuple t) {
		linda.write(t);
	}
	
	public Tuple take(Tuple template) {
		Tuple res;
		Linda4Server servCourant;
		
		res = this.linda.tryTake(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<Linda4Server> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((Linda4Server) i).tryTakeServer(template);
			}
			
			//A t-on un candidat?
			if(res == null) {
				//Pas de candidat
				
				//On enregistre des eventRegister chez tout le monde
				DemandeTransmission cbd = new DemandeTransmission(this);
				CbDist cbdv;
				try {
					cbdv = new CbDist(cbd);
					for(Linda4Server s:this.servs) {
						s.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, cbdv);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				

				
				
				//On s'endort et on sera reveillé par un wite d'un callback
				this.linda.take(template);
			} else {
				//On atrouvé un candidat
				return res;
			}
		}
		return res;
	}

	@Override
	public Tuple read(Tuple template) {
		Tuple res;
		Linda4Server servCourant;
		
		res = this.linda.tryRead(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<Linda4Server> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((Linda4Server) i).tryReadServer(template);
			}
			
			//A t-on un candidat?
			if(res == null) {
				//Pas de candidat
				
				//On enregistre des eventRegister chez tout le monde
				DemandeTransmission cbd = new DemandeTransmission(this);
				CbDist cbdv;
				try {
					cbdv = new CbDist(cbd);
					for(Linda4Server s:this.servs) {
						s.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, cbdv);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				

				
				
				//On s'endort et on sera reveillé par un wite d'un callback
				this.linda.read(template);
			} else {
				//On atrouvé un candidat
				return res;
			}
		}
		return res;
	}

	@Override
	public Tuple tryTake(Tuple template) {
		Tuple res;
		Linda4Server servCourant;
		
		res = this.linda.tryTake(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<Linda4Server> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((Linda4Server) i).tryTakeServer(template);
			}
		}
		return res;
	}

	@Override
	public Tuple tryRead(Tuple template) {
		Tuple res;
		Linda4Server servCourant;
		
		res = this.linda.tryRead(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<Linda4Server> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((Linda4Server) i).tryReadServer(template);
			}
		}
		return res;
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		return linda.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		return linda.readAll(template);
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, CbDist callback) {
		linda.eventRegister(mode,timing,template,new CallbackServ(callback));
	}

	@Override
	public void debug(String prefix) {
		linda.debug(prefix);
	}

	@Override
	public void newServer(String uri) {
		if(ismaster){
			for(Linda4Server actuel : servs){
				actuel.newServer(uri);
			}
			try {
				this.servs.add((Linda4Server)Naming.lookup(uri));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			try {
				this.servs.add((Linda4Server)Naming.lookup(uri));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Regarde uniquement en local
    public Tuple tryTakeServer(Tuple template) {
    	Tuple res = null;
    	
    	res = this.linda.tryTake(template);
    	
    	return res;
    }

    public Tuple tryReadServer(Tuple template){
    	Tuple res = null;
    	res= this.linda.tryRead(template);
    	return res;
    }

}
