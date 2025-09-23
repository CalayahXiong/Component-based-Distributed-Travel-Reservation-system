package Server.RMI;

import Server.Common.TransactionalManager;
import Server.Interface.IMiddleware;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class RMIMiddleware implements IMiddleware {
    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_35_";

    // References to the actual RMs
    private IResourceManager flightRM;
    private IResourceManager carRM;
    private IResourceManager roomRM;
    private IResourceManager customerRM;

    private TransactionalManager TM;

    public RMIMiddleware(IResourceManager flight, IResourceManager car,
                         IResourceManager room, IResourceManager customer) {
        this.flightRM = flight;
        this.carRM = car;
        this.roomRM = room;
        this.customerRM = customer;
        TM = new TransactionalManager();
    }

    // -------------------- Transaction Lifecycle --------------------
    @Override
    public int startTransaction() throws RemoteException {
        return TM.start();
    }

    @Override
    public boolean commitTransaction(int tid) throws RemoteException {
        List<IResourceManager> rms = Arrays.asList(flightRM, carRM, roomRM, customerRM);
        return TM.commit(tid, rms);
    }

    @Override
    public boolean abortTransaction(int tid) throws RemoteException {
        List<IResourceManager> rms = Arrays.asList(flightRM, carRM, roomRM, customerRM);
        return TM.abort(tid, rms);
    }


    // -------------------- Flight --------------------
    @Override
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightRM.addFlight(tid, flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        return flightRM.deleteFlight(tid, flightNum);
    }

    @Override
    public int queryFlight(int tid, int flightNumber) throws RemoteException {
        return flightRM.queryFlight(tid, flightNumber);
    }

    @Override
    public int queryFlightPrice(int tid, int flightNumber) throws RemoteException {
        return flightRM.queryFlightPrice(tid, flightNumber);
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNumber) throws RemoteException {
        return flightRM.reserveFlight(tid, customerID, flightNumber);
    }

    @Override
    public boolean cancelFlightReservation(int tid, int customerID, int flightNumber) throws RemoteException {
        return flightRM.cancelFlightReservation(tid, customerID, flightNumber);
    }

    // -------------------- Car --------------------
    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        return carRM.addCars(tid, location, count, price);
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        return carRM.deleteCars(tid, location);
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        return carRM.queryCars(tid, location);
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        return carRM.queryCarsPrice(tid, location);
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        return carRM.reserveCar(tid, customerID, location);
    }

    @Override
    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
        return carRM.cancelCarReservation(tid, customerID, location);
    }

    // -------------------- Room --------------------
    @Override
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        return roomRM.addRooms(tid, location, count, price);
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        return roomRM.deleteRooms(tid, location);
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        return roomRM.queryRooms(tid, location);
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        return roomRM.queryRoomsPrice(tid, location);
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        return roomRM.reserveRoom(tid, customerID, location);
    }

    @Override
    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
        return roomRM.cancelRoomReservation(tid, customerID, location);
    }

    // -------------------- Customer --------------------
    @Override
    public boolean newCustomer(int tid, int cid) throws RemoteException {
        return customerRM.newCustomer(tid, cid);
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        return customerRM.deleteCustomer(tid, customerID);
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        return customerRM.queryCustomerInfo(tid, customerID);
    }

    // -------------------- Bundle (multi-RM) --------------------
    @Override
    public boolean bundle(int tid, int customerID, Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        Vector<Integer> reservedFlights = new Vector<>();
        boolean carReserved = false;
        boolean roomReserved = false;

        try {
            // Reserve flights first
            for (String f : flightNumbers) {
                int flightNum = Integer.parseInt(f);
                if (flightRM.reserveFlight(tid, customerID, flightNum)) {
                    reservedFlights.add(flightNum);
                } else {
                    rollbackReservations(tid, customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            // Reserve car
            if (car) {
                carReserved = carRM.reserveCar(tid, customerID, location);
                if (!carReserved) {
                    rollbackReservations(tid, customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            // Reserve room
            if (room) {
                roomReserved = roomRM.reserveRoom(tid, customerID, location);
                if (!roomReserved) {
                    rollbackReservations(tid, customerID, reservedFlights, carReserved, roomReserved, location);
                    return false;
                }
            }

            return true; // success
        } catch (RemoteException e) {
            rollbackReservations(tid, customerID, reservedFlights, carReserved, roomReserved, location);
            throw e; // propagate exception
        }
    }

    private boolean rollbackReservations(int tid,
                                         int customerID,
                                         Vector<Integer> reservedFlights,
                                         boolean carReserved,
                                         boolean roomReserved,
                                         String location) {
        boolean rollbackSuccess = true;

        try {
            // Undo flights
            for (Integer f : reservedFlights) {
                if (!flightRM.cancelFlightReservation(tid, customerID, f)) {
                    rollbackSuccess = false;
                }
            }

            // Undo car
            if (carReserved) {
                if (!carRM.cancelCarReservation(tid, customerID, location)) {
                    rollbackSuccess = false;
                }
            }

            // Undo room
            if (roomReserved) {
                if (!roomRM.cancelRoomReservation(tid, customerID, location)) {
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
