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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTReturnCode;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;


/**
 * This class implements the multiplexing in the service side.It controls and monitors IPC calls
 * from remote clients.
 * Note: OTGuard keep two maps for the ID of shared memory.
 * 1. One is for the id map with the remote CA
 * library. The id in this case is set by the CA library.
 * 2. Another one is to map the shared memory with the JNI layer.
 * In sum, for one shared memory, there are two ids for it in OTGuard.
 */
public class OTGuard {
    String TAG = "OTGuard";
    String mQuote;
    Map<Integer, OTCaller> mOTCallerList; // <pid, caller>
    boolean mConnectedToOT = false;
    Context mContext;
    String mTeeName;
    Map<Integer, OTSharedMemory> smIDMap; // <smIdInJni, SharedMemory>
    Map<Integer, Integer> sessionIdMap; // <sessionIdInJni, sessionIdForCaller>
    Random smIdGenerator;

    public OTGuard(String quote, Context context){
        this.mQuote = quote;
        this.mContext = context;

        this.mOTCallerList = new HashMap<>();
        this.smIDMap = new HashMap<>();
        this.sessionIdMap = new HashMap<>();
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
     * @param callerId
     * @param teeName
     * @return
     */
    public int initializeContext(int callerId, String teeName){
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

        if ( findCallerById(callerId) == null ) {
            //create new caller identity if not exist.
            Log.i(TAG, "Caller not existed. Create one.");

            OTCaller caller = new OTCaller(callerId);
            this.mOTCallerList.put(callerId, caller);
        }else{
            Log.i(TAG, "Caller existed. Will not create a new one");
        }

        return return_code;
    }

    public void teecFinalizeContext(int callerId){

        if ( mOTCallerList.size() == 0 ){
            Log.d(TAG, "Nothing to finalize.");
            return;
        }

        if ( findCallerById(callerId) == null ){
            Log.e(TAG, "Unknown caller callerId:" + callerId);
            return;
        }

        //TODO: release all resources including shared memory and openning sessions.

        // remove the caller.
        mOTCallerList.remove(callerId);

        Log.i(TAG, "context for " + callerId + " is finalized");

        if ( mOTCallerList.size() == 0 ){
            Log.i(TAG, "caller list empty now, finalize the context in opentee");

            NativeLibtee.teecFinalizeContext();
        }

    }

    public int teecRegisterSharedMemory(int callerId, OTSharedMemory otSharedMemory){
        if (findCallerById(callerId) == null) return OTReturnCode.TEEC_ERROR_ACCESS_DENIED;

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
        smBuilder.setMID(otSharedMemory.getId());
        smBuilder.setMFlag(otSharedMemory.getFlags());

        int return_code = NativeLibtee.teecRegisterSharedMemory(smBuilder.build().toByteArray(),
                tmpSmID);

        // upon succeed from Libtee:
        if ( return_code == ITEEClient.TEEC_SUCCESS ){
            // add the OTSharedMemory into the OTSharedMemory list of the caller
            findCallerById(callerId).addSharedMemory(otSharedMemory);

            // add the OTSharedMemory along with the signed id to smIDMap to keep track of shared memory
            // between OTGuard and JNI layer.
            smIDMap.put(tmpSmID, otSharedMemory);
        }

        return return_code;
    }

    public void teecReleaseSharedMemory(int callerId, int smId){
        OTCaller caller = findCallerById(callerId);

        if (caller == null) return;

        // release shared memory in JNI layer.
        OTSharedMemory sm = caller.getSharedMemoryBySmId(smId);
        Integer smIdInJni = findIdInJniById(smId);

        if ( smIdInJni != null ){
            // found the id in jni.
            Log.d(TAG, smId + " found in jni with id:" + smIdInJni );
            NativeLibtee.teecReleaseSharedMemory(smIdInJni);
        }

        // remove shared memory from caller.
        caller.removeSharedMemoryBySmId(smId);
    }

    public int teecOpenSession(int callerId, int sid, UUID uuid, int connMethod, int connData, byte[] opsInBytes){
        return 0;
    }

    private OTCaller findCallerById(int callerId){
        return mOTCallerList.get(callerId);
    }

    public Integer findIdInJniById(int idInCaller){
        for(Map.Entry<Integer, OTSharedMemory> entry: smIDMap.entrySet()){
            if ( entry.getValue().getId() == idInCaller ) return entry.getKey();
        }

        return null;
    }

    // there is need to regenerate the id for the shared memory since different applications may
    // send the shared memory with the same id which is identical in ther context while not in OTGuard.
    // the same principle also apply to the Session.
    private int generateSharedMemoryId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000); // assume the maximum allowed  num of this generator is 50000.
        }while(occupiedSmId(id));

        Log.d(TAG, "Generating memory id:" + id);

        return id;
    }

    private boolean occupiedSmId(int id){
        return smIDMap.containsKey(id);
    }

    private int generateSessionId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000); // reuse the shared memory id random number generator.
        }while(occupiedSid(id));

        return id;
    }

    private boolean occupiedSid(int id){
        return sessionIdMap.containsKey(id);
    }
}
