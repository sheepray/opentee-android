package fi.aalto.ssg.opentee.test_app;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Test_APP";
    private HandlerThread mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new HandlerThread("Tough worker");
        mHandler.start();

        Handler handler = new Handler(mHandler.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "******* Starting test ********");
                Log.d(TAG, "******* End of test ********");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if ( mHandler != null ) mHandler.quitSafely();
        super.onDestroy();
    }
}
