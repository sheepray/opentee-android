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

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.AccessConflictException;
import fi.aalto.ssg.opentee.exception.AccessDeniedException;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.BadStateException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CancelErrorException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.ItemNotFoundException;
import fi.aalto.ssg.opentee.exception.NoDataException;
import fi.aalto.ssg.opentee.exception.NoStorageSpaceException;
import fi.aalto.ssg.opentee.exception.NotImplementedException;
import fi.aalto.ssg.opentee.exception.NotSupportedException;
import fi.aalto.ssg.opentee.exception.OutOfMemoryException;
import fi.aalto.ssg.opentee.exception.OverflowException;
import fi.aalto.ssg.opentee.exception.SecurityErrorException;
import fi.aalto.ssg.opentee.exception.ShortBufferException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.exception.TargetDeadException;
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
            } catch (TEEClientException e) {
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

    public ProxyApis teecInitializeContext() throws TEEClientException {
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

    public void teecRegisterSharedMemory(OTSharedMemory otSharedMemory) throws TEEClientException, RemoteException {
        if ( mService == null ){
            throw new GenericErrorException("Service unavailable");
        }

        if(otSharedMemory.asByteArray() == null){
            Log.e(TAG, "otshared memory is null");
            return;
        }else{
            Log.e(TAG, new String(otSharedMemory.asByteArray()));
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

    public void teecReleaseSharedMemory(int smId) throws GenericErrorException, RemoteException {
        if ( mService == null ){
            throw new GenericErrorException("Service unavailable");
        }

        mService.teecReleaseSharedMemory(smId);
    }

    public void teecOpenSession(int sessionId,
                               UUID uuid,
                               ITEEClient.IContext.ConnectionMethod connectionMethod,
                               int connectionData,
                               byte[] opInArray) throws TEEClientException, RemoteException {
        if ( mService == null ){
            throw new GenericErrorException("Service unavailable");
        }

        //ByteArrayWrapper baw = new ByteArrayWrapper(opInArray);

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

            //with byte array wrapper
            /*
            rc = mService.teecOpenSessionWithByteArrayWrapper(sessionId,
                    new ParcelUuid(uuid),
                    connectionMethod.ordinal(),
                    connectionData,
                    baw,
                    retOrigin);
                    */
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

    public void teecCloseSession(int sessionId) throws RemoteException {
        //TODO:
        mService.teecCloseSession(sessionId);
    }

    //note: switch statement can also apply in here.
    public static void throwExceptionBasedOnReturnCode(int return_code) throws TEEClientException{
        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new AccessConflictException("Access conflict.");
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new AccessDeniedException("Access denied.");
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new BadFormatException("Bad format");
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new BadParametersException("Bad parameters.");
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new BadStateException("Bad state");
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new BusyException("Busy");
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new CancelErrorException("Cancel");
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new CommunicationErrorException("Communication error");
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ExcessDataException("Excess data");
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new GenericErrorException("Generic error");
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ItemNotFoundException("Item not found");
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new NoDataException("Not data provided");
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new NotImplementedException("Not impelemented");
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new NotSupportedException("Not supported");
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new OutOfMemoryException("Out of memory");
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new SecurityErrorException("Security check failed");
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ShortBufferException("Short buffer");
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ExternalCancelException("External cancel");
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new OverflowException("Overflow");
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new TargetDeadException("TEE: target dead");
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new NoStorageSpaceException("Storage no space");
            default:
                break;
                //throw new TEEClientException("Unknown error");
        }

    }

    public static void throwExceptionWithReturnOrigin(int return_code, int retOrigin) throws TEEClientException{
        ITEEClient.ReturnOriginCode returnOriginCode = intToReturnOrigin(retOrigin);

        if ( returnOriginCode == null ){
            Log.e(TAG, "Incorrect return origin " + retOrigin);
        }

        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new AccessConflictException("Access conflict.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new AccessDeniedException("Access denied.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new BadFormatException("Bad format", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new BadParametersException("Bad parameters.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new BadStateException("Bad state", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new BusyException("Busy", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new CancelErrorException("Cancel", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new CommunicationErrorException("Communication error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ExcessDataException("Excess data", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new GenericErrorException("Generic error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ItemNotFoundException("Item not found", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new NoDataException("Not data provided", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new NotImplementedException("Not impelemented", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new NotSupportedException("Not supported", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new OutOfMemoryException("Out of memory", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new SecurityErrorException("Security check failed", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ShortBufferException("Short buffer", returnOriginCode);
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ExternalCancelException("External cancel", returnOriginCode);
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new OverflowException("Overflow", returnOriginCode);
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new TargetDeadException("TEE: target dead", returnOriginCode);
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new NoStorageSpaceException("Storage no space", returnOriginCode);
            default:
                break;
                //throw new TEEClientException("Unknown error", returnOriginCode);
        }
    }

    public boolean getConnected(){ return this.mConnected; }

    private static ITEEClient.ReturnOriginCode intToReturnOrigin(int roInt){
        int len = ITEEClient.ReturnOriginCode.values().length;
        if( len <= roInt || roInt <= 0) return null;

        return ITEEClient.ReturnOriginCode.values()[roInt - 1];
    }
}
