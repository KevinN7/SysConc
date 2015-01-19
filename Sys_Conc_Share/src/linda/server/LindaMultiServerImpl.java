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

import linda.AsynchronousCallback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.CentralizedLindaTemp1;
import linda.Tuple;

public class LindaMultiServerImpl extends UnicastRemoteObject implements LindaMultiServer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	HashSet<LindaMultiServerImpl> servs;
	HashSet<String> uris;
	LindaMultiServerImpl master;
	boolean ismaster;
	
	CentralizedLindaTemp1 linda;
	Registry registry;

	//peut etre virer le port inutile?
	public LindaMultiServerImpl(String uri, int port, boolean nismaster, String masterUri) throws RemoteException {
		this.linda = new CentralizedLindaTemp1();
		this.ismaster = nismaster;
		try {
			Naming.rebind(uri, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!ismaster) {
			try {
				master = (LindaMultiServerImpl)Naming.lookup(masterUri);
				servs = master.newServer(this);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void write(Tuple t)  throws RemoteException{
		linda.write(t);
	}
	
	public Tuple take(Tuple template)  throws RemoteException{
		Tuple res;
		LindaMultiServerImpl servCourant;
		
		//On regarde en local
		res = this.linda.tryTake(template);
		if(res == null) {
			//On regarde chez les autres serveurs
			Iterator<LindaMultiServerImpl> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((LindaMultiServerImpl) i).tryTakeLocal(template);
			}
			
			//A t-on un candidat?
			if(res == null) {
				//Pas de candidat actuellement sur tous les serveurs
				
				//On enregistre des eventRegister chez tout le monde
				DemandeTransmission cbd = new DemandeTransmission(this);
				CbDist demandeDistante;
				try {
					demandeDistante = new CbDist(cbd);
					for(LindaMultiServerImpl s:this.servs) {
						s.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, demandeDistante);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				
				//On s'endort et on sera reveillé par un wite d'un callback
				this.linda.take(template);
			}
		}
		return res;
	}

	@Override
	public Tuple read(Tuple template)  throws RemoteException{
		Tuple res;
		LindaMultiServerImpl servCourant;
		
		res = this.linda.tryRead(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<LindaMultiServerImpl> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((LindaMultiServerImpl) i).tryReadLocal(template);
			}
			
			//A t-on un candidat?
			if(res == null) {
				//Pas de candidat
				
				//On enregistre des eventRegister chez tout le monde
				DemandeTransmission cbd = new DemandeTransmission(this);
				CbDist cbdv;
				try {
					cbdv = new CbDist(cbd);
					for(LindaMultiServerImpl s:this.servs) {
						s.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, cbdv);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				
				//On s'endort et on sera reveillé par un wite d'un callback
				this.linda.read(template);
			}
		}
		return res;
	}

	@Override
	public Tuple tryTake(Tuple template)  throws RemoteException{
		Tuple res;
		LindaMultiServerImpl servCourant;
		
		//On regarde en local
		res = this.linda.tryTake(template);
		if(res == null) {
			//On regarde chez les autres serveurs
			Iterator<LindaMultiServerImpl> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((LindaMultiServerImpl) i).tryTakeLocal(template);
			}
		}
		return res;
	}

	@Override
	public Tuple tryRead(Tuple template)  throws RemoteException{
		Tuple res;
		LindaMultiServerImpl servCourant;
		
		res = this.linda.tryRead(template);
		if(res == null) {
			//On regarde chez les autres
			Iterator<LindaMultiServerImpl> i = this.servs.iterator();
			while(i.hasNext() && res == null) {
				servCourant = i.next();
				res = ((LindaMultiServerImpl) i).tryReadLocal(template);
			}
		}
		return res;
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template)  throws RemoteException{
		HashSet<Tuple> res = new HashSet<Tuple>();
		res.addAll(linda.takeAll(template));
		
		for(LindaMultiServerImpl serv:this.servs) {
			res.addAll(serv.takeAll(template));
		}
		
		return res;
	}

	@Override
	public Collection<Tuple> readAll(Tuple template)  throws RemoteException{
		return linda.readAll(template);
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, CbDist callback)  throws RemoteException{
		linda.eventRegister(mode,timing,template,new CallbackServ(callback));
	}

	@Override
	public void debug(String prefix)  throws RemoteException{
		linda.debug(prefix);
	}

	@Override
	public HashSet<LindaMultiServerImpl> newServer(LindaMultiServerImpl serv) throws RemoteException {
		HashSet<LindaMultiServerImpl> res = new HashSet<LindaMultiServerImpl>();
		
		if(ismaster){
			//Demande d'ajout pour chaque serveur esclave
			for(LindaMultiServerImpl actuel : servs){
				actuel.newServer(serv);
			}
			//Construction de la liste des serveurs actuels(priv� du nouveau serveur)
			res.addAll(servs);
		}
		//Ajout du nouveau serveur
		this.servs.add(serv);
		return res;
	}
	
	
	//Regarde uniquement en local
	@Override
    public Tuple tryTakeLocal(Tuple template)  throws RemoteException{
    	return this.linda.tryTake(template);
    }

    @Override
    public Tuple tryReadLocal(Tuple template) throws RemoteException{
    	return this.linda.tryRead(template);
    }

	@Override
	public Collection<Tuple> takeAllLocal(Tuple template)
			throws RemoteException {
		
		return this.linda.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAllLocal(Tuple template)
			throws RemoteException {
		
		return this.linda.readAll(template);
	}

}
