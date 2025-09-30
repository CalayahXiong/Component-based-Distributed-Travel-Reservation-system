package Server.TCPHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

 /**
 * RMConnection: manages a long-lived socket to each RM
 */
public class RMConnection {
    private final String rmName; //TCPFlightServer
    private final String host; //"tr-open-01.cs.mcgill.ca"
    private final int port; //5001

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // timeouts in ms
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int SO_TIMEOUT = 3000;

    public RMConnection(String rmName, String host, int port) {
        this.rmName = rmName;
        this.host = host;
        this.port = port;
    }

    synchronized void ensureConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) return;
        reconnect();
    }

    synchronized void reconnect() throws IOException {
        closeQuiet();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
        socket.setSoTimeout(SO_TIMEOUT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected RM " + rmName + "@" + host + ":" + port);
    }

    public synchronized String send(String msg, int tid) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(tid + "," + msg);
            return in.readLine();

        } catch (IOException e) {
            System.err.println("[RMConn] Error sending to " + rmName + ": " + e.getMessage());
            return "ERROR";
        }
    }

    synchronized void closeQuiet() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null; in = null; out = null;
    }
}