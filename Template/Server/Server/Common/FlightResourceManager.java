package Server.Common;

import java.rmi.RemoteException;

public abstract class FlightResourceManager extends ResourceManager {

    public FlightResourceManager(String p_name) {
        super(p_name);
    }

    // -------------------------
    // Flight-related methods
    // -------------------------

    @Override
    public boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock failed in addFlight tid=" + tid);
            }

            Flight curObj = (Flight) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Flight) readData(key);
            }

            if (curObj == null) {
                Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
                writeTransactionData(tid, key, newObj);
                Trace.info("RM::addFlight(" + tid + ") created new flight " + flightNum);
            } else {
                curObj.setCount(curObj.getCount() + flightSeats);
                if (flightPrice > 0) curObj.setPrice(flightPrice);
                writeTransactionData(tid, key, curObj);
                Trace.info("RM::addFlight(" + tid + ") updated flight " + flightNum);
            }
            return true;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in addFlight tid=" + tid, e);
        }
    }
    @Override
    public boolean deleteFlight(int tid, int flightNum) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock failed in deleteFlight tid=" + tid);
            }

            Flight curObj = (Flight) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Flight) readData(key);
            }

            if (curObj == null) {
                Trace.warn("RM::deleteFlight(" + tid + ", " + flightNum + ") failed -- flight doesn't exist");
                return false;
            }

            if (curObj.getReserved() > 0) {
                Trace.warn("RM::deleteFlight(" + tid + ", " + flightNum + ") failed -- seats already reserved");
                return false;
            }

            writeTransactionData(tid, key, null); // stage delete
            Trace.info("RM::deleteFlight(" + tid + ", " + flightNum + ") succeeded (staged)");
            return true;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in deleteFlight xid=" + tid, e);
        }
    }
    @Override
    public int queryFlight(int tid, int flightNum) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                abort(tid);
                throw new RemoteException("Lock failed in queryFlight tid=" + tid);
            }

            Flight curObj = (Flight) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Flight) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getCount();
            Trace.info("RM::queryFlight(" + tid + ", " + flightNum + ") returns count=" + value);
            return value;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in queryFlight xid=" + tid, e);
        }
    }
    @Override
    public int queryFlightPrice(int tid, int flightNum) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                abort(tid);
                throw new RemoteException("Lock failed in queryFlightPrice tid=" + tid);
            }

            Flight curObj = (Flight) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Flight) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getPrice();
            Trace.info("RM::queryFlightPrice(" + tid + ", " + flightNum + ") returns cost=$" + value);
            return value;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in queryFlightPrice xid=" + tid, e);
        }
    }
    @Override
    public boolean reserveFlight(int tid, int customerID, int flightNum) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock failed in reserveFlight tid=" + tid);
            }
            return reserveItem(customerID, key, String.valueOf(flightNum));
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in reserveFlight xid=" + tid, e);
        }
    }
    @Override
    public boolean cancelFlightReservation(int tid, int customerID, Integer flightNum) throws RemoteException {
        String key = Flight.getKey(flightNum);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock failed in cancelFlightReservation tid=" + tid);
            }

            Customer customer = (Customer) readTransactionData(tid, Customer.getKey(customerID));
            if (customer == null) {
                customer = (Customer) readData(Customer.getKey(customerID));
                if (customer != null) {
                    writeTransactionData(tid, customer.getKey(), (RMItem) customer.clone());
                }
            }
            if (customer == null || !customer.hasReserved(key)) {
                return false;
            }

            Flight flight = (Flight) readTransactionData(tid, key);
            if (flight == null) {
                flight = (Flight) readData(key);
                if (flight != null) {
                    writeTransactionData(tid, flight.getKey(), (RMItem) flight.clone());
                }
            }
            if (flight == null) {
                return false;
            }

            customer.cancelReservation(key, String.valueOf(flightNum), flight.getPrice());
            flight.setCount(flight.getCount() + 1);
            flight.setReserved(flight.getReserved() - 1);

            writeTransactionData(tid, customer.getKey(), customer);
            writeTransactionData(tid, flight.getKey(), flight);

            Trace.info("RM::cancelFlightReservation(" + tid + ", cust=" + customerID +
                    ", flight=" + flightNum + ") succeeded");
            return true;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in cancelFlightReservation xid=" + tid, e);
        }
    }

}
