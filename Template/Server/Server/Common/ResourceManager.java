// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;
import Server.Interface.IResourceManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public abstract class ResourceManager implements IResourceManager
{
    protected String m_name = "";
    protected RMHashMap m_data = new RMHashMap(); // Global storage
    protected LockManager LM = new LockManager();  // Lock manager
    protected Map<Integer, Map<String, RMItem>> transactionData = new HashMap<>(); // Transaction workspace

    public ResourceManager(String name) {
        m_name = name;
    }
    public String getName() throws RemoteException {
        return m_name;
    }

	//--------------------------------------------------------------Common Usages--------------------------------
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

	//--------------------------------------------------Flight----------------------------------------------
    public abstract boolean addFlight(int tid, int flightNum, int flightSeats, int flightPrice) throws RemoteException;
    public abstract boolean deleteFlight(int tid, int flightNum) throws RemoteException;
    public abstract int queryFlight(int tid, int flightNumber) throws RemoteException;
    public abstract int queryFlightPrice(int tid, int flightNumber) throws RemoteException;
    public abstract boolean reserveFlight(int tid, int customerID, int flightNumber) throws RemoteException;
    public abstract boolean cancelFlightReservation(int tid, int customerID, Integer f) throws RemoteException;


    //--------------------------------------------------Car---------------------------------
    public abstract boolean addCars(int tid, String location, int numCars, int price) throws RemoteException;
    public abstract boolean deleteCars(int tid, String location) throws RemoteException;
    public abstract int queryCars(int tid, String location) throws RemoteException;
    public abstract int queryCarsPrice(int tid, String location) throws RemoteException;
    public abstract boolean reserveCar(int tid, int customerID, String location) throws RemoteException;
    public abstract boolean cancelCarReservation(int tid, int customerID, String location) throws RemoteException;


	//------------------------------------------------------Room-------------------------------
    public abstract boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException;
    public abstract boolean deleteRooms(int tid, String location) throws RemoteException;
    public abstract int queryRooms(int tid, String location) throws RemoteException;
    public abstract int queryRoomsPrice(int tid, String location) throws RemoteException;
    public abstract boolean reserveRoom(int tid, int customerID, String location) throws RemoteException;
    public abstract boolean cancelRoomReservation(int tid, int customerID, String location) throws RemoteException;


	//---------------------------------------------------Customer---------------------------------------
    public abstract int newCustomer(int tid) throws RemoteException;
    public abstract boolean newCustomerID(int tid, int cid) throws RemoteException;
    public abstract boolean deleteCustomer(int tid, int customerID) throws RemoteException;
    public abstract String queryCustomerInfo(int tid, int customerID) throws RemoteException;

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
	public boolean prepare(int tid) throws RemoteException {
		Trace.info("RM::prepare(" + tid + ") called");
		Map<String, RMItem> workspace = transactionData.get(tid);
		if (workspace == null) {
			Trace.info("RM::prepare(" + tid + ") no changes, auto-commit");
			return true;
		}

		for (Map.Entry<String, RMItem> entry : workspace.entrySet()) {
			RMItem item = entry.getValue();
			if (item instanceof ReservableItem) {
				ReservableItem reservable = (ReservableItem) item;
				if (reservable.getCount() < 0) {
					Trace.warn("RM::prepare(" + tid + ") failed, negative count for " + entry.getKey());
					abort(tid);
					return false;
				}
			}
		}

		Trace.info("RM::prepare(" + tid + ") OK");
		return true;
	}
	@Override
	public boolean commit(int tid) throws RemoteException {
		Trace.info("RM::commit(" + tid + ") called");
		Map<String, RMItem> workspace = transactionData.remove(tid);
		if (workspace == null) {
			Trace.info("RM::commit(" + tid + ") nothing to commit");
			return true;
		}

		synchronized (m_data) {
			for (Map.Entry<String, RMItem> entry : workspace.entrySet()) {
				if (entry.getValue() == null) {
					// staged delete
					m_data.remove(entry.getKey());
					Trace.info("RM::commit(" + tid + ") removed key " + entry.getKey());
				} else {
					m_data.put(entry.getKey(), (RMItem) entry.getValue().clone());
					Trace.info("RM::commit(" + tid + ") updated key " + entry.getKey());
				}
			}
		}

		LM.releaseLocks(tid);
		Trace.info("RM::commit(" + tid + ") done");
		return true;
	}
	@Override
	public boolean abort(int tid) throws RemoteException {
		Trace.info("RM::abort(" + tid + ") called");
		transactionData.remove(tid);
		LM.releaseLocks(tid); //
		Trace.info("RM::abort(" + tid + ") rollback done");
		return true;
	}

}

