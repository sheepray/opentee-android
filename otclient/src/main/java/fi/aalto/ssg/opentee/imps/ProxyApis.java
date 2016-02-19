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

    public ProxyApis(String teeName, Context context){
        this.mContext = context;
        this.mTeeName = teeName;

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

        Log.d(TAG, "Return code from OT: " + return_code);

        return this;
    }

    public boolean getConnected(){ return this.mConnected; }
}