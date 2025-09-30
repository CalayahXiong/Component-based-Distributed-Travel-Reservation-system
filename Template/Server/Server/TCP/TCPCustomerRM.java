package Server.TCP;


import Server.Common.Customer;
import Server.Common.CustomerManager;
import Server.Common.RMHashMap;
import Server.Common.ReservedItem;

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
 * TCPCustomerRM
 *
 * In-memory Customer Resource Manager with transaction staging and 2PC protocol.
 * Supported messages:
 *   PREPARE,tid
 *   COMMIT,tid
 *   ABORT,tid
 *   <tid>,NewCustomer
 *   <tid>,NewCustomerID,<cid>
 *   <tid>,DeleteCustomer,<cid>
 *   <tid>,QueryCustomerInfo,<cid>
 *   <tid>,CustomerExists,<cid>
 */
public class TCPCustomerRM extends CustomerManager {
    private final int port;
    private final ServerSocket serverSocket;
    private final String name;

    public TCPCustomerRM(String name, int port) throws IOException {
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
        System.out.println("CustomerRM is handling: " + line);
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
                case "AddCustomer":
                    return "OK, new ID: " + newCustomer(tid);
                case "AddCustomerID":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return newCustomerID(tid, Integer.parseInt(parts[2].trim())) ? "OK" : "FAIL,NEW_FAILED";
                case "DeleteCustomer":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return deleteCustomer(tid, Integer.parseInt(parts[2].trim())) ? "OK" : "FAIL,DELETE_FAILED";
                case "QueryCustomer":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return "OK," + queryCustomerInfo(tid, Integer.parseInt(parts[2].trim()));
                case "CustomerExists":
                    if (parts.length < 3) return "FAIL,BAD_ARGS";
                    return customerExists(tid, Integer.parseInt(parts[2].trim())) ? "OK" : "FAIL,NOT_FOUND";
                case "CustomerReserve": { // tid ,"CustomerReserve," + customerID + ",flight-" + flightNum + ",1," + price
                    if (parts.length < 6) return "FAIL,BAD_ARGS";
                    int cid = Integer.parseInt(parts[2].trim());
                    String key = parts[3].trim();
                    int count = Integer.parseInt(parts[4].trim());
                    int price = Integer.parseInt(parts[5].trim());
                    boolean ok = customerReserve(tid, cid, key, count, price);
                    return ok ? "OK" : "FAIL";
                }
                case "QueryReserved":
                    if(parts.length < 3) return "FAIL, BAD_ARGS";
                    return String.valueOf(queryReserved(tid, parts[2].trim()));
                case "getItem": {
                    if (parts.length < 3) return "FAIL,BAD_ARGS";

                    Customer cust = (Customer) getItem(tid, parts[2]);
                    if (cust == null) {
                        return "FAIL,NO_SUCH_CUSTOMER";
                    }

                    StringBuilder sb = new StringBuilder("OK, ");
                    RMHashMap reservations = cust.getReservations();

                    boolean label = true;
                    for (String reservedKey : reservations.keySet()) {
                        ReservedItem item = (ReservedItem) reservations.get(reservedKey);
                        if (!label) sb.append("; ");
                        sb.append(reservedKey).append(":").append(item.getCount());
                        label = false;
                    }

                    return sb.toString();
                }

                case "rollbackReserve": {
                    if (parts.length < 5) return "FAIL,BAD_ARGS"; // tid, RM, customerID, key, count
                    int customerID = Integer.parseInt(parts[2].trim());
                    String key = parts[3].trim();
                    int count = Integer.parseInt(parts[4].trim());
                    return rollbackReserve(tid, customerID, key, count) ? "OK" : "FAIL,ROLLBACK_FAILED";
                }

                default: return "FAIL,UNKNOWN_CMD";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL," + e.getMessage();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 5004;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        TCPCustomerRM rm = new TCPCustomerRM("CustomerRM", port);
        rm.start();
    }

    // Unsupported ops
    @Override public boolean addFlight(int tid, String flightNum, int seats, int price) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }
    @Override public boolean deleteFlight(int tid, String flightNum) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }
    @Override public int queryFlight(int tid, String flightNum) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }
    @Override public int queryFlightPrice(int tid, String flightNum) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }
    @Override public boolean reserveFlight(int tid, int customerID, String flightNum) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }
    @Override public boolean flightExists(int tid, String flightNum) { throw new UnsupportedOperationException("CustomerRM does not handle flights"); }

    @Override public boolean addCars(int tid, String location, int numCars, int price) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }
    @Override public boolean deleteCars(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }
    @Override public int queryCars(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }
    @Override public int queryCarsPrice(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }
    @Override public boolean reserveCar(int tid, int customerID, String location) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }
    @Override public boolean carExists(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle cars"); }

    @Override public boolean addRooms(int tid, String location, int numRooms, int price) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
    @Override public boolean deleteRooms(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
    @Override public int queryRooms(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
    @Override public int queryRoomsPrice(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
    @Override public boolean reserveRoom(int tid, int customerID, String location) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
    @Override public boolean roomExists(int tid, String location) { throw new UnsupportedOperationException("CustomerRM does not handle rooms"); }
}
