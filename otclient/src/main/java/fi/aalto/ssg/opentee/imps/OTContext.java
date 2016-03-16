package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
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
    List<OTSession> mSessions = new ArrayList<>();

    public OTContext(String teeName, Context context) throws ITEEClient.Exception, RemoteException {
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
    public void finalizeContext() throws RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Nothing to finalize");
            return;
        }

        if ( mInitialized ) mInitialized = false;
        if ( mProxyApis != null ){
            mProxyApis.teecFinalizeContext();
            mProxyApis.terminateConnection();
        }

        //clear up resources.
        mSharedMemory.clear();
        mSessions.clear();
        mProxyApis = null;

        Log.i(TAG, "context finalized and connection terminated");
    }

    @Override
    public ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws ITEEClient.Exception, RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to register shared memory");
            return null;
        }

        int smId = generateSmId();

        // create a shared memory
        OTSharedMemory otSharedMemory = new OTSharedMemory(buffer, flags, smId);

        // register the shared memory
        mProxyApis.teecRegisterSharedMemory(otSharedMemory);

        // add the registered shared memory to mSharedMemory list.
        mSharedMemory.add(otSharedMemory);

        return otSharedMemory;
    }

    @Override
    public void releaseSharedMemory(ISharedMemory sharedMemory) throws ITEEClient.Exception, RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to release shared memory");
            return;
        }

        // tell remote tee to release the shared memory.
        if ( sharedMemory != null ) mProxyApis.teecReleaseSharedMemory(sharedMemory.getId());

        // remove it from shared memory list.
        mSharedMemory.remove(sharedMemory);
    }

    @Override
    public ISession openSession(UUID uuid,
                                ConnectionMethod connectionMethod,
                                int connectionData,
                                ITEEClient.Operation teecOperation) throws ITEEClient.Exception, RemoteException {
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        // sid is used to identify different sessions of one context in the OTGuard.
        int sid = generateSessionId();

        /**
         * parse teecOperation into byte array using protocol buffer.
         */
        byte[] opInArray = null;
        if ( teecOperation != null ){
            GPDataTypes.TeecOperation.Builder toBuilder = GPDataTypes.TeecOperation.newBuilder();

            if (teecOperation.getParams() != null){
                /**
                 * determine which type of parameter to parse.
                 */
                int paraType = teecOperation.getParams().get(0).getType();

                Log.d(TAG, "Found " + paraType);

                if ( paraType == ITEEClient.Parameter.Type.TEEC_PTYPE_VALUE.getId() ){
                    // it is class Value.
                    List<ITEEClient.Parameter> parameterList = teecOperation.getParams();

                    for ( ITEEClient.Parameter param: parameterList ){
                        GPDataTypes.TeecValue.Builder builder = GPDataTypes.TeecValue.newBuilder();
                        ITEEClient.Value val = (ITEEClient.Value)param;

                        builder.setA(val.getA());
                        builder.setB(val.getB());

                        int valFlagInInt = val.getFlag().getId();
                        if ( valFlagInInt == ITEEClient.Value.Flag.TEEC_VALUE_INPUT.getId() ){
                            builder.setMFlag(GPDataTypes.TeecValue.Flag.TEEC_VALUE_INPUT);
                        }
                        else if ( valFlagInInt == ITEEClient.Value.Flag.TEEC_VALUE_OUTPUT.getId() ){
                            builder.setMFlag(GPDataTypes.TeecValue.Flag.TEEC_VALUE_OUTPUT);
                        }
                        else if ( valFlagInInt == ITEEClient.Value.Flag.TEEC_VALUE_INOUT.getId() ){
                            builder.setMFlag(GPDataTypes.TeecValue.Flag.TEEC_VALUE_INOUT);
                        }

                        GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                        paramBuilder.setType(GPDataTypes.TeecParameter.Type.val);
                        paramBuilder.setTeecValue(builder.build());
                        toBuilder.addMParams(paramBuilder.build());
                    }
                }
                else if ( paraType == ITEEClient.Parameter.Type.TEEC_PTYPE_SMR.getId() ){
                    // it is registered memory reference.
                    // transfer the shared memory from public interface to implementation version.
                    // aka identify the shared memory from the public interface and pass its id instead.

                    List<ITEEClient.Parameter> parameterList = teecOperation.getParams();

                    for ( ITEEClient.Parameter param: parameterList ){
                        GPDataTypes.TeecSharedMemoryReference.Builder builder
                                = GPDataTypes.TeecSharedMemoryReference.newBuilder();
                        ITEEClient.RegisteredMemoryReference rmr
                                = (ITEEClient.RegisteredMemoryReference)param;

                        // find the id for the shared memory in rmr.
                        builder.setParentId(rmr.getSharedMemory().getId());
                        builder.setMOffset(rmr.getOffset());

                        GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                        paramBuilder.setType(GPDataTypes.TeecParameter.Type.smr);
                        paramBuilder.setTeecSharedMemoryReference(builder.build());
                        toBuilder.addMParams(paramBuilder.build());
                    }
                }else{
                    Log.e(TAG, "Unsupported Operation type. Set the operation to null");
                }

            }

            toBuilder.setMStarted(teecOperation.getStarted());
            opInArray = toBuilder.build().toByteArray();
        }


        int rc = mProxyApis.teecOpenSession(sid,
                uuid,
                connectionMethod,
                connectionData,
                opInArray);

        OTSession otSession = null;
        if ( rc == OTReturnCode.TEEC_SUCCESS ){
            otSession = new OTSession(sid, mProxyApis);
            mSessions.add(otSession);
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
        }while(occupiedSid(id));

        Log.i(TAG, "generating session id:" + id);
        return id;
    }

    private boolean occupiedSid(int id){
        for( OTSession s: mSessions ){
            if( s.getSessionId() == id ) return true;
        }
        return false;
    }
}
