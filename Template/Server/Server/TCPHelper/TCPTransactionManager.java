package Server.TCPHelper;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCPTransactionManager:
 * 1. Allocating tid, maintaining transaction status (commit, abort...)
 * 2. Not participating in commit/abort of RMs
 */
public class TCPTransactionManager {

    public enum TransactionStatus {
        ACTIVE,
        PREPARED,
        COMMITTED,
        ABORTED,
        UNKNOWN
    }

    private final AtomicInteger tidGen = new AtomicInteger(1000);
    private final ConcurrentHashMap<Integer, Set<String>> participants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, TransactionStatus> states = new ConcurrentHashMap<>();


    public int start() {
        int tid = tidGen.getAndIncrement();
        participants.put(tid, ConcurrentHashMap.newKeySet());
        states.put(tid, TransactionStatus.ACTIVE);
        System.out.println("[TM] START T" + tid);
        return tid;
    }

    public TransactionStatus getStatus(int tid) {
        return states.getOrDefault(tid, TransactionStatus.UNKNOWN);
    }

    public void addParticipant(int tid, String rmName) {
        participants.computeIfAbsent(tid, k -> ConcurrentHashMap.newKeySet()).add(rmName);
        System.out.println("[TM] T" + tid + " add participant " + rmName);
    }

    public Set<String> getParticipants(int tid) {
        return participants.getOrDefault(tid, Collections.emptySet());
    }

    public boolean commit(int tid) {
        TransactionStatus status = getStatus(tid);
        if (status != TransactionStatus.ACTIVE && status != TransactionStatus.PREPARED) {
            System.out.println("[TM] Commit ignored, T" + tid + " is already " + status);
            return false;
        }

        states.put(tid, TransactionStatus.COMMITTED);
        Set<String> rms = participants.remove(tid);
        System.out.println("[TM] COMMIT T" + tid + " participants=" + rms);
        return true;
    }

    public boolean abort(int tid) {
        TransactionStatus status = getStatus(tid);
        if (status == TransactionStatus.COMMITTED || status == TransactionStatus.ABORTED) {
            System.out.println("[TM] Abort ignored, T" + tid + " already " + status);
            return false;
        }

        states.put(tid, TransactionStatus.ABORTED);
        Set<String> rms = participants.remove(tid);
        System.out.println("[TM] ABORT T" + tid + " participants=" + rms);
        return true;
    }


    public void printStatus(int tid) {
        System.out.println("[TM] T" + tid + " status=" + getStatus(tid)
                + " participants=" + getParticipants(tid));
    }
}
