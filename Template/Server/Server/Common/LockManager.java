package Server.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockManager {
    private final Map<String, Integer> locks = new HashMap<>(); //which resource is occupied by which tid
    private final Map<Integer, List<String>> transactionLocks = new HashMap<>(); //transaction id occupy multiple resources

    public synchronized boolean lock(int tid, String resource) {
        if (!locks.containsKey(resource)) {
            locks.put(resource, tid);
            transactionLocks.computeIfAbsent(tid, k -> new ArrayList<>()).add(resource);
            return true;
        }
        return locks.get(resource) == tid; // 已经持有锁
    }

    public synchronized void releaseLocks(int tid) {
        List<String> resources = transactionLocks.getOrDefault(tid, new ArrayList<>());
        for (String r : resources) {
            locks.remove(r);
        }
        transactionLocks.remove(tid);
    }
}

