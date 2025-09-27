package Server.Common;

import Server.Interface.IMiddleware;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Middleware implements IMiddleware {

    protected IResourceManager flightRM;
    protected IResourceManager carRM;
    protected IResourceManager roomRM;
    protected IResourceManager customerRM;
    protected TransactionalManager TM;

    public Middleware(IResourceManager flight, IResourceManager car,
                      IResourceManager room, IResourceManager customer) {
        this.flightRM = flight;
        this.carRM = car;
        this.roomRM = room;
        this.customerRM = customer;
        this.TM = new TransactionalManager();
    }

    @Override
    public String getName() throws RemoteException {
        return "Middleware";
    }

    // -------------------- Transaction Lifecycle --------------------
    @Override
    public int startTransaction() throws RemoteException {
        return TM.start();
    }

    @Override
    public boolean commitTransaction(int tid) throws RemoteException {
        List<IResourceManager> rms = Arrays.asList(flightRM, carRM, roomRM, customerRM);
        return TM.commit(tid, rms);
    }

    @Override
    public boolean abortTransaction(int tid) throws RemoteException {
        List<IResourceManager> rms = Arrays.asList(flightRM, carRM, roomRM, customerRM);
        return TM.abort(tid, rms);
    }

    // -------------------- FlightRM --------------------
    @Override
    public boolean addFlight(int tid, String flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightRM.addFlight(tid, flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int tid, String flightNum) throws RemoteException {
        try {
            if (!flightRM.flightExists(tid, flightNum)) {
                Trace.warn("MW::deleteFlight(" + tid + ", " + flightNum + ") failed -- flight not found");
                return false;
            }
            String fKey = Flight.getKey(flightNum);
            int reserved = flightRM.queryReserved(tid, fKey);
            if (reserved > 0) {
                Trace.warn("MW::deleteFlight(" + tid + ", " + flightNum + ") failed -- flight has reservations");
                return false;
            }

            boolean deleted = flightRM.deleteFlight(tid, flightNum);
            if (deleted) {
                Trace.info("MW::deleteFlight(" + tid + ", " + flightNum + ") succeeded");
            } else {
                Trace.warn("MW::deleteFlight(" + tid + ", " + flightNum + ") failed in FlightRM");
            }
            return deleted;

        } catch (Exception e) {
            throw new RemoteException("MW::deleteFlight(" + tid + ", " + flightNum + ") failed due to exception", e);
        }
    }


    @Override
    public int queryFlight(int tid, String flightNumber) throws RemoteException {
        return flightRM.queryFlight(tid, flightNumber);
    }

    @Override
    public int queryFlightPrice(int tid, String flightNumber) throws RemoteException {
        return flightRM.queryFlightPrice(tid, flightNumber);
    }

    // -------------------- CarRM --------------------
    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        return carRM.addCars(tid, location, count, price);
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        return carRM.deleteCars(tid, location);
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        return carRM.queryCars(tid, location);
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        return carRM.queryCarsPrice(tid, location);
    }

    // -------------------- RoomRM --------------------
    @Override
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        return roomRM.addRooms(tid, location, count, price);
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        return roomRM.deleteRooms(tid, location);
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        return roomRM.queryRooms(tid, location);
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        return roomRM.queryRoomsPrice(tid, location);
    }

    // -------------------- CustomerM --------------------
    @Override
    public int newCustomer(int tid) throws RemoteException {
        return customerRM.newCustomer(tid);
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        return customerRM.newCustomerID(tid, cid);
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        String cuKey = Customer.getKey(customerID);

        try {
            Customer cust = (Customer) customerRM.getItem(tid, cuKey);
            if (cust == null) {
                Trace.warn("MW::deleteCustomer(" + tid + ", " + customerID + ") failed -- no such customer");
                return false;
            }

            RMHashMap reservations = cust.getReservations();
            Vector<String> flights = new Vector<>();
            Vector<String> cars = new Vector<>();
            Vector<String> rooms = new Vector<>();

            for (String reservedKey : reservations.keySet()) {
                if (reservedKey.startsWith("flight-")) {
                    flights.add(reservedKey.split("-")[1]);   // flight number
                } else if (reservedKey.startsWith("car-")) {
                    cars.add(reservedKey.split("-")[1]);      // car location
                } else if (reservedKey.startsWith("room-")) {
                    rooms.add(reservedKey.split("-")[1]);     // room location
                }
            }

            if (!rollbackReservations(tid, customerID, flights, cars, rooms)) {
                Trace.warn("MW::deleteCustomer(" + tid + ", " + customerID + ") rollback failed");
                return false;
            }

            boolean deleted = customerRM.deleteCustomer(tid, customerID);
            if (deleted) {
                Trace.info("MW::deleteCustomer(" + tid + ", " + customerID + ") succeeded");
            } else {
                Trace.warn("MW::deleteCustomer(" + tid + ", " + customerID + ") failed in CustomerRM");
            }
            return deleted;

        } catch (RemoteException e) {
            Trace.error("MW::deleteCustomer(" + tid + ", " + customerID + ") RemoteException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Trace.error("MW::deleteCustomer(" + tid + ", " + customerID + ") Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        return customerRM.queryCustomerInfo(tid, customerID);
    }

    //--------------------------------Reservation----------------------
    @Override
    public boolean reserveFlight(int tid, int customerID, String flightNumber) throws RemoteException {
        String flightKey = Flight.getKey(flightNumber);

        try {
            // 1. Check customer exists in CustomerRM
            if (!customerRM.customerExists(tid, customerID)) {
                Trace.warn("MW::reserveFlight(" + tid + ", " + customerID + ", " + flightNumber + ") failed -- customer doesn't exist");
                return false;
            }

            // 2. Query price ( RemoteException)
            int price = flightRM.queryFlightPrice(tid, flightNumber);

            // 3. Reserve seat in FlightRM
            boolean reserved = flightRM.reserveFlight(tid, customerID, flightNumber);
            if (!reserved) {
                Trace.warn("MW::reserveFlight(" + tid + ", " + customerID + ", " + flightNumber + ") failed -- flight unavailable");
                return false;
            }

            // 4. Update CustomerRM
            boolean added = customerRM.customerReserve(tid, customerID, flightKey, 1, price);
            if (!added) {
                Trace.warn("MW::reserveFlight(" + tid + ", " + customerID + ", " + flightNumber + ") failed -- could not update customer");

                // rollback ONLY in FlightRM (not whole transaction)
                flightRM.rollbackReserve(tid, customerID, flightKey);
                return false;
            }

            Trace.info("MW::reserveFlight(" + tid + ", " + customerID + ", " + flightNumber + ") succeeded");
            return true;

        } catch (RemoteException e) {
            Trace.error("MW::reserveFlight(" + tid + ", " + customerID + ", " + flightNumber + ") failed due to " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        String carKey = Car.getKey(location);

        try {
            // 1. Check customer exists
            if (!customerRM.customerExists(tid, customerID)) {
                Trace.warn("MW::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed -- customer doesn't exist");
                return false;
            }

            // 2. Query price
            int price = carRM.queryCarsPrice(tid, location);

            // 3. Try reserve car in CarRM
            boolean reserved = carRM.reserveCar(tid, customerID, location);
            if (!reserved) {
                Trace.warn("MW::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed -- no cars available");
                return false;
            }

            // 4. Update customer reservations
            boolean added = customerRM.customerReserve(tid, customerID, carKey, 1, price);
            if (!added) {
                Trace.warn("MW::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed -- could not update customer");
                carRM.rollbackReserve(tid, customerID, carKey); // rollback staged car reservation
                return false;
            }

            Trace.info("MW::reserveCar(" + tid + ", " + customerID + ", " + location + ") succeeded");
            return true;

        } catch (RemoteException e) {
            Trace.error("MW::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed due to " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        String roomKey = Room.getKey(location);

        try {
            // 1. Check customer exists
            if (!customerRM.customerExists(tid, customerID)) {
                Trace.warn("MW::reserveRoom(" + tid + ", " + customerID + ", " + location + ") failed -- customer doesn't exist");
                return false;
            }

            // 2. Query price
            int price = roomRM.queryRoomsPrice(tid, location);

            // 3. Try reserve room in RoomRM
            boolean reserved = roomRM.reserveRoom(tid, customerID, location);
            if (!reserved) {
                Trace.warn("MW::reserveRoom(" + tid + ", " + customerID + ", " + location + ") failed -- no rooms available");
                return false;
            }

            // 4. Update customer reservations
            boolean added = customerRM.customerReserve(tid, customerID, roomKey, 1, price);
            if (!added) {
                Trace.warn("MW::reserveRoom(" + tid + ", " + customerID + ", " + location + ") failed -- could not update customer");
                roomRM.rollbackReserve(tid, customerID, roomKey); // rollback staged room reservation
                return false;
            }

            Trace.info("MW::reserveRoom(" + tid + ", " + customerID + ", " + location + ") succeeded");
            return true;

        } catch (RemoteException e) {
            Trace.error("MW::reserveRoom(" + tid + ", " + customerID + ", " + location + ") failed due to " + e.getMessage());
            return false;
        }
    }

    // -------------------- Bundle (multi-RM) --------------------
    @Override
    public boolean bundle(int tid, int customerID, Vector<String> flightNumbers,
                          String location, boolean car, boolean room) throws RemoteException {
        Vector<String> reservedFlights = new Vector<>();
        Vector<String> reservedCars = new Vector<>();
        Vector<String> reservedRooms = new Vector<>();

        try {
            // 1. Reserve flights first
            for (String f : flightNumbers) {
                if (reserveFlight(tid, customerID, f)) {
                    reservedFlights.add(f);
                } else {
                    rollbackReservations(tid, customerID, reservedFlights, reservedCars, reservedRooms);
                    return false;
                }
            }

            // 2. Reserve car
            if (car) {
                if (reserveCar(tid, customerID, location)) {
                    reservedCars.add(location);
                } else {
                    rollbackReservations(tid, customerID, reservedFlights, reservedCars, reservedRooms);
                    return false;
                }
            }

            // 3. Reserve room
            if (room) {
                if (reserveRoom(tid, customerID, location)) {
                    reservedRooms.add(location);
                } else {
                    rollbackReservations(tid, customerID, reservedFlights, reservedCars, reservedRooms);
                    return false;
                }
            }

            // 4. Success
            return true;

        } catch (RemoteException e) {
            rollbackReservations(tid, customerID, reservedFlights, reservedCars, reservedRooms);
            throw e; // rethrow to client
        }
    }


    /**
     * Rollback staged reservations if bundle fails.
     */
    private boolean rollbackReservations(int tid,
                                         int customerID,
                                         Vector<String> reservedFlights,
                                         Vector<String> reservedCars,
                                         Vector<String> reservedRooms) throws RemoteException {
        boolean success = true;

        // 1. rollback flights
        for (String f : reservedFlights) {
            String flightKey = Flight.getKey(f);
            if (!flightRM.rollbackReserve(tid, customerID, flightKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback for flight=" + f);
                success = false;
            }
            if (!customerRM.rollbackReserve(tid, customerID, flightKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback in CustomerRM for flight=" + f);
                success = false;
            }
        }

        // 2. rollback cars
        for (String loc : reservedCars) {
            String carKey = Car.getKey(loc);
            if (!carRM.rollbackReserve(tid, customerID, carKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback for car@" + loc);
                success = false;
            }
            if (!customerRM.rollbackReserve(tid, customerID, carKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback in CustomerRM for car@" + loc);
                success = false;
            }
        }

        // 3. rollback rooms
        for (String loc : reservedRooms) {
            String roomKey = Room.getKey(loc);
            if (!roomRM.rollbackReserve(tid, customerID, roomKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback for room@" + loc);
                success = false;
            }
            if (!customerRM.rollbackReserve(tid, customerID, roomKey)) {
                Trace.warn("MW::rollbackReservations(" + tid + ", cust=" + customerID + ") failed rollback in CustomerRM for room@" + loc);
                success = false;
            }
        }

        return success;
    }


}
