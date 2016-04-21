package fi.aalto.ssg.opentee.imps;

/**
 * Lock for multi-threading.
 */
public class OTLock{
    private Object lock = new Object();
    private boolean unlockedBefore = false;

    public synchronized void lock(){
        if(unlockedBefore){
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
        synchronized (lock){
            lock.notify();

            unlockedBefore = true;
        }
    }
}
