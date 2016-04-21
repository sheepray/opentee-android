package fi.aalto.ssg.opentee.imps;

/**
 * Lock for multi-threading.
 */
public class OTLock {
    private boolean isLocked = false;

    public synchronized void lock() throws InterruptedException{
        while(isLocked){
            wait();
        }
        isLocked = true;
    }

    public synchronized void unlock() throws InterruptedException{
        isLocked = false;
        notify();
    }
}
