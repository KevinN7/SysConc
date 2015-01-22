package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	
	LindaServer serv;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
        	serv = (LindaServer)Naming.lookup(serverURI );
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }

	@Override
	public void write(Tuple t) {
		try {
			serv.write(t);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public Tuple take(Tuple template) {
		try {
			return serv.take(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public Tuple read(Tuple template) {
		try {
			return serv.read(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public Tuple tryTake(Tuple template) {
		try {
			return serv.tryTake(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public Tuple tryRead(Tuple template) {
		try {
			return serv.tryRead(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		try {
			return serv.takeAll(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		try {
			return serv.readAll(template);
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, Callback callback) {
		try {
			serv.eventRegister(mode,timing,template,new CbDist(callback));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void debug(String prefix) {
		try {
			serv.debug(prefix);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
    
    // TO BE COMPLETED

}
