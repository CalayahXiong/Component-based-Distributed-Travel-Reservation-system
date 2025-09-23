// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;

import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public abstract class ResourceManager implements IResourceManager
{
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap(); //like a DB, when transaction commit, the changes will be wrote into m_data

	protected LockManager LM = new LockManager();
	protected Map<Integer, Map<String, RMItem>> transactionData = new HashMap<>(); //provisional area

	public ResourceManager(String p_name)
	{
		m_name = p_name;
	}

	// Reads a clone from global m_data(no locks)
	protected RMItem readData(String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	protected void writeData(String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the encar item
	protected boolean deleteItem(String key)
	{
		Trace.info("RM::deleteItem(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		// Check if there is such an item in the storage
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(curObj.getKey());
				Trace.info("RM::deleteItem(" + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(String key)
	{
		Trace.info("RM::queryNum(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		int value = 0;
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + key + ") returns count=" + value);
		return value;
	}

	// Query the price of an item
	protected int queryPrice(String key)
	{
		Trace.info("RM::queryPrice(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		int value = 0;
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + key + ") returns cost=$" + value);
		return value;
	}

	// Reserve an item
	protected boolean reserveItem(int customerID, String key, String location)
	{
		Trace.info("RM::reserveItem(customer=" + customerID + ", " + key + ", " + location + ") called" );
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		}

		// Check if the item is available
		ReservableItem item = (ReservableItem)readData(key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{
			customer.reserve(key, location, item.getPrice());
			writeData(customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(item.getKey(), item);

			Trace.info("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}
	}

	public String getName() throws RemoteException
	{
		return m_name;
	}


	//------------------------------------------------------Filght----------------------------------------
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


	//------------------------------------------------------Car---------------------------------------------
	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	@Override
	public boolean addCars(int tid, String location, int count, int price) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in addCars xid=" + tid, e);
		}
	}

	@Override
	public boolean deleteCars(int tid, String location) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in deleteCars xid=" + tid, e);
		}
	}

	@Override
	public int queryCars(int tid, String location) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.READ)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in queryCars xid=" + tid, e);
		}
	}

	@Override
	public int queryCarsPrice(int tid, String location) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.READ)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in queryCarsPrice xid=" + tid, e);
		}
	}

	@Override
	public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
				throw new RemoteException("Lock denied in reserveCar xid=" + tid);
			}
			return reserveItem(customerID, key, location);
		} catch (DeadlockException e) {
			abort(tid);
			throw new RemoteException("Deadlock in reserveCar xid=" + tid, e);
		}
	}

	@Override
	public boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException {
		String key = Car.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
				throw new RemoteException("Lock failed in cancelCarReservation tid=" + tid);
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

			Car car = (Car) readTransactionData(tid, key);
			if (car == null) {
				car = (Car) readData(key);
				if (car != null) {
					writeTransactionData(tid, car.getKey(), (RMItem) car.clone());
				}
			}
			if (car == null) {
				return false;
			}

			customer.cancelReservation(key, String.valueOf(location), car.getPrice());
			car.setCount(car.getCount() + 1);
			car.setReserved(car.getReserved() - 1);

			writeTransactionData(tid, customer.getKey(), customer);
			writeTransactionData(tid, car.getKey(), car);

			Trace.info("RM::cancelCarReservation(" + tid + ", cust=" + customerID +
					", car=" + location + ") succeeded");
			return true;
		} catch (DeadlockException e) {
			abort(tid);
			throw new RemoteException("Deadlock in cancelCarReservation xid=" + tid, e);
		}
	}

	//------------------------------------------------------Room--------------------------------------------
	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	@Override
	public boolean addRooms(int tid, String location, int count, int price) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in addRooms xid=" + tid, e);
		}
	}

	@Override
	public boolean deleteRooms(int tid, String location) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in deleteRooms xid=" + tid, e);
		}
	}

	@Override
	public int queryRooms(int tid, String location) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.READ)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in queryRooms xid=" + tid, e);
		}
	}

	@Override
	public int queryRoomsPrice(int tid, String location) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.READ)) {
				abort(tid);
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
			abort(tid);
			throw new RemoteException("Deadlock in queryRoomsPrice xid=" + tid, e);
		}
	}

	@Override
	public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
				throw new RemoteException("Lock denied in reserveRoom xid=" + tid);
			}
			return reserveItem(customerID, key, location);
		} catch (DeadlockException e) {
			abort(tid);
			throw new RemoteException("Deadlock in reserveRoom xid=" + tid, e);
		}
	}

	@Override
	public boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException {
		String key = Room.getKey(location);
		try {
			if (!LM.lock(tid, key, LockManager.LockType.WRITE)) {
				abort(tid);
				throw new RemoteException("Lock failed in cancelRoomReservation tid=" + tid);
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

			Room room = (Room) readTransactionData(tid, key);
			if (room == null) {
				room = (Room) readData(key);
				if (room != null) {
					writeTransactionData(tid, room.getKey(), (RMItem) room.clone());
				}
			}
			if (room == null) {
				return false;
			}

			customer.cancelReservation(key, String.valueOf(location), room.getPrice());
			room.setCount(room.getCount() + 1);
			room.setReserved(room.getReserved() - 1);

			writeTransactionData(tid, customer.getKey(), customer);
			writeTransactionData(tid, room.getKey(), room);

			Trace.info("RM::cancelFlightReservation(" + tid + ", cust=" + customerID +
					", room=" + location + ") succeeded");
			return true;
		} catch (DeadlockException e) {
			abort(tid);
			throw new RemoteException("Deadlock in cancelRoomReservation xid=" + tid, e);
		}
	}

	//-----------------------------------------------------------------Customer-------------------------------------
	@Override
	public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
		String key = Customer.getKey(customerID);
		try {
			// 先尝试加读锁
			if (!LM.lock(tid, key, LockManager.LockType.READ)) {
				abort(tid);
				throw new RemoteException("Lock denied in queryCustomerInfo xid=" + tid);
			}

			// 拿到锁后查事务数据 → 持久化数据
			Customer customer = (Customer) readTransactionData(tid, key);
			if (customer == null) {
				customer = (Customer) readData(key);
			}

			if (customer == null) {
				Trace.warn("RM::queryCustomerInfo(" + tid + ", " + customerID + ") failed -- customer doesn't exist");
				return ""; // NOTE: 不要改，WC依赖空串
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
	public boolean newCustomer(int tid, int customerID) throws RemoteException {
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

	// Reserve bundle
	public boolean bundle(int tid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		return false;
	}


	//---------------------------------------------------Transaction----------------------------------------

	protected RMItem readTransactionData(int tid, String key){
		Map<String, RMItem> workspace = transactionData.get(tid); //source
		if(workspace != null && workspace.containsKey(key)){
			return workspace.get(key);
		}
		return null;
	}

	protected void writeTransactionData(int tid, String key, RMItem item){
		transactionData.computeIfAbsent(tid, k -> new HashMap<>()).put(key, item);
	}

	@Override
	public boolean prepare(int transactionalID) throws RemoteException {
		return false;
	}

	@Override
	public boolean commit(int transactionalID) throws RemoteException {
		return false;
	}

	@Override
	public boolean abort(int transactionalID) throws RemoteException {
		return false;
	}
}
 
