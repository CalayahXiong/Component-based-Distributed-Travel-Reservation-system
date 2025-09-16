package Server.Common;

import java.rmi.RemoteException;
import java.util.Vector;

public class CustomerManager extends ResourceManager {

    public CustomerManager(String p_name) {
        super(p_name);
    }

    // -------------------------
    // Customer-related methods
    // -------------------------

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        return super.queryCustomerInfo(customerID);
    }

    @Override
    public int newCustomer() throws RemoteException {
        return super.newCustomer();
    }

    @Override
    public boolean newCustomer(int customerID) throws RemoteException {
        return super.newCustomer(customerID);
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        return super.deleteCustomer(customerID);
    }

    // -------------------------
    // Unsupported flight methods
    // -------------------------

    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot add flights.");
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot delete flights.");
    }

    @Override
    public int queryFlight(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query flights.");
    }

    @Override
    public int queryFlightPrice(int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query flight prices.");
    }

    @Override
    public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot reserve flights.");
    }

    // -------------------------
    // Unsupported car methods
    // -------------------------

    @Override
    public boolean addCars(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot add cars.");
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot delete cars.");
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query cars.");
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query car prices.");
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot reserve cars.");
    }

    // -------------------------
    // Unsupported room methods
    // -------------------------

    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot add rooms.");
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot delete rooms.");
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query rooms.");
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot query room prices.");
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager cannot reserve rooms.");
    }

    // -------------------------
    // Bundle method (MW handles this)
    // -------------------------

    @Override
    public boolean bundle(int customerId, Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        throw new UnsupportedOperationException("CustomerManager does not handle bundles. Middleware should coordinate bundles.");
    }
}
