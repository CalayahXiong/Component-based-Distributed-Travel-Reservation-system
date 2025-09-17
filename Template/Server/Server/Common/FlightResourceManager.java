package Server.Common;

import java.rmi.RemoteException;

public class FlightResourceManager extends ResourceManager {

    public FlightResourceManager(String p_name) {
        super(p_name);
    }

    // -------------------------
    // Flight-related methods
    // -------------------------

    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return super.addFlight(flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        return super.deleteFlight(flightNum);
    }

    @Override
    public int queryFlight(int flightNum) throws RemoteException {
        return super.queryFlight(flightNum);
    }

    @Override
    public int queryFlightPrice(int flightNum) throws RemoteException {
        return super.queryFlightPrice(flightNum);
    }

    @Override
    public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
        return super.reserveFlight(customerID, flightNum);
    }

    @Override
    public boolean cancelFlightReservation(int customerID, Integer f) throws RemoteException {
        return super.cancelFlightReservation(customerID, f);
    }

    // -------------------------
    // Unsupported car methods
    // -------------------------

    @Override
    public boolean addCars(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot add cars.");
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot delete cars.");
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query cars.");
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query car prices.");
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot reserve cars.");
    }

    @Override
    public boolean cancelCarReservation(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot cancel car reservation.");
    }
    // -------------------------
    // Unsupported room methods
    // -------------------------

    @Override
    public boolean addRooms(String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot add rooms.");
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot delete rooms.");
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query rooms.");
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query room prices.");
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot reserve rooms.");
    }

    @Override
    public boolean cancelRoomReservation(int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot cancel room reservation.");
    }

    // -------------------------
    // Customer methods (optional: keep or delegate to MW)
    // -------------------------

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    @Override
    public int newCustomer() throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    // -------------------------
    // Bundle method (should be handled by MW)
    // -------------------------

    @Override
    public boolean bundle(int customerId, java.util.Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not handle bundles. Middleware should coordinate bundles.");
    }
}
