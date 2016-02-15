package fi.aalto.ssg.opentee.openteeandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.sharedlibrary.IOTConnectionInterface;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecContext;

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
        public TeecContext newTeecContext() throws RemoteException {
            return null;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
