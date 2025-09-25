package Server.RMI;

import Server.Common.CustomerManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMICustomerServer extends CustomerManager  {

    private static String s_serverName = "Customer_Server";
    private static String s_rmiPrefix = "group_35_";

    public RMICustomerServer(String p_name) {
        super(p_name);
    }

    // ------------------------ Stub methods for unsupported operations ------------------------
    // Flights
    @Override
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    @Override
    public int queryFlight(int tid, int flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    @Override
    public int queryFlightPrice(int tid, int flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    @Override
    public boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle flights");
    }

    // Cars
    @Override
    public boolean addCars(int tid, String location, int numCars, int price) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    @Override
    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle cars");
    }

    // Rooms
    @Override
    public boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    @Override
    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Customer RM does not handle rooms");
    }

    // ------------------------ Main method for RMI export ------------------------
    public static void main(String[] args) {

        if(args.length > 0){
            s_serverName = args[0];
        }

        try {
            RMICustomerServer customerServer = new RMICustomerServer(s_serverName);
            IResourceManager customerRM = (IResourceManager) UnicastRemoteObject.exportObject(customerServer, 0);

            Registry registry;
            try{
                registry = LocateRegistry.createRegistry(3020); //register port
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(3020);
            }

            registry.rebind(s_rmiPrefix + s_serverName, customerRM);

            Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println("'" + s_serverName + "' customer manager unbound");
                } catch (Exception e) {
                    System.err.println("Server exception during shutdown");
                    e.printStackTrace();
                }
            }));

            System.out.println("'" + s_serverName + "' customer resource manager ready and bound to '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
