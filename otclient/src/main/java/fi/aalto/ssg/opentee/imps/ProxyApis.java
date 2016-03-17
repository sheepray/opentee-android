package fi.aalto.ssg.opentee.imps;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.ShortBufferException;

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * This class handles the communication with the service on behalf of the Client Application.
 * Be aware that the mContextCApi is corresponding to the context in C API while the Context instance is
 * also called context.
 */
public class ProxyApis {
    static String TAG = "ProxyApis";

    Context mContext;
    boolean mConnected;
    String mTeeName;
    IOTConnectionInterface mService;
    Object mLock;

    public ProxyApis(String teeName, Context context, Object lock){
        this.mContext = context;
        this.mTeeName = teeName;
        this.mLock = lock;

        mConnected = false;
        mService = null;

        initConnection();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected");

            mConnected = true;
            mService = IOTConnectionInterface.Stub.asInterface(service);

            //after connected, call initializeContext.
            try {
                teecInitializeContext();
            } catch (ITEEClient.Exception e) {
                e.printStackTrace();
            }

            synchronized (mLock) {
                // release the lock from another thread.
                mLock.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected");

            mConnected = false;
            mService = null;
        }
    };

    private void initConnection(){
        if ( !mConnected ){
            Intent intent = new Intent();
            intent.setClassName(TeecConstants.OT_SERVICE_PACK_NAME,
                    TeecConstants.OT_SERVICE_CLASS_NAME);
            this.mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }

        Log.d(TAG, "Trying to create connection");
    }

    public void terminateConnection(){
        if ( mService != null ){
            this.mContext.unbindService(mServiceConnection);
        }
    }

    public ProxyApis teecInitializeContext() throws ITEEClient.Exception {
        int return_code = 0;
        try {
            return_code = mService.teecInitializeContext(mTeeName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Return code from OT: " + Integer.toHexString(return_code));

        if ( return_code != OTReturnCode.TEEC_SUCCESS ){
            throwExceptionBasedOnReturnCode(return_code);
        }

        return this;
    }

    public void teecFinalizeContext() throws RemoteException {
        if ( mService != null ){
            mService.teecFinalizeContext();
        }
    }

    public void teecRegisterSharedMemory(OTSharedMemory otSharedMemory) throws ITEEClient.Exception, RemoteException {
        if ( mService == null ){
            throw new ITEEClient.GenericErrorException("Service unavailable");
        }

        // call IPC
        int return_code = mService.teecRegisterSharedMemory(otSharedMemory);

        Log.d(TAG, "teecRegisterSharedMemory return code: " + return_code);

        if ( return_code == OTReturnCode.TEEC_SUCCESS ){
            return;
        }
        else{
            throwExceptionBasedOnReturnCode(return_code);
        }

    }

    public void teecReleaseSharedMemory(int smId) throws ITEEClient.GenericErrorException, RemoteException {
        if ( mService == null ){
            throw new ITEEClient.GenericErrorException("Service unavailable");
        }

        mService.teecReleaseSharedMemory(smId);
    }

    public void teecOpenSession(int sessionId,
                               UUID uuid,
                               ITEEClient.IContext.ConnectionMethod connectionMethod,
                               int connectionData,
                               byte[] opInArray) throws ITEEClient.Exception, RemoteException {
        if ( mService == null ){
            throw new ITEEClient.GenericErrorException("Service unavailable");
        }

        /**
         * IPC open session call.
         */
        int rc;
        int[] retOrigin = new int[1];
        if (opInArray == null){
            rc = mService.teecOpenSessionWithoutOp(sessionId,
                    new ParcelUuid(uuid),
                    connectionMethod.ordinal(),
                    connectionData,
                    retOrigin);
        }else{
            rc = mService.teecOpenSession(sessionId,
                    new ParcelUuid(uuid),
                    connectionMethod.ordinal(),
                    connectionData,
                    opInArray,
                    retOrigin);
        }

        Log.d(TAG, "teecOpenSession return code: " + rc);

        /**
         * dealing with return code.
         */
        if ( rc != OTReturnCode.TEEC_SUCCESS ){
            // throw exceptions with return origin.
            throwExceptionWithReturnOrigin(rc, retOrigin[0]);
        }
    }

    //note: switch statement can also apply in here.
    public static void throwExceptionBasedOnReturnCode(int return_code) throws ITEEClient.Exception{
        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new ITEEClient.AccessConflictException("Access conflict.");
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new ITEEClient.AccessDeniedException("Access denied.");
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new ITEEClient.BadFormatException("Bad format");
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new ITEEClient.BadParametersException("Bad parameters.");
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new ITEEClient.BadStateException("Bad state");
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new ITEEClient.BusyException("Busy");
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new ITEEClient.CancelErrorException("Cancel");
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new ITEEClient.CommunicationErrorException("Communication error");
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ITEEClient.ExcessDataException("Excess data");
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new ITEEClient.GenericErrorException("Generic error");
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ITEEClient.ItemNotFoundException("Item not found");
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new ITEEClient.NoDataException("Not data provided");
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new ITEEClient.NotImplementedException("Not impelemented");
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new ITEEClient.NotSupportedException("Not supported");
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new ITEEClient.OutOfMemoryException("Out of memory");
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new ITEEClient.SecurityErrorException("Security check failed");
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ITEEClient.ShortBufferException("Short buffer");
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ITEEClient.ExternalCancelException("External cancel");
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new ITEEClient.OverflowException("Overflow");
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new ITEEClient.TargetDeadException("TEE: target dead");
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new ITEEClient.NoStorageSpaceException("Storage no space");
            default:
                throw new ITEEClient.Exception("Unknown error");
        }

    }

    public static void throwExceptionWithReturnOrigin(int return_code, int retOrigin) throws ITEEClient.Exception{
        ITEEClient.ReturnOriginCode returnOriginCode = intToReturnOrigin(retOrigin);

        if ( returnOriginCode == null ){
            Log.e(TAG, "Incorrect return origin " + retOrigin);
        }

        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new ITEEClient.AccessConflictException("Access conflict.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new ITEEClient.AccessDeniedException("Access denied.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new ITEEClient.BadFormatException("Bad format", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new ITEEClient.BadParametersException("Bad parameters.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new ITEEClient.BadStateException("Bad state", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new ITEEClient.BusyException("Busy", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new ITEEClient.CancelErrorException("Cancel", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new ITEEClient.CommunicationErrorException("Communication error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ITEEClient.ExcessDataException("Excess data", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new ITEEClient.GenericErrorException("Generic error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ITEEClient.ItemNotFoundException("Item not found", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new ITEEClient.NoDataException("Not data provided", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new ITEEClient.NotImplementedException("Not impelemented", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new ITEEClient.NotSupportedException("Not supported", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new ITEEClient.OutOfMemoryException("Out of memory", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new ITEEClient.SecurityErrorException("Security check failed", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ITEEClient.ShortBufferException("Short buffer", returnOriginCode);
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ITEEClient.ExternalCancelException("External cancel", returnOriginCode);
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new ITEEClient.OverflowException("Overflow", returnOriginCode);
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new ITEEClient.TargetDeadException("TEE: target dead", returnOriginCode);
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new ITEEClient.NoStorageSpaceException("Storage no space", returnOriginCode);
            default:
                throw new ITEEClient.Exception("Unknown error", returnOriginCode);
        }
    }

    public boolean getConnected(){ return this.mConnected; }

    private static ITEEClient.ReturnOriginCode intToReturnOrigin(int roInt){
        int len = ITEEClient.ReturnOriginCode.values().length;
        if( len <= roInt || roInt <= 0) return null;

        return ITEEClient.ReturnOriginCode.values()[roInt - 1];
    }
}
