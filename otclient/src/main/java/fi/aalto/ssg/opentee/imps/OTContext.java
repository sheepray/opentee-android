package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This class implements the IContext interface
 */
public class OTContext implements ITEEClient.IContext {
    String TAG = "OTContext";

    String mTeeName;
    boolean mInitialized;
    ProxyApis mProxyApis = null; // one service connection per context

    public OTContext(String teeName, Context context) throws ITEEClient.ClientException, RemoteException {
        this.mTeeName = teeName;

        //connect to the OpenTEE
        // try to lock here.
        Object lock = new Object();

        ServiceGetterThread serviceGetterThread = new ServiceGetterThread(teeName, context, lock);
        serviceGetterThread.run();

        synchronized (lock) {
            // wait until service connected.
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.mInitialized = true;
        mProxyApis = serviceGetterThread.getProxyApis();

        Log.d(TAG, "Service connected.");
    }


    @Override
    public void finalizeContext() {

    }

    @Override
    public ISharedMemory registerSharedMemory(byte[] buffer, ISharedMemory.Flag flags) throws ITEEClient.ClientException {
        return null;
    }

    @Override
    public void releaseSharedMemory(ISharedMemory sharedMemory) throws ITEEClient.ClientException {

    }

    @Override
    public ISession openSession(UUID uuid, ConnectionMethod connectionMethod, Integer connectionData, ITEEClient.Operation teecOperation) throws ITEEClient.ClientException {
        return null;
    }

    @Override
    public void requestCancellation(ITEEClient.Operation teecOperation) {

    }
}
