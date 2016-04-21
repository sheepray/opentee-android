package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;
import android.util.Log;

import java.util.UUID;

import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * Created by yangr1 on 4/20/16.
 */
public class OpenSessionThread implements Runnable {
    final String TAG = "OpenSessionThread";
    ProxyApis mProxyApis;
    int mSid;
    UUID mUuid;
    ITEEClient.IContext.ConnectionMethod mConnectionMethod;
    int mConnectionData;
    byte[] mTeecOperation;
    byte[] mNewTeecOperation;
    OTLock mLock;

    private ISyncOperation mSyncOperationCallBack = new ISyncOperation.Stub(){
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void syncOperation(byte[] teecOperationInBytes) throws RemoteException {
            Log.e(TAG, "sync op called");

            mNewTeecOperation = teecOperationInBytes;

            try {
                mLock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    @Override
    public void run() {
        try {
            mProxyApis.teecOpenSession(mSid,
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
