package Server.TCP;

import Server.Common.FlightResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCPFlightRM
 *
 * Simple in-memory Flight RM that supports transaction staging and 2PC commands:
 *  - Operation messages are staged per-tid (not applied to global state).
 *  - PREPARE,tid -> validate staged changes for tid (return OK or FAIL)
 *  - COMMIT,tid  -> apply staged changes to global state and clear stage
 *  - ABORT,tid   -> discard staged changes
 *
 * Message formats (single-line, CSV):
 *  - PREPARE,<tid>
 *  - COMMIT,<tid>
 *  - ABORT,<tid>
 *  - <tid>,AddFlight,<flightNum>,<seats>,<price>
 *  - <tid>,DeleteFlight,<flightNum>
 *  - <tid>,QueryFlight,<flightNum>
 *  - <tid>,QueryFlightPrice,<flightNum>
 *  - <tid>,ReserveFlight,<customerID>,<flightNum>
 *  - <tid>,FlightExists,<flightNum>
 *
 * Replies:
 *  - OK
 *  - OK,<value> (for queries)
 *  - FAIL,<reason>
 */
public class TCPFlightRM extends FlightResourceManager {

    private final int port;

    private final String name;
    private final ServerSocket serverSocket;

    // simple constructor
    public TCPFlightRM(String name, int port) throws IOException {
        super(name);
        this.port = port;
        this.name = name;
        this.serverSocket = new ServerSocket(port);
        System.out.println(name + " listening on port " + port);
    }


    // Start server
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


    // Handler for each middleware connection (long-lived)
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

    // Parse line and dispatch
    // line: "1, PREPARE"
    //"2, AddFlight, 222, 001"
    private String handleLine(String line) {
        // support both "PREPARE,tid" and "tid,Command,..." forms
        System.out.println("FlightRM is handling: " + line);
        String[] parts = line.split(",", -1);
        if (parts.length < 2 ) return "FAIL,EMPTY";

        int tid;
        try { tid = Integer.parseInt(parts[0].trim()); } catch (NumberFormatException e) { return "FAIL,BAD_TID"; }

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

        // else assume form "<tid>,Command,..."
        String cmd = parts[1].trim();
        // ---------- operations that stage changes (thread-safe) in FlightResourceManager ----------
        try {
            switch (cmd) {
                case "AddFlight":
                    if (parts.length < 5) return "FAIL,BAD_ARGS";
                    return addFlight(tid, parts[2].trim(), Integer.parseInt(parts[3].trim()), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL,ADD_FAILED";
                case "DeleteFlight":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return deleteFlight(tid, parts[2].trim()) ? "OK" : "FAIL,DELETE_FAILED";
                case "QueryFlight":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryFlight(tid, parts[2].trim());
                case "QueryFlightPrice":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryFlightPrice(tid, parts[2].trim());
                case "ReserveFlight":
                    if (parts.length < 4) return "FAIL,BAD_ARGS";
                    int cust = Integer.parseInt(parts[2].trim());
                    String fnum = parts[3].trim();
                    return reserveFlight(tid, cust, fnum) ? "OK" : "FAIL,NO_SEAT";
                case "FlightExists":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return flightExists(tid, parts[2].trim()) ? "OK" : "FAIL,NOT_FOUND";
                case "QueryReserved":
                    if(parts.length < 3) return "FAIL, BAD_ARGS";
                    return String.valueOf(queryReserved(tid, parts[2].trim()));
                case "rollbackReserve":
                    if(parts.length < 5) return "FAIL,BAD_ARGS";
                    return rollbackReserve(tid, Integer.parseInt(parts[2].trim()), parts[3].trim(), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL IN ROLLINGBACK";
                default:
                    return "FAIL,UNKNOWN_CMD";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL," + e.getMessage();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 5001;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        TCPFlightRM rm = new TCPFlightRM("FlightRM", port);

        rm.start();
    }

    //----------- operations that unrelenting to FlightRM
    @Override
    public boolean addCars(int tid, String location, int numCars, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public boolean deleteCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public int queryCars(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public int queryCarsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }

    @Override
    public boolean reserveCar(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle cars");
    }


    // Rooms
    @Override
    public boolean addRooms(int tid, String location, int numRooms, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public boolean deleteRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public int queryRooms(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public int queryRoomsPrice(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public boolean reserveRoom(int tid, int customerID, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }

    @Override
    public boolean roomExists(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle rooms");
    }


    // Customers
    @Override
    public int newCustomer(int tid) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean newCustomerID(int tid, int cid) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean deleteCustomer(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public String queryCustomerInfo(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean customerExists(int tid, int customerID) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean customerReserve(int tid, int cid, String key, int count, int price) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }

    @Override
    public boolean carExists(int tid, String location) throws RemoteException {
        throw new UnsupportedOperationException("Flight RM does not handle customers");
    }
}
