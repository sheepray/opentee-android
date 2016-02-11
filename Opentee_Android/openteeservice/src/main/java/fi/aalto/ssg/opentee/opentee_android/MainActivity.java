/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee.opentee_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * prepare and start the opentee engine
 */
public class MainActivity extends AppCompatActivity {
    public static final String OT_ENGINE_STATUS = "ot_engine_status";

    private static final String SETTING_FILE_NAME = "setting_OTConnectionService";
    private static final String TAG = "OTConnectionService";

    private HandlerThread mWorkerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //clear the setting
        SharedPreferences.Editor editor = getSharedPreferences(OT_ENGINE_STATUS, 0).edit();
        editor.clear();
        editor.commit();

        //create another thread to avoid too much in main thread
        mWorkerHandler = new HandlerThread("tought worker");
        mWorkerHandler.start();

        //install and start the opentee-engine
        Handler handler = new Handler(mWorkerHandler.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Worker worker = new Worker();
                Context context = getApplicationContext();

                Log.d(TAG, "Ready files for opentee to run");

                //fresh copy
                boolean overwrite = true;

                //install configuration file
                worker.installConfigToHomeDir(context, OTConstants.OPENTEE_CONF_NAME);

                //install opentee
                worker.installAssetToHomeDir(context, OTConstants.OPENTEE_ENGINE_ASSET_BIN_NAME, OTConstants.OT_BIN_DIR, overwrite);
                worker.installAssetToHomeDir(context, OTConstants.LIB_LAUNCHER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);
                worker.installAssetToHomeDir(context, OTConstants.LIB_MANAGER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);

                //install TA
                worker.installAssetToHomeDir(context, OTConstants.LIB_TA_CONN_TEST_APP_ASSET_TA_NAME, OTConstants.OT_TA_DIR, overwrite);

                //put the status of the opentee engine to setting
                SharedPreferences.Editor editor = getSharedPreferences(SETTING_FILE_NAME, 0).edit();
                editor.putBoolean(OT_ENGINE_STATUS, true);
                editor.commit();

                //start the opentee engine
                try {
                    worker.startOpenTEEEngine(context);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                worker.stopExecutor();

            }
        });
    }

    @Override
    protected void onDestroy() {
        if ( mWorkerHandler != null ){
            mWorkerHandler.quitSafely();
        }

        //stop the open-tee engine if still running
        final SharedPreferences pref = getSharedPreferences(SETTING_FILE_NAME, 0);
        if ( pref.getBoolean(OT_ENGINE_STATUS, false) ){
            //open-tee engine still running, kill it.
            Worker worker = new Worker();
            worker.stopOpenTEEEngine(getApplicationContext());
            worker.stopExecutor();

            Log.d(TAG, "Stopping the engine");
        }

        super.onDestroy();
    }

}
