package fi.aalto.ssg.opentee.openteeandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.sharedlibrary.IOTConnectionInterface;

public class OTConnectionService extends Service {
    final String TAG = "OTConnectionService.Imp";
    public OTConnectionService() {
        super();
        Log.e(TAG, "creating OTConnectionService");
    }


    private final IOTConnectionInterface.Stub mBinder = new IOTConnectionInterface.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int teecInitializeContext(String name) throws RemoteException {
            return LibteeWrapper.teecInitializeContext(name);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
