package Server.TCP;

import Server.TCPHelper.RMConnection;
import Server.TCPHelper.Request;
import Server.TCPHelper.TCPTransactionManager;
import Server.TCPHelper.Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * TCPMiddleware is responsible for:
 * 1. Persistent connection with RMs,
 * 2. Listening in port:3035, and receving transaction from Client, package it to a Request, then put into MQ,
 * 3. Employ Workers to handle Request asynly. (Take one Request out of MQ -> disassemble and send msg to RMs following
 * logic in Middleware.java
 * 4. Workers collect all responses from RMs, assemble them to a result to one Request. Then send the result to Client by mapping
 * <requestId, clientSocket>
 */
public class TCPMiddleware {

    // config: RM endpoints
    private final Map<String, InetSocketAddress> rmAddrs;

    // persistent connections to RMs
    private final Map<String, RMConnection> rmConnections;

    // transaction queue (MQ)
    private final BlockingQueue<Request> txQueue = new LinkedBlockingQueue<>();

    // thread pool for client handlers
    private final ExecutorService clientAcceptPool = Executors.newCachedThreadPool();

    // worker pool for processing transactions
    private final ExecutorService workerPool;

    private final TCPTransactionManager tm = new TCPTransactionManager();

    private final int listenPort;
    private ServerSocket serverSocket;

    public TCPMiddleware(int listenPort, int workerCount) {
        this.listenPort = listenPort; //3035
        this.workerPool = Executors.newFixedThreadPool(workerCount);

        String flightHost = "tr-open-01.cs.mcgill.ca";
        String carHost    = "tr-open-02.cs.mcgill.ca";
        String roomHost   = "tr-open-03.cs.mcgill.ca";
        String custHost   = "tr-open-04.cs.mcgill.ca";
        String Host = "localhost";

        rmAddrs = new HashMap<>();
        rmAddrs.put("FlightRM", new InetSocketAddress(Host, 5001));
        rmAddrs.put("CarRM", new InetSocketAddress(Host, 5002));
        rmAddrs.put("RoomRM", new InetSocketAddress(Host, 5003));
        rmAddrs.put("CustomerRM", new InetSocketAddress(Host, 5004));

        // create RMConnection placeholders
        rmConnections = new ConcurrentHashMap<>();
        rmAddrs.forEach((k, v) -> rmConnections.put(k, new RMConnection(k, v.getHostString(), v.getPort())));
    }

    public void start() throws IOException {
        System.out.println("Starting TCPMiddleware listening on port " + listenPort);
        serverSocket = new ServerSocket(listenPort);

        // start worker threads
        for (int i = 0; i < ((ThreadPoolExecutor) workerPool).getCorePoolSize(); i++) {
            workerPool.submit(new Worker(txQueue, rmConnections, tm));
        }

        // listening clients persistently
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientAcceptPool.submit(new ClientHandler(clientSocket));
        }
    }

    public class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            PrintWriter out;
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String line;
                List<String> batchLines = new ArrayList<>();
                boolean inTxn = false;
                int clientTid = -1;

                while (true) {
                    line = in.readLine();
                    if (line == null) {
                        // client closed connection
                        System.out.println("[ClientHandler] Client disconnected: " + socket.getRemoteSocketAddress());
                        break;
                    }
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (line.equalsIgnoreCase("Start")) {
                        clientTid = tm.start();
                        inTxn = true;
                        batchLines.clear();
                        out.println("TID," + clientTid);
                        System.out.println("[TM] START T" + clientTid);
                    }

                    else if (line.equalsIgnoreCase("Quit")) {
                        System.out.println("[ClientHandler] Client requested quit: " + socket.getRemoteSocketAddress());
                        break;
                    }

                    else if (line.equalsIgnoreCase("Commit") || line.equalsIgnoreCase("Abort")) {
                        if (!inTxn) {
                            out.println("ERROR,NO_ACTIVE_TRANSACTION");
                            continue;
                        }
                        boolean abortFlag = line.equalsIgnoreCase("Abort");
                        if(!abortFlag) batchLines.add(line);
                        Request tx = new Request(
                                UUID.randomUUID().toString(),
                                clientTid,
                                abortFlag ? Collections.emptyList() : new ArrayList<>(batchLines),
                                socket,
                                abortFlag
                        );
                        txQueue.put(tx); //worker will get immediately

                        inTxn = false;
                        batchLines.clear();

                        System.out.println("[TM] " + (abortFlag ? "Abort" : "Commit") + " T" + clientTid);
                    }

                    else {
                        if (!inTxn) {
                            out.println("ERROR,NO_ACTIVE_TRANSACTION");
                            continue;
                        }
                        batchLines.add(line);
                    }
                }

            } catch (Exception e) {
                System.err.println("[ClientHandler] Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (!socket.isClosed()) socket.close();
                } catch (IOException ignored) {}
                System.out.println("[ClientHandler] Connection closed: " + socket.getRemoteSocketAddress());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 3035;
        int workers = 4;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) workers = Integer.parseInt(args[1]);

        TCPMiddleware mw = new TCPMiddleware(port, workers);
        mw.start();
    }

}
