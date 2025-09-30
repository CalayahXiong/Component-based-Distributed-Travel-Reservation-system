package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionManager {

    public enum TransactionStatus {
        ACTIVE, //ongoing
        PREPARED, //all RMs are ready
        COMMITTED, //transaction is committed
        ABORTED, //RMs rollback
        UNKNOWN //illegal
    }

    private AtomicInteger nextTid = new AtomicInteger(1);
    private Map<Integer, TransactionStatus> transactions = new ConcurrentHashMap<>();

    public TransactionStatus getStatus(int tid) {
        return transactions.getOrDefault(tid, TransactionStatus.UNKNOWN);
    }

    public int start() {
        int tid = nextTid.getAndIncrement();
        transactions.put(tid, TransactionStatus.ACTIVE);
        return tid;
    }

    public boolean commit(int tid, List<IResourceManager> rms) {
        System.out.println("[TM] Commit request for T" + tid);

        boolean allPrepared = true;

        // --- Phase 1: Prepare ---
        for (IResourceManager rm : rms) {
            try {
                boolean ok = rm.prepare(tid);
                if (!ok) {
                    allPrepared = false;
                    break;
                }
            } catch (RemoteException e) {
                System.err.println("[TM] RemoteException in prepare, T" + tid + ": " + e.getMessage());
                allPrepared = false;
                break;
            }
        }

        // --- Phase 2: Commit or Abort ---
        if (allPrepared) {
            transactions.put(tid, TransactionStatus.PREPARED);
            for (IResourceManager rm : rms) {
                try {
                    rm.commit(tid);
                } catch (RemoteException e) {
                    System.err.println("[TM] RemoteException in commit, T" + tid + ": " + e.getMessage());
                }
            }
            transactions.put(tid, TransactionStatus.COMMITTED);
            System.out.println("[TM] Transaction " + tid + " COMMITTED");
        } else {
            for (IResourceManager rm : rms) {
                try {
                    rm.abort(tid);
                } catch (RemoteException e) {
                    System.err.println("[TM] RemoteException in abort, T" + tid + ": " + e.getMessage());
                }
            }
            transactions.put(tid, TransactionStatus.ABORTED);
            System.out.println("[TM] Transaction " + tid + " ABORTED");
        }
        return allPrepared;
    }

    public boolean abort(int tid, List<IResourceManager> rms) {
        System.out.println("[TM] Abort request for T" + tid);

        TransactionStatus status = transactions.get(tid);
        if (status == TransactionStatus.ABORTED || status == TransactionStatus.COMMITTED) {
            System.out.println("[TM] Transaction " + tid + " already finished with status " + status);
            return true;
        }

        // notify all RM to abort
        for (IResourceManager rm : rms) {
            try {
                rm.abort(tid);
            } catch (RemoteException e) {
                System.err.println("[TM] RemoteException in abort, T" + tid + " at " + rm + ": " + e.getMessage());
            }
        }

        transactions.put(tid, TransactionStatus.ABORTED);
        System.out.println("[TM] Transaction " + tid + " marked as ABORTED");
        return true;
    }


}
