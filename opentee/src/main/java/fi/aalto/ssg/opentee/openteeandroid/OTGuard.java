package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import fi.aalto.ssg.opentee.exception.TEEClientException;
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
    Map<Integer, Integer> smIDMap; // <smIdInJni, placeHolder>
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

        //TODO: release all resources including shared memory and opening sessions.

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
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        int tmpSmIDInJni = generateSharedMemoryId();
        smBuilder.setMReturnSize(otSharedMemory.getReturnSize());
        smBuilder.setSize(otSharedMemory.getSize());
        smBuilder.setMID(otSharedMemory.getId());
        smBuilder.setMFlag(otSharedMemory.getFlags());

        int return_code = NativeLibtee.teecRegisterSharedMemory(smBuilder.build().toByteArray(),
                tmpSmIDInJni);

        // upon succeed from Libtee:
        if ( return_code == ITEEClient.TEEC_SUCCESS ){
            // add the OTSharedMemory into the OTSharedMemory list of the caller
            findCallerById(callerId).addSharedMemory(tmpSmIDInJni, otSharedMemory);

            // add the OTSharedMemory along with the signed id to smIDMap to keep track of shared memory
            // between OTGuard and JNI layer.
            smIDMap.put(tmpSmIDInJni, 0);
        }

        return return_code;
    }

    public void teecReleaseSharedMemory(int callerId, int smId){
        OTCaller caller = findCallerById(callerId);

        if (caller == null) return;

        // release shared memory in JNI layer.
        int smIdInJni = caller.getSmIdInJniBySmid(smId);

        if ( smIdInJni != -1 ){
            // found the id in jni.
            Log.d(TAG, smId + " found in jni with id:" + smIdInJni);
            NativeLibtee.teecReleaseSharedMemory(smIdInJni);

            // remove the shared memory in current context.
            smIDMap.remove(smIdInJni);
        }

        // remove shared memory from caller.
        caller.removeSharedMemoryBySmIdInJni(smId);
    }

    public int teecOpenSession(int callerId, int sid, UUID uuid, int connMethod, int connData, byte[] opsInBytes, int[] retOrigin){
        // known caller?
        if ( !mOTCallerList.containsKey(callerId) ) return OTReturnCode.TEEC_ERROR_ACCESS_DENIED;

        OTCaller caller = findCallerById(callerId);

        int retCode = -1;
        ReturnOriginWrapper retOriginFromJni = new ReturnOriginWrapper(-1); // to receive the return origin from jni layer

        // generate sid for JNI layer.
        int sidForJni = generateSessionId();

        // recreate the shared memory reference by replacing the memory reference id with the id in
        // JNI if the operation is referencing registered memory.
        if ( opsInBytes != null ){
            // make a deep copy of it in case the binder copy it back. Because it is just memory reference.
            // there is no need to copy the reference back.
            //byte[] opsInBytesCopy = opsInBytes.clone();//Arrays.copyOf(opsInBytes, opsInBytes.length);

            GPDataTypes.TeecOperation.Builder opBuilder = GPDataTypes.TeecOperation.newBuilder();
            try {
                opBuilder.mergeFrom(opsInBytes);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();

                return OTReturnCode.TEEC_ERROR_BAD_PARAMETERS;
            }

            GPDataTypes.TeecOperation op = opBuilder.build();

            GPDataTypes.TeecOperation.Builder toBuilder = opBuilder;

            boolean modified = false;
            for(GPDataTypes.TeecParameter para: op.getMParamsList()){
                if (op.getMParams(0).getType() == GPDataTypes.TeecParameter.Type.smr) {
                    // the parameter is shared memory reference. Replace the id of shared memory from
                    // to the corresponding shared memory id in JNI.
                    GPDataTypes.TeecSharedMemoryReference smrPara = para.getTeecSharedMemoryReference();
                    OTSharedMemory otSM = new OTSharedMemory();
                    int idSmJni = caller.getSmIdInJniBySmid(smrPara.getParentId());

                    //replace the id in here.
                    GPDataTypes.TeecSharedMemoryReference.Builder smrParaWithReplacedIdBuilder =
                            GPDataTypes.TeecSharedMemoryReference.newBuilder(smrPara);
                    smrParaWithReplacedIdBuilder.setParentId(idSmJni);

                    GPDataTypes.TeecParameter.Builder tpBuilder = GPDataTypes.TeecParameter.newBuilder(para);
                    tpBuilder.setTeecSharedMemoryReference(smrParaWithReplacedIdBuilder.build());

                    toBuilder.removeMParams(0);
                    GPDataTypes.TeecParameter tmpParam = tpBuilder.build();
                    toBuilder.addMParams(tmpParam);

                    Log.d(TAG,
                            "Using shared memory reference in operation. Replace the id "
                                    + smrPara.getParentId()
                                    + " of shared memory to the id "
                                    + tmpParam.getTeecSharedMemoryReference().getParentId()
                                    + " in jni.");
                    modified = true;
                }
            }

            if(modified) {
                // recreate TeecOperation after pid changed.
                GPDataTypes.TeecOperation newOp = toBuilder.build();
                opsInBytes = newOp.toByteArray();
            }

        }

        // call the teecOpenSession in native libtee.
        retCode = NativeLibtee.teecOpenSession(sidForJni,
                uuid,
                connMethod,
                connData,
                opsInBytes,
                retOriginFromJni);

        retOrigin[0] = retOriginFromJni.getReturnOrigin();

        // upon success, add session to that caller.
        if(retCode == OTReturnCode.TEEC_SUCCESS) {
            findCallerById(callerId).addSession(sidForJni, new OTCaller.OTCallerSession(sid));
            sessionIdMap.put(sidForJni, sid);
        }

        return retCode;
    }

    public void teecCloseSession(int callerId, int sid){
        if ( !mOTCallerList.containsKey(callerId) ) Log.e(TAG, "Incorrect callerId:" + callerId);

        OTCaller caller = findCallerById(callerId);
        // remove session id from caller.
        int sidInJni = caller.removeSessionBySid(sid);

        // remove sidInJni from global var.
        sessionIdMap.remove(sidInJni);

        NativeLibtee.teecCloseSession(sidInJni);
    }

    private OTCaller findCallerById(int callerId){
        return mOTCallerList.get(callerId);
    }


    // there is need to regenerate the id for the shared memory since different applications may
    // send the shared memory with the same id which is identical in ther context while not in OTGuard.
    // the same principle also apply to the Session.
    private int generateSharedMemoryId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000); // assume the maximum allowed  num of this generator is 50000.
        }while(occupiedSmId(id));

        Log.d(TAG, "Generating memory id for both OTGuard and JNI:" + id);

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

        Log.d(TAG, "Generating session id for both OTGuard and JNI:" + id);

        return id;
    }

    private boolean occupiedSid(int id){
        return sessionIdMap.containsKey(id);
    }
}
