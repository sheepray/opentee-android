package fi.aalto.ssg.opentee.imps;

import android.util.Log;

/**
 * Lock for multi-threading.
 */
public class OTLock{
    final String TAG = "OTLock";
    private boolean locked = false;

    public synchronized void lock(){
        Log.i(TAG, "lock in " + Thread.currentThread().getId());

        try {
            while(locked) {
                Log.d(TAG, "waiting to get lock...");
                wait();
            }
            locked = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void unlock(){
        Log.i(TAG, "unlock" + Thread.currentThread().getId());

        locked = false;
        notify();
    }
}
