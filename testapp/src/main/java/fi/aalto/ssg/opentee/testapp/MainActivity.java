package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTClient;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "Test_APP";
    public static String TEE_NAME = null;   //currently only one default TEE

    HandlerThread mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * put the test in a separate thread. And developers are suggested to do so.
         */
        mHandler = new HandlerThread("Tough worker");
        mHandler.start();

        Handler handler = new Handler(mHandler.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //test();
                newTest();
            }
        });

    }


    /**
     * new test functions after the redesign of data types and apis.
     */
    void newTest() {
        Log.d(TAG, "******* Starting test ********");

        ITEEClient client = new OTClient();
        ITEEClient.IContext ctx = null;
        try {
            if (client != null) ctx = client.initializeContext(TEE_NAME, getApplication());
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "******* End of test ********");
    }
}