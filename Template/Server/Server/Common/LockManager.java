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
    public synchronized boolean lock(int tid, String resource, LockType type) throws DeadlockException{
        try {
            locks.putIfAbsent(resource, new HashMap<>());
            Map<Integer, LockType> holders = locks.get(resource);

            // Case 1: resource not locked
            if (holders.isEmpty()) {
                holders.put(tid, type);
                transactionLocks.computeIfAbsent(tid, k -> new HashSet<>()).add(resource);
                return true;
            }

            // Case 2: transaction already holds a lock
            if (holders.containsKey(tid)) {
                LockType current = holders.get(tid);

                // Already has WRITE
                if (current == LockType.WRITE) {
                    return true; // nothing to change
                }

                // Already has READ
                if (current == LockType.READ && type == LockType.READ) {
                    return true; // reentrant read
                }

                // Upgrade READ -> WRITE (only if no other holders)
                if (current == LockType.READ && type == LockType.WRITE) {
                    if (holders.size() == 1) {
                        holders.put(tid, LockType.WRITE);
                        return true;
                    } else {
                        return false; // other readers exist
                    }
                }
            }

            // Case 3: other transactions hold locks
            if (type == LockType.READ) {
                // allow if no WRITE locks exist
                boolean hasWrite = holders.values().stream().anyMatch(t -> t == LockType.WRITE);
                if (!hasWrite) {
                    holders.put(tid, LockType.READ);
                    transactionLocks.computeIfAbsent(tid, k -> new HashSet<>()).add(resource);
                    return true;
                }
            } else if (type == LockType.WRITE) {
                // cannot grant if any other holders exist
                return false;
            }

            return false;
        } catch (Exception e) {
            System.err.println("LockManager::lock failed for T" + tid + " on " + resource + " -> " + e.getMessage());
            releaseLocksSafe(tid);
            return false;
        }
    }

    /**
     * Release all locks held by a transaction.
     */
    public synchronized void releaseLocks(int tid) throws RemoteException {
        try {
            releaseLocksSafe(tid);
        } catch (Exception e) {
            throw new RemoteException("LockManager::releaseLocks failed for T" + tid, e);
        }
    }

    private void releaseLocksSafe(int tid) {
        // remove tid from all resources
        for (Map<Integer, LockType> holders : locks.values()) {
            holders.remove(tid);
        }
        // clear record of this transaction
        transactionLocks.remove(tid);
        System.out.println("LockManager::releaseLocks released all locks for T" + tid);
    }
}
