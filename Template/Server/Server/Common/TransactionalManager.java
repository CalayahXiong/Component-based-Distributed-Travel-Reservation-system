package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionalManager {

    public enum TransactionStatus {
        ACTIVE, PREPARED, COMMITTED, ABORTED
    }

    private AtomicInteger nextTid = new AtomicInteger(1);
    private Map<Integer, TransactionStatus> transactions = new ConcurrentHashMap<>();

    public int startTransaction() {
        int tid = nextTid.getAndIncrement();
        transactions.put(tid, TransactionStatus.ACTIVE);
        return tid;
    }

    public void commitTransaction(int tid, List<IResourceManager> rms) throws RemoteException {
        // 2PC: phase 1
        boolean allPrepared = true;
        for (IResourceManager rm : rms) {
            if (!rm.prepare(tid)) {
                allPrepared = false;
                break;
            }
        }
        // 2PC: phase 2
        if (allPrepared) {
            for (IResourceManager rm : rms) {
                rm.commit(tid);
            }
            transactions.put(tid, TransactionStatus.COMMITTED);
        } else {
            for (IResourceManager rm : rms) {
                rm.abort(tid);
            }
            transactions.put(tid, TransactionStatus.ABORTED);
        }
    }
}
