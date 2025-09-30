package Client;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class TCPClient extends Client {
    private static String s_serverHost = "localhost";
    private static int s_serverPort = 3035;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<String> txLog = new ArrayList<>();

    public TCPClient() {
        super();
    }

    public void connectServer() {
        try {
            socket = new Socket(s_serverHost, s_serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to Middleware at " + s_serverHost + ":" + s_serverPort);
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + s_serverHost + ":" + s_serverPort);
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected String invokeRemote(Command cmd, Vector<String> arguments) throws Exception {
        switch (cmd) {
            // ---------------- Transaction lifecycle ----------------
            case Start: {
                // ask middleware to start and return tid (middleware returns "TID,<n>")
                txLog.clear();
                out.println("Start");
                out.flush();

                String line = in.readLine();
                if (line == null) throw new IOException("No response from middleware on BEGIN");
                if (!line.startsWith("TID,")) {
                    throw new IOException("Unexpected BEGIN response: " + line);
                }
                currentTid = Integer.parseInt(line.split(",", 2)[1].trim());
                return String.valueOf(currentTid);
            }

            case Commit: {
                if (currentTid == -1) return "No active transaction";

                // send all queued requests (one per line), then COMMIT marker
                for (String req : txLog) {
                    out.println(req);
                }
                out.println("Commit");
                out.flush();

                // read transaction result block (middleware 会返回 TXN_RESULT_BEGIN ... TXN_RESULT_END)
                StringBuilder sb = new StringBuilder();
                String line;
                boolean started = false;
                while ((line = in.readLine()) != null) {
                    // if middleware uses markers:
                    if (!started) {
                        if (line.equals("TXN_RESULT_BEGIN")) { started = true; continue; }
                        // tolerate middleware that doesn't send BEGIN marker and directly starts with results:
                        started = true;
                    }
                    if (line.equals("TXN_RESULT_END")) break;
                    sb.append(line).append("\n");
                    // if middleware closes socket after sending result, readLine() will later return null and loop exits
                }

                txLog.clear();
                currentTid = -1;
                return sb.toString().trim();
            }

            case Abort: {
                if (currentTid == -1) {
                    txLog.clear();
                    return "No active transaction to abort";
                }
                out.println("Abort"); //the requests typed before won't be sent to mw
                out.flush();

                // read final result block
                StringBuilder sb = new StringBuilder();
                String line;
                boolean started = false;
                while ((line = in.readLine()) != null) {
                    if (!started) {
                        if (line.equals("TXN_RESULT_BEGIN")) { started = true; continue; }
                        started = true;
                    }
                    if (line.equals("TXN_RESULT_END")) break;
                    sb.append(line).append("\n");
                }
                currentTid = -1;
                return sb.toString().trim();
            }

            case Quit: {
                System.out.println("Quitting client");

                out.println("Quit");
                out.flush();

                try {
                    socket.close();
                } catch (IOException ignore) {}

                return "Client quit";
            }


            // ---------------- Flights ----------------
            case AddFlight: {
                // arguments: [?, flightNum, seats, price] (按你原 RMI 客户端的索引)
                String flightNum = arguments.get(1);
                int seats = toInt(arguments.get(2));
                int price = toInt(arguments.get(3));
                String line = "AddFlight," + flightNum + "," + seats + "," + price;
                txLog.add(line);
                return "queued: " + line;
            }

            case DeleteFlight: {
                String flightNum = arguments.get(1);
                String line = "DeleteFlight," + flightNum;
                txLog.add(line);
                return "queued: " + line;
            }

            case QueryFlight: {
                String flightNum = arguments.get(1);
                String line = "QueryFlight," + flightNum;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            case QueryFlightPrice: {
                String flightNum = arguments.get(1);
                String line = "QueryFlightPrice," + flightNum;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            // ---------------- Cars ----------------
            case AddCars: {
                String loc = arguments.get(1);
                int num = toInt(arguments.get(2));
                int price = toInt(arguments.get(3));
                String line = "AddCars," + loc + "," + num + "," + price;
                txLog.add(line);
                return "queued: " + line;
            }

            case DeleteCars: {
                String loc = arguments.get(1);
                String line = "DeleteCars," + loc;
                txLog.add(line);
                return "queued: " + line;
            }

            case QueryCars: {
                String loc = arguments.get(1);
                String line = "QueryCars," + loc;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            case QueryCarsPrice: {
                String loc = arguments.get(1);
                String line = "QueryCarsPrice," + loc;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            // ---------------- Rooms ----------------
            case AddRooms: {
                String loc = arguments.get(1);
                int num = toInt(arguments.get(2));
                int price = toInt(arguments.get(3));
                String line = "AddRooms," + loc + "," + num + "," + price;
                txLog.add(line);
                return "queued: " + line;
            }

            case DeleteRooms: {
                String loc = arguments.get(1);
                String line = "DeleteRooms," + loc;
                txLog.add(line);
                return "queued: " + line;
            }

            case QueryRooms: {
                String loc = arguments.get(1);
                String line = "QueryRooms," + loc;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            case QueryRoomsPrice: {
                String loc = arguments.get(1);
                String line = "QueryRoomsPrice," + loc;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            // ---------------- Customers ----------------
            case AddCustomer: {
                String line = "AddCustomer";
                txLog.add(line);
                return "queued: " + line;
            }

            case AddCustomerID: {
                int cid = toInt(arguments.get(1));
                String line = "AddCustomerID," + cid;
                txLog.add(line);
                return "queued: " + line;
            }

            case DeleteCustomer: {
                int cid = toInt(arguments.get(1));
                String line = "DeleteCustomer," + cid;
                txLog.add(line);
                return "queued: " + line;
            }

            case QueryCustomer: {
                int cid = toInt(arguments.get(1));
                String line = "QueryCustomer," + cid;
                txLog.add(line);
                return "queued: " + line + " (result visible after COMMIT)";
            }

            // ---------------- Reservations ----------------
            case ReserveFlight: {
                int cid = toInt(arguments.get(1));
                String flight = arguments.get(2);
                String line = "ReserveFlight," + cid + "," + flight;
                txLog.add(line);
                return "queued: " + line;
            }

            case ReserveCar: {
                int cid = toInt(arguments.get(1));
                String loc = arguments.get(2);
                String line = "ReserveCar," + cid + "," + loc;
                txLog.add(line);
                return "queued: " + line;
            }

            case ReserveRoom: {
                int cid = toInt(arguments.get(1));
                String loc = arguments.get(2);
                String line = "ReserveRoom," + cid + "," + loc;
                txLog.add(line);
                return "queued: " + line;
            }

            case Bundle: {
                // arguments layout in your RMI client: Bundle,customerID,flightNum...,location,car?,room?
                // copy all arguments (from index 1 onwards) into single CSV line
                StringJoiner sj = new StringJoiner(",");
                sj.add("Bundle");
                for (int i = 1; i < arguments.size(); i++) {
                    sj.add(arguments.get(i));
                }
                String line = sj.toString();
                txLog.add(line);
                return "queued: " + line;
            }

            default:
                return "Unsupported command: " + cmd;
        }
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            try {
                s_serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[1]);
                System.exit(1);
            }
        }

        try {
            TCPClient client = new TCPClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            System.err.println("\u001B[31;1mClient exception: \u001B[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
