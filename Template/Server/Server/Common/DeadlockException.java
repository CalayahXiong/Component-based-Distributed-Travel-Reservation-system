package Server.Common;

public class DeadlockException extends Exception{
    private final int tid;
    private final String resource;

    public DeadlockException(int tid, String resource, String message) {
        super(message);
        this.tid = tid;
        this.resource = resource;
    }

    public int getTid() {
        return tid;
    }

    public String getResource() {
        return resource;
    }
}
