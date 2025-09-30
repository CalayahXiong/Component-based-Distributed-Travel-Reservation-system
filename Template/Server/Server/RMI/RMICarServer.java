package Server.RMI;

import Server.Common.CarResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMICarServer extends CarResourceManager {

    private static String s_serverName = "Car_Server";
    private static String s_rmiPrefix = "group_35_";

    public RMICarServer(String p_name) {
        super(p_name);
    }

    // ------------------------ Stub methods for unsupported operations ------------------------
    // Flights
    @Override
    public boolean addFlight(int tid, String flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean deleteFlight(int tid, String flightNum) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public int queryFlight(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public int queryFlightPrice(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean flightExists(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }


    // Rooms
    @Override
    public boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean roomExists(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }


    // Customers
    @Override
    public int newCustomer(int tid) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean customerExists(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean customerReserve(int tid, int cid, String key, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    // ------------------------ Main method for RMI export ------------------------
    public static void main(String[] args) {

        if(args.length > 0){
            s_serverName = args[0];
        }

        try {
            RMICarServer carServer = new RMICarServer(s_serverName);
            IResourceManager carRM = (IResourceManager) UnicastRemoteObject.exportObject(carServer, 0);

            Registry registry;
            try{
                registry = LocateRegistry.createRegistry(3035); //register port
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(3035);
            }

            registry.rebind(s_rmiPrefix + s_serverName, carRM);

            Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println("'" + s_serverName + "' car resource manager unbound");
                } catch (Exception e) {
                    System.err.println("Server exception during shutdown");
                    e.printStackTrace();
                }
            }));

            System.out.println("'" + s_serverName + "' car resource manager ready and bound to '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
