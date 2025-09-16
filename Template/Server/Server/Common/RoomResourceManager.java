package Server.Common;

import java.rmi.RemoteException;

public class RoomResourceManager extends ResourceManager {

    public RoomResourceManager(String p_name) {
        super(p_name);
    }

    // -------------------------
    // Room-related methods
    // -------------------------

    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        return super.addRooms(location, count, price);
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        return super.deleteRooms(location);
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        return super.queryRooms(location);
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return super.queryRoomsPrice(location);
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return super.reserveRoom(customerID, location);
    }

    // -------------------------
    // Unsupported flight methods
    // -------------------------

    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot add flights.");
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot delete flights.");
    }

    @Override
    public int queryFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query flights.");
    }

    @Override
    public int queryFlightPrice(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query flight prices.");
    }

    @Override
    public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot reserve flights.");
    }

    // -------------------------
    // Unsupported car methods
    // -------------------------

    @Override
    public boolean addCars(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot add cars.");
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot delete cars.");
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query cars.");
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query car prices.");
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot reserve cars.");
    }

    // -------------------------
    // Customer methods (optional: keep or throw)
    // -------------------------

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    @Override
    public int newCustomer() throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    // -------------------------
    // Bundle method (MW handles this)
    // -------------------------

    @Override
    public boolean bundle(int customerId, java.util.Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not handle bundles. Middleware should coordinate bundles.");
    }
}
