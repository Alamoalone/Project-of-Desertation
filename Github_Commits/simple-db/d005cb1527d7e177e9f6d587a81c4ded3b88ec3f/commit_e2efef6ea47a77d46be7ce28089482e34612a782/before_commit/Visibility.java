package com.northeastern.edu.simpledb.backend.vm;

import com.northeastern.edu.simpledb.backend.tm.TransactionManager;

public class Visibility {

    public static boolean isVersionSkip(TransactionManager tm, Transaction t, Entry e) {
        long xmax = e.getXmax();
        if (t.level == 0) return false;
        else return tm.isCommitted(xmax) && (xmax > t.xid || t.isInSnapShot(xmax));
    }

    public static boolean isVisible(TransactionManager tm, Transaction t, Entry e) {
        if(t.level == 0) return readCommitted(tm, t, e);
        else return repeatableRead(tm, t, e);
    }

    // determine if record(e) is visible to transaction(t)
    private static boolean readCommitted(TransactionManager tm, Transaction t, Entry e) {
        long xid = t.xid;
        long xmin = e.getXmin();
        long xmax = e.getXmax();
        if (xmin == xid && xmax == 0) return true; // e had been created by the current transaction and wasn't deleted

        if (tm.isCommitted(xmin)) {
            if (xmax == 0) return true; // e had been committed and wasn't deleted
            return xmax != xid && !tm.isCommitted(xmax); // e had been deleted by other transaction but wasn't committed
        }
        return false;
    }

    private static boolean repeatableRead(TransactionManager tm, Transaction t, Entry e) {
        long xid = t.xid;
        long xmin = e.getXmin();
        long xmax = e.getXmax();
        if (xmin == xid && xmax == 0) return true; // e had been created by the current transaction and wasn't deleted

        if (tm.isCommitted(xmin) && xmin < xid && !t.isInSnapShot(xmin)) { // e was committed before when the current transaction created
            if (xmax == 0) return true; // not deleted so far
            if (xmax != xid) {
                return !tm.isCommitted(xmax) || xmax > xid || t.isInSnapShot(xmax); // e was deleted by the transaction created after the current transaction
            }
        }
        return false;
    }

}
