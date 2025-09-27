package Server.RMI;

import Server.Common.FlightResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Concrete RMI server for Flights only
 */
public class RMIFlightServer extends FlightResourceManager {

    private static String s_serverName = "Flight_Server";
    private static String s_rmiPrefix = "group_35_";

    public RMIFlightServer(String name) {
        super(name);
    }


    // ------------------------ Stub methods for unsupported operations ------------------------
    // Cars
    @Override
    public boolean addCars(int tid, String location, int numCars, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

//    @Override
//    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
//        throw new UnsupportedOperationException("Flight RM does not handle cars");
//    }

    // Rooms
    @Override
    public boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }
  //  @Override
//    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
//        throw new UnsupportedOperationException("Flight RM does not handle rooms");
//    }
//

    // Customers
    @Override
    public int newCustomer(int tid) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean customerExists(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean customerReserve(int tid, int cid, String key, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }


    // ------------------------ Main method for RMI export ------------------------
    public static void main(String[] args) {

        if (args.length > 0) {
            s_serverName = args[0];
        }

        try {
            // Create the concrete flight RM
            RMIFlightServer flightServer = new RMIFlightServer(s_serverName);

            // Export as RMI object
            IResourceManager flightRM = (IResourceManager) UnicastRemoteObject.exportObject(flightServer, 0);

            // Setup registry
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(3035);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(3035);
            }

            registry.rebind(s_rmiPrefix + s_serverName, flightRM);

            // Shutdown hook to unbind
            Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println("'" + s_serverName + "' flight resource manager unbound");
                } catch (Exception e) {
                    System.err.println("Server exception during shutdown");
                    e.printStackTrace();
                }
            }));

            System.out.println("'" + s_serverName + "' flight resource manager ready and bound to '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
