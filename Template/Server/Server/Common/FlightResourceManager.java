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
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return super.addFlight(tid, flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        return super.deleteFlight(tid, flightNum);
    }

    @Override
    public int queryFlight(int tid, int flightNum) throws RemoteException {
        return super.queryFlight(tid, flightNum);
    }

    @Override
    public int queryFlightPrice(int tid, int flightNum) throws RemoteException {
        return super.queryFlightPrice(tid, flightNum);
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNum) throws RemoteException {
        return super.reserveFlight(tid, customerID, flightNum);
    }

    @Override
    public boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException {
        return super.cancelFlightReservation(tid, customerID, f);
    }

    // -------------------------
    // Unsupported car methods
    // -------------------------

    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot add cars.");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot delete cars.");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query cars.");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query car prices.");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot reserve cars.");
    }

    @Override
    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot cancel car reservation.");
    }
    // -------------------------
    // Unsupported room methods
    // -------------------------

    @Override
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot add rooms.");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot delete rooms.");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query rooms.");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot query room prices.");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot reserve rooms.");
    }

    @Override
    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager cannot cancel room reservation.");
    }

    // -------------------------
    // Customer methods (optional: keep or delegate to MW)
    // -------------------------

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    @Override
    public boolean newCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("FlightResourceManager does not manage customers.");
    }

    // -------------------------
    // Bundle method (should be handled by MW)
    // -------------------------

//    @Override
//    public boolean bundle(int tid, int customerId, java.util.Vector<String> flightNumbers,
//                          String location, boolean car, boolean room) throws RemoteException {
//        throw new UnsupportedOperationException("FlightResourceManager does not handle bundles. Middleware should coordinate bundles.");
//    }
}
