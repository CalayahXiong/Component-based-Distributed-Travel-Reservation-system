package Server.Common;

import java.rmi.RemoteException;
import java.util.*;

public class LockManager {
    public enum LockType { READ, WRITE }

    // resource -> (tid -> lockType)
    private final Map<String, Map<Integer, LockType>> locks = new HashMap<>();
    // tid -> set of resources
    private final Map<Integer, Set<String>> transactionLocks = new HashMap<>();

    /**
     * Try to acquire a lock on a resource for a transaction.
     * @param tid transaction id
     * @param resource resource key
     * @param type requested lock type
     * @return true if lock granted, false if conflict
     */
    public synchronized boolean lock(int tid, String resource, LockType type) throws DeadlockException {
        locks.putIfAbsent(resource, new HashMap<>());
        Map<Integer, LockType> holders = locks.get(resource);

        LockType current = holders.get(tid);

        if (current != null) {
            // already holds a lock
            if (current == LockType.WRITE) {
                return true; // already strongest
            }
            if (current == LockType.READ) {
                if (type == LockType.READ) {
                    return true; // still compatible
                }
                if (type == LockType.WRITE) {
                    if (holders.size() == 1) { // upgrade
                        holders.put(tid, LockType.WRITE);
                        return true;
                    }
                    return false;
                }
            }
        } else {
            // no existing lock, request new
            if (type == LockType.READ) {
                boolean hasWrite = holders.values().stream().anyMatch(t -> t == LockType.WRITE);
                if (!hasWrite) {
                    holders.put(tid, LockType.READ);
                    transactionLocks.computeIfAbsent(tid, k -> new HashSet<>()).add(resource);
                    return true;
                }
                return false;
            }
            if (type == LockType.WRITE) {
                boolean onlyThisTid = holders.isEmpty();
                if (onlyThisTid) {
                    holders.put(tid, LockType.WRITE);
                    transactionLocks.computeIfAbsent(tid, k -> new HashSet<>()).add(resource);
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Release all locks held by a transaction.
     */
    public synchronized void releaseLocks(int tid) throws RemoteException {
        try {
            // remove tid from all resources
            for (Map<Integer, LockType> holders : locks.values()) {
                holders.remove(tid);
            }
            // clear record of this transaction
            transactionLocks.remove(tid);
            System.out.println("LockManager::releaseLocks released all locks for T" + tid);
        } catch (Exception e) {
            throw new RemoteException("LockManager::releaseLocks failed for T" + tid, e);
        }
    }

}
