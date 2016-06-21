package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read setting from "config.properties".
 */
public class Setting {
    final String TAG = "Setting";
    Properties mProp;

    public Setting(Context context){
        if(context == null){
            Log.e(TAG, "invalid application context.");
            return;
        }

        final String propFileName = "config.properties";

        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(propFileName);
        } catch (IOException e) {
            Log.e(TAG, "unable to read setting from " + propFileName);
            return;
        }

        mProp = new Properties();
        try {
            mProp.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "unable to load input stream from setting file.");
        }
    }

    public Properties getProperties(){return mProp;}
}
