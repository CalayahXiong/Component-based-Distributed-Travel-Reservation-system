package Server.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface IMiddleware extends Remote {
    // Flight
    boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException;
    boolean deleteFlight(int flightNum) throws RemoteException;
    int queryFlight(int flightNumber) throws RemoteException;
    int queryFlightPrice(int flightNumber) throws RemoteException;
    boolean reserveFlight(int customerID, int flightNumber) throws RemoteException;

    // Car
    boolean addCars(String location, int count, int price) throws RemoteException;
    boolean deleteCars(String location) throws RemoteException;
    int queryCars(String location) throws RemoteException;
    int queryCarsPrice(String location) throws RemoteException;
    boolean reserveCar(int customerID, String location) throws RemoteException;

    // Room
    boolean addRooms(String location, int count, int price) throws RemoteException;
    boolean deleteRooms(String location) throws RemoteException;
    int queryRooms(String location) throws RemoteException;
    int queryRoomsPrice(String location) throws RemoteException;
    boolean reserveRoom(int customerID, String location) throws RemoteException;

    // Customer
    int newCustomer() throws RemoteException;
    boolean newCustomer(int cid) throws RemoteException;
    boolean deleteCustomer(int customerID) throws RemoteException;
    String queryCustomerInfo(int customerID) throws RemoteException;

    // Bundle
    boolean bundle(int customerID, Vector<String> flightNumbers,
                   String location, boolean car, boolean room) throws RemoteException;

    String getName() throws RemoteException;
}
