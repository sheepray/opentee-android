package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * This class implements the IContext interface
 */
public class OTContext implements ITEEClient.IContext {
    String TAG = "OTContext";

    String mTeeName;
    boolean mInitialized = false;
    ProxyApis mProxyApis = null; // one service connection per context
    Random smIdGenerator;

    List<OTSharedMemory> mSharedMemory = new ArrayList<>();
    HashMap<Integer, Integer> mSessionMap = new HashMap<>(); // <sessionId, placeHolder>

    public OTContext(String teeName, Context context) throws TEEClientException {
        this.mTeeName = teeName;
        this.smIdGenerator = new Random();

        /**
         * connect to the OpenTEE
         */
        Object lock = new Object();
        ServiceGetterThread serviceGetterThread = new ServiceGetterThread(teeName, context, lock);
        serviceGetterThread.run();

        synchronized (lock) {
            // wait until service connected.
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.mInitialized = true;
        mProxyApis = serviceGetterThread.getProxyApis();

        Log.d(TAG, "Service connected.");
    }


    @Override
    public void finalizeContext() throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Nothing to finalize");
            return;
        }

        if ( mInitialized ) mInitialized = false;
        if ( mProxyApis != null ){
            try {
                mProxyApis.teecFinalizeContext();
            } catch (RemoteException e) {
                throw new CommunicationErrorException("Communication error with remote TEE service.");
            }
            mProxyApis.terminateConnection();
        }

        //clear up resources.
        mSharedMemory.clear();
        mSessionMap.clear();
        mProxyApis = null;

        Log.i(TAG, "context finalized and connection terminated");
    }

    @Override
    public ITEEClient.ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to register shared memory");
            return null;
        }

        int smId = generateSmId();

        // create a shared memory
        OTSharedMemory otSharedMemory = new OTSharedMemory(buffer, flags, smId);

        // register the shared memory
        try {
            mProxyApis.teecRegisterSharedMemory(otSharedMemory);
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // add the registered shared memory to mSharedMemory list.
        mSharedMemory.add(otSharedMemory);

        return otSharedMemory;
    }

    @Override
    public void releaseSharedMemory(ITEEClient.ISharedMemory sharedMemory) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to release shared memory");
            return;
        }

        // tell remote tee to release the shared memory.
        if ( sharedMemory != null ) try {
            mProxyApis.teecReleaseSharedMemory(sharedMemory.getId());
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // remove it from shared memory list.
        mSharedMemory.remove(sharedMemory);
    }

    private byte[] OperationAsByteArray(ITEEClient.Operation teecOperation){
        if ( teecOperation == null )return null;

        GPDataTypes.TeecOperation.Builder toBuilder = GPDataTypes.TeecOperation.newBuilder();

        byte[] opInArray = null;
        if (teecOperation.getParams() != null && teecOperation.getParams().size() > 0){
            /**
             * determine which type of parameter to parse.
             */

            List<ITEEClient.Parameter> parameterList = teecOperation.getParams();

            for ( ITEEClient.Parameter param: parameterList ){
                if( param.getType() == ITEEClient.Parameter.Type.TEEC_PTYPE_VALUE.getId()){
                    Log.i(TAG, "Param is " + ITEEClient.Parameter.Type.TEEC_PTYPE_VALUE);

                    GPDataTypes.TeecValue.Builder builder = GPDataTypes.TeecValue.newBuilder();
                    ITEEClient.Value val = (ITEEClient.Value)param;

                    builder.setA(val.getA());
                    builder.setB(val.getB());

                    int valFlagInInt = val.getFlag().getId();
                    builder.setMFlag(GPDataTypes.TeecValue.Flag.values()[valFlagInInt]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.val);
                    paramBuilder.setTeecValue(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else if ( param.getType() == ITEEClient.Parameter.Type.TEEC_PTYPE_SMR.getId() ){
                    Log.i(TAG, "Param is " + ITEEClient.Parameter.Type.TEEC_PTYPE_SMR);

                    GPDataTypes.TeecSharedMemoryReference.Builder builder
                            = GPDataTypes.TeecSharedMemoryReference.newBuilder();
                    ITEEClient.RegisteredMemoryReference rmr
                            = (ITEEClient.RegisteredMemoryReference)param;
                    OTSharedMemory teecSM = (OTSharedMemory)rmr.getSharedMemory();

                    //create gp shared memory from teec shared memory.
                    GPDataTypes.TeecSharedMemory.Builder gpSMBuilder = GPDataTypes.TeecSharedMemory.newBuilder();
                    gpSMBuilder.setSize(teecSM.getSize());
                    gpSMBuilder.setMID(teecSM.getId());
                    gpSMBuilder.setMFlag(teecSM.getFlags());
                    gpSMBuilder.setMBuffer(ByteString.copyFrom(teecSM.asByteArray()));
                    gpSMBuilder.setMReturnSize(teecSM.getReturnSize());

                    builder.setParent(gpSMBuilder.build());
                    builder.setMOffset(rmr.getOffset());
                    builder.setMFlag(GPDataTypes.TeecSharedMemoryReference.Flag.values()[rmr.getFlag().ordinal()]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.smr);
                    paramBuilder.setTeecSharedMemoryReference(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else{
                    Log.e(TAG, "Unsupported Operation type. Set the operation to null");
                }

            }
        }

        toBuilder.setMStarted(teecOperation.getStarted());
        opInArray = toBuilder.build().toByteArray();

        return opInArray;
    }

    @Override
    public ITEEClient.ISession openSession(UUID uuid,
                                ConnectionMethod connectionMethod,
                                int connectionData,
                                ITEEClient.Operation teecOperation) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        // sid is used to identify different sessions of one context in the OTGuard.
        int sid = generateSessionId();

        /**
         * parse teecOperation into byte array using protocol buffer.
         */
        byte[] opInArray = OperationAsByteArray(teecOperation);

        try {
            mProxyApis.teecOpenSession(sid,
                    uuid,
                    connectionMethod,
                    connectionData,
                    opInArray);
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // upon success
        OTSession otSession =  new OTSession(sid, mProxyApis);
        mSessionMap.put(sid, 0);

        GPDataTypes.TeecOperation.Builder teecOpResultBuilder = GPDataTypes.TeecOperation.newBuilder();

        try {
            teecOpResultBuilder.mergeFrom(opInArray);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        GPDataTypes.TeecOperation toResult = teecOpResultBuilder.build();

        // clean old params.
        List<ITEEClient.Parameter> params = teecOperation.getParams();

        List<GPDataTypes.TeecParameter> tpResults = toResult.getMParamsList();
        for(int i = 0; i < tpResults.size(); i++){
            GPDataTypes.TeecParameter tpResult = tpResults.get(i);
            if(tpResult.getType().ordinal() == ITEEClient.Parameter.Type.TEEC_PTYPE_VALUE.getId()){
                Log.i(TAG, "Param is VALUE");

                // value
                ITEEClient.Value value = (ITEEClient.Value)params.get(i);
                // update Value values.
                params.set(
                        i,
                        new ITEEClient.Value(value.getFlag(),
                                tpResult.getTeecValue().getA(),
                                tpResult.getTeecValue().getB())
                );
            }
            else if(tpResult.getType().ordinal() == ITEEClient.Parameter.Type.TEEC_PTYPE_SMR.getId()){
                // registered memory reference
                Log.i(TAG, "Param is RMR");


            }else{
                Log.e(TAG, "Incorrect param type:" + tpResult.getType());
            }
        }

        return otSession;
    }

    @Override
    public void requestCancellation(ITEEClient.Operation teecOperation) {

    }

    private int generateSmId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(occupiedSmId(id));

        Log.i(TAG, "generating shared memory id:" + id);
        return id;
    }

    private boolean occupiedSmId(int id){
        for(OTSharedMemory sm: mSharedMemory ){
            if ( sm.getId() == id ) return true;
        }
        return false;
    }

    private int generateSessionId(){
        // reuse the random generator of shared memory.
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(mSessionMap.containsKey(id));

        Log.i(TAG, "generating session id:" + id);
        return id;
    }
}
