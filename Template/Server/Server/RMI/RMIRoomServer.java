package Server.RMI;

import Server.Common.RoomResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIRoomServer extends RoomResourceManager {

    private static String s_serverName = "Room_Server";
    private static String s_rmiPrefix = "group_35_";

    public RMIRoomServer(String p_name) {
        super(p_name);
    }

    // ------------------------ Stub methods for unsupported operations ------------------------
    // Flights
    @Override
    public boolean addFlight(int tid, String flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

    @Override
    public boolean deleteFlight(int tid, String flightNum) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

    @Override
    public int queryFlight(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

    @Override
    public int queryFlightPrice(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

    @Override
    public boolean flightExists(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle flights");
    }

//    @Override
//    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle flights");
//    }

//    @Override
//    public int queryFlight(int tid, int flightNumber) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle flights");
//    }
//
//    @Override
//    public int queryFlightPrice(int tid, int flightNumber) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle flights");
//    }
//
//    @Override
//    public boolean reserveFlight(int tid, int customerID, int flightNumber) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle flights");
//    }
//
//    @Override
//    public boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle flights");
//    }

    // Cars
    @Override
    public boolean addCars(int tid, String location, int numCars, int price) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle cars");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle cars");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle cars");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle cars");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle cars");
    }

//    @Override
//    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
//        throw new UnsupportedOperationException("Room RM does not handle cars");
//    }

    // Customer operations (except reservation)
    @Override
    public int newCustomer(int tid) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle newCustomer creation");
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle newCustomerID creation");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle customer deletion");
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle customer info");
    }

    @Override
    public boolean customerExists(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle customer info");
    }

    @Override
    public boolean customerReserve(int tid, int cid, String key, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("Room RM does not handle customer info");
    }

    // ------------------------ Main method for RMI export ------------------------
    public static void main(String[] args) {

        if(args.length > 0){
            s_serverName = args[0];
        }

        try {
            RMIRoomServer roomServer = new RMIRoomServer(s_serverName);
            IResourceManager roomRM = (IResourceManager) UnicastRemoteObject.exportObject(roomServer, 0);

            Registry registry;
            try{
                registry = LocateRegistry.createRegistry(3035); //register port
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(3035);
            }

            registry.rebind(s_rmiPrefix + s_serverName, roomRM);

            Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println("'" + s_serverName + "' room resource manager unbound");
                } catch (Exception e) {
                    System.err.println("Server exception during shutdown");
                    e.printStackTrace();
                }
            }));

            System.out.println("'" + s_serverName + "' room resource manager ready and bound to '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
