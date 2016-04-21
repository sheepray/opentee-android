package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.imps.OTFactoryMethods;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
        int tmpSmIDInJni = generateSharedMemoryId();
        if(otSharedMemory != null){
            smBuilder.setMBuffer( ByteString.copyFrom( otSharedMemory.asByteArray()) );
            smBuilder.setMReturnSize(otSharedMemory.getReturnSize());
            smBuilder.setSize(otSharedMemory.getSize());
            smBuilder.setMID(otSharedMemory.getId());
            smBuilder.setMFlag(otSharedMemory.getFlags());

        }
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

    /*
    * recreate the shared memory reference by replacing the memory reference id with the id in
    * JNI if the operation is referencing registered memory.
    * If reversed is true, then it replaces smid with smidInJni. Otherwise, it replaces smidInJni
    * with smid.
    * */
    private byte[] replaceSMId(OTCaller caller, byte[] opsInBytes, boolean reversed){
        if ( opsInBytes == null ) return null;

        GPDataTypes.TeecOperation.Builder opBuilder = GPDataTypes.TeecOperation.newBuilder();
        try {
            opBuilder.mergeFrom(opsInBytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();

            Log.e(TAG, "Internal error.");
        }

        GPDataTypes.TeecOperation op = opBuilder.build();

        GPDataTypes.TeecOperation.Builder toBuilder = opBuilder;

        boolean modified = false;
        int i = 0;
        for(GPDataTypes.TeecParameter para: op.getMParamsList()){
            Log.d(TAG, "param type:" + op.getMParams(i).getType());

            if (op.getMParams(i).getType() == GPDataTypes.TeecParameter.Type.smr) {
                Log.d(TAG, "Param is smr");

                // the parameter is shared memory reference. Replace the id of shared memory from
                // to the corresponding shared memory id in JNI.
                GPDataTypes.TeecSharedMemoryReference smrPara = para.getTeecSharedMemoryReference();

                int smid;
                if(reversed){
                    Log.i(TAG, "smid to smidInJni");

                    // find the id in JNI
                    smid = caller.getSmIdInJniBySmid(smrPara.getParent().getMID());
                }else{
                    Log.i(TAG, "smidInJni to smid");

                    smid = caller.getSmIdBySmIdInJni(smrPara.getParent().getMID());
                }

                /**
                 * replace the id in here. Since the id is changed, a new GP shared memory will
                 * be created.
                 */
                GPDataTypes.TeecSharedMemoryReference.Builder smrParaWithReplacedIdBuilder =
                        GPDataTypes.TeecSharedMemoryReference.newBuilder(smrPara);

                GPDataTypes.TeecSharedMemory.Builder gpSMBuilder = GPDataTypes.TeecSharedMemory.newBuilder(smrPara.getParent());
                gpSMBuilder.setMID(smid);

                // replace the share memory in smr
                smrParaWithReplacedIdBuilder.setParent(gpSMBuilder.build());

                GPDataTypes.TeecParameter.Builder tpBuilder = GPDataTypes.TeecParameter.newBuilder(para);
                tpBuilder.setTeecSharedMemoryReference(smrParaWithReplacedIdBuilder.build());


                GPDataTypes.TeecParameter tmpParam = tpBuilder.build();
                toBuilder.setMParams(i, tmpParam);

                Log.d(TAG,
                        "Using shared memory reference in operation. Replace the id "
                                + smrPara.getParent().getMID()
                                + " of shared memory to the id "
                                + tmpParam.getTeecSharedMemoryReference().getParent().getMID()
                                + " in jni.");
                modified = true;
            }
            i++;
        }

        if(modified) {
            // recreate TeecOperation after pid changed.
            GPDataTypes.TeecOperation newOp = toBuilder.build();
            opsInBytes = newOp.toByteArray();
        }

        return opsInBytes;
    }

    public int teecOpenSession(int callerId, int sid, UUID uuid, int connMethod, int connData, byte[] opsInBytes, int[] retOrigin, ISyncOperation iSyncOperation){
        // known caller?
        if ( !mOTCallerList.containsKey(callerId) ) return OTReturnCode.TEEC_ERROR_ACCESS_DENIED;

        OTCaller caller = findCallerById(callerId);

        opsInBytes = replaceSMId(caller, opsInBytes, true);

        // generate sid for JNI layer.
        int sidForJni = generateSessionId();
        IntWrapper retOriginFromJni = new IntWrapper(-1); // to receive the return origin from jni layer.
        IntWrapper returnCode = new IntWrapper(-1); // to receive the return code from jni layer.
        // call the teecOpenSession in native libtee.
        byte[] newOpInBytes = NativeLibtee.teecOpenSession(sidForJni,
                uuid,
                connMethod,
                connData,
                opsInBytes,
                retOriginFromJni,
                returnCode);

        Log.d(TAG, "teecOpenSession returned.");

        retOrigin[0] = retOriginFromJni.getValue();

        // upon success, add session to that caller.
        if(returnCode.getValue() == OTReturnCode.TEEC_SUCCESS) {
            findCallerById(callerId).addSession(sidForJni, new OTCaller.OTCallerSession(sid));
            sessionIdMap.put(sidForJni, sid);

            opsInBytes = replaceSMId(caller, opsInBytes, false);
        }

        //test code
        //OTFactoryMethods.print_op_in_bytes(TAG, newOpInBytes);

        if(iSyncOperation != null){
            // sync operation back.
            try {
                Log.d(TAG, "Operation sync back using callback function.");
                iSyncOperation.syncOperation(newOpInBytes);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return returnCode.getValue();
    }

    public void teecCloseSession(int callerId, int sid){
        if ( !mOTCallerList.containsKey(callerId) ) Log.e(TAG, "Incorrect callerId:" + callerId);

        OTCaller caller = findCallerById(callerId);
        // remove session id from caller.
        int sidInJni = caller.removeSessionBySid(sid);

        if(sidInJni == -1) return;

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
