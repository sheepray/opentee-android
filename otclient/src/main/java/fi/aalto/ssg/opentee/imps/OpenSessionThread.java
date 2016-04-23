package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;
import android.util.Log;

import java.util.UUID;

import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Created by yangr1 on 4/20/16.
 */
public class OpenSessionThread implements Runnable {
    final String TAG = "OpenSessionThread";
    ProxyApis mProxyApis = null;
    int mSid;
    UUID mUuid;
    ITEEClient.IContext.ConnectionMethod mConnectionMethod;
    int mConnectionData;
    byte[] mTeecOperation = null;
    byte[] mNewTeecOperation = null;
    OTLock mLock = null;
    ReturnValueWrapper mReturnValue = null;

    private ISyncOperation mSyncOperationCallBack = new ISyncOperation.Stub(){
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void syncOperation(byte[] teecOperationInBytes) throws RemoteException {
            Log.e(TAG, "sync op called in thread with id " + Thread.currentThread().getId());

            mNewTeecOperation = teecOperationInBytes;

            if(mLock != null) mLock.unlock();
            else{
                Log.e(TAG, "[internal error] lock is null");
            }
        }
    };

    public OpenSessionThread(ProxyApis proxyApis,
                             int sid,
                             UUID uuid,
                             ITEEClient.IContext.ConnectionMethod connectionMethod,
                             int connectionData,
                             byte[] teecOperation,
                             OTLock lock){
        this.mProxyApis = proxyApis;
        this.mSid = sid;
        this.mUuid = uuid;
        this.mConnectionMethod = connectionMethod;
        this.mConnectionData = connectionData;
        this.mTeecOperation = teecOperation;
        this.mLock = lock;
    }

    public synchronized byte[] getNewOperationInBytes(){
        return this.mNewTeecOperation;
    }

    public synchronized ReturnValueWrapper getReturnValue(){ return this.mReturnValue; }

    @Override
    public void run() {
        if (mLock != null) mLock.lock();

        try {
            mReturnValue = mProxyApis.teecOpenSession(mSid,
                    mUuid,
                    mConnectionMethod,
                    mConnectionData,
                    mTeecOperation,
                    mSyncOperationCallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Communication error with remote TEE service.");
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
    }
}
