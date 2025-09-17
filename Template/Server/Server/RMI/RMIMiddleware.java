package Server.RMI;

import Server.Interface.IMiddleware;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class RMIMiddleware implements IMiddleware {
    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_35_";

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

    @Override
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        return flightRM.reserveFlight(customerID, flightNumber);
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

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return carRM.reserveCar(customerID, location);
    }

    // -------------------- Room --------------------
    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        return roomRM.addRooms(location, count, price);
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        return roomRM.deleteRooms(location);
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        return roomRM.queryRooms(location);
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return roomRM.queryRoomsPrice(location);
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return roomRM.reserveRoom(customerID, location);
    }

    // -------------------- Customer --------------------
    @Override
    public int newCustomer() throws RemoteException {
        return customerRM.newCustomer();
    }

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

    // -------------------- Bundle (跨 RM 的逻辑) --------------------
    @Override
    public boolean bundle(int customerID, Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {

        Vector<Integer> reservedFlights = new Vector<>();
        boolean carReserved = false;
        boolean roomReserved = false;

        try {
            // Reserve flights first
            for (String f : flightNumbers) {
                int flightNum = Integer.parseInt(f);
                if (flightRM.reserveFlight(customerID, flightNum)) {
                    reservedFlights.add(flightNum);
                } else {
                    rollbackReservations(customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            // Reserve car
            if (car) {
                carReserved = carRM.reserveCar(customerID, location);
                if (!carReserved) {
                    rollbackReservations(customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            // Reserve room
            if (room) {
                roomReserved = roomRM.reserveRoom(customerID, location);
                if (!roomReserved) {
                    rollbackReservations(customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            return true; // success
        } catch (RemoteException e) {
            rollbackReservations(customerID, reservedFlights, carReserved, roomReserved, location);
            throw e; // propagate exception
        }
    }

    private boolean rollbackReservations(int customerID,
                                         Vector<Integer> reservedFlights,
                                         boolean carReserved,
                                         boolean roomReserved,
                                         String location) {
        boolean rollbackSuccess = true;

        try {
            // Undo flights
            for (Integer f : reservedFlights) {
                if (!flightRM.cancelFlightReservation(customerID, f)) {
                    rollbackSuccess = false;
                }
            }

            // Undo car
            if (carReserved) {
                if (!carRM.cancelCarReservation(customerID, location)) {
                    rollbackSuccess = false;
                }
            }

            // Undo room
            if (roomReserved) {
                if (!roomRM.cancelRoomReservation(customerID, location)) {
                    rollbackSuccess = false;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            rollbackSuccess = false;
        }

        return rollbackSuccess;
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

            IMiddleware stub = (IMiddleware) UnicastRemoteObject.exportObject(mw, 0);
            registry.rebind(s_rmiPrefix + s_serverName, stub);

            System.out.println("Middleware bound as '" + s_rmiPrefix + s_serverName + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
