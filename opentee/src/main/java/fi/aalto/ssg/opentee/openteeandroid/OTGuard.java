package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTReturnCode;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;


/**
 * This class implements the multiplexing in the service side.It controls and monitors IPC calls
 * from remote clients.
 */
public class OTGuard {
    String TAG = "OTGuard";
    String mQuote;
    List<OTCaller> mOTCallerList;
    boolean mConnectedToOT = false;
    Context mContext;
    String mTeeName;
    HashMap<OTSharedMemory, Integer> smIDMap;
    Random smIdGenerator;

    public OTGuard(String quote, Context context){
        this.mQuote = quote;
        this.mContext = context;

        this.mOTCallerList = new ArrayList<OTCaller>();
        this.smIDMap = new HashMap<>();
        this.smIdGenerator = new Random();

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

            return_code = NativeLibtee.teecInitializeContext(teeName, otSocketFilePath);

            Log.e(TAG, " return code " + Integer.toHexString(return_code));

            if ( return_code == ITEEClient.TEEC_SUCCESS){
                mConnectedToOT = true;
                mTeeName = teeName;

                Log.i(TAG, "Connected to opentee.");
            }

        }

        if ( findCallerById(callerID) == null ) {
            //create new caller identity if not exist.
            Log.i(TAG, "Caller not existed. Create one.");

            OTCaller caller = new OTCaller(callerID);
            this.mOTCallerList.add(caller);
        }else{
            Log.i(TAG, "Caller existed. Will not create a new one");
        }

        return return_code;
    }

    public void teecFinalizeContext(int callerID){

        if ( mOTCallerList.size() == 0 ){
            Log.d(TAG, "Nothing to finalize.");
            return;
        }

        if ( findCallerById(callerID) == null ){
            Log.e(TAG, "Unknown caller callerID:" + callerID);
            return;
        }

        //TODO: release all resources including shared memory and openning sessions.

        // remove the caller.
        mOTCallerList.remove(findCallerById(callerID));

        Log.i(TAG, "context for " + callerID + " is finalized");

        if ( mOTCallerList.size() == 0 ){
            Log.i(TAG, "caller list empty now, finalize the context in opentee");

            NativeLibtee.teecFinalizeContext();
        }

    }

    public int teecRegisterSharedMemory(int callerID, OTSharedMemory otSharedMemory){
        /**
         * call Libtee to register shared memory
         */
        // serialize the otSharedMemory into byte array.
        GPDataTypes.TeecSharedMemory.Builder smBuilder = GPDataTypes.TeecSharedMemory.newBuilder();
        try {
            smBuilder.setMBuffer( ByteString.copyFrom( otSharedMemory.asByteArray()) );
        } catch (ITEEClient.Exception e) {
            e.printStackTrace();
        }

        int tmpSmID = generateSharedMemoryId();
        smBuilder.setMReturnSize(otSharedMemory.getReturnSize());
        smBuilder.setMFlag(otSharedMemory.getFlags());

        int return_code = NativeLibtee.teecRegisterSharedMemory(smBuilder.build().toByteArray(),
                                                                tmpSmID);

        // upon succeed from Libtee:
        if ( return_code == ITEEClient.TEEC_SUCCESS ){
            // add the OTSharedMemory into the OTSharedMemory list of the caller
            findCallerById(callerID).addSharedMemory(otSharedMemory);

            // add the OTSharedMemory along with the signed id to smIDMap to keep track of shared memory
            // between OTGuard and JNI layer.
            smIDMap.put(otSharedMemory, tmpSmID);
        }

        return return_code;
    }

    private OTCaller findCallerById(int callerID){
        for ( OTCaller caller: mOTCallerList ){
            if ( caller.getID() == callerID ){
                return caller;
            }
        }

        return null;
    }

    private int generateSharedMemoryId(){
        int id = smIdGenerator.nextInt(1000);

        Log.d(TAG, "Generating memory id:" + id);

        return id;
    }
}
