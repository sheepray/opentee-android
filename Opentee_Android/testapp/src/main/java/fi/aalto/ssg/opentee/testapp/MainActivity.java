package fi.aalto.ssg.opentee.testapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import fi.aalto.ssg.opentee.sharedlibrary.IOTConnectionInterface;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "Test_APP";
    static final String SERVICE_PACK_NAME  = "fi.aalto.ssg.opentee.openteeandroid";
    static final String SERVICE_CLASS_NAME = "fi.aalto.ssg.opentee.openteeandroid.OTConnectionService";

    HandlerThread mHandler;
    boolean mBound= false;
    IOTConnectionInterface mService = null;
    Button mButton = null;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBound = true;
            mService = IOTConnectionInterface.Stub.asInterface(service);

            Log.d(TAG, "connected to service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;

            Log.d(TAG, "disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button)findViewById(R.id.mButtonStartTest);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    mHandler = new HandlerThread("Tough worker");
                    mHandler.start();

                    Handler handler = new Handler(mHandler.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "******* Starting test ********");

                            try {
                                mService.newTeecContext();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }

                            Log.d(TAG, "******* End of test ********");
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Not connected, please wait", Toast.LENGTH_SHORT).show();
                }

            }
        });

        initConnection();

    }

    public void initConnection(){
        Intent intent = new Intent();
        intent.setClassName(SERVICE_PACK_NAME, SERVICE_CLASS_NAME);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);

        Log.d(TAG, "trying to connect");
    }

    @Override
    protected void onDestroy() {
        if ( mHandler != null ) mHandler.quitSafely();
        if ( mBound ) unbindService(mServiceConnection);
        super.onDestroy();
    }
}
