package Server.TCPHelper;

import Server.Common.Trace;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Worker: processes one transaction at a time.
 * Collects each request’s result, ensures all-or-nothing commit.
 */
public class Worker implements Runnable {

    private final BlockingQueue<Request> txQueue;
    private final Map<String, RMConnection> rmConnections;
    private final TCPTransactionManager tm;

    public Worker(BlockingQueue<Request> txQueue,
                  Map<String, RMConnection> rmConnections,
                  TCPTransactionManager tm) {
        this.txQueue = txQueue;
        this.rmConnections = rmConnections;
        this.tm = tm;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request tx = txQueue.take();
                int tid = tx.getTid();
                Socket clientSocket = tx.getClientSocket();

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                if (tx.abortFlag) {
                    tm.abort(tid);
                    broadcastAbort(tid);
                    out.println("TXN_RESULT_BEGIN");
                    out.println("Transaction " + tid + " ABORTED");
                    out.println("TXN_RESULT_END");
                    continue;
                }

                boolean success = true;
                List<String> results = new ArrayList<>();

                for (String req : tx.getRequests()) {
                    //System.out.println(tx.getRequests()); //
                    String[] parts = req.split(",");
                    String command = parts[0].trim();
                    String resp;

                    switch (command) {
                        // ------------ Flight ------------
                        case "AddFlight":
                        case "QueryFlight":
                        case "QueryFlightPrice":
                            resp = sendToRM("FlightRM", tid, req);
                            break;

                        case "DeleteFlight": {
                            String flightNum = parts[1].trim();
                            String exists = sendToRM("FlightRM", tid, "FlightExists," + flightNum);
                            if (exists == null || !exists.startsWith("OK")) {
                                resp = "FAIL,FlightNotFound";
                                break;
                            }
                            String reserved = sendToRM("FlightRM", tid, "QueryReserved," + flightNum);
                            if (reserved == null || !reserved.startsWith("OK,")) {
                                resp = "FAIL,QueryReservedError";
                                break;
                            }
                            int r = Integer.parseInt(reserved.split(",")[1]);
                            if (r > 0) {
                                resp = "FAIL,FlightHasReservations";
                                break;
                            }
                            resp = sendToRM("FlightRM", tid, req);
                            break;
                        }

                        // ------------ Car ------------
                        case "AddCars":
                        case "QueryCars":
                        case "QueryCarsPrice":
                            resp = sendToRM("CarRM", tid, req);
                            break;

                        case "DeleteCars": {
                            String loc = parts[1].trim();
                            String exists = sendToRM("CarRM", tid, "CarExists," + loc);
                            if (exists == null || !exists.startsWith("OK")) {
                                resp = "FAIL,CarNotFound";
                                break;
                            }
                            String reserved = sendToRM("CarRM", tid, "QueryReserved," + loc);
                            if (reserved == null || !reserved.startsWith("OK,")) {
                                resp = "FAIL,QueryReservedError";
                                break;
                            }
                            int r = Integer.parseInt(reserved.split(",")[1]);
                            if (r > 0) {
                                resp = "FAIL,CarHasReservations";
                                break;
                            }
                            resp = sendToRM("CarRM", tid, req);
                            break;
                        }

                        // ------------ Room ------------
                        case "AddRooms":
                        case "QueryRooms":
                        case "QueryRoomsPrice":
                            resp = sendToRM("RoomRM", tid, req);
                            break;

                        case "DeleteRooms": {
                            String loc = parts[1].trim();
                            String exists = sendToRM("RoomRM", tid, "RoomExists," + loc);
                            if (exists == null || !exists.startsWith("OK")) {
                                resp = "FAIL,RoomNotFound";
                                break;
                            }
                            String reserved = sendToRM("RoomRM", tid, "QueryReserved," + loc);
                            if (reserved == null || !reserved.startsWith("OK,")) {
                                resp = "FAIL,QueryReservedError";
                                break;
                            }
                            int r = Integer.parseInt(reserved.split(",")[1]);
                            if (r > 0) {
                                resp = "FAIL,RoomHasReservations";
                                break;
                            }
                            resp = sendToRM("RoomRM", tid, req);
                            break;
                        }

                        // ------------ Customer ------------
                        case "AddCustomer":
                        case "AddCustomerID":
                        case "QueryCustomer":
                            resp = sendToRM("CustomerRM", tid, req);
                            break;
                        case "DeleteCustomer": {
                            try {
                                int customerID = Integer.parseInt(parts[1]);

                                // 1. Get customer reservations
                                String custResp = sendToRM("CustomerRM", tid, "getItem,customer-" + customerID);
                                if (custResp == null || custResp.startsWith("ERROR") || custResp.equals("NOT_FOUND")) {
                                    resp = "FAIL,NoSuchCustomer";
                                    break;
                                }

                                // Response: OK,<reservationKey1>:count; <reservationKey2>:count; ...
                                System.out.println("---------------------------------------------" + custResp);

                                String[] tokens = custResp.split(",", 2);
                                if (tokens.length < 2) {
                                    resp = "FAIL,BadCustomerFormat";
                                    break;
                                }

                                String reservationsStr = tokens[1].trim();
                                if (reservationsStr.isEmpty()) {
                                    reservationsStr = ""; // no reservations
                                }

                                boolean rollbackOK = true;

                                String[] items = reservationsStr.split(";");
                                for (String item : items) {
                                    String[] partsItem = item.trim().split(":");
                                    String reservedKey = partsItem[0];
                                    int count = (partsItem.length > 1) ? Integer.parseInt(partsItem[1]) : 1;

                                    if (count <= 0) continue;

                                    String rmType;
                                    if (reservedKey.startsWith("flight-")) {
                                        rmType = "FlightRM";
                                    } else if (reservedKey.startsWith("car-")) {
                                        rmType = "CarRM";
                                    } else if (reservedKey.startsWith("room-")) {
                                        rmType = "RoomRM";
                                    } else {
                                        continue; // unknown type, skip
                                    }

                                    String res = sendToRM(rmType, tid, "rollbackReserve," + customerID + "," + reservedKey + "," + count);
                                    if (res == null || !res.startsWith("OK")) {
                                        rollbackOK = false;
                                        Trace.warn("Worker::DeleteCustomer rollback failed for " + reservedKey);
                                    }
                                }

                                if (!rollbackOK) {
                                    resp = "FAIL,RollbackFailed";
                                    break;
                                }

                                // 2. Delete customer after successful rollbacks
                                String delResp = sendToRM("CustomerRM", tid, "DeleteCustomer," + customerID);
                                resp = (delResp != null && delResp.startsWith("OK")) ? "OK,CustomerDeleted"
                                        : "FAIL,DeleteCustomerFailed";

                            } catch (Exception e) {
                                resp = "ERROR,DeleteCustomer," + e.getMessage();
                                e.printStackTrace();
                            }
                            break;
                        }

                        // ------------ Reservation ------------
                        case "ReserveFlight":
                        case "ReserveCar":
                        case "ReserveRoom":
                        case "Bundle":
                            resp = handleReservation(tid, parts);
                            break;

                        // ------------ Commit (marker only) ------------
                        case "Commit":
                            resp = "Commit requested";
                            break;

                        default:
                            resp = "ERROR,UnknownCommand," + command;
                            success = false;
                    }

                    results.add(command + " -> " + resp);

                    if (resp == null || resp.startsWith("ERROR") || resp.startsWith("FAIL")) {
                        success = false;
                        break; // fail fast
                    }
                }

                // ------------ Final decision at Commit ------------
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    if (success) {
                        boolean prepared = broadcastPrepare(tid);
                        //System.out.println("Prepare:" + prepared);
                        if (prepared) {
                            System.out.println("Worker call commit!!!");
                            if(broadcastCommit(tid)) {
                                tm.commit(tid);
                                out.println("TXN_RESULT_BEGIN");
                                results.forEach(out::println);
                                out.println("Transaction " + tid + " COMMITTED");
                                out.println("TXN_RESULT_END");
                            }
                        } else {
                            tm.abort(tid);
                            broadcastAbort(tid);
                            out.println("TXN_RESULT_BEGIN");
                            results.forEach(out::println);
                            out.println("Transaction " + tid + " ABORTED");
                            out.println("TXN_RESULT_END");
                        }
                    } else {
                        tm.abort(tid);
                        broadcastAbort(tid);
                        out.println("TXN_RESULT_BEGIN");
                        results.forEach(out::println);
                        out.println("Transaction " + tid + " ABORTED");
                        out.println("TXN_RESULT_END");
                    }
                } catch (Exception e) {
                    System.err.println("[Worker] exception: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.err.println("[Worker] exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ------------ helpers ------------

    private String sendToRM(String rmName, int tid, String msg) {
        RMConnection conn = rmConnections.get(rmName);
        if (conn == null) {
            return "ERROR,RMNotFound," + rmName;
        }
        tm.addParticipant(tid, rmName);
        return conn.send(msg, tid);
    }

    private boolean broadcastPrepare(int tid) {
        for (String rm : tm.getParticipants(tid)) {
            String resp = rmConnections.get(rm).send("Prepare", tid);
            if (resp == null || !resp.startsWith("OK")) {
                return false;
            }
        }
        return true;
    }

    private boolean broadcastCommit(int tid) {
        boolean allSuccess = true;

        for (String rm : tm.getParticipants(tid)) {
            System.out.println("send commit request to " + rm);

            String resp =rmConnections.get(rm).send("Commit", tid);
            if (resp == null || !resp.startsWith("OK")) {
                System.err.println("[broadcastCommit] Commit failed for " + rm + " in T" + tid);
                allSuccess = false;
            }
        }

        return allSuccess;
    }


    private void broadcastAbort(int tid) {
        for (String rm : tm.getParticipants(tid)) {
            rmConnections.get(rm).send("Abort", tid);
        }
    }

    // ------------ Reservation handler ------------
    private String handleReservation(int tid, String[] parts) {
        System.out.println("Worker now working on:" + Arrays.toString(parts));
        String cmd = parts[0];
        try {
            switch (cmd) {
                case "ReserveFlight": {
                    int customerID = Integer.parseInt(parts[1]);
                    String flightNum = parts[2];

                    // 1. Check customer exists
                    String exists = sendToRM("CustomerRM", tid, "CustomerExists," + customerID);
                    if (!"OK".equals(exists)) return "FAIL,CustomerDoesNotExist";

                    // 2. Query flight price (optional)
                    String priceResp = sendToRM("FlightRM", tid, "QueryFlightPrice," + flightNum);
                    if (!priceResp.startsWith("OK")) return "FAIL,FlightNotFound";
                    int price = Integer.parseInt(priceResp.split(",")[1]);

                    // 3. Reserve flight
                    String reserved = sendToRM("FlightRM", tid, "ReserveFlight," + customerID + "," + flightNum);
                    if (!"OK".equals(reserved)) return "FAIL,FlightNotReserved";

                    // 4. Update customer reservations
                    String added = sendToRM("CustomerRM", tid, "CustomerReserve," + customerID + ",flight-" + flightNum + ",1," + price);
                    if (!"OK".equals(added)) {
                        sendToRM("FlightRM", tid, "RollbackReserve," + customerID + "," + flightNum); // rollback
                        return "FAIL,CouldNotUpdateCustomer";
                    }

                    return "Flight reserved";

                }
                case "ReserveCar": {
                    int customerID = Integer.parseInt(parts[1]);
                    String location = parts[2];

                    // check customer
                    String exists = sendToRM("CustomerRM", tid, "CustomerExists," + customerID);
                    if (!"OK".equals(exists)) return "FAIL,CustomerDoesNotExist";

                    // query price
                    String priceResp = sendToRM("CarRM", tid, "QueryCarsPrice," + location);
                    if (!priceResp.startsWith("OK")) return "FAIL,CarNotFound";
                    int price = Integer.parseInt(priceResp.split(",")[1]);

                    // reserve
                    String reserved = sendToRM("CarRM", tid, "ReserveCar," + customerID + "," + location);
                    if (!"OK".equals(reserved)) return "FAIL,CarNotReserved";

                    // update customer
                    String added = sendToRM("CustomerRM", tid, "CustomerReserve," + customerID + ",car-" + location + ",1," + price);
                    if (!"OK".equals(added)) {
                        sendToRM("CarRM", tid, "RollbackReserve," + customerID + "," + location);
                        return "FAIL,CouldNotUpdateCustomer";
                    }

                    return "Car reserved";
                }
                case "ReserveRoom": {
                    int customerID = Integer.parseInt(parts[1]);
                    String location = parts[2];

                    String exists = sendToRM("CustomerRM", tid, "CustomerExists," + customerID);
                    if (!"OK".equals(exists)) return "FAIL,CustomerDoesNotExist";

                    String priceResp = sendToRM("RoomRM", tid, "QueryRoomsPrice," + location);
                    if (!priceResp.startsWith("OK")) return "FAIL,RoomNotFound";
                    int price = Integer.parseInt(priceResp.split(",")[1]);

                    String reserved = sendToRM("RoomRM", tid, "ReserveRoom," + customerID + "," + location);
                    if (!"OK".equals(reserved)) return "FAIL,RoomNotReserved";

                    String added = sendToRM("CustomerRM", tid, "CustomerReserve," + customerID + ",room-" + location + ",1," + price);
                    if (!"OK".equals(added)) {
                        sendToRM("RoomRM", tid, "RollbackReserve," + customerID + "," + location);
                        return "FAIL,CouldNotUpdateCustomer";
                    }

                    return "Room reserved";
                }
                case "Bundle": {
                    return handleBundle(tid, parts);
                }
                default:
                    return "ERROR,UnknownReservationCommand," + cmd;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL,Exception:" + e.getMessage();
        }
    }


    // ------------ Bundle (all-or-nothing) ------------
// Bundle, cid, <flightnum...>, location, T/F(car), T/F(room)
    private String handleBundle(int tid, String[] parts) {
        try {
            int customerId = Integer.parseInt(parts[1]);

            //  parts[3] to parts[length-3]
            List<String> flightNos = new ArrayList<>();
            for (int i = 2; i < parts.length - 3; i++) {
                flightNos.add(parts[i]);
            }

            String location = parts[parts.length - 3];
            boolean reserveCar = Boolean.parseBoolean(parts[parts.length - 2]);
            boolean reserveRoom = Boolean.parseBoolean(parts[parts.length - 1]);

            List<String> ops = new ArrayList<>();
            for (String f : flightNos) {
                ops.add("ReserveFlight," + customerId + "," + f);
            }
            if (reserveCar) {
                ops.add("ReserveCar," + customerId + "," + location);
            }
            if (reserveRoom) {
                ops.add("ReserveRoom," + customerId + "," + location);
            }

            for (String op : ops) {
                String resp = handleReservation(tid, op.split(","));
                if (resp == null || resp.startsWith("ERROR") || resp.startsWith("FAIL")) {
                    return "ERROR,BundleFailed -> " + op;
                }
            }
            return "OK,BundleSuccess";

        } catch (Exception e) {
            return "ERROR,InvalidBundleFormat," + e.getMessage();
        }
    }

}
