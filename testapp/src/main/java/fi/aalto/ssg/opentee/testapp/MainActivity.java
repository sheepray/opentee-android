package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.OpenTEE;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "Test_APP";
    public final String TEE_NAME = null;   //currently only one default TEE

    // uuid for ta_conn_test in IOpenTEE:
    // uuid = {0x12345678, 0x8765, 0x4321, {'T','A','C','O','N','N','T','E'}}
    public UUID TA_CONN_TEST_UUID;

    //HandlerThread mHandler;

    /**
     * Take a string and transfer its each char to a byte. Then combine all the bytes to a long var.
     * If the string is too long, this func will only retrieve the lower 8 characters.
     * @param strVal
     * @return
     */
    long strToLong(String strVal){
        if(strVal == null || strVal.isEmpty()) return 0;

        byte[] vals = strVal.getBytes();
        int tailFlag = vals.length > 8 ? 8 : vals.length;
        long result = 0;
        for(int i = 0; i < tailFlag; i++){
            result = result << 8;
            result += vals[i];

            //test code
            Log.d(TAG, i + ":" + vals[i]);
        }
        return result;
    }

    void prepareGlobalVars(){
        Log.d(TAG, "******* Start preparing global vars ********");

        /**
         * prepare the uuid of ta to connect to.
         */
        //long clockSeqAndNode = strToLong(new String("TACONNTE"));
        long clockSeqAndNode = strToLong(new String("OMNISHAR"));
        TA_CONN_TEST_UUID = new UUID(0x1234567887654321L, clockSeqAndNode);

        Log.d(TAG, "clockSeqAndNode:" + Long.toHexString( clockSeqAndNode ));


        Log.d(TAG, "******* End preparing global vars ********");
    }






    /* RSA wrapped root key blob */
    private byte[] rootKey;

    /* Chain of directory key blobs wrapped with the root key */
    private Keychain keychain;

    /* Data buffer used for testing */
    private byte data[] =  new byte[]{
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF
    };

    /* UI Elements */
    private Button InitializeButton;
    private Button FinalizeButton;
    private Button CreateDirectoryKeyButton;
    private Button EncryptFileButton;
    private Button DecryptFileButton;

    private TextView logView;




    public static final int CMD_UPDATE_LOGVIEW = 1;

    public static final int ID_CREATE_ROOT_KEY_BUTTON = 0xffff0000;
    public static final int ID_INI_BUTTON = 0xffff0001;
    public static final int ID_FINALIZE_BUTTON = 0xffff0002;
    public static final int ID_DO_ENCRY_BUTTON = 0xffff0003;
    public static final int ID_DO_DECRY_BUTTON = 0xffff0004;

    Handler.Callback updateUiCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case CMD_UPDATE_LOGVIEW:
                    if(msg.obj != null){
                        Log.d(TAG, "update logview with " + msg.obj);

                        switch (msg.arg1){
                            case ID_CREATE_ROOT_KEY_BUTTON:
                                if(msg.arg2 == 1) InitializeButton.setEnabled(true);
                                break;
                            case ID_INI_BUTTON:
                                if(msg.arg2 == 1){
                                    CreateDirectoryKeyButton.setEnabled(true);
                                    EncryptFileButton.setEnabled(true);
                                    InitializeButton.setEnabled(false);
                                    FinalizeButton.setEnabled(true);
                                }
                                break;
                            case ID_FINALIZE_BUTTON:
                                if(msg.arg2 == 1){
                                    CreateDirectoryKeyButton.setEnabled(false);
                                    EncryptFileButton.setEnabled(false);
                                    DecryptFileButton.setEnabled(false);
                                    InitializeButton.setEnabled(true);

                                }
                                break;

                            case ID_DO_ENCRY_BUTTON:
                                if(msg.arg2 == 1){
                                    DecryptFileButton.setEnabled(true);
                                }
                                break;

                            default:
                                Log.e(TAG, "unknown id");
                                break;
                        }

                        logView.append( msg.obj + "\n");
                    }
                    break;

                default:
                    Log.e(TAG, "unknown msg");
                    break;
            }
            return true;
        }
    };

    Handler mUpdateUi;
    Worker mWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keychain = new Keychain();

        InitializeButton = (Button) findViewById(R.id.button_initialize);
        FinalizeButton = (Button) findViewById(R.id.button_finalize);
        CreateDirectoryKeyButton = (Button) findViewById(R.id.button_create_directory_key);
        EncryptFileButton = (Button) findViewById(R.id.button_encrypt_file);
        DecryptFileButton = (Button) findViewById(R.id.button_decrypt_file);

        logView = (TextView)findViewById(R.id.view_log);
        //logView.append("OmniShare Trustlet Tester App\n");
        logView.append("Data buffer: " + HexUtils.encodeHexString(data) + "\n");

        mUpdateUi = new Handler(updateUiCallBack);

        mWorker = new Worker("LOL tough worker", mUpdateUi, getApplicationContext());
        mWorker.start();



        /**
         * prepare global vars.
         */
        //prepareGlobalVars();

        /*
        //put the test in a separate thread. And developers are suggested to do so.
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
        */
    }


    /**
     * Trigger root key generation from UI.
     *
     * @param v Parent view
     */
    public void doCreateRootKey(View v) {
        Log.d(TAG, "Generate root key in " + Thread.currentThread().getId() );

        Handler workerHandler = mWorker.getHandler();
        Message msg = workerHandler.obtainMessage(Worker.CMD_GENERATE_ROOT_KEY);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger trustlet initialization from UI.
     *
     * @param v Parent view
     */
    public void doInitialize(View v) {
        Log.d(TAG, "doInitialize");

        Handler workerHandler = mWorker.getHandler();
        Message msg = workerHandler.obtainMessage(Worker.CMD_INIT);
        workerHandler.sendMessage(msg);

    }

    /**
     * Trigger trustlet finalization from UI.
     *
     * @param v Parent view
     */
    public void doFinalize(View v) {
        //TODO:
    }

    /**
     * Trigger directory key generation from UI.
     *
     * @param v Parent view
     */
    public void doCreateDirectoryKey(View v) {
        //TODO:
    }

    /**
     * Trigger encrypt operation from UI.
     *
     * @param v Parent view.
     */
    public void doEncryptFile(View v) {
        //TODO:
    }

    /**
     * Trigger decrypt operation from UI.
     *
     * @param v Parent view.
     */
    public void doDecryptFile(View v) {
        //TODO:
    }









    /**
     * new test functions after the redesign of data types and apis. Developers should only create
     * one OTClient() for each process. Otherwise, it will be regarded as the same.
     */
    void newTest() {
        Log.d(TAG, "******* Starting test ********");

        ITEEClient client = OpenTEE.newTEEClient();
        ITEEClient.IContext ctx = null;
        try {
            if (client != null) ctx = client.initializeContext(TEE_NAME, getApplication());
        } catch (TEEClientException e) {
            e.printStackTrace();
        }


        byte[] buffer0 = {'f', 'i', 'r', 's', 't', ',', 's', 'm'};
        ITEEClient.ISharedMemory sm0 = null;

        try {
            sm0 = ctx.registerSharedMemory(buffer0, ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }


        // let's create a shared memory;
        byte[] buffer = {
                's', 's', 'g',
                'a', 'a', 'l', 't', 'o'};
        ITEEClient.ISharedMemory sharedMemory = null;

        Log.d(TAG, "Create shared memory");

        try {
            sharedMemory = ctx.registerSharedMemory(buffer,
                    ITEEClient.ISharedMemory.TEEC_MEM_INPUT | ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        Log.e(TAG, buffer + " vs " + sharedMemory.asByteArray() + " " +  (buffer == sharedMemory.asByteArray()));


        /**
         * Register another shared memory.
         */
        int sizeOfBuffer2 = 256;
        byte[] buffer2 = new byte[sizeOfBuffer2];
        Arrays.fill(buffer2, (byte)'a');

        /*
        {
                'o', 'p', 'e', 'n',
                't', 'e', 'e'};
        */

        ITEEClient.ISharedMemory sharedMemory2 = null;

        Log.d(TAG, "Create shared memory 2");

        try {
            sharedMemory2 = ctx.registerSharedMemory(buffer2,
                    ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        //open session
        UUID uuid = TA_CONN_TEST_UUID;
        ITEEClient.IRegisteredMemoryReference iRmr
                = client.newRegisteredMemoryReference(sharedMemory,
                ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INOUT,
                0);

        ITEEClient.IValue iValue
                = client.newValue(ITEEClient.IValue.Flag.TEEC_VALUE_INOUT,
                                  0x33,
                                  0x66);

        final ITEEClient.IOperation iOperation = client.newOperation(iRmr, iValue);

        // request cancellation in a seperate thread.
        Thread stt = new Thread(
                new SecondTask(ctx, iOperation)
        );
        stt.start();

        ITEEClient.ISession session = null;

        try {
             session = ctx.openSession(uuid,
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    iOperation);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        ITEEClient.ISession session2 = null;
        try {
            session2 = ctx.openSession(uuid,
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            session2.closeSession();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        //check result
        Log.d(TAG, "new shared memory=" + new String(iRmr.getSharedMemory().asByteArray()));
        Log.d(TAG, "new a " + iValue.getA() + " b " + iValue.getB());

        //invoke command
        ITEEClient.IRegisteredMemoryReference iRmr2
                = client.newRegisteredMemoryReference(sharedMemory,
                ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INOUT,
                0);

        ITEEClient.IValue iValue2
                = client.newValue(ITEEClient.IValue.Flag.TEEC_VALUE_INOUT,
                0x88,
                0x66);

        ITEEClient.IOperation iOperation2 = client.newOperation(iRmr2, iValue2);

        // request cancellation in a separate thread.
        Thread stt2 = new Thread(
                new SecondTask(ctx, iOperation2)
        );
        stt2.start();

        int commandId = 0x12345678;
        try {
            session.invokeCommand(commandId, iOperation2);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            session.invokeCommand(commandId, null);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        //check result
        Log.d(TAG, "new shared memory=" + new String(iRmr2.getSharedMemory().asByteArray()));
        Log.d(TAG, "new a " + iValue2.getA() + " b " + iValue2.getB());


        // close session
        try {
            if (session != null) session.closeSession();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
        /*
        // open session
        UUID uuid = TA_CONN_TEST_UUID;
        int started = 0;
        //ITEEClient.RegisteredMemoryReference rmrOne = new ITEEClient.RegisteredMemoryReference(sharedMemory, ITEEClient.RegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT, 0);
        //ITEEClient.RegisteredMemoryReference rmrTwo = new ITEEClient.RegisteredMemoryReference(sharedMemory2, ITEEClient.RegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT, 0);
        //ITEEClient.Operation op = new ITEEClient.Operation(started, rmrOne, rmrTwo);
        //ITEEClient.Operation op = new ITEEClient.Operation(started, rmrTwo); // omnishare_ta only allows one input parameter.

        // create mem reference to the shared memory.
        ITEEClient.RegisteredMemoryReference rmr =
                new ITEEClient.RegisteredMemoryReference(sharedMemory,
                                                         ITEEClient.RegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT,
                                                         0);
        ITEEClient.Value value = new ITEEClient.Value(ITEEClient.Value.Flag.TEEC_VALUE_INOUT, 10, 17);
        ITEEClient.Operation op = new ITEEClient.Operation(started, rmr, value);
        ITEEClient.ISession session = null;  // test:open a second session and test sync memory.

        try {
            // test: change the content of buffer
            byte[] msg_to_enc = {
                    'x', 'j', 't', 'u'
            };

            // only encrypt msg_to_enc[1:7]
            System.arraycopy(msg_to_enc, 0, buffer, 0, buffer.length > msg_to_enc.length? msg_to_enc.length:buffer.length);

            Log.i(TAG, "New buffer:" + new String(buffer));
            Log.i(TAG, "New buffer in shared memory:" + new String(sharedMemory.asByteArray()));
            Log.e(TAG, buffer + " vs " + sharedMemory.asByteArray() + " " +  (buffer == sharedMemory.asByteArray()));

            session = ctx.openSession(uuid,
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,   // public authentication
                    0,   // no login data
                    op
                    );
        } catch (TEEClientException e) {
            e.printStackTrace();

            Log.e(TAG, "Return origin: " + e.getReturnOrigin());
        }



        ITEEClient.Operation op2 = new ITEEClient.Operation(started); // generate_root_key
        ITEEClient.ISession sessionTwo = null;
        try {
            sessionTwo = ctx.openSession(uuid,
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    0,
                    op2);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
        try {
            sessionTwo.closeSession();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
        // invoke command.
        try {
            session.invokeCommand(0, // commandId.
                    null); // no operation.
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        // close session
        try {
            session.closeSession();

            Log.d(TAG, "Session closed.");
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
        */



        // release shared memory
        try {
            ctx.releaseSharedMemory(sharedMemory2);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ctx.releaseSharedMemory(sharedMemory);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ctx.releaseSharedMemory(sm0);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ctx.finalizeContext();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "******* End of test ********");
    }
}