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
 * Request cancellation task
 */
public class RequestCancellationTask implements Runnable {
    final String TAG = "RequestCancellationTask";

    OTOperation mOp;
    Context mContext;

    boolean mConnected;
    IOTConnectionInterface mService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected");

            mConnected = true;
            mService = IOTConnectionInterface.Stub.asInterface(service);

            task();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected");

            mConnected = false;
            mService = null;
        }
    };

    public RequestCancellationTask(Context context, OTOperation op){
        this.mContext = context;
        this.mOp = op;
    }

    public void task(){
        //byte[] opInBytes = OTFactoryMethods.OperationAsByteArray(TAG, mOp);
        try {
            mService.teecRequestCancellation(mOp.hashCode());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mContext.unbindService(mServiceConnection);
    }

    @Override
    public void run() {
        Intent intent = new Intent();
        intent.setClassName(TeecConstants.OT_SERVICE_PACK_NAME,
                TeecConstants.OT_SERVICE_CLASS_NAME);
        mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }
}
