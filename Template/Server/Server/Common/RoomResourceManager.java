package Server.Common;

import java.rmi.RemoteException;

public abstract class RoomResourceManager extends ResourceManager {

    public RoomResourceManager(String p_name) {
        super(p_name);
    }

    //------------------------------------------------------Room--------------------------------------------
    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    @Override
    public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
        String key = Room.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                //abort(tid);
                throw new RemoteException("Lock denied in addRooms xid=" + tid);
            }

            Room curObj = (Room) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Room) readData(key);
            }

            if (curObj == null) {
                Room newObj = new Room(location, count, price);
                writeTransactionData(tid, key, newObj);
                Trace.info("RM::addRooms(" + tid + ") created " + location);
            } else {
                curObj.setCount(curObj.getCount() + count);
                if (price > 0) curObj.setPrice(price);
                writeTransactionData(tid, key, curObj);
                Trace.info("RM::addRooms(" + tid + ") updated " + location);
            }
            return true;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in addRooms xid=" + tid, e);
        }
    }
    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        String key = Room.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                //abort(tid);
                throw new RemoteException("Lock denied in deleteRooms xid=" + tid);
            }

            Room curObj = (Room) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Room) readData(key);
            }

            if (curObj == null) {
                Trace.warn("RM::deleteRooms(" + tid + ", " + location + ") failed -- location doesn't exist");
                return false;
            }
            if (curObj.getReserved() > 0) {
                Trace.warn("RM::deleteRooms(" + tid + ", " + location + ") failed -- rooms reserved");
                return false;
            }

            writeTransactionData(tid, key, null);
            Trace.info("RM::deleteRooms(" + tid + ", " + location + ") staged delete");
            return true;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in deleteRooms xid=" + tid, e);
        }
    }
    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        String key = Room.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                //abort(tid);
                throw new RemoteException("Lock denied in queryRooms xid=" + tid);
            }

            Room curObj = (Room) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Room) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getCount();
            Trace.info("RM::queryRooms(" + tid + ", " + location + ") returns " + value);
            return value;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in queryRooms xid=" + tid, e);
        }
    }
    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        String key = Room.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.READ)) {
                //abort(tid);
                throw new RemoteException("Lock denied in queryRoomsPrice xid=" + tid);
            }

            Room curObj = (Room) readTransactionData(tid, key);
            if (curObj == null) {
                curObj = (Room) readData(key);
            }

            int value = (curObj == null) ? 0 : curObj.getPrice();
            Trace.info("RM::queryRoomsPrice(" + tid + ", " + location + ") returns $" + value);
            return value;
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in queryRoomsPrice xid=" + tid, e);
        }
    }
    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        String key = Room.getKey(location);
        try {
            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
                //abort(tid);
                throw new RemoteException("Lock denied in reserveRoom xid=" + tid);
            }
            return reserveItem(customerID, key, location);
        } catch (DeadlockException e) {
            //abort(tid);
            throw new RemoteException("Deadlock in reserveRoom xid=" + tid, e);
        }
    }
//    @Override
//    public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
//        String key = Room.getKey(location);
//        try {
//            if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
//                abort(tid);
//                throw new RemoteException("Lock failed in cancelRoomReservation tid=" + tid);
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
//            Room room = (Room) readTransactionData(tid, key);
//            if (room == null) {
//                room = (Room) readData(key);
//                if (room != null) {
//                    writeTransactionData(tid, room.getKey(), (RMItem) room.clone());
//                }
//            }
//            if (room == null) {
//                return false;
//            }
//
//            customer.cancelReservation(key, String.valueOf(location), room.getPrice());
//            room.setCount(room.getCount() + 1);
//            room.setReserved(room.getReserved() - 1);
//
//            writeTransactionData(tid, customer.getKey(), customer);
//            writeTransactionData(tid, room.getKey(), room);
//
//            Trace.info("RM::cancelFlightReservation(" + tid + ", cust=" + customerID +
//                    ", room=" + location + ") succeeded");
//            return true;
//        } catch (DeadlockException e) {
//            abort(tid);
//            throw new RemoteException("Deadlock in cancelRoomReservation xid=" + tid, e);
//        }
//    }
}
