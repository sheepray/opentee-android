package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.content.ContextWrapper;
import android.telecom.Call;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTReturnCode;
import fi.aalto.ssg.opentee.imps.TeecConstants;
import fi.aalto.ssg.opentee.imps.pbdatatypes.PbDataTypes;


/**
 * This class implements the multiplexing in the service side.It controls and monitors IPC calls
 * from remote clients.
 */
public class OTGuard {
    String TAG = "OTGuard";
    String mQuote;
    List<OTCaller> mOTCallerList = new ArrayList<OTCaller>();
    boolean mConnectedToOT = false;
    Context mContext;

    public OTGuard(String quote, Context context){
        this.mQuote = quote;
        this.mContext = context;

        Log.e(TAG, this.mQuote);
    }

    /**
     *
     * @param pid process id.
     * @return true if allowed or false if not.
     */
    public boolean accessControlCheck(int pid){
        return Arrays.asList(OTPermissions.ALLOWED_PID_LIST).contains(pid);
    }

    String getOtSocketFilePath() throws IOException, InterruptedException {
        //String oTSocketFilePath;
        return OTUtils.getFullPath(mContext) + "/open_tee_socket";
    }

    /**
     *
     * @param callerID
     * @param teeName
     * @return
     */
    public int initializeContext(int callerID, String teeName){
        int return_code = OTReturnCode.TEEC_SUCCESS;

        /**
         * If not connected to opentee, then connect.
         */
        if ( !mConnectedToOT ){
            String otSocketFilePath = null;
            try {
                otSocketFilePath = getOtSocketFilePath();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ( otSocketFilePath == null ){
                Log.e(TAG, "try to get OPENTEE_SOCKET_FILE_PATH failed");
                return OTReturnCode.TEEC_ERROR_GENERIC;
            }

            Log.e(TAG, "initializeContext teeName: " + teeName + " OT_SOCKET_FILE_PATH:" + otSocketFilePath);

            return_code = LibteeWrapper.teecInitializeContext(teeName, otSocketFilePath);

            Log.e(TAG, " return code " + Integer.toHexString(return_code));

            if ( return_code == ITEEClient.TEEC_SUCCESS){
                mConnectedToOT = true;

                Log.i(TAG, "Connected to opentee.");
            }

        }

        /**
         * caller id identification.
         */
        boolean existedCaller = false;
        for ( OTCaller iOTCaller: this.mOTCallerList){
            if ( callerID == iOTCaller.getID() ){
                existedCaller = true;
            }
        }

        if ( !existedCaller ) {
            //create new caller identity if not exist.
            Log.i(TAG, "Caller not existed. Create one.");

            OTCaller caller = new OTCaller(callerID);
            this.mOTCallerList.add(caller);
        }else{
            Log.i(TAG, "Caller existed. Will not create a new one");
        }

        return return_code;
    }
}
