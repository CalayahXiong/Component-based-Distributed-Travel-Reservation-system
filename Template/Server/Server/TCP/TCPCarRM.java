package Server.TCP;

import Server.Common.CarResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPCarRM extends CarResourceManager {
    private final int port;
    private final String name;
    private final ServerSocket serverSocket;

    public TCPCarRM(String name, int port) throws IOException {
        super(name);
        this.name = name;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        System.out.println(name + " listening on port " + port);
    }

    public void start() {
        ExecutorService pool = Executors.newCachedThreadPool();
        while (true) {
            try {
                Socket s = serverSocket.accept();
                pool.submit(new Handler(s));
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        pool.shutdown();
    }

    private class Handler implements Runnable {
        private final Socket socket;
        Handler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            String client = socket.getRemoteSocketAddress().toString();
            System.out.println(name + " connected: " + client);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String resp = handleLine(line);
                    out.println(resp);
                }
            } catch (IOException e) {
                System.err.println(name + " connection error: " + e.getMessage());
            }
        }
    }

    private String handleLine(String line) {
        System.out.println("CarRM is handling: " + line);
        String[] parts = line.split(",", -1);
        if (parts.length < 2) return "FAIL,EMPTY";

        int tid;
        try { tid = Integer.parseInt(parts[0].trim()); }
        catch (NumberFormatException e) { return "FAIL,BAD_TID"; }

        // --- PREPARE/COMMIT/ABORT ---
        String first = parts[1].trim().toUpperCase();
        if ("PREPARE".equals(first) || "COMMIT".equals(first) || "ABORT".equals(first)) {

            switch (first) {
                case "PREPARE":
                    try {
                        return prepare(tid) ? "OK" : "FAIL,PREPARE_FAILED";
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                case "COMMIT":
                    try {
                        return commit(tid) ? "OK" : "FAIL,COMMIT_FAILED";
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                case "ABORT":
                    try {
                        return abort(tid) ? "OK" : "FAIL,ABORT_FAILED";
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
            }
        }

        // --- Normal ops ---
        String cmd = parts[1].trim();

        try {
            switch (cmd) {
                case "AddCars":
                    if (parts.length < 5) return "FAIL,BAD_ARGS";
                    return addCars(tid, parts[2].trim(), Integer.parseInt(parts[3].trim()), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL,ADD_FAILED";
                case "DeleteCars":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return deleteCars(tid, parts[2].trim()) ? "OK" : "FAIL,DELETE_FAILED";
                case "QueryCars":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryCars(tid, parts[2].trim());
                case "QueryCarsPrice":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryCarsPrice(tid, parts[2].trim());
                case "ReserveCar":
                    if (parts.length < 4) return "FAIL,BAD_ARGS";
                    int cust = Integer.parseInt(parts[2].trim());
                    String loc = parts[3].trim();
                    return reserveCar(tid, cust, loc) ? "OK" : "FAIL,NO_CAR";
                case "QueryReserved":
                    if(parts.length < 3) return "FAIL, BAD_ARGS";
                    return String.valueOf(queryReserved(tid, parts[2].trim()));
                case "rollbackReserve":
                    if(parts.length < 5) return "FAIL,BAD_ARGS";
                    return rollbackReserve(tid, Integer.parseInt(parts[2].trim()), parts[3].trim().toLowerCase(), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL IN ROLLING-BACK";
                default:
                    return "FAIL,UNKNOWN_CMD";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL," + e.getMessage();
        }
    }


    public static void main(String[] args) throws Exception {
        int port = 5002;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        TCPCarRM rm = new TCPCarRM("CarRM", port);
        rm.start();
    }

    // ------------------------ Stub methods for unsupported operations ------------------------
    // Flights
    @Override
    public boolean addFlight(int tid, String flightNum, int flightSeats, int flightPrice) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean deleteFlight(int tid, String flightNum) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public int queryFlight(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public int queryFlightPrice(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean reserveFlight(int tid, int customerID, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }

    @Override
    public boolean flightExists(int tid, String flightNumber) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle flights");
    }


    // Rooms
    @Override
    public boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }

    @Override
    public boolean roomExists(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle rooms");
    }


    // Customers
    @Override
    public int newCustomer(int tid) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean customerExists(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }

    @Override
    public boolean customerReserve(int tid, int cid, String key, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("Car RM does not handle customers");
    }
}
