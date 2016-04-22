package fi.aalto.ssg.opentee.openteeandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;

public class OTConnectionService extends Service {
    String TAG = "OTConnectionService.Imp";
    String mQuote = "You Shall Not Pass!";
    static OTGuard mOTGuard = null; // only need one OTGuard.

    public OTConnectionService() {
        super();
        Log.d(TAG, "creating OTConnectionService");
        this.mOTGuard = new OTGuard(this.mQuote, this);
    }


    private final IOTConnectionInterface.Stub mBinder = new IOTConnectionInterface.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public synchronized int teecInitializeContext(String teeName) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to initialize context.");

            return mOTGuard.initializeContext(Binder.getCallingPid(), teeName);
        }

        @Override
        public synchronized void teecFinalizeContext() throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to finalize context.");

            mOTGuard.teecFinalizeContext(Binder.getCallingPid());
        }

        @Override
        public synchronized int teecRegisterSharedMemory(OTSharedMemory sharedMemory) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to register shared memory.");

            return mOTGuard.teecRegisterSharedMemory(Binder.getCallingPid(), sharedMemory);
            //return 0;
        }

        @Override
        public synchronized void teecReleaseSharedMemory(int smId){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to release shared memory with id:"
                    + smId);

            mOTGuard.teecReleaseSharedMemory(Binder.getCallingPid(),
                    smId);
        }

        @Override
        public synchronized int teecOpenSessionWithoutOp(int sid, ParcelUuid parcelUuid, int connMethod, int connData, int[] retOrigin) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to open session without operation.");

            return mOTGuard.teecOpenSession(Binder.getCallingPid(),
                    sid,
                    parcelUuid.getUuid(),
                    connMethod,
                    connData,
                    null,
                    retOrigin,
                    null);
        }

        @Override
        public synchronized int teecOpenSession(int sid, ParcelUuid parcelUuid, int connMethod, int connData, byte[] teecOperation, int[] retOrigin, ISyncOperation iSyncOperation) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to open session with operations.");

            return mOTGuard.teecOpenSession(Binder.getCallingPid(),
                    sid,
                    parcelUuid.getUuid(),
                    connMethod,
                    connData,
                    teecOperation,
                    retOrigin,
                    iSyncOperation);
        }

        @Override
        public synchronized void teecCloseSession(int sid){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to close session.");

            mOTGuard.teecCloseSession(Binder.getCallingPid(), sid);
        }

        @Override
        public synchronized int teecInvokeCommandWithoutOp(int sid, int commandId, int[] returnOrigin){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to invoke command without operation.");

            return mOTGuard.teecInvokeCommand(Binder.getCallingPid(),
                    sid,
                    commandId,
                    returnOrigin,
                    null,
                    null);
        }

        @Override
        public synchronized int teecInvokeCommand(int sid, int commandId, byte[] teecOperation, int[] returnOrigin, ISyncOperation syncOperation){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to invoke command with operations.");

            return mOTGuard.teecInvokeCommand(Binder.getCallingPid(),
                    sid,
                    commandId,
                    returnOrigin,
                    teecOperation,
                    syncOperation);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, intent.get)
        return mBinder;
    }
}
