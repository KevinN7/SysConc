package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import linda.Tuple;

public interface CallbackDistant extends Remote {
	
	void call(Tuple t) throws RemoteException;
	
}
