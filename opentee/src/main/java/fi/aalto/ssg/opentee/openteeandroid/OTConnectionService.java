package fi.aalto.ssg.opentee.openteeandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.IOTConnectionInterface;

public class OTConnectionService extends Service {
    String TAG = "OTConnectionService.Imp";
    String mQuote = "You Shall Not Pass!";
    OTGuard mOTGuard = null;

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
        public int teecInitializeContext(String teeName) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me.");
            return mOTGuard.initializeContext(Binder.getCallingPid(), teeName);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, intent.get)
        return mBinder;
    }
}
