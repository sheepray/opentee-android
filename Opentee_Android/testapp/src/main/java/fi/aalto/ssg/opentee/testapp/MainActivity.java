package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSession;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSharedMemory;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.Teec;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecContext;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecParameter;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecRegisteredMemoryReference;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecUuid;
import fi.aalto.ssg.opentee.sharedlibrary.imp.OTSession;
import fi.aalto.ssg.opentee.sharedlibrary.imp.OTSharedMemory;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "Test_APP";
    public static String TEE_NAME = null;   //currently only one default TEE

    HandlerThread mHandler;
    Button mButton = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button)findViewById(R.id.mButtonStartTest);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
;
        mHandler = new HandlerThread("Tough worker");
        mHandler.start();

        Handler handler = new Handler(mHandler.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                test();
            }
        });

    }

    void test(){
        Log.d(TAG, "******* Starting test ********");

        /**
         * initialize context
         */
        ITeecContext ctx = null;
        try {
            ctx = Teec.initializeContext(TEE_NAME, getApplicationContext());
        } catch (TeecConnectionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        /**
         * wait for 1 second
         */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * prepare to open session
         */
        byte[] clockAndSeqArray = {
                0x1, 0x2, 0x3, 0x4,
                0x5, 0x6, 0x7, 0x8
        };
        TeecUuid uuid = new TeecUuid(0x12345678,
                (short)0x1234,
                (short)0x5678,
                clockAndSeqArray);// TA to connect to.
        TeecOperation operation = new TeecOperation(0, // can be cancelled
                null); // no parameter


        // open session
        ITeecSession session = null;
        try {
            session = ctx.teecOpenSession(uuid, // uuid
                    null,   // no authentication
                    null,   // no data
                    operation);
        } catch (TeecException e) {
            e.printStackTrace();
        }

        /**
         * create buffer
         */
        byte[] buffer = {
                'o', 'p', 't', 'e', 'e',
                's', 's', 'g',
                'A', 'a','l', 't', '0'
        };


        Log.d(TAG, "******* start of invoking command ********");
        Log.d(TAG, buffer.toString());

        /**
         * register buffer as shared memory
         */
        ITeecSharedMemory sharedMemory = null;
        try {
            sharedMemory = ctx.teecRegisterSharedMemory(buffer, // the shared memory to register
                    ITeecSharedMemory.flag.TEEC_MEM_INOUT); // buffer for both in and out
        } catch (TeecException e) {
            e.printStackTrace();
        }


        /**
         * invoke command example
         */
        //prepare to invoke command
        TeecRegisteredMemoryReference registeredMemoryReference = new TeecRegisteredMemoryReference(
                TeecParameter.Type.TEEC_MEMREF_WHOLE,   // use whole buffer
                sharedMemory,   // shared memory reference
                0   // start from beginning
        );

        TeecRegisteredMemoryReference[] registeredMemoryReferenceArray = new TeecRegisteredMemoryReference[4];
        registeredMemoryReferenceArray[0] = registeredMemoryReference;

        TeecOperation commandOperation = new TeecOperation(0,   // can be cancelled
                registeredMemoryReferenceArray); // pass in registered memory reference array

        // invoke comand
        try {
            if ( session != null ) {
                session.teecInvokeCommand(0, // operation id: encrypt
                        commandOperation);
            }
        } catch (TeecException e) {
            e.printStackTrace();
        }

        /**
         * check the result after invoking encryption command
         */
        Log.d(TAG, buffer.toString());
        Log.d(TAG, "******* end of invoking command ********");


        /**
         * release shared memory
         */
        try {
            ctx.teecReleaseSharedMemory(sharedMemory);
        } catch (TeecException e) {
            e.printStackTrace();
        }


        /**
         * close session
         */
        try {
            if ( session != null ) session.teecCloseSession();
        } catch (TeecException e) {
            e.printStackTrace();
        }

        /**
         * finalize context
         */
        if ( ctx != null ){
            ctx.teecFinalizeContext();
        }

        Log.d(TAG, "******* End of test ********");
    }

}
