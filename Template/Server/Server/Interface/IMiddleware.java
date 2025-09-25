package Server.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface IMiddleware extends Remote {

    // Transaction Lifecycle
    public int startTransaction() throws RemoteException;
    public boolean commitTransaction(int tid) throws RemoteException;
    public boolean abortTransaction(int tid) throws RemoteException;

    // Flight
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException;
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException;
    public int queryFlight(int tid, int flightNumber) throws RemoteException;
    public int queryFlightPrice(int tid, int flightNumber) throws RemoteException;
    public boolean reserveFlight(int tid, int customerID, int flightNumber) throws RemoteException;
    public boolean cancelFlightReservation(int tid, int customerID, int flightNumber) throws RemoteException;

    // Car
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException;
    public boolean deleteCars(int tid, String location) throws RemoteException;
    public int queryCars(int tid, String location) throws RemoteException;
    public int queryCarsPrice(int tid, String location) throws RemoteException;
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException;
    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException;

    // Room
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException;
    public boolean deleteRooms(int tid, String location) throws RemoteException;
    public int queryRooms(int tid, String location) throws RemoteException;
    public int queryRoomsPrice(int tid, String location) throws RemoteException;
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException;
    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException;

    // Customer
    public int newCustomer(int tid) throws RemoteException;
    public boolean newCustomerID(int tid, int cid) throws RemoteException;
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException;
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException;

    // Bundle
    public boolean bundle(int tid, int customerID, Vector<String> flightNumbers,
                   String location, boolean car, boolean room) throws RemoteException;

    public String getName() throws RemoteException;

}
