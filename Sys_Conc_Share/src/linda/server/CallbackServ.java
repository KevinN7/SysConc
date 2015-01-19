package linda.server;

import java.rmi.RemoteException;

import linda.Callback;
import linda.Tuple;

public class CallbackServ implements Callback {

	private CallbackDistant cbdist;
	
	public CallbackServ(CallbackDistant cb){
		cbdist = cb;
	}
	
	@Override
	public void call(Tuple t)  {
		
		try {
			cbdist.call(t);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}