package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import linda.Callback;
import linda.Tuple;

public class CbDist extends UnicastRemoteObject implements CallbackDistant {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Callback cb;

	protected CbDist(Callback c) throws RemoteException {
		super();
		this.cb = c;
	}

	@Override
	public void call(Tuple t) throws RemoteException {
		cb.call(t);
	}

}
