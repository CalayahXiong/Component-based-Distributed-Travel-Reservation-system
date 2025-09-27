package Server.Common;

import java.rmi.RemoteException;

public abstract class CarResourceManager extends ResourceManager {

    public CarResourceManager(String p_name) {
        super(p_name);
    }

    //------------------------------------------------------Car---------------------------------------------
    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    @Override
    public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
        String key = Car.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                //abort(tid);
                throw new RemoteException("Lock denied in addCars xid=" + tid);
            }

            Car curObj = (Car) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Car) readData(key);
            }

            if (curObj == null) {
                Car newObj = new Car(location, count, price);
                writeTransactionData(tid, key, newObj);
                Trace.info("RM::addCars(" + tid + ") created new location " + location);
            } else {
                curObj.setCount(curObj.getCount() + count);
                if (price > 0) curObj.setPrice(price);
                writeTransactionData(tid, key, curObj);
                Trace.info("RM::addCars(" + tid + ") updated " + location);
            }
            return true;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in addCars xid=" + tid, e);
        }
    }
    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        String key = Car.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                //abort(tid);
                throw new RemoteException("Lock denied in deleteCars xid=" + tid);
            }

            Car curObj = (Car) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Car) readData(key);
            }

            if (curObj == null) {
                Trace.warn("RM::deleteCars(" + tid + ") failed -- location doesn't exist");
                return false;
            }
            if (curObj.getReserved() > 0) {
                Trace.warn("RM::deleteCars(" + tid + ") failed -- cars already reserved");
                return false;
            }

            writeTransactionData(tid, key, null);
            Trace.info("RM::deleteCars(" + tid + ") staged delete for " + location);
            return true;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in deleteCars xid=" + tid, e);
        }
    }
    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        String key = Car.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                //abort(tid);
                throw new RemoteException("Lock denied in queryCars xid=" + tid);
            }

            Car curObj = (Car) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Car) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getCount();
            Trace.info("RM::queryCars(" + tid + ", " + location + ") returns " + value);
            return value;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in queryCars xid=" + tid, e);
        }
    }
    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        String key = Car.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                //abort(tid);
                throw new RemoteException("Lock denied in queryCarsPrice xid=" + tid);
            }

            Car curObj = (Car) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Car) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getPrice();
            Trace.info("RM::queryCarsPrice(" + tid + ", " + location + ") returns $" + value);
            return value;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in queryCarsPrice xid=" + tid, e);
        }
    }
    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        String key = Car.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                throw new RemoteException("Lock denied in reserveCar xid=" + tid);
            }

            Car car = (Car) readTransactionData(tid, key);
            if (car == null) {
                car = (Car) readData(key);
                if (car != null) {
                    writeTransactionData(tid, key, (RMItem) car.clone());
                }
            }

            if (car == null) {
                Trace.warn("CarRM::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed -- no such location");
                return false;
            }

            if (car.getCount() == 0) {
                Trace.warn("CarRM::reserveCar(" + tid + ", " + customerID + ", " + location + ") failed -- no cars available");
                return false;
            }

            car.setCount(car.getCount() - 1);
            car.setReserved(car.getReserved() + 1);

            writeTransactionData(tid, key, car);

            Trace.info("CarRM::reserveCar(" + tid + ", " + customerID + ", " + location + ") succeeded");
            return true;

        } catch (DeadlockException e) {
            throw new RemoteException("Deadlock in reserveCar xid=" + tid, e);
        }
    }

//    @Override
//    public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
//        String key = Car.getKey(location);
//        try {
//            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
//                abort(tid);
//                throw new RemoteException("Lock failed in cancelCarReservation tid=" + tid);
//            }
//
//            Customer customer = (Customer) readTransactionData(tid, Customer.getKey(customerID));
//            if (customer == null) {
//                customer = (Customer) readData(Customer.getKey(customerID));
//                if (customer != null) {
//                    writeTransactionData(tid, customer.getKey(), (RMItem) customer.clone());
//                }
//            }
//            if (customer == null || !customer.hasReserved(key)) {
//                return false;
//            }
//
//            Car car = (Car) readTransactionData(tid, key);
//            if (car == null) {
//                car = (Car) readData(key);
//                if (car != null) {
//                    writeTransactionData(tid, car.getKey(), (RMItem) car.clone());
//                }
//            }
//            if (car == null) {
//                return false;
//            }
//
//            customer.cancelReservation(key, String.valueOf(location), car.getPrice());
//            car.setCount(car.getCount() + 1);
//            car.setReserved(car.getReserved() - 1);
//
//            writeTransactionData(tid, customer.getKey(), customer);
//            writeTransactionData(tid, car.getKey(), car);
//
//            Trace.info("RM::cancelCarReservation(" + tid + ", cust=" + customerID +
//                    ", car=" + location + ") succeeded");
//            return true;
//        } catch (DeadlockException e) {
//            abort(tid);
//            throw new RemoteException("Deadlock in cancelCarReservation xid=" + tid, e);
//        }
//    }
}
