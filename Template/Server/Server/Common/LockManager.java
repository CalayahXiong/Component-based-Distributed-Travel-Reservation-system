package Server.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockManager {
    public enum LockType { READ, WRITE } //read left for future
    private final Map<String, List<Lock>> locks = new HashMap<>(); //
    private final Map<Integer, List<String>> transactionLocks = new HashMap<>(); // TID -> R

    private static class Lock {
        int tid;
        LockType type;

        Lock(int tid, LockType type) {
            this.tid = tid;
            this.type = type;
        }
    }

    /**
     * Given a transaction ID, a resource, means this transaction needs this resource.
     * This function is asking to lock this resource for transaction ID.
     * If this resource is locked by another transaction, return False -> locking request failed. So this transaction failed.
     * @param tid
     * @param resource
     * @return
     */
    public synchronized boolean lock(int tid, String resource, LockType type) throws DeadlockException{
        List<Lock> currentLocks = locks.computeIfAbsent(resource, k -> new ArrayList<>());

        // no lock, can be locked directly
        if (currentLocks.isEmpty()) {
            currentLocks.add(new Lock(tid, type));
            transactionLocks.computeIfAbsent(tid, k -> new ArrayList<>()).add(resource);
            return true;
        }

        // locked but is read request
        if (type == LockType.READ) {
            // they are all read type
            if (currentLocks.stream().allMatch(l -> l.type == LockType.READ)) {
                currentLocks.add(new Lock(tid, type));
                transactionLocks.computeIfAbsent(tid, k -> new ArrayList<>()).add(resource);
                return true;
            }
            // there is a write lock, then has to wait
            return false;
        }

        // locked and is write request
        if (type == LockType.WRITE) {
            // locked but the reading owner is itself
            // cannot upgrade the read lock to write lock when there are more than 1 reading lock, cuz it will cause conflicts in reading content
            if (currentLocks.size() == 1 && currentLocks.get(0).tid == tid) {
                currentLocks.get(0).type = LockType.WRITE;
                return true;
            }
            //
            return false;
        }

        return false;
    }

    /**
     * Transaction id is done, the resources it occupied before should all be unlocked.
     * @param tid
     */
    public synchronized void releaseLocks(int tid) {
        List<String> resources = transactionLocks.remove(tid);
        if (resources != null) {
            for (String res : resources) {
                locks.get(res).removeIf(l -> l.tid == tid);
                if (locks.get(res).isEmpty()) {
                    locks.remove(res);
                }
            }
        }
        //Trace.info("RM::release(" + tid + ") locks");
        System.out.println("Release occurred, locks for: " + tid);
    }

}

