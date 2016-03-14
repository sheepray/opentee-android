package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    Random smIdGenerator;

    List<OTSharedMemory> mSharedMemory = new ArrayList<OTSharedMemory>();

    public OTContext(String teeName, Context context) throws ITEEClient.Exception, RemoteException {
        this.mTeeName = teeName;
        this.smIdGenerator = new Random();

        /**
         * connect to the OpenTEE
         */
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
    public void finalizeContext() throws RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Nothing to finalize");
            return;
        }

        if ( mInitialized ) mInitialized = false;
        if ( mProxyApis != null ){
            mProxyApis.teecFinalizeContext();
            mProxyApis.terminateConnection();
        }

        Log.i(TAG, "context finalized and connection terminated");
    }

    @Override
    public ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws ITEEClient.Exception, RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to register shared memory");
            return null;
        }

        int smId = generateSmId();

        // create a shared memory
        OTSharedMemory otSharedMemory = new OTSharedMemory(buffer, flags, smId);

        // register the shared memory
        mProxyApis.teecRegisterSharedMemory(otSharedMemory);

        // add the registered shared memory to mSharedMemory list.
        mSharedMemory.add(otSharedMemory);

        return otSharedMemory;
    }

    @Override
    public void releaseSharedMemory(ISharedMemory sharedMemory) throws ITEEClient.Exception, RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to release shared memory");
            return;
        }

        // tell remote tee to release the shared memory.
        mProxyApis.teecReleaseSharedMemory(sharedMemory.getId());

        // remove it from shared memory list.
        mSharedMemory.remove(sharedMemory);
    }

    @Override
    public ISession openSession(UUID uuid, ConnectionMethod connectionMethod, Integer connectionData, ITEEClient.Operation teecOperation) throws ITEEClient.Exception {
        return null;
    }

    @Override
    public void requestCancellation(ITEEClient.Operation teecOperation) {

    }

    private int generateSmId(){
        int id = smIdGenerator.nextInt(500);
        Log.i(TAG, "generating shared memory id:" + id);
        return id;
    }
}
