package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.shm.CentralizedLinda;

public class LindaServer extends UnicastRemoteObject implements LindaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	CentralizedLinda linda;
	Registry registry;

	public LindaServer(String uri, int port) throws RemoteException {
		registry = LocateRegistry.createRegistry(port);
		linda = new CentralizedLinda();
		try {
			Naming.rebind(uri, this);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void write(Tuple t) {
		linda.write(t);
	}

	@Override
	public Tuple take(Tuple template) {
		return linda.take(template);
	}

	@Override
	public Tuple read(Tuple template) {
		return linda.read(template);
	}

	@Override
	public Tuple tryTake(Tuple template) {
		return tryTake(template);
	}

	@Override
	public Tuple tryRead(Tuple template) {
		return linda.tryRead(template);
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

}
