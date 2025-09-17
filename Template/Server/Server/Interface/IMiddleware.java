package Server.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface IMiddleware extends Remote {
    // Flight
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException;
    public boolean deleteFlight(int flightNum) throws RemoteException;
    public int queryFlight(int flightNumber) throws RemoteException;
    public int queryFlightPrice(int flightNumber) throws RemoteException;
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException;

    // Car
    public boolean addCars(String location, int count, int price) throws RemoteException;
    public boolean deleteCars(String location) throws RemoteException;
    public int queryCars(String location) throws RemoteException;
    public int queryCarsPrice(String location) throws RemoteException;
    public boolean reserveCar(int customerID, String location) throws RemoteException;

    // Room
    public boolean addRooms(String location, int count, int price) throws RemoteException;
    public boolean deleteRooms(String location) throws RemoteException;
    public int queryRooms(String location) throws RemoteException;
    public int queryRoomsPrice(String location) throws RemoteException;
    public boolean reserveRoom(int customerID, String location) throws RemoteException;

    // Customer
    public int newCustomer() throws RemoteException;
    public boolean newCustomer(int cid) throws RemoteException;
    public boolean deleteCustomer(int customerID) throws RemoteException;
    public String queryCustomerInfo(int customerID) throws RemoteException;

    // Bundle
    public boolean bundle(int customerID, Vector<String> flightNumbers,
                   String location, boolean car, boolean room) throws RemoteException;

    public String getName() throws RemoteException;
}
