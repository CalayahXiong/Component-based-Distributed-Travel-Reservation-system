package Server.RMI;

import Server.Common.Middleware;
import Server.Interface.IMiddleware;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware extends Middleware {

    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_35_";

    public RMIMiddleware(IResourceManager flight, IResourceManager car,
                         IResourceManager room, IResourceManager customer) {
        super(flight, car, room, customer);
    }

    public static void main(String[] args) {
        try {
            // Setup registry
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(3035);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(3035);
            }

//            IResourceManager flightRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Flight_Server");
//            IResourceManager carRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Car_Server");
//            IResourceManager roomRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Room_Server");
//            IResourceManager customerRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Customer_Server");
            String flightHost = "tr-open-01.cs.mcgill.ca";
            String carHost    = "tr-open-02.cs.mcgill.ca";
            String roomHost   = "tr-open-03.cs.mcgill.ca";
            String custHost   = "tr-open-04.cs.mcgill.ca";
            String Host = "localhost";

            Registry flightRegistry   = LocateRegistry.getRegistry(Host, 3035);
            Registry carRegistry      = LocateRegistry.getRegistry(Host, 3035);
            Registry roomRegistry     = LocateRegistry.getRegistry(Host, 3035);
            Registry customerRegistry = LocateRegistry.getRegistry(Host, 3035);

            IResourceManager flightRM   = (IResourceManager) flightRegistry.lookup(s_rmiPrefix + "Flight_Server");
            IResourceManager carRM      = (IResourceManager) carRegistry.lookup(s_rmiPrefix + "Car_Server");
            IResourceManager roomRM     = (IResourceManager) roomRegistry.lookup(s_rmiPrefix + "Room_Server");
            IResourceManager customerRM = (IResourceManager) customerRegistry.lookup(s_rmiPrefix + "Customer_Server");


            RMIMiddleware mw = new RMIMiddleware(flightRM, carRM, roomRM, customerRM);

            IMiddleware stub = (IMiddleware) UnicastRemoteObject.exportObject(mw, 0);
            registry.rebind(s_rmiPrefix + s_serverName, stub);

            System.out.println("Middleware bound as '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
