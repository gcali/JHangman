package jhangmanserver.remote.rmi;

import java.rmi.RemoteException;

import rmi_interface.ClientCallbackRMI;

public interface CallbackProcedure {
    
    public void execute(ClientCallbackRMI callback) throws RemoteException;

}
