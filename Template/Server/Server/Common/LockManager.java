package Server.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockManager {
    public enum LockType { READ, WRITE }; //read left for future
    private final Map<String, Integer> locks = new HashMap<>(); //which resource is occupied by which tid
    private final Map<Integer, List<String>> transactionLocks = new HashMap<>(); //transaction id occupy multiple resources

    /**
     * Given a transaction ID, a resource, means this transaction needs this resource.
     * This function is asking to lock this resource for transaction ID.
     * If this resource is locked by another transaction, return False -> locking request failed. So this transaction failed.
     * @param tid
     * @param resource
     * @return
     */
    public synchronized boolean lock(int tid, String resource) {
        if (!locks.containsKey(resource)) {
            locks.put(resource, tid);
            transactionLocks.computeIfAbsent(tid, k -> new ArrayList<>()).add(resource);
            return true;
        }
        return locks.get(resource) == tid;
    }

    /**
     * Transaction id is done, the resources it occupied before should all be unlocked.
     * @param tid
     */
    public synchronized void releaseLocks(int tid) {
        List<String> resources = transactionLocks.getOrDefault(tid, new ArrayList<>());
        for (String r : resources) {
            locks.remove(r);
        }
        transactionLocks.remove(tid);
    }

    /**
     * return the transaction id that is occupying this resource right now.
     * @param resource
     * @return
     */
    public synchronized Integer getOwner(String resource){
        return locks.get(resource);
    }
}

