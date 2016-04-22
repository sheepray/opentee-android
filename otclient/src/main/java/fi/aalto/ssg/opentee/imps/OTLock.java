package fi.aalto.ssg.opentee.imps;

import android.util.Log;

/**
 * Lock for multi-threading.
 */
public class OTLock{
    final String TAG = "OTLock";
    private Object lock = new Object();
    private boolean unlockedBefore = false;

    public synchronized void lock(){
        Log.i(TAG, "lock");

        if(unlockedBefore){
            Log.i(TAG, "unlockedBefore, so won't lock again.");

            unlockedBefore = false;
            return;
        }

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void unlock(){
        Log.i(TAG, "unlock");

        synchronized (lock){
            unlockedBefore = true;

            lock.notify();
        }
    }
}
