package fi.aalto.ssg.opentee.testapp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by yangr1 on 4/28/16.
 */
public class Worker extends HandlerThread {
    final int OMS_MAX_RSA_MODULO_SIZE = 256;

    final String TAG = "Worker";
    public final static int CMD_GENERATE_ROOT_KEY = 1;
    public final static int CMD_INIT = 2;
    Handler mUiHandler;
    Context mContext;

    /* Data buffer used for testing */
    private byte data[] =  new byte[]{
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF
    };

    /* RSA wrapped root key blob */
    private byte[] rootKey;

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMsg in " + currentThread().getId());

            if(Looper.getMainLooper() == Looper.myLooper()){
                Log.e(TAG, "within the main thread");
            }

            switch (msg.what){
                case CMD_GENERATE_ROOT_KEY:
                    Log.i(TAG, "asked to generate root key");

                    rootKey = new byte[OMS_MAX_RSA_MODULO_SIZE];
                    boolean status = Omnishare.generateRootKey(rootKey, mContext);

                    Message uiMsg = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_CREATE_ROOT_KEY_BUTTON,
                            status? 1 : 0,
                            status? "root key generated" : "root key generation failed, try again");
                    mUiHandler.sendMessage(uiMsg);
                    break;
                case CMD_INIT:
                    Log.i(TAG, "asked to initialize");

                    boolean status_init = Omnishare.omnishareInit(rootKey, mContext);

                    Message uiMsg_init = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_INI_BUTTON,
                            status_init? 1: 0,
                            status_init? "initialized" : " fail to initialize");

                    mUiHandler.sendMessage(uiMsg_init);
                    break;
                default:
                    Log.e(TAG, "unknown message type");
                    break;
            }
            return true;
        }
    };

    Handler mHandler;

    public Worker(String name, Handler uiHandler, Context context) {
        super(name);
        this.mUiHandler = uiHandler;
        this.mContext = context;
    }

    public Handler getHandler(){
        return this.mHandler;
    }

    @Override
    protected void onLooperPrepared(){
        super.onLooperPrepared();
        mHandler = new Handler(getLooper(), callback);
    }
}
