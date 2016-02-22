package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class implements the multiplexing in the service side.It controls and monitors AIDL calls
 * from remote clients.
 */
public class OTGuard {
    String TAG = "OTGuard";
    String mQuote;
    List<Caller> mCallerList = new ArrayList<Caller>();

    public OTGuard(String quote){
        this.mQuote = quote;

        Log.e(TAG, this.mQuote);
    }

    /**
     * children classes definitions
     */
    class Caller{
        int mCallerID;

        public Caller(int id){this.mCallerID = id;}
    }


    /**
     *
     * @param pid process id.
     * @return true if allowed or false if not.
     */
    public boolean accessControlCheck(int pid){
        return Arrays.asList(OTPermissions.ALLOWED_PID_LIST).contains(pid);
    }

    /**
     *
     * @param callerID
     * @param teeName
     * @return
     */
    public int initializeContext(int callerID, String teeName){
        return LibteeWrapper.teecInitializeContext(teeName);
    }
}
