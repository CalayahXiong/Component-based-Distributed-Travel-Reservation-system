package Server.RMI;

import Server.Common.CarResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMICarServer extends CarResourceManager {

    public RMICarServer(String p_name) {
        super(p_name);
    }

    private static String s_serverName = "Car_Server";
    private static String s_rmiPrefix = "group_38_";

    public static void main(String[] args) {

        if(args.length > 0){
            s_serverName = args[0];
        }

        try {
            RMICarServer carServer = new RMICarServer(s_serverName);
            IResourceManager carRM = (IResourceManager) UnicastRemoteObject.exportObject(carServer, 0);

            Registry l_regristry;
            try{
                l_regristry = LocateRegistry.createRegistry(3020); //register port
            } catch (RemoteException e) {
                l_regristry = LocateRegistry.getRegistry(3020);
            }

            final Registry registry = l_regristry;
            registry.rebind(s_rmiPrefix + s_serverName, carRM);

            Runtime.getRuntime().addShutdownHook(new Thread()  {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + s_serverName);
                        System.out.println("'" + s_serverName + "car resource manager unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
