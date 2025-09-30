package Client;

import Server.Interface.IMiddleware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;

public class RMIClient extends Client
{
	private static String s_serverHost = "localhost"; //"tr-open-05.cs.mcgill.ca";
        // recommended to change port last digits to your group number
	private static int s_serverPort = 3035;
	private static String s_serverName = "Middleware";

	//TODO: ADD YOUR GROUP NUMBER TO COMPILE
	private static String s_rmiPrefix = "group_35_";

	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			s_serverName = args[1];
		}
		if (args.length > 2)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		// Get a reference to the RMIRegister
		try {
			RMIClient client = new RMIClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public RMIClient()
	{
		super();
	}

	public void connectServer()
	{
		connectServer(s_serverHost, s_serverPort, s_serverName);
	}

	public void connectServer(String server, int port, String name)
	{
		try {
			boolean first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(server, port);
					m_middleware = (IMiddleware) registry.lookup(s_rmiPrefix + name);
					System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					if (first) {
						System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	protected String invokeRemote(Command cmd, Vector<String> arguments) throws Exception {
		switch (cmd) {
			case Start:
				return String.valueOf(m_middleware.startTransaction());

			case Commit:
				return m_middleware.commitTransaction(currentTid) ? "committed" : "failed";

			case Abort:
				return m_middleware.abortTransaction(currentTid) ? "aborted" : "not aborted";

			// -------- Flights --------
			case AddFlight: {
				String flightNum = arguments.get(1);
				int seats = toInt(arguments.get(2));
				int price = toInt(arguments.get(3));
				return m_middleware.addFlight(currentTid, flightNum, seats, price) ?
						"Flight added" : "Flight not added";
			}
			case DeleteFlight:
				return m_middleware.deleteFlight(currentTid, arguments.get(1)) ?
						"Flight deleted" : "Flight not deleted";

			case QueryFlight:
				return "Seats available: " + m_middleware.queryFlight(currentTid, arguments.get(1));

			case QueryFlightPrice:
				return "Flight price: " + m_middleware.queryFlightPrice(currentTid, arguments.get(1));

			// -------- Cars --------
			case AddCars: {
				String loc = arguments.get(1);
				int num = toInt(arguments.get(2));
				int price = toInt(arguments.get(3));
				return m_middleware.addCars(currentTid, loc, num, price) ?
						"Cars added" : "Cars not added";
			}
			case DeleteCars:
				return m_middleware.deleteCars(currentTid, arguments.get(1)) ?
						"Cars deleted" : "Cars not deleted";

			case QueryCars:
				return "Cars available: " + m_middleware.queryCars(currentTid, arguments.get(1));

			case QueryCarsPrice:
				return "Car price: " + m_middleware.queryCarsPrice(currentTid, arguments.get(1));

			// -------- Rooms --------
			case AddRooms: {
				String loc = arguments.get(1);
				int num = toInt(arguments.get(2));
				int price = toInt(arguments.get(3));
				return m_middleware.addRooms(currentTid, loc, num, price) ?
						"Rooms added" : "Rooms not added";
			}
			case DeleteRooms:
				return m_middleware.deleteRooms(currentTid, arguments.get(1)) ?
						"Rooms deleted" : "Rooms not deleted";

			case QueryRooms:
				return "Rooms available: " + m_middleware.queryRooms(currentTid, arguments.get(1));

			case QueryRoomsPrice:
				return "Room price: " + m_middleware.queryRoomsPrice(currentTid, arguments.get(1));

			// -------- Customers --------
			case AddCustomer:
				return "Customer ID: " + m_middleware.newCustomer(currentTid);

			case AddCustomerID: {
				int cid = toInt(arguments.get(1));
				return m_middleware.newCustomerID(currentTid, cid) ?
						"Customer " + cid + " added" : "Customer exists";
			}
			case DeleteCustomer: {
				int cid = toInt(arguments.get(1));
				return m_middleware.deleteCustomer(currentTid, cid) ?
						"Customer deleted" : "Customer not deleted";
			}
			case QueryCustomer: {
				int cid = toInt(arguments.get(1));
				return m_middleware.queryCustomerInfo(currentTid, cid);
			}

			// -------- Reservations --------
			case ReserveFlight: {
				int cid = toInt(arguments.get(1));
				String flight = arguments.get(2);
				return m_middleware.reserveFlight(currentTid, cid, flight) ?
						"Flight reserved" : "Flight not reserved";
			}
			case ReserveCar: {
				int cid = toInt(arguments.get(1));
				String loc = arguments.get(2);
				return m_middleware.reserveCar(currentTid, cid, loc) ?
						"Car reserved" : "Car not reserved";
			}
			case ReserveRoom: {
				int cid = toInt(arguments.get(1));
				String loc = arguments.get(2);
				return m_middleware.reserveRoom(currentTid, cid, loc) ?
						"Room reserved" : "Room not reserved";
			}
			case Bundle: {
				int cid = toInt(arguments.get(1));
				Vector<String> flights = new Vector<>();
				for (int i = 2; i < arguments.size() - 3; i++) {
					flights.add(arguments.get(i));
				}
				String loc = arguments.get(arguments.size() - 3);
				boolean car = toBoolean(arguments.get(arguments.size() - 2));
				boolean room = toBoolean(arguments.get(arguments.size() - 1));
				return m_middleware.bundle(currentTid, cid, flights, loc, car, room) ?
						"Bundle reserved" : "Bundle not reserved";
			}
			case Quit: {
				return "Client quit";
			}

			default:
				return "Unsupported command: " + cmd;
		}
	}

}

