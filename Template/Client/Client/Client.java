package Client;

import Server.Interface.*;

import java.util.*;
import java.io.*;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;

public abstract class Client {
	IMiddleware m_middleware = null;
	int currentTid = -1;

	public Client() {
		super();
	}

	public abstract void connectServer();

	//protected abstract String invokeRemote(String cmd, Vector<String> arguments) throws Exception;

	public void start() {
		System.out.println();
		System.out.println("Type \"help\" for list of supported commands");

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			String command = "";
			Vector<String> arguments;

			try {
				System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
				command = stdin.readLine();
				if (command == null) {
					break;
				}
				command = command.trim();
			} catch (IOException io) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
				io.printStackTrace();
				System.exit(1);
			}

			try {
				arguments = parse(command);

				// avoid null input
				if (arguments.size() == 0) {
					System.out.println("Invalid or empty command. Type \"help\" for usage.");
					continue;
				}

				Command cmd = Command.fromString(arguments.elementAt(0));

				try {
					execute(cmd, arguments);
				} catch (ConnectException e) {
					connectServer();
					execute(cmd, arguments);
				}
			} catch (IllegalArgumentException | ServerException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			} catch (ConnectException | UnmarshalException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mConnection to server lost");
			} catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}
	}

	public void execute(Command cmd, Vector<String> arguments) throws Exception {
		switch (cmd) {
			case Help: {
				if (arguments.size() == 1) {
					System.out.println(Command.description());
				} else if (arguments.size() == 2) {
					Command l_cmd = Command.fromString(arguments.elementAt(1));
					System.out.println(l_cmd);
				} else {
					System.err.println("Usage: help or help,<CommandName>");
				}
				break;
			}

			// ---------------- Transaction commands ----------------
			case Start: {
				checkArgumentsCount(1, arguments.size());
				currentTid = Integer.parseInt(invokeRemote(cmd, arguments).trim()); //m_middleware.startTransaction();
				System.out.println("New transaction started, xid=" + currentTid);
				break;
			}
			case Commit:
			case Abort: {
				checkArgumentsCount(1, arguments.size());
				if (currentTid == -1) {
					System.out.println("No active transaction");
				} else {
					String reply = invokeRemote(cmd, arguments);
					System.out.println("Transaction " + currentTid + " " + reply);
					currentTid = -1;
				}
				break;
			}

			case AddFlight:
			case AddRooms:
			case AddCars: {
				checkArgumentsCount(4, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println(response);
				break;
			}
			case DeleteFlight:
			case QueryCustomer:
			case DeleteCustomer:
			case AddCustomerID:
			case DeleteRooms:
			case DeleteCars: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println(response);
				break;
			}
			case QueryFlight: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Seats available: " + response);
				break;
			}
			case QueryFlightPrice: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Flight price: " + response);
				break;
			}
			case QueryCars: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Cars available: " + response);
				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Car price: " + response);
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Rooms available: " + response);
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(2, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Room price: " + response);
				break;
			}

			case AddCustomer: {
				checkArgumentsCount(1, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println("Add customer ID: " + response);
				break;
			}

			case ReserveFlight:
			case ReserveRoom:
			case ReserveCar: {
				checkArgumentsCount(3, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println(response);
				break;
			}
			case Bundle: {
				if (arguments.size() < 6) {
					System.err.println("Usage: Bundle,customerID,flightNum...,location,car?,room?");
					break;
				}
				String response = invokeRemote(cmd, arguments);
				System.out.println(response);
				break;
			}

			case Quit:
				checkArgumentsCount(1, arguments.size());
				String response = invokeRemote(cmd, arguments);
				System.out.println(response);
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

	protected abstract String invokeRemote(Command cmd, Vector<String> arguments) throws Exception;
}
