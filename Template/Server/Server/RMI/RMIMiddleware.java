package Server.RMI;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class RMIMiddleware implements IResourceManager {
    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_20_";

    // References to the actual RMs
    private IResourceManager flightRM;
    private IResourceManager carRM;
    private IResourceManager roomRM;
    private IResourceManager customerRM;

    public RMIMiddleware(IResourceManager flight, IResourceManager car,
                         IResourceManager room, IResourceManager customer) {
        this.flightRM = flight;
        this.carRM = car;
        this.roomRM = room;
        this.customerRM = customer;
    }

    // -------------------- Flight --------------------
    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightRM.addFlight(flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        return flightRM.deleteFlight(flightNum);
    }

    @Override
    public int queryFlight(int flightNumber) throws RemoteException {
        return flightRM.queryFlight(flightNumber);
    }

    @Override
    public int queryFlightPrice(int flightNumber) throws RemoteException {
        return flightRM.queryFlightPrice(flightNumber);
    }

    // -------------------- Car --------------------
    @Override
    public boolean addCars(String location, int count, int price) throws RemoteException {
        return carRM.addCars(location, count, price);
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        return carRM.deleteCars(location);
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        return carRM.queryCars(location);
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        return carRM.queryCarsPrice(location);
    }

    // -------------------- Room --------------------
    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        return roomRM.addRooms(location, count, price);
    }

    @Override
    public int newCustomer() throws RemoteException {
        return customerRM.newCustomer();
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        return roomRM.deleteRooms(location);
    }

    @Override
    public int queryRooms( String location) throws RemoteException {
        return roomRM.queryRooms(location);
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return roomRM.queryRoomsPrice(location);
    }

    // -------------------- Customer --------------------
    @Override
    public boolean newCustomer(int cid) throws RemoteException {
        return customerRM.newCustomer(cid);
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        return customerRM.deleteCustomer(customerID);
    }

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        return customerRM.queryCustomerInfo(customerID);
    }

    // -------------------- Reservations --------------------
    @Override
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        return flightRM.reserveFlight(customerID, flightNumber);
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return carRM.reserveCar(customerID, location);
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return roomRM.reserveRoom(customerID, location);
    }

    @Override
    // haven't achieve roll back, and test for concurrency
    public boolean bundle(int customerID, Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        // Example: first reserve all flights, then optional car/room
        boolean success = true;
        for (String f : flightNumbers) {
            if (!flightRM.reserveFlight(customerID, Integer.parseInt(f))) {
                success = false;
            }
        }
        if (car) {
            success = success && carRM.reserveCar(customerID, location);
        }
        if (room) {
            success = success && roomRM.reserveRoom(customerID, location);
        }
        return success;
    }

    // -------------------- Misc --------------------
    @Override
    public String getName() throws RemoteException {
        return s_serverName;
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(3020);

            IResourceManager flightRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Flight_Server");
            IResourceManager carRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Car_Server");
            IResourceManager roomRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Room_Server");
            IResourceManager customerRM = (IResourceManager) registry.lookup(s_rmiPrefix + "Customer_Server");

            RMIMiddleware mw = new RMIMiddleware(flightRM, carRM, roomRM, customerRM);

            IResourceManager stub = (IResourceManager) UnicastRemoteObject.exportObject(mw, 0);
            registry.rebind(s_rmiPrefix + s_serverName, stub);

            System.out.println("Middleware bound as '" + s_rmiPrefix + s_serverName + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
