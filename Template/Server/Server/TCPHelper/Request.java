package Server.TCPHelper;

import java.net.Socket;
import java.util.List;

/**
 * One Request OBJ contains
 * requestID used to be uniquely mapped : <requestId, clientSocket>
 * tid will be used in lock event
 * all the requests sent in one transaction
 * clientSocket
 */
public class Request {
    private final String requestId;
    private final int tid;
    private final List<String> requests;
    private final Socket clientSocket;
    public boolean abortFlag;

    public Request(String requestId, int tid, List<String> requests, Socket client, boolean abortFlag) {
        this.requestId = requestId;
        this.tid = tid;
        this.requests = requests;
        this.clientSocket = client;
        this.abortFlag = abortFlag;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getTid() {
        return tid;
    }

    public List<String> getRequests() {
        return requests;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}

