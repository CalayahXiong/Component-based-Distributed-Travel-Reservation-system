package Client;

import Server.Interface.*;

import java.util.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;

public abstract class Client {
	IMiddleware m_middleware = null;
	int currentTid = -1;   // 当前事务 ID

	public Client() {
		super();
	}

	public abstract void connectServer();

	public void start() {
		System.out.println();
		System.out.println("Type \"help\" for list of supported commands");

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			String command = "";
			Vector<String> arguments = new Vector<>();

			try {
				System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
				command = stdin.readLine().trim();
			} catch (IOException io) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
				io.printStackTrace();
				System.exit(1);
			}

			try {
				arguments = parse(command);
				Command cmd = Command.fromString((String)arguments.elementAt(0));
				try {
					execute(cmd, arguments);
				} catch (ConnectException e) {
					connectServer();
					execute(cmd, arguments);
				}
			} catch (IllegalArgumentException|ServerException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			} catch (ConnectException|UnmarshalException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mConnection to server lost");
			} catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}
	}

	public void execute(Command cmd, Vector<String> arguments) throws RemoteException, NumberFormatException {
		switch (cmd) {
			case Help: {
				if (arguments.size() == 1) {
					System.out.println(Command.description());
				} else if (arguments.size() == 2) {
					Command l_cmd = Command.fromString((String)arguments.elementAt(1));
					System.out.println(l_cmd.toString());
				} else {
					System.err.println("Usage: help or help,<CommandName>");
				}
				break;
			}

			// ---------------- Transaction commands ----------------
			case Start: {
				checkArgumentsCount(1, arguments.size());
				currentTid = m_middleware.startTransaction();
				System.out.println("New transaction started, xid=" + currentTid);
				break;
			}
			case Commit: {
				checkArgumentsCount(1, arguments.size());
				if (currentTid == -1) {
					System.out.println("No active transaction");
				} else {
					if (m_middleware.commitTransaction(currentTid)) {
						System.out.println("Transaction " + currentTid + " committed");
					} else {
						System.out.println("Transaction " + currentTid + " failed to commit");
					}
					currentTid = -1;
				}
				break;
			}
			case Abort: {
				checkArgumentsCount(1, arguments.size());
				if (currentTid == -1) {
					System.out.println("No active transaction");
				} else {
					if(m_middleware.abortTransaction(currentTid))
						System.out.println("Transaction " + currentTid + " aborted");
					currentTid = -1;
				}
				break;
			}

			// ---------------- Flight ----------------
			case AddFlight: {
				checkArgumentsCount(4, arguments.size());
				int flightNum = toInt(arguments.elementAt(1));
				int flightSeats = toInt(arguments.elementAt(2));
				int flightPrice = toInt(arguments.elementAt(3));

				if (m_middleware.addFlight(currentTid, flightNum, flightSeats, flightPrice)) {
					System.out.println("Flight added");
				} else {
					System.out.println("Flight could not be added");
				}
				break;
			}
			case DeleteFlight: {
				checkArgumentsCount(2, arguments.size());
				int flightNum = toInt(arguments.elementAt(1));
				if (m_middleware.deleteFlight(currentTid, flightNum)) {
					System.out.println("Flight Deleted");
				} else {
					System.out.println("Flight could not be deleted");
				}
				break;
			}
			case QueryFlight: {
				checkArgumentsCount(2, arguments.size());
				int flightNum = toInt(arguments.elementAt(1));
				int seats = m_middleware.queryFlight(currentTid, flightNum);
				System.out.println("Seats available: " + seats);
				break;
			}
			case QueryFlightPrice: {
				checkArgumentsCount(2, arguments.size());
				int flightNum = toInt(arguments.elementAt(1));
				int price = m_middleware.queryFlightPrice(currentTid, flightNum);
				System.out.println("Flight price: " + price);
				break;
			}

			// ---------------- Car ----------------
			case AddCars: {
				checkArgumentsCount(4, arguments.size());
				String location = arguments.elementAt(1);
				int numCars = toInt(arguments.elementAt(2));
				int price = toInt(arguments.elementAt(3));
				if (m_middleware.addCars(currentTid, location, numCars, price)) {
					System.out.println("Cars added");
				} else {
					System.out.println("Cars could not be added");
				}
				break;
			}
			case DeleteCars: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				if (m_middleware.deleteCars(currentTid, location)) {
					System.out.println("Cars Deleted");
				} else {
					System.out.println("Cars could not be deleted");
				}
				break;
			}
			case QueryCars: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				int numCars = m_middleware.queryCars(currentTid, location);
				System.out.println("Cars available: " + numCars);
				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				int price = m_middleware.queryCarsPrice(currentTid, location);
				System.out.println("Car price: " + price);
				break;
			}

			// ---------------- Room ----------------
			case AddRooms: {
				checkArgumentsCount(4, arguments.size());
				String location = arguments.elementAt(1);
				int numRooms = toInt(arguments.elementAt(2));
				int price = toInt(arguments.elementAt(3));
				if (m_middleware.addRooms(currentTid, location, numRooms, price)) {
					System.out.println("Rooms added");
				} else {
					System.out.println("Rooms could not be added");
				}
				break;
			}
			case DeleteRooms: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				if (m_middleware.deleteRooms(currentTid, location)) {
					System.out.println("Rooms Deleted");
				} else {
					System.out.println("Rooms could not be deleted");
				}
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				int numRooms = m_middleware.queryRooms(currentTid, location);
				System.out.println("Rooms available: " + numRooms);
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(2, arguments.size());
				String location = arguments.elementAt(1);
				int price = m_middleware.queryRoomsPrice(currentTid, location);
				System.out.println("Room price: " + price);
				break;
			}

			// ---------------- Customer ----------------
			case AddCustomer: {
				checkArgumentsCount(2, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				if (m_middleware.newCustomer(currentTid, customerID)) {
					System.out.println("Customer " + customerID + " added");
				} else {
					System.out.println("Customer already exists");
				}
				break;
			}
			case DeleteCustomer: {
				checkArgumentsCount(2, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				if (m_middleware.deleteCustomer(currentTid, customerID)) {
					System.out.println("Customer deleted");
				} else {
					System.out.println("Customer could not be deleted");
				}
				break;
			}
			case QueryCustomer: {
				checkArgumentsCount(2, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				String bill = m_middleware.queryCustomerInfo(currentTid, customerID);
				System.out.println(bill);
				break;
			}

			// ---------------- Reservations ----------------
			case ReserveFlight: {
				checkArgumentsCount(3, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));
				if (m_middleware.reserveFlight(currentTid, customerID, flightNum)) {
					System.out.println("Flight Reserved");
				} else {
					System.out.println("Flight could not be reserved");
				}
				break;
			}
			case ReserveCar: {
				checkArgumentsCount(3, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				if (m_middleware.reserveCar(currentTid, customerID, location)) {
					System.out.println("Car Reserved");
				} else {
					System.out.println("Car could not be reserved");
				}
				break;
			}
			case ReserveRoom: {
				checkArgumentsCount(3, arguments.size());
				int customerID = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				if (m_middleware.reserveRoom(currentTid, customerID, location)) {
					System.out.println("Room Reserved");
				} else {
					System.out.println("Room could not be reserved");
				}
				break;
			}
			case Bundle: {
				if (arguments.size() < 6) {
					System.err.println("Usage: Bundle,customerID,flightNum...,location,car?,room?");
					break;
				}
				int customerID = toInt(arguments.elementAt(1));
				Vector<String> flightNumbers = new Vector<>();
				for (int i = 0; i < arguments.size() - 5; ++i) {
					flightNumbers.addElement(arguments.elementAt(2+i));
				}
				String location = arguments.elementAt(arguments.size()-3);
				boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
				boolean room = toBoolean(arguments.elementAt(arguments.size()-1));
				if (m_middleware.bundle(currentTid, customerID, flightNumbers, location, car, room)) {
					System.out.println("Bundle Reserved");
				} else {
					System.out.println("Bundle could not be reserved");
				}
				break;
			}

			case Quit:
				checkArgumentsCount(1, arguments.size());
				System.out.println("Quitting client");
				System.exit(0);
		}
	}

	public static Vector<String> parse(String command) {
		Vector<String> arguments = new Vector<>();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		while (tokenizer.hasMoreTokens()) {
			arguments.add(tokenizer.nextToken().trim());
		}
		return arguments;
	}

	public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException {
		if (!expected.equals(actual)) {
			throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1));
		}
	}

	public static int toInt(String string) throws NumberFormatException {
		return Integer.parseInt(string);
	}

	public static boolean toBoolean(String string) {
		return Boolean.parseBoolean(string);
	}
}
