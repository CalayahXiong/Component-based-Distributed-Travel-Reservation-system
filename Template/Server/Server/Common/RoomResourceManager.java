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
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        return super.addRooms(tid, location, count, price);
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        return super.deleteRooms(tid, location);
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        return super.queryRooms(tid, location);
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        return super.queryRoomsPrice(tid, location);
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        return super.reserveRoom(tid, customerID, location);
    }

    // -------------------------
    // Unsupported flight methods
    // -------------------------

    @Override
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot add flights.");
    }

    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot delete flights.");
    }

    @Override
    public int queryFlight(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query flights.");
    }

    @Override
    public int queryFlightPrice(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query flight prices.");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot reserve flights.");
    }

    @Override
    public boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot cancel flight reservation.");
    }

    // -------------------------
    // Unsupported car methods
    // -------------------------

    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot add cars.");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot delete cars.");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query cars.");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot query car prices.");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot reserve cars.");
    }

    @Override
    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager cannot cancel car reservation.");
    }

    // -------------------------
    // Customer methods (optional: keep or throw)
    // -------------------------

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int tid,  int customerID) throws RemoteException {
        throw new UnsupportedOperationException("RoomResourceManager does not manage customers.");
    }

    // -------------------------
    // Bundle method (MW handles this)
    // -------------------------
//
//    @Override
//    public boolean bundle(int tid, int customerId, java.util.Vector<String> flightNumbers,
//                          String location, boolean car, boolean room) throws RemoteException {
//        throw new UnsupportedOperationException("RoomResourceManager does not handle bundles. Middleware should coordinate bundles.");
//    }
}
