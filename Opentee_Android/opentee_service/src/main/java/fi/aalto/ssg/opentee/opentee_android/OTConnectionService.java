package fi.aalto.ssg.opentee.opentee_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import fi.aalto.ssg.opentee.sharedlibrary.IOTConnectionInterface;

public class OTConnectionService extends Service {
    public OTConnectionService() {
        super();
        Toast.makeText(getApplicationContext(), "Creating OT connection service", Toast.LENGTH_SHORT).show();
    }

    private IOTConnectionInterface.Stub mBinder = new IOTConnectionInterface.Stub(){
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void test() throws RemoteException {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
