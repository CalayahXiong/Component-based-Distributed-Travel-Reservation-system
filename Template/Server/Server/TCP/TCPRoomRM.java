package Server.TCP;

import Server.Common.RoomResourceManager;

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
 * TCPRoomRM
 *
 * In-memory Room Resource Manager with transaction staging and 2PC protocol.
 * Supported messages:
 *   PREPARE,tid
 *   COMMIT,tid
 *   ABORT,tid
 *   <tid>,AddRoom,<location>,<numRooms>,<price>
 *   <tid>,DeleteRoom,<location>
 *   <tid>,QueryRoom,<location>
 *   <tid>,QueryRoomPrice,<location>
 *   <tid>,ReserveRoom,<customerID>,<location>
 *   <tid>,RoomExists,<location>
 */
public class TCPRoomRM extends RoomResourceManager {
    private final int port;
    private final ServerSocket serverSocket;
    private final String name;

    public TCPRoomRM(String name, int port) throws IOException {
        super(name);
        this.port = port;
        this.name = name;
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
        System.out.println("RoomRM is handling: " + line);
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
                case "AddRooms":
                    if (parts.length < 5) return "FAIL,BAD_ARGS";
                    return addRooms(tid, parts[2].trim(), Integer.parseInt(parts[3].trim()), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL,ADD_FAILED";
                case "DeleteRooms":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return deleteRooms(tid, parts[2].trim()) ? "OK" : "FAIL,DELETE_FAILED";
                case "QueryRooms":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryRooms(tid, parts[2].trim());
                case "QueryRoomsPrice":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryRoomsPrice(tid, parts[2].trim());
                case "ReserveRoom":
                    if (parts.length < 4) return "FAIL,BAD_ARGS";
                    int cust = Integer.parseInt(parts[2].trim());
                    String loc = parts[3].trim();
                    return reserveRoom(tid, cust, loc) ? "OK" : "FAIL,NO_ROOM";
                case "QueryReserved":
                    if(parts.length < 3) return "FAIL, BAD_ARGS";
                    return String.valueOf(queryReserved(tid, parts[2].trim()));
                case "rollbackReserve":
                    if(parts.length < 5) return "FAIL,BAD_ARGS";
                    return rollbackReserve(tid, Integer.parseInt(parts[2].trim()), parts[3].trim().toLowerCase(), Integer.parseInt(parts[4].trim())) ? "OK" : "FAIL IN ROLLINGBACK";
                default:
                    return "FAIL,UNKNOWN_CMD";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL," + e.getMessage();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 5003;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        TCPRoomRM rm = new TCPRoomRM("RoomRM", port);
        rm.start();
    }

    // Unsupported ops
    @Override public boolean addFlight(int tid, String flightNum, int seats, int price) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }
    @Override public boolean deleteFlight(int tid, String flightNum) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }
    @Override public int queryFlight(int tid, String flightNum) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }
    @Override public int queryFlightPrice(int tid, String flightNum) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }
    @Override public boolean reserveFlight(int tid, int customerID, String flightNum) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }
    @Override public boolean flightExists(int tid, String flightNum) { throw new UnsupportedOperationException("RoomRM does not handle flights"); }

    @Override public boolean addCars(int tid, String location, int numCars, int price) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }
    @Override public boolean deleteCars(int tid, String location) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }
    @Override public int queryCars(int tid, String location) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }
    @Override public int queryCarsPrice(int tid, String location) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }
    @Override public boolean reserveCar(int tid, int customerID, String location) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }
    @Override public boolean carExists(int tid, String location) { throw new UnsupportedOperationException("RoomRM does not handle cars"); }

    @Override public int newCustomer(int tid) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
    @Override public boolean newCustomerID(int tid, int cid) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
    @Override public boolean deleteCustomer(int tid, int customerID) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
    @Override public String queryCustomerInfo(int tid, int customerID) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
    @Override public boolean customerExists(int tid, int customerID) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
    @Override public boolean customerReserve(int tid, int cid, String key, int count, int price) { throw new UnsupportedOperationException("RoomRM does not handle customers"); }
}
