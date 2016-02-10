package fi.aalto.ssg.opentee.opentee_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class OTConnectionService extends Service {
    public OTConnectionService() {
        super();
        Toast.makeText(getApplicationContext(), "Creating OT connection service", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
