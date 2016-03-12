package fi.aalto.ssg.opentee.imps;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ITEEClient;

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

    public static void throwExceptionBasedOnReturnCode(int return_code) throws ITEEClient.Exception{

        if ( return_code == OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT ){
            throw new ITEEClient.AccessConflictException("Access conflict.");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_ACCESS_DENIED ){
            throw new ITEEClient.AccessConflictException("Access denied.");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_BAD_FORMAT ){
            throw new ITEEClient.AccessConflictException("Bad format");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_BAD_PARAMETERS ){
            throw new ITEEClient.AccessConflictException("Bad parameters.");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_BAD_STATE ){
            throw new ITEEClient.AccessConflictException("Bad state");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_BUSY ){
            throw new ITEEClient.AccessConflictException("Busy");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_CANCEL ){
            throw new ITEEClient.AccessConflictException("Cancel");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_COMMUNICATION ){
            throw new ITEEClient.AccessConflictException("Communication error");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_EXCESS_DATA ){
            throw new ITEEClient.AccessConflictException("Excess data");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_GENERIC ){
            throw new ITEEClient.AccessConflictException("Generic error");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND ){
            throw new ITEEClient.AccessConflictException("Item not found");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_NO_DATA ){
            throw new ITEEClient.AccessConflictException("Not data provided");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED ){
            throw new ITEEClient.AccessConflictException("Not impelemented");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_NOT_SUPPORTED ){
            throw new ITEEClient.AccessConflictException("Not supported");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY ){
            throw new ITEEClient.AccessConflictException("Out of memory");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_SECURITY ){
            throw new ITEEClient.AccessConflictException("Security check failed");
        }
        else if ( return_code == OTReturnCode.TEEC_ERROR_SHORT_BUFFER ){
            throw new ITEEClient.AccessConflictException("Short buffer");
        }
        else{
            throw new ITEEClient.Exception("Unknown error");
        }

    }

    public boolean getConnected(){ return this.mConnected; }
}
