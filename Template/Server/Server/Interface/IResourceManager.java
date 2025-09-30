package Server.Interface;

import Server.Common.RMItem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
 * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface IResourceManager extends Remote
{

    /**
     * Convenience for probing the resource manager.
     *
     * @return Name
     */
    public String getName()
            throws RemoteException;

    /**
     * get an item
     * @return
     * @throws RemoteException
     */
    public RMItem getItem(int tid, String key)
        throws RemoteException;


//    /**
//     * Write request into m_data
//     * @param customerID
//     * @param flightKey
//     * @param valueOf
//     * @return
//     * @throws RemoteException
//     */
//    public boolean reserveItem(int customerID, String flightKey, String valueOf)
//        throws RemoteException;

    /**
     * Query reservation of this ReservableItem with this key(flightNumber/carlocation/roomLocation)
     * @param tid
     * @param key
     * @return
     * @throws RemoteException
     */
    public int queryReserved(int tid, String key)
        throws RemoteException;

    /**
     * read from transaction history
     * @param tid
     * @param key
     * @return
     */
    public RMItem readTransactionData(int tid, String key) throws RemoteException;

    /**
     * write request into transaction data
     * @param tid
     * @param key
     * @param item
     * @return
     * @throws RemoteException
     */
    public boolean writeTransactionData(int tid, String key, RMItem item) throws RemoteException;

    //-----------------------------------------Flight------------------------------------
    /**
     * Add seats to a flight.
     *
     * In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return Success
     */
    public boolean addFlight(int tid, String flightNum, int flightSeats, int flightPrice)
	throws RemoteException;

    /**
     * Delete the flight.
     *
     * deleteFlight implies whole deletion of the flight. If there is a
     * reservation on the flight, then the flight cannot be deleted
     *
     * @return Success
     */
    public boolean deleteFlight(int tid, String flightNum)
            throws RemoteException;

    /**
     * Query the status of a flight.
     *
     * @return Number of empty seats
     */
    public int queryFlight(int tid, String flightNumber)
            throws RemoteException;

    /**
     * Query the status of a flight.
     *
     * @return Price of a seat in this flight
     */
    public int queryFlightPrice(int tid, String flightNumber)
            throws RemoteException;

    /**
     * Reserve a seat on this flight.
     *
     * @return Success
     */
    public boolean reserveFlight(int tid, int customerID, String flightNumber)
            throws RemoteException;

    /**
     * Check flight existence
     * @param tid
     * @param flightNumber
     * @return
     * @throws RemoteException
     */
    public boolean flightExists(int tid, String flightNumber)
        throws RemoteException;

//    /**
//     * Cancel a flight reservation
//     * @param customerID
//     * @param flightNum
//     * @return
//     * @throws RemoteException
//     */
//    public boolean cancelFlightReservation(int tid, int customerID, Integer flightNum)
//            throws RemoteException;

    //----------------------------------Car-------------------------------------------

    //-----------------------------------------------Car-----------------------------------------
    /**
     * Add car at a location.
     *
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     *
     * @return Success
     */
    public boolean addCars(int tid, String location, int numCars, int price)
	throws RemoteException;

    /**
     * Delete all cars at a location.
     *
     * It may not succeed if there are reservations for this location
     *
     * @return Success
     */
    public boolean deleteCars(int tid, String location)
            throws RemoteException;

    /**
     * Query the status of a car location.
     *
     * @return Number of available cars at this location
     */
    public int queryCars(int tid, String location)
            throws RemoteException;

    /**
     * Query the status of a car location.
     *
     * @return Price of car
     */
    public int queryCarsPrice(int tid, String location)
            throws RemoteException;

    /**
     * Reserve a car at this location.
     *
     * @return Success
     */
    public boolean reserveCar(int tid, int customerID, String location)
            throws RemoteException;

    /**
     * Cancel car reservation
     * @param tid
     * @param customerID
     * @param location
     * @return
     * @throws RemoteException
     */
    //boolean cancelCarReservation(int tid, int customerID, String location)
        //throws RemoteException;

    //----------------------------------Room----------------------          ------
    /**
     * Add room at a location.
     *
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     *
     * @return Success
     */
    public boolean addRooms(int tid, String location, int numRooms, int price)
	throws RemoteException;

    /**
     * Delete all rooms at a location.
     *
     * It may not succeed if there are reservations for this location.
     *
     * @return Success
     */
    public boolean deleteRooms(int tid, String location)
            throws RemoteException;

    /**
     * Query the status of a room location.
     *
     * @return Number of available rooms at this location
     */
    public int queryRooms(int tid, String location)
            throws RemoteException;

    /**
     * Query the status of a room location.
     *
     * @return Price of a room
     */
    public int queryRoomsPrice(int tid, String location)
            throws RemoteException;

    /**
     * Reserve a room at this location.
     *
     * @return Success
     */
    public boolean reserveRoom(int tid, int customerID, String location)
            throws RemoteException;


    public boolean roomExists(int tid, String location)
        throws RemoteException;


    //----------------------------------------------------Customer-----------------------------
    /**
     * Sys will generate for customer
     * @param tid
     * @return
     * @throws RemoteException
     */
    public int newCustomer(int tid)
        throws RemoteException;
    /**
     * Add customer's id
     *
     * @return Unique customer identifier
     */
    public boolean newCustomerID(int tid, int cid)
	throws RemoteException; 

    /**
     * Delete a customer and associated reservations.
     *
     * @return Success
     */
    public boolean deleteCustomer(int tid, int customerID)
	throws RemoteException; 

    /**
     * Query the customer reservations.
     *
     * @return A formatted bill for the customer
     */
    public String queryCustomerInfo(int tid, int customerID)
	throws RemoteException;

    /**
     * Check a customer existence
     * @param tid
     * @param customerID
     * @return
     * @throws RemoteException
     */
    public boolean customerExists(int tid, int customerID)
            throws RemoteException;

    /**
     * CustomerRM reserve item into transaction data
     * @param tid
     * @param cid
     * @param key
     * @param count
     * @return
     * @throws RemoteException
     */
    public boolean customerReserve(int tid, int cid, String key, int count, int price)
        throws RemoteException;


    //---------------------------------------------------------Transaction-----------------------------------------------

    /**
     * Check the resource locked status & availability.
     * If these two are ok, then write the modification into log, return yes.
     * Else return false.
     * @param transactionalID
     * @return
     * @throws RemoteException
     */
    public boolean prepare(int transactionalID)
        throws RemoteException;

    /**
     * Update the modification to DB and release lock.
     * @param transactionalID
     * @return
     * @throws RemoteException
     */
    public boolean commit(int transactionalID)
        throws RemoteException;

    /**
     * Rollback the previous resources and release the lock.
     * @param transactionalID
     * @return
     * @throws RemoteException
     */
    public boolean abort(int transactionalID)
        throws RemoteException;

    /**
     * Roll back one reservation to RM. reserve(tid, cid, key, -1)
     * @param tid
     * @param cid
     * @param key
     * @return
     * @throws RemoteException
     */
    public boolean rollbackReserve(int tid, int cid, String key, int count)
        throws RemoteException;

    public boolean carExists(int tid, String location)
        throws RemoteException;
}
