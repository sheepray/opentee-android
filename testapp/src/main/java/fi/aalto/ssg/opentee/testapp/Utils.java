package fi.aalto.ssg.opentee.testapp;

import android.util.Log;

/**
 * Own util functions.
 */
public class Utils {
    public final static String TAG = "Utils";
    /**
     * print byte array in a form of char (added '0').
     */
    public static synchronized void printByteArray(byte[] data){
        if(data == null || data.length == 0){
            Log.e(TAG, "byte array is null or empty");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < data.length; i++){
            if(i % 8 == 0) sb.append('\n') ;
            else sb.append(' ') ;

            String tmpStr = Integer.toHexString(data[i]);
            //if (tmpStr.length() > 1) tmpStr = tmpStr.substring(tmpStr.length() - 2);

            sb.append("(" + data[i] + ")");
            sb.append(tmpStr);
        }

        Log.i(TAG, sb.toString());
    }
}
