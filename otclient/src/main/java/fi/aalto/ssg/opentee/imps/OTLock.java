package fi.aalto.ssg.opentee.imps;

import android.util.Log;

/**
 * Lock for multi-threading.
 */
public class OTLock{
    final String TAG = "OTLock";
    private Object lock = new Object();
    private boolean locked = false;

    public synchronized void lock(){
        Log.i(TAG, "lock");

        try {
            while(locked) {
                synchronized (lock) {
                    Log.d(TAG, "wating to get lock...");

                    lock.wait();
                }
            }
            locked = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void unlock(){
        Log.i(TAG, "unlock");

        synchronized (lock){
            locked = false;
            lock.notify();
        }
    }
}
