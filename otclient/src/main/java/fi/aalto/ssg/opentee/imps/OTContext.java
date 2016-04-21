package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import fi.aalto.ssg.opentee.ISyncOperation;
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

    @Override
    public ITEEClient.ISession openSession(UUID uuid,
                                ConnectionMethod connectionMethod,
                                int connectionData,
                                ITEEClient.IOperation teecOperation) throws TEEClientException{
        //TODO: teecOperation started check?

        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        // sid is used to identify different sessions of one context in the OTGuard.
        int sid = generateSessionId();

        /**
         * parse teecOperation into byte array using protocol buffer.
         */
        byte[] opInArray = OTFactoryMethods.OperationAsByteArray(TAG, teecOperation);

        OTLock otLock = new OTLock();
        OpenSessionThread openSessionThread = new OpenSessionThread(mProxyApis,
                sid,
                uuid,
                connectionMethod,
                connectionData,
                opInArray,
                otLock);

        openSessionThread.run();

        try {
            otLock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new GenericErrorException(e.getMessage());
        }

        byte[] teecOperationInBytes = openSessionThread.getNewOperationInBytes();

        if(teecOperationInBytes == null){
            Log.e(TAG, "op is empty");
        }

        GPDataTypes.TeecOperation op = OTFactoryMethods.transferOpInBytesToOperation(TAG, teecOperationInBytes);

        //test code
        OTFactoryMethods.print_op(TAG, op);

        OTOperation otOperation = (OTOperation)teecOperation;
        for(int i = 0; i < op.getMParamsCount(); i++){
            GPDataTypes.TeecParameter param = op.getMParamsList().get(i);
            if(param.getType() == GPDataTypes.TeecParameter.Type.val){
                OTValue otValue = (OTValue)otOperation.getParam(i);

                otValue.setA(param.getTeecValue().getA());
                otValue.setB(param.getTeecValue().getB());
            }
            else if (param.getType() == GPDataTypes.TeecParameter.Type.smr){
                OTRegisteredMemoryReference otRmr = (OTRegisteredMemoryReference)otOperation.getParam(i);
                OTSharedMemory otSm = (OTSharedMemory)otRmr.getSharedMemory();

                GPDataTypes.TeecSharedMemoryReference teecSmr = param.getTeecSharedMemoryReference();

                otSm.updateBuffer(teecSmr.getParent().getMBuffer().toByteArray(),
                        otRmr.getOffset(),
                        teecSmr.getParent().getMReturnSize());

            }
            else{
                Log.e(TAG, "Unknown type of parameter.");
            }
        }

        // upon success
        OTSession otSession =  new OTSession(sid, mProxyApis);
        mSessionMap.put(sid, 0);

        //TODO: set started field of Operation to 0;

        return otSession;
    }

    @Override
    public void requestCancellation(ITEEClient.IOperation iOperation) {

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
