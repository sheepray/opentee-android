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
            teecInitializeContext();

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

    public ProxyApis teecInitializeContext() {
        int return_code = 0;
        try {
            return_code = mService.teecInitializeContext(mTeeName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        /*
            if ( return_code != TeecResult.TEEC_SUCCESS){
                throw new TeecConnectionException(Integer.toString(return_code));
            }
        */

        Log.d(TAG, "Return code from OT: " + Integer.toHexString(return_code));

        return this;
    }

    public void teecFinalizeContext() throws RemoteException {
        if ( mService != null ){
            mService.teecFinalizeContext();
        }
    }

    public void teecRegisterSharedMemory(OTSharedMemory otSharedMemory) throws ITEEClient.GenericErrorException, RemoteException {
        if ( mService == null ){
            throw new ITEEClient.GenericErrorException("Service unavailable");
        }

        // call IPC
        mService.teecRegisterSharedMemory(otSharedMemory);
        // based on the return code, throw different exceptions if not succeed.

    }

    public boolean getConnected(){ return this.mConnected; }
}
