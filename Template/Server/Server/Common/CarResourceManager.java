package Server.Common;

import java.rmi.RemoteException;

public class CarResourceManager extends ResourceManager {

    public CarResourceManager(String p_name) {
        super(p_name);
    }

    // -------------------------
    // Car-related methods
    // -------------------------

    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        return super.addCars(tid, location, count, price);
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        return super.deleteCars(tid, location);
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        return super.queryCars(tid, location);
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        return super.queryCarsPrice(tid, location);
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        return super.reserveCar(tid, customerID, location);
    }

    // -------------------------
    // Unsupported flight methods
    // -------------------------

    @Override
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot add flights.");
    }

    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot delete flights.");
    }

    @Override
    public int queryFlight(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query flights.");
    }

    @Override
    public int queryFlightPrice(int tid, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query flight prices.");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot reserve flights.");
    }

    @Override
    public boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot cancel flight reservation.");
    }

    // -------------------------
    // Unsupported room methods
    // -------------------------

    @Override
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot add rooms.");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot delete rooms.");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query rooms.");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query room prices.");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot reserve rooms.");
    }

    @Override
    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot cancel room reservation.");
    }

    // -------------------------
    // Customer methods (optional: keep or throw)
    // -------------------------

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }


    // -------------------------
    // Bundle method (MW handles this)
    // -------------------------

    @Override
    public boolean bundle(int tid, int customerId, java.util.Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not handle bundles. Middleware should coordinate bundles.");
    }
}
