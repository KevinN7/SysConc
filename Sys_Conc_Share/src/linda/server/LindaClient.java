package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.TupleFormatException;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	
	LindaServer serv;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "//localhost:4000/LindaServer".
     * @throws NotBoundException 
     * @throws RemoteException 
     * @throws MalformedURLException 
     */
    public LindaClient(String serverURI) throws MalformedURLException, RemoteException, NotBoundException {
        serv = (LindaServer)Naming.lookup(serverURI );
    }

	@Override
	public void write(Tuple t) {
		serv.write(t);		
	}

	@Override
	public Tuple take(Tuple template) {
		return serv.take(template);
	}

	@Override
	public Tuple read(Tuple template) {
		return serv.read(template);
	}

	@Override
	public Tuple tryTake(Tuple template) {
		return serv.tryTake(template);
	}

	@Override
	public Tuple tryRead(Tuple template) {
		return serv.tryRead(template);
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		return serv.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		return serv.readAll(template);
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing,
			Tuple template, Callback callback) {
		serv.eventRegister(mode,timing,template,callback);
	}

	@Override
	public void debug(String prefix) {
		serv.debug(prefix);		
	}
    
    // TO BE COMPLETED

}
