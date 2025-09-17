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
    public boolean addCars(String location, int count, int price) throws RemoteException {
        return super.addCars(location, count, price);
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        return super.deleteCars(location);
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        return super.queryCars(location);
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        return super.queryCarsPrice(location);
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return super.reserveCar(customerID, location);
    }

    // -------------------------
    // Unsupported flight methods
    // -------------------------

    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot add flights.");
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot delete flights.");
    }

    @Override
    public int queryFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query flights.");
    }

    @Override
    public int queryFlightPrice(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query flight prices.");
    }

    @Override
    public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot reserve flights.");
    }

    @Override
    public boolean cancelFlightReservation(int customerID, Integer f) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot cancel flight reservation.");
    }

    // -------------------------
    // Unsupported room methods
    // -------------------------

    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot add rooms.");
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot delete rooms.");
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query rooms.");
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot query room prices.");
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot reserve rooms.");
    }

    @Override
    public boolean cancelRoomReservation(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager cannot cancel room reservation.");
    }

    // -------------------------
    // Customer methods (optional: keep or throw)
    // -------------------------

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }

    @Override
    public int newCustomer() throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not manage customers.");
    }



    // -------------------------
    // Bundle method (MW handles this)
    // -------------------------

    @Override
    public boolean bundle(int customerId, java.util.Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        throw new UnsupportedOperationException("CarResourceManager does not handle bundles. Middleware should coordinate bundles.");
    }
}
