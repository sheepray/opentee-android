package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTClient;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;

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
     * new test functions after the redesign of data types and apis. Developers should only create
     * one OTClient() for each process. Otherwise, it will be regarded as the same.
     */
    void newTest() {
        Log.d(TAG, "******* Starting test ********");

        ITEEClient client = new OTClient();
        ITEEClient.IContext ctx = null;
        try {
            if (client != null) ctx = client.initializeContext(TEE_NAME, getApplication());

            //test code
            //if (client != null) ctx = client.initializeContext(TEE_NAME, getApplication());
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // let's create a shared memory;
        byte[] buffer = {
                's', 's', 'g',
                'a', 'a', 'l', 't', 'o'};
        ITEEClient.IContext.ISharedMemory sharedMemory = null;

        Log.d(TAG, "Create shared memory");

        try {
            sharedMemory = ctx.registerSharedMemory(buffer,
                    ITEEClient.IContext.ISharedMemory.TEEC_MEM_INPUT | ITEEClient.IContext.ISharedMemory.TEEC_MEM_OUTPUT);
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        byte[] msg_to_enc = {
                0x1, 0x2, 0x3, 0x4,
                0x5, 0x6, 0x7, 0x8
        };

        // only encrypt msg_to_enc[1:7]
        buffer = Arrays.copyOf(msg_to_enc, msg_to_enc.length);

        // open session
        UUID uuid = new UUID(0x12345678, 0x87654321);
        ITEEClient.IContext.ISession session = null;

        try {
            session = ctx.openSession(uuid,
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,   // public authentication
                    0,   // no login data
                    null
                    );
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();

            Log.e(TAG, "Return origin: " + e.getReturnOrigin());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // invoke command.


        // close session

        // release shared memory
        try {
            ctx.releaseSharedMemory(sharedMemory);
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            ctx.finalizeContext();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "******* End of test ********");
    }
}