package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CustomerManager extends ResourceManager {

    private final AtomicInteger localCounter = new AtomicInteger(0);

    public CustomerManager(String p_name) {
        super(p_name);
    }

    //-----------------------------------------------------Customer-------------------------------------
    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        String key = Customer.getKey(customerID);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                abort(tid);
                throw new RemoteException("Lock denied in queryCustomerInfo xid=" + tid);
            }

            Customer customer = (Customer) readTransactionData(tid, key);
            if (customer == null) {
                customer = (Customer) readData(key);
                System.out.println("Customer" + customerID + " from m_data");
            }

            if (customer == null) {
                Trace.warn("RM::queryCustomerInfo(" + tid + ", " + customerID + ") failed -- customer doesn't exist");
                return "";
            } else {
                Trace.info("RM::queryCustomerInfo(" + tid + ", " + customerID + ") returns bill");
                return customer.getBill();
            }
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in queryCustomerInfo xid=" + tid, e);
        }
    }
    @Override
    public int newCustomer(int tid) throws RemoteException
    {
        Trace.info("RM::newCustomer() called");
        // Generate a globally unique ID for the new customer; if it generates duplicates for you, then adjust
        int cid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf(Math.round(Math.random() * 100 + 1)));
        Customer customer = new Customer(cid);
        writeData(customer.getKey(), customer);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
        return cid;
    }
    @Override
    public boolean newCustomerID(int tid, int customerID) throws RemoteException {
        String key = Customer.getKey(customerID);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock denied in newCustomer xid=" + tid);
            }

            Customer customer = (Customer) readTransactionData(tid, key);
            if (customer == null) {
                customer = (Customer) readData(key);
            }

            if (customer == null) {
                Customer newCust = new Customer(customerID);
                writeTransactionData(tid, key, newCust);
                Trace.info("RM::newCustomer(" + tid + ", " + customerID + ") created");
                return true;
            } else {
                Trace.warn("RM::newCustomer(" + tid + ", " + customerID + ") failed -- already exists");
                return false;
            }
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in newCustomer xid=" + tid, e);
        }
    }
    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        String key = Customer.getKey(customerID);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                abort(tid);
                throw new RemoteException("Lock denied in deleteCustomer xid=" + tid);
            }

            Customer customer = (Customer) readTransactionData(tid, key);
            if (customer == null) {
                customer = (Customer) readData(key);
            }

            if (customer == null) {
                Trace.warn("RM::deleteCustomer(" + tid + ", " + customerID + ") failed -- doesn't exist");
                return false;
            }

            //  rollback reservations
            RMHashMap reservations = customer.getReservations();
            for (String reservedKey : reservations.keySet()) {
                ReservedItem reservedItem = customer.getReservedItem(reservedKey);

                if (!LM.lock(tid, reservedKey, LockManager.LockType.WRITE)) {
                    abort(tid);
                    throw new RemoteException("Lock denied in deleteCustomer xid=" + tid + " for resource=" + reservedKey);
                }

                ReservableItem item = (ReservableItem) readTransactionData(tid, reservedKey);
                if (item == null) {
                    item = (ReservableItem) readData(reservedKey);
                }

                if (item != null) {
                    item.setReserved(item.getReserved() - reservedItem.getCount());
                    item.setCount(item.getCount() + reservedItem.getCount());
                    writeTransactionData(tid, reservedKey, item);
                }
            }

            writeTransactionData(tid, key, null);

            Trace.info("RM::deleteCustomer(" + tid + ", " + customerID + ") staged delete");
            return true;
        } catch (DeadlockException e) {
            abort(tid);
            throw new RemoteException("Deadlock in deleteCustomer xid=" + tid, e);
        }
    }
}
