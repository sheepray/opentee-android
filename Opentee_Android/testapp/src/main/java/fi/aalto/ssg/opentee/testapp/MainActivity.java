package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.Teec;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.TeecContext;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "Test_APP";

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
                mHandler = new HandlerThread("Tough worker");
                mHandler.start();

                Handler handler = new Handler(mHandler.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //DO NOTHING
                    }
                });

            }
        });

        test();

    }

    void test(){
        Log.d(TAG, "******* Starting test ********");

        TeecContext ctx = null;
        try {
            ctx = Teec.initializeContext(TAG, getApplicationContext());
        } catch (TeecConnectionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //wait for a while
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if ( ctx != null ){
            ctx.teecFinalizeContext();
        }

        Log.d(TAG, "******* End of test ********");
    }

}
